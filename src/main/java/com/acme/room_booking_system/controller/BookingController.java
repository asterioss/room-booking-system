package com.acme.room_booking_system.controller;

import com.acme.room_booking_system.model.ApiError;
import com.acme.room_booking_system.model.dto.BookingRequest;
import com.acme.room_booking_system.model.dto.BookingResponse;
import com.acme.room_booking_system.service.BookingService;
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

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Booking Controller", description = "Manage bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    @Operation(summary = "Get All Bookings", description = "Retrieve a list of all bookings")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bookings Retrieved Successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        List<BookingResponse> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{roomName}")
    @Operation(summary = "Get Bookings By Room And Date", description = "Retrieve all bookings for a specific room and date")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Entity Not Found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<List<BookingResponse>> getBookingsByRoomAndDate(@PathVariable String roomName,
                                                                          @RequestParam @Valid LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("The date cannot be in the past.");
        }

        List<BookingResponse> bookings = bookingService.getBookingsByRoomAndDate(roomName, date);
        return ResponseEntity.ok(bookings);
    }

    @PostMapping
    @Operation(summary = "Create Booking", description = "Create a new booking for a room")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Booking Created Successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Entity Not Found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<BookingResponse> createBooking(@RequestBody @Valid BookingRequest request) {
        BookingResponse booking = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Booking", description = "Update an existing booking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking Updated Successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Entity Not Found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<BookingResponse> updateBooking(@PathVariable Long id, @RequestBody @Valid BookingRequest request) {
        BookingResponse updatedBooking = bookingService.updateBooking(id, request);
        return ResponseEntity.ok(updatedBooking);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel Booking", description = "Cancel an existing booking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Booking Canceled Successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Entity Not Found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.ok().build();
    }
}
