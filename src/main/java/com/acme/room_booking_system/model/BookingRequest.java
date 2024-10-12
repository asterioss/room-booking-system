package com.acme.room_booking_system.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {
    private String roomName;
    private String employeeEmail;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
}
