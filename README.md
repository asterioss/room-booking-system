# Room Booking System API
A backend system for managing room bookings. This API provides functionality to create, update, view and delete rooms as well as manage bookings. The system ensures valid booking durations, prevents overlapping bookings and restricts deleting rooms with active bookings.

## Features

### Room Management
- **Create** new rooms.
- **Update** existing rooms.
- **View** all rooms or specific rooms.
- **Delete** rooms (only if no active bookings are present).

### Booking Management
- **Book** rooms for a specific time slot.
- **Update** existing bookings.
- **View** bookings for a specific room and date.
- **Cancel** future bookings (past bookings cannot be canceled).

### Validation
- **Overlapping Bookings**: Prevents double bookings for the same room and time slot.
- **Minimum Duration**: Ensures bookings are at least 1 hour or consecutive multiples of 1 hour (e.g. 2 hours, 3 hours).
- **Room Deletion Restriction**: Rooms with active bookings cannot be deleted.

## Technologies
- **Java 17**
- **Spring Boot 3.3.4**
- **Spring Security** (with Basic Authentication)
- **H2 Database** (In-memory for development)
- **Spring Data JPA and Hibernate**
- **Lombok**
- **Swagger** (API Documentation): [http://localhost:8080/swagger-ui/index.html#/](http://localhost:8080/swagger-ui/index.html#/)

## Setup Instructions

### Environment Variables
The following environment variables are required before running the application:

| Variable Name                | Description                       |
|------------------------------|-----------------------------------|
| `BASIC_AUTH_USERNAME`        | Username for basic authentication |
| `BASIC_AUTH_PASSWORD`        | Password for basic authentication |
| `SPRING_DATASOURCE_USERNAME` | Database username                 |
| `SPRING_DATASOURCE_PASSWORD` | Database password                 |
