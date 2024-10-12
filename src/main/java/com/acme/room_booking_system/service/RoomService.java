package com.acme.room_booking_system.service;

import com.acme.room_booking_system.helper.RoomHelper;
import com.acme.room_booking_system.model.Room;
import com.acme.room_booking_system.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomHelper roomHelper;

    public Room createRoom(String name) {
        roomHelper.isRoomNameExists(name);

        Room room = new Room();
        room.setName(name);
        return roomRepository.save(room);
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Room updateRoom(Long roomId, String newName) {
        Room room = roomHelper.findRoomById(roomId);

        roomHelper.isRoomNameExists(newName);

        room.setName(newName);
        return roomRepository.save(room);
    }

    public void deleteRoom(Long roomId) {
        Room room = roomHelper.findRoomById(roomId);

        //check if the room has active bookings and prevent deletion
        if (!room.getBookings().isEmpty()) {
            throw new IllegalArgumentException("Cannot delete room with active bookings.");
        }

        roomRepository.delete(room);
    }
}