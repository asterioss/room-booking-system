package com.acme.room_booking_system.controller;

import com.acme.room_booking_system.model.ApiError;
import com.acme.room_booking_system.model.dto.RoomRequest;
import com.acme.room_booking_system.model.dto.RoomResponse;
import com.acme.room_booking_system.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@Tag(name = "Room Controller", description = "Manage rooms and their availability")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    @Operation(summary = "Get All Rooms", description = "Retrieve a list of all rooms")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rooms retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<List<RoomResponse>> getAllRooms() {
        List<RoomResponse> rooms = roomService.getAllRooms();
        return ResponseEntity.ok(rooms);
    }

    @PostMapping
    @Operation(summary = "Create Room", description = "Create a new room")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Room created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid room name",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<RoomResponse> createRoom(@RequestBody @Valid RoomRequest roomRequest) {
        RoomResponse roomResponse = roomService.createRoom(roomRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(roomResponse);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Room", description = "Update the name of an existing room")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Room updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid room name",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Room not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<RoomResponse> updateRoom(@PathVariable Long id, @RequestBody @Valid RoomRequest roomRequest) {
        RoomResponse updatedRoom = roomService.updateRoom(id, roomRequest);
        return ResponseEntity.ok(updatedRoom);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Room", description = "Delete an existing room")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Room deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Room not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
}
