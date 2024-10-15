package com.acme.room_booking_system.exception;

public class InvalidBookingDurationException extends RuntimeException {

    public InvalidBookingDurationException(String message) {
        super(message);
    }
}
