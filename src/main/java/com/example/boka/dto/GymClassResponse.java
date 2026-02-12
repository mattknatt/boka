package com.example.boka.dto;

import com.example.boka.entity.ClassStatus;

import java.time.LocalDateTime;

public record GymClassResponse(
        Long id,
        String classTypeName,
        String instructorFirstName,
        String instructorLastName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer capacity,
        Integer availableSpots,
        ClassStatus status,
        LocalDateTime createdAt
) {}