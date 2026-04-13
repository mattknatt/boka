package com.example.boka.gym.domain.event;

public record GymUpdatedEvent(
        Long id,
        String name,
        String address
) {}
