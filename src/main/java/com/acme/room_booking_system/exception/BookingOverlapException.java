package com.acme.room_booking_system.exception;

public class BookingOverlapException extends BadRequestException {

    public BookingOverlapException(String message) {
        super(message);
    }
}
