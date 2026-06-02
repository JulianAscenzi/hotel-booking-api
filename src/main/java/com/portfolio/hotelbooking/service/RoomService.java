package com.portfolio.hotelbooking.service;

import com.portfolio.hotelbooking.dto.RoomRequest;
import com.portfolio.hotelbooking.dto.RoomResponse;
import com.portfolio.hotelbooking.model.RoomType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface RoomService {

    Page<RoomResponse> getRooms(RoomType type, Pageable pageable);

    List<RoomResponse> getAvailableRooms(LocalDate checkIn, LocalDate checkOut);

    RoomResponse getRoom(Long id);

    RoomResponse createRoom(RoomRequest request);

    RoomResponse updateRoom(Long id, RoomRequest request);

    void deleteRoom(Long id);
}
