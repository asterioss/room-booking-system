package com.acme.room_booking_system.exception;

public class RoomDeletionException extends BadRequestException {

    public RoomDeletionException(String message) {
        super(message);
    }
}
