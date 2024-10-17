package com.acme.room_booking_system.helper;

import com.acme.room_booking_system.exception.BookingOverlapException;
import com.acme.room_booking_system.exception.InvalidBookingDurationException;
import com.acme.room_booking_system.model.dto.BookingRequest;
import com.acme.room_booking_system.model.dto.BookingResponse;
import com.acme.room_booking_system.model.entity.Booking;
import com.acme.room_booking_system.model.entity.Room;
import com.acme.room_booking_system.repository.BookingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class BookingHelper {

    private final BookingRepository bookingRepository;

    public void validateBookingAndDates(BookingRequest request, Room room, Long bookingId) {
        validatePastDateAndTimes(request.getDate(), request.getStartTime());
        validateBookingDuration(request.getStartTime(), request.getEndTime());
        validateBookingOverlap(room, request, bookingId);
    }

    //validate that the booking date and times are not in the past
    private void validatePastDateAndTimes(LocalDate date, LocalTime startTime) {
        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        if (date.isBefore(today)) {
            throw new IllegalArgumentException("The booking date cannot be in the past.");
        }

        if (date.equals(today) && startTime.isBefore(currentTime)) {
            throw new IllegalArgumentException("The booking start time cannot be in the past.");
        }
    }

    //validate booking duration is at least 1 hour or consecutive multiples of 1 hour (60 minutes, 120 minutes, 180 minutes, etc.)
    private void validateBookingDuration(LocalTime startTime, LocalTime endTime) {
        long minutes = Duration.between(startTime, endTime).toMinutes();

        if (minutes < 60 || minutes % 60 != 0) {
            throw new InvalidBookingDurationException("Booking must be at least 1 hour or a multiple of 1 hour.");
        }
    }

    //validate if the booking overlaps with others
    private void validateBookingOverlap(Room room, BookingRequest request, Long bookingId) {
        boolean overlapExists = (bookingId == null)
                //check for overlap when creating a new booking
                ? bookingRepository.existsByRoomAndDateAndStartTimeLessThanAndEndTimeGreaterThan(
                        room, request.getDate(), request.getEndTime(), request.getStartTime())
                //check for overlap when updating a booking excluding the current booking id
                : bookingRepository.existsByRoomAndDateAndStartTimeLessThanAndEndTimeGreaterThanAndIdNot(
                        room, request.getDate(), request.getEndTime(), request.getStartTime(), bookingId);

        if (overlapExists) {
            throw new BookingOverlapException("Booking time overlaps with another booking.");
        }
    }

    public Booking findBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));
    }

    public Booking mapToBooking(Room room, BookingRequest request) {
        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setEmployeeEmail(request.getEmployeeEmail());
        booking.setDate(request.getDate());
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        return booking;
    }

    public BookingResponse mapToBookingResponse(String roomName, Booking booking) {
        return new BookingResponse(roomName, booking.getEmployeeEmail(),
                booking.getDate(), booking.getStartTime(), booking.getEndTime());
    }
}
