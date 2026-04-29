package com.example.boka.gymclass.application;

public record ClassTypeResponse(
        Long id,
        String name,
        Integer defaultCapacity,
        Integer durationMinutes
) {}
