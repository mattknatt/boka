package com.example.boka.dto;

import com.example.boka.entity.UserRole;

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