package com.acme.room_booking_system.exception;

public class BookingCancellationException extends RuntimeException {

    public BookingCancellationException(String message) {
        super(message);
    }
}
