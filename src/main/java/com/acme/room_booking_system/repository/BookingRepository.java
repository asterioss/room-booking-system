package com.acme.room_booking_system.repository;

import com.acme.room_booking_system.model.Booking;
import com.acme.room_booking_system.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByRoomAndDate(Room room, LocalDate date);
    boolean existsByRoomAndDateAndStartTimeBetween(Room room, LocalDate date, LocalTime start, LocalTime end);
    boolean existsByRoomAndDateAndStartTimeBetweenAndIdNot(Room room, LocalDate date, LocalTime start, LocalTime end, Long id);
}