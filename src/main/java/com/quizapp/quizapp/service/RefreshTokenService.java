package com.quizapp.quizapp.service;

import com.quizapp.quizapp.config.JwtUtil;
import com.quizapp.quizapp.exception.AuthenticationFailureException;

import io.jsonwebtoken.JwtException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RefreshTokenService {

    private final JwtUtil jwtUtil;
    private final long refreshTokenExpirationMs;
    private final Map<String, RefreshTokenRecord> refreshTokenStore = new ConcurrentHashMap<>();

    public RefreshTokenService(JwtUtil jwtUtil,
                               @Value("${jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs) {
        this.jwtUtil = jwtUtil;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public String issueRefreshToken(String username) {
        String refreshToken = jwtUtil.generateRefreshToken(username);
        String tokenHash = hashToken(refreshToken);
        Instant expiresAt = Instant.now().plusMillis(refreshTokenExpirationMs);

        refreshTokenStore.put(tokenHash, new RefreshTokenRecord(username, expiresAt));
        return refreshToken;
    }

    public String validateRefreshToken(String refreshToken) {
        try {
            if (refreshToken == null || refreshToken.isBlank()) {
                throw new AuthenticationFailureException("Refresh token is required");
            }

            if (!jwtUtil.isRefreshToken(refreshToken)) {
                throw new AuthenticationFailureException("Invalid refresh token");
            }

            String username = jwtUtil.extractUsername(refreshToken);
            String tokenHash = hashToken(refreshToken);
            RefreshTokenRecord record = refreshTokenStore.get(tokenHash);

            if (record == null) {
                throw new AuthenticationFailureException("Invalid refresh token");
            }

            if (record.expiresAt.isBefore(Instant.now())) {
                refreshTokenStore.remove(tokenHash);
                throw new AuthenticationFailureException("Refresh token has expired");
            }

            if (!record.username.equals(username)) {
                throw new AuthenticationFailureException("Invalid refresh token");
            }

            return username;
        } catch (JwtException | IllegalArgumentException exception) {
            throw new AuthenticationFailureException("Invalid refresh token");
        }
    }

    public void revokeRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        refreshTokenStore.remove(hashToken(refreshToken));
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Unable to hash refresh token", exception);
        }
    }

    private static class RefreshTokenRecord {
        private final String username;
        private final Instant expiresAt;

        private RefreshTokenRecord(String username, Instant expiresAt) {
            this.username = username;
            this.expiresAt = expiresAt;
        }
    }
}