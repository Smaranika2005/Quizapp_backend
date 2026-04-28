package com.quizapp.quizapp.controller;

import com.quizapp.quizapp.entity.User;
import com.quizapp.quizapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody User user) {

        // Check if identifier already exists
        User existingUser = userRepository.findByIdentifier(user.getIdentifier());

        if (existingUser != null) {
            return ResponseEntity.badRequest().body("User already registered, please login");
        }

        // Hash the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    // ⚠️ DEPRECATED - Use /auth/login instead
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        return ResponseEntity.status(301).body("Use /auth/login endpoint instead");
    }
}