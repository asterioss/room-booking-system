package com.acme.room_booking_system.model.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

    @NotBlank(message = "Room name is required")
    private String roomName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Employee email is required")
    private String employeeEmail;

    @NotNull(message = "Booking date is required")
    private LocalDate date;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @AssertTrue(message = "Start time must be before end time.")
    public boolean isStartTimeBeforeEndTime() {
        return startTime != null && endTime != null && startTime.isBefore(endTime);
    }
}
