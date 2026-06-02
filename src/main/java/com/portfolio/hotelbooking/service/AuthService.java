package com.portfolio.hotelbooking.service;

import com.portfolio.hotelbooking.dto.AuthResponse;
import com.portfolio.hotelbooking.dto.LoginRequest;
import com.portfolio.hotelbooking.dto.RegisterRequest;
import com.portfolio.hotelbooking.model.User;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    User getCurrentUser();
}
