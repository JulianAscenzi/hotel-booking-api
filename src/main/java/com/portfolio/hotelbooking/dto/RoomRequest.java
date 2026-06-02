package com.portfolio.hotelbooking.dto;

import com.portfolio.hotelbooking.model.RoomType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RoomRequest(
        @NotBlank @Size(max = 30) String number,
        @NotNull RoomType type,
        @NotNull @Positive BigDecimal pricePerNight,
        @NotBlank @Size(max = 1000) String description,
        Boolean active
) {
}
