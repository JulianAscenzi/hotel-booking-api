package com.portfolio.hotelbooking.dto;

import com.portfolio.hotelbooking.model.RoomType;

import java.io.Serializable;
import java.math.BigDecimal;

public record RoomResponse(
        Long id,
        String number,
        RoomType type,
        BigDecimal pricePerNight,
        String description,
        boolean active
) implements Serializable {
}
