package com.acme.room_booking_system.service;

import com.acme.room_booking_system.helper.RoomHelper;
import com.acme.room_booking_system.model.Booking;
import com.acme.room_booking_system.model.BookingRequest;
import com.acme.room_booking_system.model.BookingResponse;
import com.acme.room_booking_system.model.Room;
import com.acme.room_booking_system.repository.BookingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomHelper roomHelper;

    public List<BookingResponse> getBookingsByRoom(String roomName, LocalDate date) {
        Room room = roomHelper.findRoomByName(roomName);

        List<Booking> bookings = bookingRepository.findByRoomAndDate(room, date);

        return bookings.stream()
                .map(booking -> new BookingResponse(
                        booking.getEmployeeEmail(), booking.getStartTime(), booking.getEndTime()))
                .collect(Collectors.toList());
    }

    public Booking createBooking(BookingRequest request) {
        Room room = roomHelper.findRoomByName(request.getRoomName());

        validateBookingDuration(request.getStartTime(), request.getEndTime());

        //check for overlapping bookings
        validateBookingOverlap(room, request);

        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setEmployeeEmail(request.getEmployeeEmail());
        booking.setDate(request.getDate());
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        return bookingRepository.save(booking);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public void cancelBooking(Long bookingId) {
        Booking booking = findBookById(bookingId);

        if (booking.getDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Cannot cancel past bookings");
        }

        bookingRepository.delete(booking);
    }

    public Booking updateBooking(Long bookingId, BookingRequest request) {
        Booking booking = findBookById(bookingId);

        Room room = roomHelper.findRoomByName(request.getRoomName());

        validateBookingDuration(request.getStartTime(), request.getEndTime());

        //check for overlapping bookings and ignore the current booking we update
        validateBookingOverlap(room, request, bookingId);

        //booking.setRoom(room);
        booking.setEmployeeEmail(request.getEmployeeEmail());
        booking.setDate(request.getDate());
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());

        return bookingRepository.save(booking);
    }

    //validate booking duration (at least 1 hour or consecutive multiples of 1 hour)
    private void validateBookingDuration(LocalTime startTime, LocalTime endTime) {
        long hours = Duration.between(startTime, endTime).toHours();

        if (hours < 1 || hours % 1 != 0) {
            throw new IllegalArgumentException("Booking must be at least 1 hour or a multiple of 1 hour.");
        }
    }

    private void validateBookingOverlap(Room room, BookingRequest request) {
        boolean overlapExists = bookingRepository.existsByRoomAndDateAndStartTimeBetween(
                room, request.getDate(), request.getStartTime(), request.getEndTime());

        overlapExists(overlapExists);
    }

    private void validateBookingOverlap(Room room, BookingRequest request, Long bookingId) {
        boolean overlapExists = bookingRepository.existsByRoomAndDateAndStartTimeBetweenAndIdNot(
                room, request.getDate(), request.getStartTime(), request.getEndTime(), bookingId);

        overlapExists(overlapExists);
    }

    private void overlapExists(boolean overlapExists) {
        if (overlapExists) {
            throw new IllegalArgumentException("Booking time overlaps with another booking.");
        }
    }

    public Booking findBookById(Long bookingId) {
       return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));
    }
}
