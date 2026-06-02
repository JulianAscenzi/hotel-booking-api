package com.portfolio.hotelbooking.mapper;

import com.portfolio.hotelbooking.dto.RoomRequest;
import com.portfolio.hotelbooking.dto.RoomResponse;
import com.portfolio.hotelbooking.model.Room;

public final class RoomMapper {

    private RoomMapper() {
    }

    public static RoomResponse toResponse(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getNumber(),
                room.getType(),
                room.getPricePerNight(),
                room.getDescription(),
                room.isActive()
        );
    }

    public static Room toEntity(RoomRequest request) {
        Room room = new Room();
        updateEntity(room, request);
        if (request.active() == null) {
            room.setActive(true);
        }
        return room;
    }

    public static void updateEntity(Room room, RoomRequest request) {
        room.setNumber(request.number());
        room.setType(request.type());
        room.setPricePerNight(request.pricePerNight());
        room.setDescription(request.description());
        if (request.active() != null) {
            room.setActive(request.active());
        }
    }
}
