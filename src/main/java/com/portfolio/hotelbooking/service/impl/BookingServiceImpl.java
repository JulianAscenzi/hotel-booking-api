package com.portfolio.hotelbooking.service.impl;

import com.portfolio.hotelbooking.dto.BookingRequest;
import com.portfolio.hotelbooking.dto.BookingResponse;
import com.portfolio.hotelbooking.exception.BusinessException;
import com.portfolio.hotelbooking.exception.ResourceNotFoundException;
import com.portfolio.hotelbooking.mapper.BookingMapper;
import com.portfolio.hotelbooking.model.Booking;
import com.portfolio.hotelbooking.model.BookingStatus;
import com.portfolio.hotelbooking.model.Role;
import com.portfolio.hotelbooking.model.Room;
import com.portfolio.hotelbooking.model.User;
import com.portfolio.hotelbooking.repository.BookingRepository;
import com.portfolio.hotelbooking.repository.RoomRepository;
import com.portfolio.hotelbooking.service.AuthService;
import com.portfolio.hotelbooking.service.BookingService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final AuthService authService;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              RoomRepository roomRepository,
                              AuthService authService) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.authService = authService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookings() {
        User currentUser = authService.getCurrentUser();
        List<Booking> bookings = currentUser.getRole() == Role.ADMIN
                ? bookingRepository.findAllByOrderByCreatedAtDesc()
                : bookingRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
        return bookings.stream().map(BookingMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBooking(Long id) {
        Booking booking = findBooking(id);
        assertCanAccess(booking, authService.getCurrentUser());
        return BookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    @CacheEvict(value = "availableRooms", allEntries = true)
    public BookingResponse createBooking(BookingRequest request) {
        if (!request.checkOut().isAfter(request.checkIn())) {
            throw new BusinessException("Check-out date must be after check-in date");
        }

        Room room = roomRepository.findByIdAndActiveTrue(request.roomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        boolean overlaps = bookingRepository.existsOverlappingBooking(
                room.getId(),
                request.checkIn(),
                request.checkOut(),
                BookingStatus.CANCELLED);
        if (overlaps) {
            throw new BusinessException("Room is not available for the requested dates");
        }

        long nights = ChronoUnit.DAYS.between(request.checkIn(), request.checkOut());
        Booking booking = new Booking();
        booking.setUser(authService.getCurrentUser());
        booking.setRoom(room);
        booking.setCheckIn(request.checkIn());
        booking.setCheckOut(request.checkOut());
        booking.setTotalPrice(room.getPricePerNight().multiply(BigDecimal.valueOf(nights)));
        booking.setStatus(BookingStatus.PENDING);

        return BookingMapper.toResponse(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    @CacheEvict(value = "availableRooms", allEntries = true)
    public BookingResponse cancelBooking(Long id) {
        Booking booking = findBooking(id);
        assertCanAccess(booking, authService.getCurrentUser());
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BusinessException("Only pending bookings can be cancelled");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        return BookingMapper.toResponse(bookingRepository.save(booking));
    }

    private Booking findBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    }

    private void assertCanAccess(Booking booking, User user) {
        if (user.getRole() != Role.ADMIN && !booking.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Access denied");
        }
    }
}
