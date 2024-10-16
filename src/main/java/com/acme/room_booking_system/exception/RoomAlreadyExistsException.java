package com.acme.room_booking_system.exception;

public class RoomAlreadyExistsException extends BadRequestException {

    public RoomAlreadyExistsException(String message) {
        super(message);
    }
}
