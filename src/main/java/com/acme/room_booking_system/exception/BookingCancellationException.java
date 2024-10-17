package com.acme.room_booking_system.exception;

public class BookingCancellationException extends BadRequestException {

    public BookingCancellationException(String message) {
        super(message);
    }
}
