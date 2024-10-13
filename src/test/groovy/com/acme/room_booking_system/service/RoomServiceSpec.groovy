package com.acme.room_booking_system.service

import com.acme.room_booking_system.model.Booking
import com.acme.room_booking_system.model.Room
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
        given: "A room name"
        def roomName = "Room A"
        def room = new Room(id: 1L, name: roomName)

        when: "The room is created"
        roomHelper.isRoomNameExists(roomName) >> { } //no exception thrown
        roomRepository.save(_) >> room

        def createdRoom = roomService.createRoom(roomName)

        then: "The room is saved and returned"
        createdRoom != null
        createdRoom.name == roomName
        1 * roomHelper.isRoomNameExists(roomName)
    }

    def "should throw exception if room name already exists during room creation"() {
        given: "A room name that already exists"
        def roomName = "Room A"

        when: "The room is created"
        roomHelper.isRoomNameExists(roomName) >> { throw new IllegalArgumentException("Room name already exists.") }

        def createdRoom = roomService.createRoom(roomName)

        then: "An exception is thrown"
        def e = thrown(IllegalArgumentException)
        e.message == "Room name already exists."
        //1 * roomHelper.isRoomNameExists(roomName)
    }

    def "should get all rooms successfully"() {
        given: "A list of rooms"
        def rooms = [new Room(id: 1L, name: "Room A"), new Room(id: 2L, name: "Room B")]

        when: "All rooms are retrieved"
        roomRepository.findAll() >> rooms

        def result = roomService.getAllRooms()

        then: "The rooms are returned"
        result.size() == 2
        result[0].name == "Room A"
        result[1].name == "Room B"
        //1 * roomRepository.findAll() // Ensure findAll was called
    }

    def "should update room successfully"() {
        given: "An existing room and a new name"
        def roomId = 1L
        def oldRoom = new Room(id: roomId, name: "Room A")
        def newRoomName = "Room B"
        def updatedRoom = new Room(id: roomId, name: newRoomName)  //the updated room with the new name

        when: "The room is updated"
        roomHelper.findRoomById(roomId) >> oldRoom
        roomHelper.isRoomNameExists(newRoomName) >> { }  //no exception thrown, new name is available
        roomRepository.save(_) >> updatedRoom

        def result = roomService.updateRoom(roomId, newRoomName)

        then: "The room is updated and saved"
        result.name == newRoomName  // Ensure the room's name was updated
        //1 * roomHelper.findRoomById(roomId)  // Ensure findRoomById was called once
        //1 * roomHelper.isRoomNameExists(newRoomName)  // Ensure the new name existence check was done
        //1 * roomRepository.save(_)  // Ensure the room was saved once
    }


    def "should throw exception if room name already exists during update"() {
        given: "An existing room and a new name that already exists"
        def roomId = 1L
        def oldRoom = new Room(id: roomId, name: "Room A")
        def newRoomName = "Room B" //the new name already exists and throw exception

        when: "The room update is attempted"
        roomHelper.findRoomById(roomId) >> oldRoom
        roomHelper.isRoomNameExists(newRoomName) >> { throw new IllegalArgumentException("Room name already exists.") }

        roomService.updateRoom(roomId, newRoomName)

        then: "An exception is thrown"
        def e = thrown(IllegalArgumentException)
        e.message == "Room name already exists."
        //1 * roomHelper.findRoomById(roomId) // Ensure findRoomById was called
        //1 * roomHelper.isRoomNameExists(newRoomName) // Ensure name existence check was done
        //0 * roomRepository.save(_)
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
        //1 * roomHelper.findRoomById(roomId) // Ensure findRoomById was called
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

        //1 * roomHelper.findRoomById(roomId) // Ensure findRoomById was called
        //0 * roomRepository.delete(_)
    }
}
