package com.acme.room_booking_system.helper;

import com.acme.room_booking_system.exception.RoomAlreadyExistsException;
import com.acme.room_booking_system.model.Room;
import com.acme.room_booking_system.repository.RoomRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RoomHelper {

    @Autowired
    private RoomRepository roomRepository;

    public Room findRoomById(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));
    }

    public Room findRoomByName(String name) {
        return roomRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));
    }

    public void isRoomNameExists(String name) {
        //check if a room with the name already exists
        if (roomRepository.findByName(name).isPresent()) {
            throw new RoomAlreadyExistsException("Room with name " + name + " already exists.");
        }
    }
}
