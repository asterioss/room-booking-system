package com.acme.room_booking_system.exception;

public class BookingOverlapException extends RuntimeException {

    public BookingOverlapException(String message) {
        super(message);
    }
}
