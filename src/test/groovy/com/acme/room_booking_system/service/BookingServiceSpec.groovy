package com.acme.room_booking_system.service

import com.acme.room_booking_system.exception.BookingCancellationException
import com.acme.room_booking_system.exception.BookingOverlapException
import com.acme.room_booking_system.exception.InvalidBookingDurationException
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
    RoomHelper roomHelper = Mock()

    def setup() {
        bookingService = new BookingService(bookingRepository, roomHelper)
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

        when: "The booking is created"
        roomHelper.findRoomByName(request.roomName) >> room
        bookingRepository.existsByRoomAndDateAndStartTimeBetween(room, request.date, request.startTime, request.endTime) >> false
        bookingRepository.save(_) >> booking

        def createdBooking = bookingService.createBooking(request)

        then: "The booking is saved and returned as BookingResponse object"
        createdBooking.roomName == roomName
        createdBooking.employeeEmail == request.employeeEmail
        createdBooking.startTime == request.startTime
        createdBooking.endTime == request.endTime
    }

    def "Throw exception if booking duration is not multiple of 1 hour"() {
        given: "A booking request with invalid time duration"
        def roomName = "Room A"
        def room = new Room(id: 1L, name: roomName)
        def request = new BookingRequest(roomName, "asterios@gmail.com", LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(10, 55))

        when: "The booking is created"
        roomHelper.findRoomByName(request.roomName) >> room
        bookingRepository.existsByRoomAndDateAndStartTimeBetween(room, request.date, request.startTime, request.endTime) >> true

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
        bookingRepository.existsByRoomAndDateAndStartTimeBetween(room, request.date, request.startTime, request.endTime) >> true

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

        when: "All bookings are retrieved"
        bookingRepository.findAll() >> bookings

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

        when: "The booking is updated"
        bookingRepository.findById(1L) >> Optional.of(booking)
        roomHelper.findRoomByName(request.roomName) >> room
        bookingRepository.existsByRoomAndDateAndStartTimeBetweenAndIdNot(room, request.date, request.startTime, request.endTime, 1L) >> false
        bookingRepository.save(_) >> booking

        def updatedBooking = bookingService.updateBooking(1L, request)

        then: "The booking is updated and saved"
        updatedBooking.startTime == request.startTime
        updatedBooking.endTime == request.endTime
    }

    def "Throw exception for overlapping time during booking update"() {
        given: "An existing booking and an update request with overlapping time"
        def roomName = "Room A"
        def room = new Room(id: 1L, name: roomName)
        def booking = new Booking(id: 1L, room: room, date: LocalDate.now(), startTime: LocalTime.of(9, 0), endTime: LocalTime.of(10, 0))
        def request = new BookingRequest(roomName, "asterios@gmail.com", LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0))

        when: "The booking update is attempted"
        bookingRepository.findById(1L) >> Optional.of(booking)
        roomHelper.findRoomByName(request.roomName) >> room
        bookingRepository.existsByRoomAndDateAndStartTimeBetweenAndIdNot(room, request.date, request.startTime, request.endTime, 1L) >> true

        bookingService.updateBooking(1L, request)

        then: "An exception is thrown"
        def e = thrown(BookingOverlapException)
        e.message == "Booking time overlaps with another booking."
    }

    def "Throw exception if booking not found by ID"() {
        given: "No booking exists with the given ID"
        bookingRepository.findById(1L) >> Optional.empty()

        when: "Booking retrieval is attempted"
        bookingService.findBookingById(1L)

        then: "An exception is thrown"
        def e = thrown(EntityNotFoundException)
        e.message == "Booking not found with id: 1"
    }

    def "Cancel future booking successfully"() {
        given: "A future booking"
        def booking = new Booking(id: 1L, date: LocalDate.now().plusDays(1))

        when: "The booking is cancelled"
        bookingRepository.findById(1L) >> Optional.of(booking)
        bookingService.cancelBooking(1L)

        then: "The booking is deleted"
        1 * bookingRepository.delete(booking)
    }

    def "Throw exception if trying to cancel past booking"() {
        given: "A past booking"
        def booking = new Booking(id: 1L, date: LocalDate.now().minusDays(1))

        when: "Cancellation is attempted"
        bookingRepository.findById(1L) >> Optional.of(booking)
        bookingService.cancelBooking(1L)

        then: "An exception is thrown"
        def e = thrown(BookingCancellationException)
        e.message == "Cannot cancel past bookings."
        0 * bookingRepository.delete(_)
    }
}
