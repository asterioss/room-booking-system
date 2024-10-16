package com.acme.room_booking_system.service;

import com.acme.room_booking_system.exception.BookingCancellationException;
import com.acme.room_booking_system.exception.BookingOverlapException;
import com.acme.room_booking_system.exception.InvalidBookingDurationException;
import com.acme.room_booking_system.helper.RoomHelper;
import com.acme.room_booking_system.model.entity.Booking;
import com.acme.room_booking_system.model.dto.BookingRequest;
import com.acme.room_booking_system.model.dto.BookingResponse;
import com.acme.room_booking_system.model.entity.Room;
import com.acme.room_booking_system.repository.BookingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
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
            bookings = Collections.emptyList();
        }

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
        validateBooking(request, room, null);

        Booking booking = mapToBooking(room, request);
        booking = bookingRepository.save(booking);

        return mapToBookingResponse(room.getName(), booking);
    }

    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(booking -> mapToBookingResponse(booking.getRoom().getName(), booking))
                .collect(Collectors.toList());
    }

    public BookingResponse updateBooking(Long bookingId, BookingRequest request) {
        Booking existingBooking = findBookingById(bookingId);
        Room room = roomHelper.findRoomByName(request.getRoomName());

        //validate duration and overlapping bookings, excluding the current booking
        validateBooking(request, room, bookingId);

        //existingBooking.setRoom(room);
        existingBooking.setEmployeeEmail(request.getEmployeeEmail());
        existingBooking.setDate(request.getDate());
        existingBooking.setStartTime(request.getStartTime());
        existingBooking.setEndTime(request.getEndTime());

        Booking updatedBooking = bookingRepository.save(existingBooking);

        return mapToBookingResponse(room.getName(), updatedBooking);
    }

    public void cancelBooking(Long bookingId) {
        Booking booking = findBookingById(bookingId);

        //prevent canceling past bookings
        if (booking.getDate().isBefore(LocalDate.now())) {
            throw new BookingCancellationException("Cannot cancel past bookings.");
        }

        bookingRepository.delete(booking);
    }

    private void validateBooking(BookingRequest request, Room room, Long bookingId) {
        validateBookingDuration(request.getStartTime(), request.getEndTime());
        validateBookingOverlap(room, request, bookingId);
    }

    //validate booking duration (at least 1 hour or consecutive multiples of 1 hour)
    private void validateBookingDuration(LocalTime startTime, LocalTime endTime) {
        long minutes = Duration.between(startTime, endTime).toMinutes();

        //ensures that the booking duration is exactly 60 minutes, 120 minutes, 180 minutes, etc.
        if (minutes < 60 || minutes % 60 != 0) {
            throw new InvalidBookingDurationException("Booking must be at least 1 hour or a multiple of 1 hour.");
        }
    }

    //validate if the booking overlaps with others
    private void validateBookingOverlap(Room room, BookingRequest request, Long bookingId) {
        boolean overlapExists = (bookingId == null)
                //check for overlap when creating a new booking
                ? bookingRepository.existsByRoomAndDateAndStartTimeBetween(
                        room, request.getDate(), request.getStartTime(), request.getEndTime())
                //check for overlap when updating a booking
                : bookingRepository.existsByRoomAndDateAndStartTimeBetweenAndIdNot(
                        room, request.getDate(), request.getStartTime(), request.getEndTime(), bookingId);

        if (overlapExists) {
            throw new BookingOverlapException("Booking time overlaps with another booking.");
        }
    }

    public Booking findBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));
    }

    private Booking mapToBooking(Room room, BookingRequest request) {
        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setEmployeeEmail(request.getEmployeeEmail());
        booking.setDate(request.getDate());
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        return booking;
    }

    private BookingResponse mapToBookingResponse(String roomName, Booking booking) {
        return new BookingResponse(
                roomName,
                booking.getEmployeeEmail(),
                booking.getDate(),
                booking.getStartTime(),
                booking.getEndTime());
    }
}
