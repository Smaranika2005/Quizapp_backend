package com.quizapp.quizapp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RoleExtractor {

    @Autowired
    private JwtUtil jwtUtil;

    public String extractRoleFromAuthHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        return jwtUtil.extractRole(token);
    }

    public String extractUsernameFromAuthHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        return jwtUtil.extractUsername(token);
    }

    public boolean isTeacher(String authHeader) {
        String role = extractRoleFromAuthHeader(authHeader);
        return "TEACHER".equalsIgnoreCase(role);
    }

    public boolean isStudent(String authHeader) {
        String role = extractRoleFromAuthHeader(authHeader);
        return "STUDENT".equalsIgnoreCase(role);
    }
}
