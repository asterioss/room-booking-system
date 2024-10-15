package com.acme.room_booking_system.service

import com.acme.room_booking_system.model.Booking
import com.acme.room_booking_system.model.Room
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

    def "should get bookings by room and date successfully"() {
        given: "A room and a list of bookings"
        def room = new Room(id: 1L, name: "Room A")
        def bookings = [
                new Booking(id: 1L, employeeEmail: "john.doe@example.com", startTime: LocalTime.of(10, 0), endTime: LocalTime.of(11, 0)),
                new Booking(id: 2L, employeeEmail: "jane.doe@example.com", startTime: LocalTime.of(11, 0), endTime: LocalTime.of(12, 0))
        ]
        def date = LocalDate.now()

        when: "Bookings are retrieved by room and date"
        roomHelper.findRoomByName("Room A") >> room
        bookingRepository.findByRoomAndDate(room, date) >> bookings

        def result = bookingService.getBookingsByRoom("Room A", date)

        then: "The bookings are returned as BookingResponse objects"
        result.size() == 2
        result[0].employeeEmail == "john.doe@example.com"
        result[1].employeeEmail == "jane.doe@example.com"
        //1 * roomHelper.findRoomByName("Room A")
        //1 * bookingRepository.findByRoomAndDate(room, date)  // Ensure that this method is called once
    }

    def "should handle no bookings found"() {
        given: "A room with no bookings"
        def room = new Room(id: 1L, name: "Room A")

        when: "Bookings are retrieved by room and date"
        roomHelper.findRoomByName("Room A") >> room
        bookingRepository.findByRoomAndDate(room, LocalDate.now()) >> []

        def result = bookingService.getBookingsByRoom("Room A", LocalDate.now())

        then: "An empty list is returned"
        result.size() == 0
        1 * roomHelper.findRoomByName("Room A")
        //1 * bookingRepository.findByRoomAndDate(room, LocalDate.now())
    }

    def "should create a booking successfully"() {
        given: "A valid booking request"
        def room = new Room(id: 1L, name: "Room A")
        def request = new BookingRequest("Room A", "john.doe@example.com", LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0))
        def booking = new Booking(room: room, employeeEmail: request.employeeEmail, date: request.date, startTime: request.startTime, endTime: request.endTime)

        when: "The booking is created"
        roomHelper.findRoomByName(request.roomName) >> room
        bookingRepository.existsByRoomAndDateAndStartTimeBetween(room, request.date, request.startTime, request.endTime) >> false
        bookingRepository.save(_) >> booking

        def createdBooking = bookingService.createBooking(request)

        then: "The booking is saved and returned as BookingResponse"
        createdBooking.roomName == "Room A"
        createdBooking.employeeEmail == request.employeeEmail
        createdBooking.startTime == request.startTime
        createdBooking.endTime == request.endTime
        //1 * roomHelper.findRoomByName(request.roomName)
        //1 * bookingRepository.save(_)
    }

    def "should throw exception if booking time overlaps during creation"() {
        given: "A booking request with overlapping time"
        def room = new Room(id: 1L, name: "Room A")
        def request = new BookingRequest("Room A", "john.doe@example.com", LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0))

        when: "The booking is created"
        roomHelper.findRoomByName(request.roomName) >> room
        bookingRepository.existsByRoomAndDateAndStartTimeBetween(room, request.date, request.startTime, request.endTime) >> true

        bookingService.createBooking(request)

        then: "An exception is thrown"
        def e = thrown(IllegalArgumentException)
        e.message == "Booking time overlaps with another booking."
    }

    def "should return all bookings"() {
        given: "A list of bookings"
        def bookings = [
                new Booking(room: new Room(name: "Room A"), employeeEmail: "john.doe@example.com", date: LocalDate.now(), startTime: LocalTime.of(10, 0), endTime: LocalTime.of(11, 0)),
                new Booking(room: new Room(name: "Room B"), employeeEmail: "jane.doe@example.com", date: LocalDate.now(), startTime: LocalTime.of(11, 0), endTime: LocalTime.of(12, 0))
        ]

        when: "All bookings are retrieved"
        bookingRepository.findAll() >> bookings

        def result = bookingService.getAllBookings()

        then: "The list of bookings is returned as BookingResponse"
        result.size() == 2
        result[0].employeeEmail == "john.doe@example.com"
        result[1].employeeEmail == "jane.doe@example.com"
        //1 * bookingRepository.findAll()
    }

    def "should cancel future booking successfully"() {
        given: "A future booking"
        def booking = new Booking(id: 1L, date: LocalDate.now().plusDays(1))
        bookingRepository.findById(1L) >> Optional.of(booking)

        when: "The booking is cancelled"
        bookingService.cancelBooking(1L)

        then: "The booking is deleted"
        1 * bookingRepository.delete(booking)
    }

    def "should throw exception if trying to cancel past booking"() {
        given: "A past booking"
        def booking = new Booking(id: 1L, date: LocalDate.now().minusDays(1))
        bookingRepository.findById(1L) >> Optional.of(booking)

        when: "Cancellation is attempted"
        bookingService.cancelBooking(1L)

        then: "An exception is thrown"
        def e = thrown(IllegalStateException)
        e.message == "Cannot cancel past bookings."
    }

    def "should update booking successfully"() {
        given: "An existing booking and a valid update request"
        def room = new Room(id: 1L, name: "Room A")
        def booking = new Booking(id: 1L, room: room, date: LocalDate.now(), startTime: LocalTime.of(9, 0), endTime: LocalTime.of(10, 0))
        def request = new BookingRequest("Room A", "john.doe@example.com", LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0))

        when: "The booking is updated"
        bookingRepository.findById(1L) >> Optional.of(booking)
        roomHelper.findRoomByName(request.roomName) >> room
        bookingRepository.existsByRoomAndDateAndStartTimeBetweenAndIdNot(room, request.date, request.startTime, request.endTime, 1L) >> false
        bookingRepository.save(_) >> booking

        def updatedBooking = bookingService.updateBooking(1L, request)

        then: "The booking is updated and saved"
        updatedBooking.startTime == request.startTime
        updatedBooking.endTime == request.endTime
        //1 * bookingRepository.save(_)
    }

    def "should throw exception for overlapping time during booking update"() {
        given: "An existing booking and an update request with overlapping time"
        def room = new Room(id: 1L, name: "Room A")
        def booking = new Booking(id: 1L, room: room, date: LocalDate.now(), startTime: LocalTime.of(9, 0), endTime: LocalTime.of(10, 0))
        def request = new BookingRequest("Room A", "john.doe@example.com", LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0))

        when: "The booking update is attempted"
        bookingRepository.findById(1L) >> Optional.of(booking)
        roomHelper.findRoomByName(request.roomName) >> room
        bookingRepository.existsByRoomAndDateAndStartTimeBetweenAndIdNot(room, request.date, request.startTime, request.endTime, 1L) >> true

        bookingService.updateBooking(1L, request)

        then: "An exception is thrown"
        def e = thrown(IllegalArgumentException)
        e.message == "Booking time overlaps with another booking."
    }

    def "should throw exception if booking not found by ID"() {
        given: "No booking exists with the given ID"
        bookingRepository.findById(1L) >> Optional.empty()

        when: "Booking retrieval is attempted"
        bookingService.findBookingById(1L)

        then: "An exception is thrown"
        def e = thrown(EntityNotFoundException)
        e.message == "Booking not found with id: 1"
    }
}
