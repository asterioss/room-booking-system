package com.acme.room_booking_system.service;

import com.acme.room_booking_system.exception.BookingCancellationException;
import com.acme.room_booking_system.helper.BookingHelper;
import com.acme.room_booking_system.helper.RoomHelper;
import com.acme.room_booking_system.model.entity.Booking;
import com.acme.room_booking_system.model.dto.BookingRequest;
import com.acme.room_booking_system.model.dto.BookingResponse;
import com.acme.room_booking_system.model.entity.Room;
import com.acme.room_booking_system.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingHelper bookingHelper;
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

        bookingHelper.validateBookingAndDates(request, room, null);

        Booking booking = bookingHelper.mapToBooking(room, request);
        booking = bookingRepository.save(booking);

        return bookingHelper.mapToBookingResponse(room.getName(), booking);
    }

    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(booking -> bookingHelper.mapToBookingResponse(booking.getRoom().getName(), booking))
                .collect(Collectors.toList());
    }

    public BookingResponse updateBooking(Long bookingId, BookingRequest request) {
        Booking existingBooking = bookingHelper.findBookingById(bookingId);
        Room room = roomHelper.findRoomByName(request.getRoomName());

        bookingHelper.validateBookingAndDates(request, room, bookingId);

        //existingBooking.setRoom(room);
        existingBooking.setEmployeeEmail(request.getEmployeeEmail());
        existingBooking.setDate(request.getDate());
        existingBooking.setStartTime(request.getStartTime());
        existingBooking.setEndTime(request.getEndTime());

        Booking updatedBooking = bookingRepository.save(existingBooking);

        return bookingHelper.mapToBookingResponse(room.getName(), updatedBooking);
    }

    public void cancelBooking(Long bookingId) {
        Booking booking = bookingHelper.findBookingById(bookingId);

        //prevent canceling past bookings
        if (booking.getDate().isBefore(LocalDate.now())) {
            throw new BookingCancellationException("Cannot cancel past bookings.");
        }

        bookingRepository.delete(booking);
    }
}
