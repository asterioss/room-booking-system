package com.acme.room_booking_system.repository;

import com.acme.room_booking_system.model.entity.Booking;
import com.acme.room_booking_system.model.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByRoomAndDate(Room room, LocalDate date);
    boolean existsByRoomAndDateAndStartTimeLessThanAndEndTimeGreaterThan(Room room, LocalDate date, LocalTime startTime, LocalTime endTime);
    boolean existsByRoomAndDateAndStartTimeLessThanAndEndTimeGreaterThanAndIdNot(Room room, LocalDate date, LocalTime startTime, LocalTime endTime, Long id);
}