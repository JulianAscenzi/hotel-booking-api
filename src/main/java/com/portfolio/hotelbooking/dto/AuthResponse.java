package com.portfolio.hotelbooking.dto;

import java.io.Serializable;

public record AuthResponse(
        String token,
        String tokenType,
        UserResponse user
) implements Serializable {
}
