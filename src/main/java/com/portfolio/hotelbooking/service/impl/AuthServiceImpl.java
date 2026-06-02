package com.portfolio.hotelbooking.service.impl;

import com.portfolio.hotelbooking.dto.AuthResponse;
import com.portfolio.hotelbooking.dto.LoginRequest;
import com.portfolio.hotelbooking.dto.RegisterRequest;
import com.portfolio.hotelbooking.exception.BusinessException;
import com.portfolio.hotelbooking.exception.ResourceNotFoundException;
import com.portfolio.hotelbooking.mapper.UserMapper;
import com.portfolio.hotelbooking.model.Role;
import com.portfolio.hotelbooking.model.User;
import com.portfolio.hotelbooking.repository.UserRepository;
import com.portfolio.hotelbooking.security.JwtUtil;
import com.portfolio.hotelbooking.security.UserDetailsImpl;
import com.portfolio.hotelbooking.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("Email is already registered");
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.CLIENT);

        User saved = userRepository.save(user);
        UserDetailsImpl principal = new UserDetailsImpl(saved);
        return new AuthResponse(jwtUtil.generateToken(principal), "Bearer", UserMapper.toResponse(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password()));
        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return new AuthResponse(jwtUtil.generateToken(principal), "Bearer", UserMapper.toResponse(user));
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl principal)) {
            throw new ResourceNotFoundException("Authenticated user not found");
        }
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
}
