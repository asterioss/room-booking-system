package com.acme.room_booking_system.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private String employeeEmail;
    private LocalTime startTime;
    private LocalTime endTime;
}
