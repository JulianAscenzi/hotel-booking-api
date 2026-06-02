package com.portfolio.hotelbooking.service.impl;

import com.portfolio.hotelbooking.dto.RoomRequest;
import com.portfolio.hotelbooking.dto.RoomResponse;
import com.portfolio.hotelbooking.exception.BusinessException;
import com.portfolio.hotelbooking.exception.ResourceNotFoundException;
import com.portfolio.hotelbooking.mapper.RoomMapper;
import com.portfolio.hotelbooking.model.BookingStatus;
import com.portfolio.hotelbooking.model.Room;
import com.portfolio.hotelbooking.model.RoomType;
import com.portfolio.hotelbooking.repository.BookingRepository;
import com.portfolio.hotelbooking.repository.RoomRepository;
import com.portfolio.hotelbooking.service.RoomService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public RoomServiceImpl(RoomRepository roomRepository, BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoomResponse> getRooms(RoomType type, Pageable pageable) {
        Page<Room> rooms = type == null
                ? roomRepository.findByActiveTrue(pageable)
                : roomRepository.findByActiveTrueAndType(type, pageable);
        return rooms.map(RoomMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "availableRooms", key = "{#checkIn, #checkOut}")
    public List<RoomResponse> getAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        validateDateRange(checkIn, checkOut);
        return roomRepository.findByActiveTrue().stream()
                .filter(room -> !bookingRepository.existsOverlappingBooking(
                        room.getId(),
                        checkIn,
                        checkOut,
                        BookingStatus.CANCELLED))
                .map(RoomMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponse getRoom(Long id) {
        return RoomMapper.toResponse(findActiveRoom(id));
    }

    @Override
    @Transactional
    @CacheEvict(value = "availableRooms", allEntries = true)
    public RoomResponse createRoom(RoomRequest request) {
        if (roomRepository.existsByNumber(request.number())) {
            throw new BusinessException("Room number already exists");
        }
        Room room = RoomMapper.toEntity(request);
        return RoomMapper.toResponse(roomRepository.save(room));
    }

    @Override
    @Transactional
    @CacheEvict(value = "availableRooms", allEntries = true)
    public RoomResponse updateRoom(Long id, RoomRequest request) {
        Room room = findActiveRoom(id);
        if (roomRepository.existsByNumberAndIdNot(request.number(), id)) {
            throw new BusinessException("Room number already exists");
        }
        RoomMapper.updateEntity(room, request);
        return RoomMapper.toResponse(roomRepository.save(room));
    }

    @Override
    @Transactional
    @CacheEvict(value = "availableRooms", allEntries = true)
    public void deleteRoom(Long id) {
        Room room = findActiveRoom(id);
        if (bookingRepository.existsFutureBookingForRoom(id, LocalDate.now(), BookingStatus.CANCELLED)) {
            throw new BusinessException("Room has future bookings and cannot be deleted");
        }
        room.setActive(false);
        roomRepository.save(room);
    }

    private Room findActiveRoom(Long id) {
        return roomRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
    }

    private void validateDateRange(LocalDate checkIn, LocalDate checkOut) {
        if (!checkOut.isAfter(checkIn)) {
            throw new BusinessException("Check-out date must be after check-in date");
        }
    }
}
