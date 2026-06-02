package com.portfolio.hotelbooking.service;

import com.portfolio.hotelbooking.dto.RoomRequest;
import com.portfolio.hotelbooking.dto.RoomResponse;
import com.portfolio.hotelbooking.exception.BusinessException;
import com.portfolio.hotelbooking.exception.ResourceNotFoundException;
import com.portfolio.hotelbooking.model.BookingStatus;
import com.portfolio.hotelbooking.model.Room;
import com.portfolio.hotelbooking.model.RoomType;
import com.portfolio.hotelbooking.repository.BookingRepository;
import com.portfolio.hotelbooking.repository.RoomRepository;
import com.portfolio.hotelbooking.service.impl.RoomServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private RoomServiceImpl roomService;

    @Test
    void getAvailableRoomsExcludesOverlappingBookings() {
        Room available = room(1L, "101");
        Room booked = room(2L, "102");
        LocalDate checkIn = LocalDate.now().plusDays(10);
        LocalDate checkOut = checkIn.plusDays(2);

        when(roomRepository.findByActiveTrue()).thenReturn(List.of(available, booked));
        when(bookingRepository.existsOverlappingBooking(1L, checkIn, checkOut, BookingStatus.CANCELLED)).thenReturn(false);
        when(bookingRepository.existsOverlappingBooking(2L, checkIn, checkOut, BookingStatus.CANCELLED)).thenReturn(true);

        List<RoomResponse> result = roomService.getAvailableRooms(checkIn, checkOut);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().number()).isEqualTo("101");
    }

    @Test
    void createRoomRejectsDuplicateNumber() {
        RoomRequest request = new RoomRequest("101", RoomType.DOUBLE, BigDecimal.valueOf(150), "Nice room", true);
        when(roomRepository.existsByNumber("101")).thenReturn(true);

        assertThatThrownBy(() -> roomService.createRoom(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Room number already exists");
    }

    @Test
    void deleteRoomSoftDeletesWhenThereAreNoFutureBookings() {
        Room room = room(1L, "101");
        when(roomRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(room));
        when(bookingRepository.existsFutureBookingForRoom(eq(1L), any(LocalDate.class), eq(BookingStatus.CANCELLED)))
                .thenReturn(false);

        roomService.deleteRoom(1L);

        assertThat(room.isActive()).isFalse();
        verify(roomRepository).save(room);
    }

    @Test
    void getRoomThrowsWhenInactiveOrMissing() {
        when(roomRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.getRoom(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Room not found");
    }

    private Room room(Long id, String number) {
        Room room = new Room();
        room.setId(id);
        room.setNumber(number);
        room.setType(RoomType.DOUBLE);
        room.setPricePerNight(BigDecimal.valueOf(150));
        room.setDescription("Nice room");
        room.setActive(true);
        return room;
    }
}
