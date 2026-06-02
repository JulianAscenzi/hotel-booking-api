package com.portfolio.hotelbooking.dto;

import com.portfolio.hotelbooking.model.BookingStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record BookingResponse(
        Long id,
        UserResponse user,
        RoomResponse room,
        LocalDate checkIn,
        LocalDate checkOut,
        BigDecimal totalPrice,
        BookingStatus status,
        Instant createdAt
) implements Serializable {
}
