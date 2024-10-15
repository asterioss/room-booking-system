package com.acme.room_booking_system.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)  // This will exclude null values from the JSON response
public class BookingResponse {
    private String roomName;
    private String employeeEmail;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    public BookingResponse(String employeeEmail, LocalTime startTime, LocalTime endTime) {
        this.employeeEmail = employeeEmail;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
