package com.portfolio.hotelbooking.mapper;

import com.portfolio.hotelbooking.dto.UserResponse;
import com.portfolio.hotelbooking.model.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
