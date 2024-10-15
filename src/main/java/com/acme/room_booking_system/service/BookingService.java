package com.acme.room_booking_system.service;

import com.acme.room_booking_system.helper.RoomHelper;
import com.acme.room_booking_system.model.Booking;
import com.acme.room_booking_system.model.dto.BookingRequest;
import com.acme.room_booking_system.model.dto.BookingResponse;
import com.acme.room_booking_system.model.Room;
import com.acme.room_booking_system.repository.BookingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomHelper roomHelper;

    public List<BookingResponse> getBookingsByRoomAndDate(String roomName, LocalDate date) {
        Room room = roomHelper.findRoomByName(roomName);
        List<Booking> bookings = bookingRepository.findByRoomAndDate(room, date);

        if (bookings == null) {
            bookings = new ArrayList<>();
        }

        BookingResponse bookingResponse = new BookingResponse();

        return bookings.stream()
                .map(booking ->  new BookingResponse(
                        booking.getEmployeeEmail(),
                        booking.getStartTime(),
                        booking.getEndTime()))
                .collect(Collectors.toList());
    }

    public BookingResponse createBooking(BookingRequest request) {
        Room room = roomHelper.findRoomByName(request.getRoomName());

        //validate duration and overlapping bookings
        validateBookingDuration(request.getStartTime(), request.getEndTime());
        validateBookingOverlap(room, request, null);

        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setEmployeeEmail(request.getEmployeeEmail());
        booking.setDate(request.getDate());
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());

        booking = bookingRepository.save(booking);

        return fillBookingResponse(room.getName(), booking);
    }

    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(booking -> fillBookingResponse(booking.getRoom().getName(), booking))
                .collect(Collectors.toList());
    }

    public BookingResponse updateBooking(Long bookingId, BookingRequest request) {
        Booking booking = findBookingById(bookingId);
        Room room = roomHelper.findRoomByName(request.getRoomName());

        //validate duration and overlapping bookings, excluding the current booking
        validateBookingDuration(request.getStartTime(), request.getEndTime());
        validateBookingOverlap(room, request, bookingId);

        //booking.setRoom(room);
        booking.setEmployeeEmail(request.getEmployeeEmail());
        booking.setDate(request.getDate());
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());

        booking = bookingRepository.save(booking);

        return fillBookingResponse(room.getName(), booking);
    }

    public void cancelBooking(Long bookingId) {
        Booking booking = findBookingById(bookingId);

        // Prevent canceling past bookings
        if (booking.getDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Cannot cancel past bookings.");
        }

        bookingRepository.delete(booking);
    }

    private BookingResponse fillBookingResponse(String name, Booking booking) {
        return new BookingResponse(
                name,
                booking.getEmployeeEmail(),
                booking.getDate(),
                booking.getStartTime(),
                booking.getEndTime());
    }

    //validate booking duration (at least 1 hour or consecutive multiples of 1 hour)
    private void validateBookingDuration(LocalTime startTime, LocalTime endTime) {
        long minutes = Duration.between(startTime, endTime).toMinutes();

        //ensures that the booking duration is exactly 60 minutes, 120 minutes, 180 minutes, etc.
        if (minutes < 60 || minutes % 60 != 0) {
            throw new IllegalArgumentException("Booking must be at least 1 hour or a multiple of 1 hour.");
        }
    }

    // Validate if the booking overlaps with others
    private void validateBookingOverlap(Room room, BookingRequest request, Long bookingId) {
        boolean overlapExists;

        if (bookingId == null) {
            // Check for overlap when creating a new booking
            overlapExists = bookingRepository.existsByRoomAndDateAndStartTimeBetween(
                    room, request.getDate(), request.getStartTime(), request.getEndTime());
        } else {
            // Check for overlap when updating a booking
            overlapExists = bookingRepository.existsByRoomAndDateAndStartTimeBetweenAndIdNot(
                    room, request.getDate(), request.getStartTime(), request.getEndTime(), bookingId);
        }

        if (overlapExists) {
            throw new IllegalArgumentException("Booking time overlaps with another booking.");
        }
    }

    private Booking findBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));
    }
}
