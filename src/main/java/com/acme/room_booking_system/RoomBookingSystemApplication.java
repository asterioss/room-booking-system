package com.acme.room_booking_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing //enable jpa auditing for the entities
public class RoomBookingSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoomBookingSystemApplication.class, args);
	}

}
