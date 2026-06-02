package com.portfolio.hotelbooking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record BookingRequest(
        @NotNull Long roomId,
        @NotNull @Future LocalDate checkIn,
        @NotNull @Future LocalDate checkOut
) {
}
