package com.acme.room_booking_system.exception;

public class InvalidBookingDurationException extends BadRequestException {

    public InvalidBookingDurationException(String message) {
        super(message);
    }
}
