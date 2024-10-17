package com.acme.room_booking_system.helper;

import com.acme.room_booking_system.exception.RoomAlreadyExistsException;
import com.acme.room_booking_system.model.entity.Room;
import com.acme.room_booking_system.repository.RoomRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoomHelper {

    private final RoomRepository roomRepository;

    public Room findRoomById(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found with id: " + roomId));
    }

    public Room findRoomByName(String name) {
        return roomRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Room not found with name: " + name));
    }

    //check if a room name already exists
    public void checkRoomNameUniqueness(String name) {
        if (roomRepository.existsByName(name)) {
            throw new RoomAlreadyExistsException("Room with name " + name + " already exists.");
        }
    }
}
