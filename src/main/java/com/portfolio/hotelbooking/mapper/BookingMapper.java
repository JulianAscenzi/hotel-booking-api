package com.portfolio.hotelbooking.mapper;

import com.portfolio.hotelbooking.dto.BookingResponse;
import com.portfolio.hotelbooking.model.Booking;

public final class BookingMapper {

    private BookingMapper() {
    }

    public static BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                UserMapper.toResponse(booking.getUser()),
                RoomMapper.toResponse(booking.getRoom()),
                booking.getCheckIn(),
                booking.getCheckOut(),
                booking.getTotalPrice(),
                booking.getStatus(),
                booking.getCreatedAt()
        );
    }
}
