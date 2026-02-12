package com.example.boka.dto;

public record ClassTypeResponse(
        Long id,
        String name,
        String description,
        Integer defaultCapacity,
        Integer durationMinutes,
        Boolean isActive
) {}