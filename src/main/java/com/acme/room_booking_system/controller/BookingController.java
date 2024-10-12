package com.acme.room_booking_system.controller;

import com.acme.room_booking_system.model.ApiError;
import com.acme.room_booking_system.model.Booking;
import com.acme.room_booking_system.model.BookingRequest;
import com.acme.room_booking_system.model.BookingResponse;
import com.acme.room_booking_system.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Booking Controller", description = "Manage bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @GetMapping
    @Operation(summary = "Get Bookings By Room", description = "Retrieve all bookings for a specific room and date")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Room not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<List<BookingResponse>> getBookingsByRoom(@RequestParam String roomName, @RequestParam LocalDate date) {
        return ResponseEntity.ok(bookingService.getBookingsByRoom(roomName, date));
    }

    @PostMapping
    @Operation(summary = "Create Booking", description = "Create a new booking for a room")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Booking created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid booking request",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Room not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Booking> createBooking(@RequestBody @Valid BookingRequest request) {
        return new ResponseEntity<>(bookingService.createBooking(request), HttpStatus.CREATED);
    }

    /*@GetMapping
    @Operation(summary = "Get All Bookings", description = "Retrieve a list of all bookings")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully")
    })
    public ResponseEntity<List<Booking>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }*/

    @PutMapping("/{id}")
    @Operation(summary = "Update Booking", description = "Update an existing booking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking updated successfully"),
            @ApiResponse(responseCode = "404", description = "Booking or room not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Booking> updateBooking(@PathVariable Long id, @RequestBody @Valid BookingRequest request) {
        Booking updatedBooking = bookingService.updateBooking(id, request);
        return new ResponseEntity<>(updatedBooking, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel Booking", description = "Cancel an existing booking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking canceled successfully"),
            @ApiResponse(responseCode = "404", description = "Booking not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.ok().build();
        //return ResponseEntity.noContent().build();
    }
}
