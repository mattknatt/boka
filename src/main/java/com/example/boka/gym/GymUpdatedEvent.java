package com.example.boka.gym;

public record GymUpdatedEvent(
        Long id,
        String name,
        String address
) {}
