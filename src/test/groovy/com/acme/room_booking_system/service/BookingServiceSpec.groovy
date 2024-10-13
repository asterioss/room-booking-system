package com.acme.room_booking_system.service

import com.acme.room_booking_system.model.Booking
import com.acme.room_booking_system.model.BookingRequest
import com.acme.room_booking_system.model.Room
import com.acme.room_booking_system.repository.BookingRepository
import com.acme.room_booking_system.helper.RoomHelper
import spock.lang.Specification
import jakarta.persistence.EntityNotFoundException

import java.time.LocalDate
import java.time.LocalTime

class BookingServiceSpec extends Specification {

    BookingService bookingService

    BookingRepository bookingRepository = Mock()
    RoomHelper roomHelper = Mock()

    def setup() {
        bookingService = new BookingService(bookingRepository, roomHelper)
    }

    def "should get bookings by room and date"() {
        given: "A room and date with bookings"
        def room = new Room(id: 1L, name: "Room A")
        def booking = new Booking(employeeEmail: "john.doe@example.com", startTime: LocalTime.of(10, 0), endTime: LocalTime.of(11, 0))
        def bookings = [booking]

        when: "Bookings are retrieved"
        roomHelper.findRoomByName("Room A") >> room
        bookingRepository.findByRoomAndDate(room, LocalDate.now()) >> bookings

        def result = bookingService.getBookingsByRoom("Room A", LocalDate.now())

        then: "The bookings are returned as BookingResponse objects"
        result.size() == 1
        result[0].employeeEmail == "john.doe@example.com"
        result[0].startTime == LocalTime.of(10, 0)
        result[0].endTime == LocalTime.of(11, 0)
        //1 * bookingRepository.findByRoomAndDate(room, LocalDate.now())  // Ensure findByRoomAndDate was called
    }

    def "should create a booking successfully"() {
        given: "A valid booking request and a free room"
        def room = new Room(id: 1L, name: "Room A")
        def request = new BookingRequest("Room A", "john.doe@example.com", LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0))
        def booking = new Booking(room: room, employeeEmail: request.employeeEmail, date: request.date, startTime: request.startTime, endTime: request.endTime)

        when: "The booking is created"
        roomHelper.findRoomByName(request.roomName) >> room
        bookingRepository.existsByRoomAndDateAndStartTimeBetween(room, request.date, request.startTime, request.endTime) >> false  // No overlap
        bookingRepository.save(_) >> booking

        def createdBooking = bookingService.createBooking(request)

        then: "The booking is saved and returned"
        createdBooking != null
        createdBooking.employeeEmail == request.employeeEmail
        createdBooking.startTime == request.startTime
        createdBooking.endTime == request.endTime
        //1 * bookingRepository.save(_)
    }

    def "should throw exception for booking overlap during creation"() {
        given: "A booking request with overlapping time"
        def room = new Room(id: 1L, name: "Room A")
        def request = new BookingRequest("Room A", "john.doe@example.com", LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0))

        when: "The booking is created"
        roomHelper.findRoomByName(request.roomName) >> room
        bookingRepository.existsByRoomAndDateAndStartTimeBetween(room, request.date, request.startTime, request.endTime) >> true  // Overlap exists

        bookingService.createBooking(request)

        then: "An exception is thrown"
        def e = thrown(IllegalArgumentException)
        e.message == "Booking time overlaps with another booking."
    }

    def "should return all bookings"() {
        given: "A list of bookings"
        def bookings = [new Booking(id: 1L, employeeEmail: "john.doe@example.com"), new Booking(id: 2L, employeeEmail: "jane.doe@example.com")]

        when: "All bookings are retrieved"
        bookingRepository.findAll() >> bookings

        def result = bookingService.getAllBookings()

        then: "The bookings are returned"
        result.size() == 2
        result[0].employeeEmail == "john.doe@example.com"
        result[1].employeeEmail == "jane.doe@example.com"
        1 * bookingRepository.findAll()  // Ensure findAll was called
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

    def "should throw exception when cancelling past booking"() {
        given: "A past booking"
        def booking = new Booking(id: 1L, date: LocalDate.now().minusDays(1))
        bookingRepository.findById(1L) >> Optional.of(booking)

        when: "Cancellation is attempted"
        bookingService.cancelBooking(1L)

        then: "An exception is thrown"
        def e = thrown(IllegalStateException)
        e.message == "Cannot cancel past bookings"
        0 * bookingRepository.delete(_)
    }

    def "should update booking successfully"() {
        given: "An existing booking and a valid update request"
        def room = new Room(id: 1L, name: "Room A")
        def booking = new Booking(id: 1L, room: room, date: LocalDate.now(), startTime: LocalTime.of(9, 0), endTime: LocalTime.of(10, 0))
        def request = new BookingRequest("Room A", "john.doe@example.com", LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0))

        when: "The booking is updated"
        bookingRepository.findById(1L) >> Optional.of(booking)
        roomHelper.findRoomByName(request.roomName) >> room
        bookingRepository.existsByRoomAndDateAndStartTimeBetweenAndIdNot(room, request.date, request.startTime, request.endTime, 1L) >> false  // No overlap
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
        bookingRepository.existsByRoomAndDateAndStartTimeBetweenAndIdNot(room, request.date, request.startTime, request.endTime, 1L) >> true  // Overlap exists

        bookingService.updateBooking(1L, request)

        then: "An exception is thrown"
        def e = thrown(IllegalArgumentException)
        e.message == "Booking time overlaps with another booking."
        0 * bookingRepository.save(_)
    }

    def "should find booking by ID"() {
        given: "A booking exists with the given ID"
        def booking = new Booking(id: 1L, room: new Room(), employeeEmail: "john.doe@example.com", date: LocalDate.now())

        when: "The booking is retrieved"
        bookingRepository.findById(1L) >> Optional.of(booking)

        def foundBooking = bookingService.findBookById(1L)

        then: "The booking is returned"
        foundBooking != null
        foundBooking.employeeEmail == "john.doe@example.com"
    }

    def "should throw exception if booking not found by ID"() {
        given: "No booking exists with the given ID"
        bookingRepository.findById(1L) >> Optional.empty()

        when: "Booking retrieval is attempted"
        bookingService.findBookById(1L)

        then: "An exception is thrown"
        def e = thrown(EntityNotFoundException)
        e.message == "Booking not found"
    }
}
