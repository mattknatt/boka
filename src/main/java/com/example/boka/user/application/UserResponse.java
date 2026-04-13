package com.example.boka.user.application;

import com.example.boka.user.domain.UserRole;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        UserRole role,
        Boolean isActive,
        LocalDateTime createdAt
) {}
