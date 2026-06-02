package com.portfolio.hotelbooking.service;

import com.portfolio.hotelbooking.dto.BookingRequest;
import com.portfolio.hotelbooking.dto.BookingResponse;
import com.portfolio.hotelbooking.exception.BusinessException;
import com.portfolio.hotelbooking.model.Booking;
import com.portfolio.hotelbooking.model.BookingStatus;
import com.portfolio.hotelbooking.model.Role;
import com.portfolio.hotelbooking.model.Room;
import com.portfolio.hotelbooking.model.RoomType;
import com.portfolio.hotelbooking.model.User;
import com.portfolio.hotelbooking.repository.BookingRepository;
import com.portfolio.hotelbooking.repository.RoomRepository;
import com.portfolio.hotelbooking.service.impl.BookingServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void createBookingCalculatesTotalPrice() {
        Room room = room();
        User user = user(10L, Role.CLIENT);
        LocalDate checkIn = LocalDate.now().plusDays(10);
        LocalDate checkOut = checkIn.plusDays(3);

        when(roomRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(room));
        when(bookingRepository.existsOverlappingBooking(1L, checkIn, checkOut, BookingStatus.CANCELLED)).thenReturn(false);
        when(authService.getCurrentUser()).thenReturn(user);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(99L);
            return booking;
        });

        BookingResponse response = bookingService.createBooking(new BookingRequest(1L, checkIn, checkOut));

        assertThat(response.totalPrice()).isEqualByComparingTo("450.00");
        assertThat(response.status()).isEqualTo(BookingStatus.PENDING);
    }

    @Test
    void createBookingRejectsOverlap() {
        Room room = room();
        LocalDate checkIn = LocalDate.now().plusDays(10);
        LocalDate checkOut = checkIn.plusDays(2);

        when(roomRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(room));
        when(bookingRepository.existsOverlappingBooking(1L, checkIn, checkOut, BookingStatus.CANCELLED)).thenReturn(true);

        assertThatThrownBy(() -> bookingService.createBooking(new BookingRequest(1L, checkIn, checkOut)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Room is not available for the requested dates");
    }

    @Test
    void cancelBookingRejectsNonPendingBooking() {
        Booking booking = booking(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));
        when(authService.getCurrentUser()).thenReturn(booking.getUser());

        assertThatThrownBy(() -> bookingService.cancelBooking(5L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Only pending bookings can be cancelled");
    }

    private Booking booking(BookingStatus status) {
        Booking booking = new Booking();
        booking.setId(5L);
        booking.setUser(user(10L, Role.CLIENT));
        booking.setRoom(room());
        booking.setCheckIn(LocalDate.now().plusDays(10));
        booking.setCheckOut(LocalDate.now().plusDays(12));
        booking.setTotalPrice(BigDecimal.valueOf(300));
        booking.setStatus(status);
        return booking;
    }

    private Room room() {
        Room room = new Room();
        room.setId(1L);
        room.setNumber("101");
        room.setType(RoomType.DOUBLE);
        room.setPricePerNight(BigDecimal.valueOf(150));
        room.setDescription("Nice room");
        room.setActive(true);
        return room;
    }

    private User user(Long id, Role role) {
        User user = new User();
        user.setId(id);
        user.setName("User");
        user.setEmail("user" + id + "@hotel.com");
        user.setPassword("encoded");
        user.setRole(role);
        return user;
    }
}
