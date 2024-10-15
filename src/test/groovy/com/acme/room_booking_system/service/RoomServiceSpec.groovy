package com.acme.room_booking_system.service

import com.acme.room_booking_system.exception.RoomAlreadyExistsException
import com.acme.room_booking_system.model.Booking
import com.acme.room_booking_system.model.Room
import com.acme.room_booking_system.model.dto.RoomRequest
import com.acme.room_booking_system.model.dto.RoomResponse
import com.acme.room_booking_system.repository.RoomRepository
import com.acme.room_booking_system.helper.RoomHelper
import spock.lang.Specification

class RoomServiceSpec extends Specification {

    RoomService roomService
    RoomRepository roomRepository = Mock()
    RoomHelper roomHelper = Mock()

    def setup() {
        roomService = new RoomService(roomRepository, roomHelper)
    }

    def "should create room successfully"() {
        given: "A RoomRequest with a unique name"
        def request = new RoomRequest(name: "Room A")
        def room = new Room(name: request.getName())

        when: "The room is created"
        roomHelper.validateRoomNameUniqueness(request.name) >> { } // No exception thrown
        roomRepository.save(_) >> room // Mock save to return the room

        def createdRoom = roomService.createRoom(request)

        then: "The room is saved and returned"
        createdRoom.name == "Room A"
        1 * roomHelper.validateRoomNameUniqueness(request.name)
        1 * roomRepository.save(_)
    }

    def "should throw exception if room name already exists during creation"() {
        given: "A RoomRequest with an already existing room name"
        def request = new RoomRequest(name: "Room A")

        when: "The room creation is attempted"
        roomHelper.validateRoomNameUniqueness(request.name) >> { throw new RoomAlreadyExistsException("Room name already exists.") }

        roomService.createRoom(request)

        then: "An exception is thrown"
        def e = thrown(RoomAlreadyExistsException)
        e.message == "Room name already exists."
        //1 * roomHelper.validateRoomNameUniqueness(request.name)
        //0 * roomRepository.save(_)
    }

    def "should return all rooms successfully"() {
        given: "A list of rooms"
        def rooms = [new Room(name: "Room A"), new Room(name: "Room B")]

        // Mock findAll to return the list of rooms
        roomRepository.findAll() >> rooms

        when: "All rooms are retrieved"
        def result = roomService.getAllRooms()

        then: "The list of rooms is returned as RoomResponse"
        result.size() == 2
        result[0].name == "Room A"
        result[1].name == "Room B"
        //1 * roomRepository.findAll() // Ensure findAll was called
    }

    def "should update room successfully"() {
        given: "An existing room and a valid RoomRequest"
        def roomId = 1L
        def oldRoom = new Room(id: roomId, name: "Room A")
        def request = new RoomRequest(name: "Room B")

        when: "The room is updated"
        roomHelper.findRoomById(roomId) >> oldRoom // Mock findRoomById
        roomHelper.validateRoomNameUniqueness(request.getName()) >> { } // No exception thrown
        roomRepository.save(_) >> oldRoom // Mock save to return the room

        def updatedRoom = roomService.updateRoom(roomId, request)

        then: "The room is updated and saved"
        updatedRoom.name == "Room B" // Check the updated room name
        //1 * roomHelper.findRoomById(roomId)
        //1 * roomHelper.validateRoomNameUniqueness(request.getName())
        //1 * roomRepository.save(_)
    }

    def "should throw exception if trying to delete room with active bookings"() {
        given: "An existing room with active bookings"
        def roomId = 1L
        def room = new Room(id: roomId, name: "Conference Room A", bookings: [new Booking()])

        when: "Room deletion is attempted"
        roomHelper.findRoomById(roomId) >> room

        roomService.deleteRoom(roomId)

        then: "An exception is thrown"
        def e = thrown(IllegalArgumentException)
        e.message == "Cannot delete room with active bookings."
        0 * roomRepository.delete(_)
    }

    def "should delete room successfully when no active bookings"() {
        given: "An existing room with no active bookings"
        def roomId = 1L
        def room = new Room(id: roomId, name: "Conference Room A", bookings: [])

        when: "The room is deleted"
        roomHelper.findRoomById(roomId) >> room

        roomService.deleteRoom(roomId)

        then: "The room is deleted"
        1 * roomRepository.delete(room) // Ensure delete was called
    }
}
