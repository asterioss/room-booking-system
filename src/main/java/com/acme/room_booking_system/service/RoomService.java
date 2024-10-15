package com.acme.room_booking_system.service;

import com.acme.room_booking_system.exception.RoomDeletionException;
import com.acme.room_booking_system.helper.RoomHelper;
import com.acme.room_booking_system.model.entity.Room;
import com.acme.room_booking_system.model.dto.RoomRequest;
import com.acme.room_booking_system.model.dto.RoomResponse;
import com.acme.room_booking_system.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomHelper roomHelper;

    public RoomResponse createRoom(RoomRequest request) {
        roomHelper.checkRoomNameUniqueness(request.getName());

        Room room = new Room();
        room.setName(request.getName());

        roomRepository.save(room);
        return new RoomResponse(room.getName());
    }

    public List<RoomResponse> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(room -> new RoomResponse(room.getName()))
                .collect(Collectors.toList());
    }

    public RoomResponse updateRoom(Long roomId, RoomRequest request) {
        Room room = roomHelper.findRoomById(roomId);

        roomHelper.checkRoomNameUniqueness(request.getName());

        room.setName(request.getName());

        roomRepository.save(room);
        return new RoomResponse(room.getName());
    }

    public void deleteRoom(Long roomId) {
        Room room = roomHelper.findRoomById(roomId);

        //prevent deletion if the room has active bookings
        if (!room.getBookings().isEmpty()) {
            throw new RoomDeletionException("Cannot delete room with active bookings.");
        }

        roomRepository.delete(room);
    }
}
