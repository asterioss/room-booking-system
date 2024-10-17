package com.acme.room_booking_system.service

import com.acme.room_booking_system.exception.BookingCancellationException
import com.acme.room_booking_system.exception.BookingOverlapException
import com.acme.room_booking_system.exception.InvalidBookingDurationException
import com.acme.room_booking_system.helper.BookingHelper
import com.acme.room_booking_system.model.dto.BookingResponse
import com.acme.room_booking_system.model.entity.Booking
import com.acme.room_booking_system.model.entity.Room
import com.acme.room_booking_system.model.dto.BookingRequest
import com.acme.room_booking_system.repository.BookingRepository
import com.acme.room_booking_system.helper.RoomHelper
import jakarta.persistence.EntityNotFoundException
import spock.lang.Specification

import java.time.LocalDate
import java.time.LocalTime

class BookingServiceSpec extends Specification {

    BookingService bookingService
    BookingRepository bookingRepository = Mock()
    BookingHelper bookingHelper = Mock()
    RoomHelper roomHelper = Mock()

    def setup() {
        bookingService = new BookingService(bookingRepository, bookingHelper, roomHelper)
    }

    def "Get bookings by room and date successfully"() {
        given: "A room and a list of bookings"
        def roomName = "Room A"
        def room = new Room(id: 1L, name: roomName)
        def bookings = [
                new Booking(id: 1L, employeeEmail: "asterios@gmail.com", startTime: LocalTime.of(10, 0), endTime: LocalTime.of(11, 0)),
                new Booking(id: 2L, employeeEmail: "stelios@gmail.com", startTime: LocalTime.of(11, 0), endTime: LocalTime.of(12, 0))
        ]
        def date = LocalDate.now()

        when: "Bookings are retrieved by room and date"
        roomHelper.findRoomByName(roomName) >> room
        bookingRepository.findByRoomAndDate(room, date) >> bookings

        def result = bookingService.getBookingsByRoomAndDate(roomName, date)

        then: "The bookings are returned as BookingResponse list"
        result.size() == 2
        result[0].employeeEmail == "asterios@gmail.com"
        result[1].employeeEmail == "stelios@gmail.com"
        result[0].startTime == LocalTime.of(10, 0)
        result[1].endTime == LocalTime.of(12, 0)
    }

    def "Handle no bookings found"() {
        given: "A room with no bookings"
        def roomName = "Room A"
        def room = new Room(id: 1L, name: roomName)
        def date = LocalDate.now()

        when: "Bookings are retrieved by room and date"
        roomHelper.findRoomByName(roomName) >> room
        bookingRepository.findByRoomAndDate(room, date) >> []

        def result = bookingService.getBookingsByRoomAndDate(roomName, date)

        then: "An empty list is returned"
        result.size() == 0
        1 * roomHelper.findRoomByName(roomName)
    }

    def "Create a booking successfully"() {
        given: "A valid booking request and a new booking"
        def roomName = "Room A"
        def room = new Room(id: 1L, name: roomName)
        def request = new BookingRequest(roomName, "asterios@gmail.com", LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0))
        def booking = new Booking(room: room, employeeEmail: request.employeeEmail, date: request.date, startTime: request.startTime, endTime: request.endTime)
        def bookingResponse = new BookingResponse(roomName, request.employeeEmail, request.date, request.startTime, request.endTime)

        when: "The booking is created"
        roomHelper.findRoomByName(request.roomName) >> room
        bookingRepository.existsByRoomAndDateAndStartTimeLessThanAndEndTimeGreaterThan(room, request.date, request.startTime, request.endTime) >> false
        bookingHelper.mapToBooking(room, request) >> booking
        bookingRepository.save(_) >> booking
        bookingHelper.mapToBookingResponse(room.getName(), booking) >> bookingResponse

        def createdBooking = bookingService.createBooking(request)

