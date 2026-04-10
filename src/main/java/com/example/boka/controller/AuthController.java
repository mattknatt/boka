package com.example.boka.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import com.example.boka.dto.UserRegistrationRequest;
import com.example.boka.entity.AuthProvider;
import com.example.boka.entity.User;
import com.example.boka.entity.UserRole;
import com.example.boka.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationRequest registrationRequest) {
        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email already in use"));
        }

        User user = new User();
        user.setEmail(registrationRequest.getEmail());
        user.setFirstName(registrationRequest.getFirstName());
        user.setLastName(registrationRequest.getLastName());
        user.setPasswordHash(passwordEncoder.encode(registrationRequest.getPassword()));
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setRole(UserRole.MEMBER);
        user.setIsActive(true);

        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        Object principal = authentication.getPrincipal();
        Map<String, Object> userInfo = new HashMap<>();

        if (principal instanceof OAuth2User oAuth2User) {
            userInfo.put("name", oAuth2User.getAttribute("name"));
            userInfo.put("email", oAuth2User.getAttribute("email"));
            userInfo.put("picture", oAuth2User.getAttribute("picture"));
            userInfo.put("type", "OAUTH2");
        } else if (principal instanceof org.springframework.security.core.userdetails.User userDetails) {
            User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
            if (user != null) {
                userInfo.put("name", user.getFirstName() + " " + user.getLastName());
                userInfo.put("email", user.getEmail());
                userInfo.put("type", "LOCAL");
                userInfo.put("role", user.getRole().name());
            }
        }

        return ResponseEntity.ok(userInfo);
    }
}
