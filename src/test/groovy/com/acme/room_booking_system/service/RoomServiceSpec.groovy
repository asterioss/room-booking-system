package com.acme.room_booking_system.service

import com.acme.room_booking_system.exception.RoomAlreadyExistsException
import com.acme.room_booking_system.exception.RoomDeletionException
import com.acme.room_booking_system.model.entity.Booking
import com.acme.room_booking_system.model.entity.Room
import com.acme.room_booking_system.model.dto.RoomRequest
import com.acme.room_booking_system.repository.RoomRepository
import com.acme.room_booking_system.helper.RoomHelper
import jakarta.persistence.EntityNotFoundException
import spock.lang.Specification

class RoomServiceSpec extends Specification {

    RoomService roomService
    RoomRepository roomRepository = Mock()
    RoomHelper roomHelper = Mock()

    def setup() {
        roomService = new RoomService(roomRepository, roomHelper)
    }

    def "Create room successfully"() {
        given: "A RoomRequest with a unique name"
        def request = new RoomRequest(name: "Room A")
        def room = new Room(name: request.getName())

        when: "The room is created"
        roomHelper.checkRoomNameUniqueness(request.name) >> { } //no exception thrown
        roomRepository.save(_) >> room

        def createdRoom = roomService.createRoom(request)

        then: "The room is saved and returned"
        createdRoom.name == "Room A"
        1 * roomHelper.checkRoomNameUniqueness(request.name)
        1 * roomRepository.save(_)
    }

    def "Throw exception if room name already exists during creation"() {
        given: "A RoomRequest with an already existing room name"
        def request = new RoomRequest(name: "Room A")

        when: "The room creation is attempted"
        roomHelper.checkRoomNameUniqueness(request.name) >> { throw new RoomAlreadyExistsException("Room with name " + request.name + " already exists.") }

        roomService.createRoom(request)

        then: "An exception is thrown"
        def e = thrown(RoomAlreadyExistsException)
        e.message == "Room with name " + request.name + " already exists."
    }

    def "Return all rooms successfully"() {
        given: "A list of rooms"
        def rooms = [new Room(id: 1L, name: "Room A"), new Room(id: 2L, name: "Room B")]

        when: "All rooms are retrieved"
        roomRepository.findAll() >> rooms

        def result = roomService.getAllRooms()

        then: "The list of rooms is returned as RoomResponse list"
        result.size() == 2
        result[0].name == "Room A"
        result[1].name == "Room B"
    }

    def "Update room successfully"() {
        given: "An existing room and a valid RoomRequest"
        def roomId = 1L
        def oldRoom = new Room(id: roomId, name: "Room A")
        def request = new RoomRequest(name: "Room B")

        when: "The room is updated"
        roomHelper.findRoomById(roomId) >> oldRoom
        roomHelper.checkRoomNameUniqueness(request.getName()) >> { }
        roomRepository.save(_) >> oldRoom

        def updatedRoom = roomService.updateRoom(roomId, request)

        then: "The room is updated and saved"
        updatedRoom.name == "Room B"
    }

    def "Delete room successfully when no active bookings"() {
        given: "An existing room with no active bookings"
        def roomId = 1L
        def room = new Room(id: roomId, name: "Room A", bookings: [])

        when: "The room is deleted"
        roomHelper.findRoomById(roomId) >> room

        roomService.deleteRoom(roomId)

        then: "The room is deleted"
        1 * roomRepository.delete(room)
    }

    def "Throw exception if room not found by ID"() {
        given: "No room exists with the given ID"
        def roomId = 1L
        roomRepository.findById(roomId) >> Optional.empty()

        when: "The room deletion is attempted"
        roomHelper.findRoomById(roomId) >> { throw new EntityNotFoundException("Room not found with id: " + roomId) }

        roomService.deleteRoom(roomId)

        then: "An exception is thrown"
        def e = thrown(EntityNotFoundException)
        e.message == "Room not found with id: 1"
    }

    def "Throw exception if trying to delete room with active bookings"() {
        given: "An existing room with active bookings"
        def roomId = 1L
        def room = new Room(id: roomId, name: "Room A", bookings: [new Booking()])

        when: "Room deletion is attempted"
        roomHelper.findRoomById(roomId) >> room

        roomService.deleteRoom(roomId)

        then: "An exception is thrown"
        def e = thrown(RoomDeletionException)
        e.message == "Cannot delete room with active bookings."
        0 * roomRepository.delete(_)
    }
}