        then: "The booking is saved and returned as BookingResponse object"
        createdBooking != null
        createdBooking.roomName == roomName
        createdBooking.employeeEmail == request.employeeEmail
        createdBooking.startTime == request.startTime
        createdBooking.endTime == request.endTime
    }

    def "Throw exception when booking date is in the past"() {
        given: "A booking request with a date in the past"
        def roomName = "Room A"
        def room = new Room(id: 1L, name: roomName)
        def pastDate = LocalDate.now().minusDays(1)
        def request = new BookingRequest(roomName, "asterios@gmail.com", pastDate, LocalTime.of(10, 0), LocalTime.of(11, 0))

        when: "The booking is attempted"
        roomHelper.findRoomByName(request.roomName) >> room
        bookingHelper.validateBookingAndDates(request, room, null) >> {
            throw new IllegalArgumentException("The booking date cannot be in the past.")
        }

        bookingService.createBooking(request)

        then: "An exception is thrown"
        def e = thrown(IllegalArgumentException)
        e.message == "The booking date cannot be in the past."
    }

    def "Throw exception when booking time is in the past"() {
        given: "A booking request with a time in the past"
        def roomName = "Room A"
        def room = new Room(id: 1L, name: roomName)
        def request = new BookingRequest(roomName, "asterios@gmail.com", LocalDate.now(), LocalTime.now().minusHours(1), LocalTime.now())

        when: "The booking is attempted"
        roomHelper.findRoomByName(request.roomName) >> room
        bookingHelper.validateBookingAndDates(request, room, null) >> {
            throw new IllegalArgumentException("The booking start time cannot be in the past.")
        }

        bookingService.createBooking(request)

        then: "An exception is thrown"
        def e = thrown(IllegalArgumentException)
        e.message == "The booking start time cannot be in the past."
    }

    def "Throw exception if booking duration is not multiple of 1 hour"() {
        given: "A booking request with invalid time duration"
        def roomName = "Room A"
        def room = new Room(id: 1L, name: roomName)
        def request = new BookingRequest(roomName, "asterios@gmail.com", LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(10, 55))

        when: "The booking is created"
        roomHelper.findRoomByName(request.roomName) >> room
        bookingHelper.validateBookingAndDates(request, room, null) >> {
            throw new InvalidBookingDurationException("Booking must be at least 1 hour or a multiple of 1 hour.")
        }

        bookingService.createBooking(request)

        then: "An exception is thrown"
        def e = thrown(InvalidBookingDurationException)
        e.message == "Booking must be at least 1 hour or a multiple of 1 hour."
    }

    def "Throw exception if booking time overlaps during creation"() {
        given: "A booking request with overlapping time"
        def roomName = "Room A"
        def room = new Room(id: 1L, name: roomName)
        def request = new BookingRequest(roomName, "asterios@gmail.com", LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0))

        when: "The booking is created"
        roomHelper.findRoomByName(request.roomName) >> room
        bookingHelper.validateBookingAndDates(request, room, null) >> {
            throw new BookingOverlapException("Booking time overlaps with another booking.")
        }

        bookingService.createBooking(request)

        then: "An exception is thrown"
        def e = thrown(BookingOverlapException)
        e.message == "Booking time overlaps with another booking."
    }

    def "Return all bookings"() {
        given: "A list of bookings"
        def bookings = [
                new Booking(id: 1L, room: new Room(name: "Room A"), employeeEmail: "asterios@gmail.com", date: LocalDate.now(), startTime: LocalTime.of(10, 0), endTime: LocalTime.of(11, 0)),
                new Booking(id: 2L, room: new Room(name: "Room B"), employeeEmail: "stelios@gmail.com", date: LocalDate.now(), startTime: LocalTime.of(11, 0), endTime: LocalTime.of(12, 0))
        ]
        def bookingResponses = [
                new BookingResponse("Room A", "asterios@gmail.com", LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0)),
                new BookingResponse("Room B", "stelios@gmail.com", LocalDate.now(), LocalTime.of(11, 0), LocalTime.of(12, 0))
        ]

        when: "All bookings are retrieved"
        bookingRepository.findAll() >> bookings
        bookingHelper.mapToBookingResponse("Room A", bookings[0]) >> bookingResponses[0]
        bookingHelper.mapToBookingResponse("Room B", bookings[1]) >> bookingResponses[1]

        def result = bookingService.getAllBookings()

        then: "The list of bookings is returned as BookingResponse list"
        result.size() == 2
        result[0].employeeEmail == "asterios@gmail.com"
        result[1].employeeEmail == "stelios@gmail.com"
        result[0].startTime == LocalTime.of(10, 0)
        result[1].endTime == LocalTime.of(12, 0)
    }

    def "Update booking successfully"() {
        given: "An existing booking and a valid update request"
        def roomName = "Room A"
        def room = new Room(id: 1L, name: roomName)
        def booking = new Booking(id: 1L, room: room, date: LocalDate.now(), startTime: LocalTime.of(9, 0), endTime: LocalTime.of(10, 0))
        def request = new BookingRequest(roomName, "asterios@gmail.com", LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0))
        def updatedBooking = new Booking(id: 1L, room: room, employeeEmail: request.employeeEmail, date: request.date, startTime: request.startTime, endTime: request.endTime)
        def bookingResponse = new BookingResponse(roomName, request.employeeEmail, request.date, request.startTime, request.endTime)

        when: "The booking is updated"
        bookingHelper.findBookingById(1L) >> booking
        roomHelper.findRoomByName(request.roomName) >> room
        bookingRepository.save(_) >> updatedBooking
        bookingHelper.mapToBookingResponse(room.getName(), updatedBooking) >> bookingResponse

        def result = bookingService.updateBooking(1L, request)

        then: "The booking is updated and saved"
        result != null
        result.roomName == roomName
        result.employeeEmail == request.employeeEmail
        result.startTime == request.startTime
        result.endTime == request.endTime
    }

    def "Throw exception if trying to update booking with a past time"() {
        given: "An existing booking and an update request with a past time"
        def roomName = "Room A"
        def room = new Room(id: 1L, name: roomName)
        def booking = new Booking(id: 1L, room: room, date: LocalDate.now(), startTime: LocalTime.of(9, 0), endTime: LocalTime.of(10, 0))
        def pastStartTime = LocalTime.now().minusHours(1)
        def request = new BookingRequest(roomName, "asterios@gmail.com", LocalDate.now(), pastStartTime, LocalTime.now())

        when: "The booking update is attempted"
        bookingHelper.findBookingById(1L) >> booking
        roomHelper.findRoomByName(request.roomName) >> room
        bookingHelper.validateBookingAndDates(request, room, 1L) >> {
            throw new IllegalArgumentException("The booking start time cannot be in the past.")
        }

        bookingService.updateBooking(1L, request)

        then: "An exception is thrown"
        def e = thrown(IllegalArgumentException)
        e.message == "The booking start time cannot be in the past."
    }

    def "Throw exception for overlapping time during booking update"() {
        given: "An existing booking and an update request with overlapping time"
        def roomName = "Room A"
        def room = new Room(id: 1L, name: roomName)
        def booking = new Booking(id: 1L, room: room, date: LocalDate.now(), startTime: LocalTime.of(9, 0), endTime: LocalTime.of(10, 0))
        def request = new BookingRequest(roomName, "asterios@gmail.com", LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0))

        when: "The booking update is attempted"
        bookingHelper.findBookingById(1L) >> booking
        roomHelper.findRoomByName(request.roomName) >> room
        bookingHelper.validateBookingAndDates(request, room, 1L) >> {
            throw new BookingOverlapException("Booking time overlaps with another booking.")
        }

        bookingService.updateBooking(1L, request)

        then: "An exception is thrown"
        def e = thrown(BookingOverlapException)
        e.message == "Booking time overlaps with another booking."
    }

    def "Cancel future booking successfully"() {
        given: "A future booking"
        def booking = new Booking(id: 1L, date: LocalDate.now().plusDays(1))

        when: "The booking is cancelled"
        bookingRepository.findById(1L) >> Optional.of(booking)
        bookingHelper.findBookingById(1L) >> booking
        bookingService.cancelBooking(1L)

        then: "The booking is deleted"
        1 * bookingRepository.delete(booking)
    }

    def "Throw exception if booking not found by ID"() {
        given: "No booking exists with the given ID"
        def bookingId = 1L
        bookingRepository.findById(bookingId) >> Optional.empty()

        when: "Booking retrieval is attempted"
        bookingHelper.findBookingById(bookingId) >> {
            throw new EntityNotFoundException("Booking not found with id: " + bookingId)
        }

        bookingService.cancelBooking(bookingId)

        then: "An exception is thrown"
        def e = thrown(EntityNotFoundException)
        e.message == "Booking not found with id: 1"
    }

    def "Throw exception if trying to cancel past booking"() {
        given: "A past booking"
        def booking = new Booking(id: 1L, date: LocalDate.now().minusDays(1))

        when: "Cancellation is attempted"
        bookingHelper.findBookingById(1L) >> booking
        bookingService.cancelBooking(1L)

        then: "An exception is thrown"
        def e = thrown(BookingCancellationException)
        e.message == "Cannot cancel past bookings."
        0 * bookingRepository.delete(_)
    }
}
