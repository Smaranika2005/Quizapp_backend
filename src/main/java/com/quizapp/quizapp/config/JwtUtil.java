package com.quizapp.quizapp.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    private Key getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateAccessToken(String username) {
        return generateToken(username, ACCESS_TOKEN_TYPE, accessTokenExpirationMs, null);
    }

    public String generateAccessToken(String username, String role) {
        return generateToken(username, ACCESS_TOKEN_TYPE, accessTokenExpirationMs, role);
    }

    public String generateRefreshToken(String username) {
        return generateToken(username, REFRESH_TOKEN_TYPE, refreshTokenExpirationMs, null);
    }

    private String generateToken(String username, String tokenType, long expirationMs, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("token_type", tokenType);
        if (role != null) {
            claims.put("role", role);
        }

        return Jwts.builder()
                .setSubject(username)
                .addClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractTokenType(String token) {
        Claims claims = extractAllClaims(token);
        Object tokenType = claims.get("token_type");
        return tokenType == null ? null : tokenType.toString();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        Object role = claims.get("role");
        return role == null ? null : role.toString();
    }

    public boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return extractedUsername.equals(username);
    }

    public boolean isAccessToken(String token) {
        String tokenType = extractTokenType(token);
        return tokenType == null || ACCESS_TOKEN_TYPE.equals(tokenType);
    }

    public boolean isRefreshToken(String token) {
        return REFRESH_TOKEN_TYPE.equals(extractTokenType(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}