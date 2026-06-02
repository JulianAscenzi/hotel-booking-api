package com.portfolio.hotelbooking.dto;

import com.portfolio.hotelbooking.model.Role;

import java.io.Serializable;
import java.time.Instant;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        Instant createdAt
) implements Serializable {
}
