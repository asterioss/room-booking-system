package com.acme.room_booking_system.controller;

import com.acme.room_booking_system.model.ApiError;
import com.acme.room_booking_system.model.Room;
import com.acme.room_booking_system.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@Tag(name = "Room Controller", description = "Manage rooms and their availability")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @PostMapping
    @Operation(summary = "Create Room", description = "Create a new room")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Room created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid room name",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Room> createRoom(@RequestParam String name) {
        Room room = roomService.createRoom(name);
        return new ResponseEntity<>(room, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get All Rooms", description = "Retrieve a list of all rooms")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rooms retrieved successfully")
    })
    public ResponseEntity<List<Room>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Room", description = "Update the name of an existing room")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Room updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid room name",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Room not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Room> updateRoom(@PathVariable Long id, @RequestParam String name) {
        Room updatedRoom = roomService.updateRoom(id, name);
        return new ResponseEntity<>(updatedRoom, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Room", description = "Delete an existing room")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Room deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Room not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return new ResponseEntity<>(HttpStatus.OK); // or HttpStatus.NO_CONTENT for a successful deletion
    }

    /*@GetMapping("/availability")
    public ResponseEntity<List<Room>> getAvailableRooms(@RequestParam LocalDate date,
                                                        @RequestParam LocalTime startTime,
                                                        @RequestParam LocalTime endTime) {
        List<Room> availableRooms = roomService.findAvailableRooms(date, startTime, endTime);
        return new ResponseEntity<>(availableRooms, HttpStatus.OK);
    }*/
}
