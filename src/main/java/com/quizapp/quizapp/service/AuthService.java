package com.quizapp.quizapp.service;

import com.quizapp.quizapp.config.JwtUtil;
import com.quizapp.quizapp.dto.AuthResponse;
import com.quizapp.quizapp.dto.LoginRequest;
import com.quizapp.quizapp.dto.RefreshTokenRequest;
import com.quizapp.quizapp.entity.User;
import com.quizapp.quizapp.exception.AuthenticationFailureException;
import com.quizapp.quizapp.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       JwtUtil jwtUtil,
                       RefreshTokenService refreshTokenService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.passwordEncoder = passwordEncoder;
    }

    public void signup(User user) {
        if (user.getIdentifier() == null || user.getIdentifier().trim().isEmpty()) {
            throw new IllegalArgumentException("Identifier is required");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (user.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }

        User existingUser = userRepository.findByIdentifier(user.getIdentifier().trim());
        if (existingUser != null) {
            throw new IllegalArgumentException("User already registered");
        }

        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("STUDENT");
        } else {
            user.setRole(user.getRole().trim().toUpperCase());
        }

        user.setIdentifier(user.getIdentifier().trim());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        String identifier = request.getIdentifier() == null ? "" : request.getIdentifier().trim();

        if (identifier.isEmpty() || request.getPassword() == null || request.getPassword().isBlank()) {
            throw new AuthenticationFailureException("Identifier and password are required");
        }

        User user = userRepository.findByIdentifier(identifier);

        if (user == null) {
            throw new AuthenticationFailureException("User not registered");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthenticationFailureException("Invalid credentials"); // Maps directly to requested responses
        }

        return createAuthResponse(user.getIdentifier(), user.getRole());
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        String username = refreshTokenService.validateRefreshToken(request.getRefreshToken());
        User user = userRepository.findByIdentifier(username);
        refreshTokenService.revokeRefreshToken(request.getRefreshToken());
        return createAuthResponse(username, user != null ? user.getRole() : null);
    }

    private AuthResponse createAuthResponse(String username, String role) {
        String accessToken = jwtUtil.generateAccessToken(username, role);
        String refreshToken = refreshTokenService.issueRefreshToken(username);
        String normalizedRole = role == null ? null : role.toUpperCase();
        return new AuthResponse(accessToken, refreshToken, username, normalizedRole);
    }
}