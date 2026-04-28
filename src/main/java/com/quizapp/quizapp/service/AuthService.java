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

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByIdentifier(request.getIdentifier());

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthenticationFailureException("Invalid credentials");
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
        return new AuthResponse(accessToken, refreshToken);
    }
}