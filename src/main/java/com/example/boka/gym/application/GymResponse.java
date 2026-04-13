package com.example.boka.gym.application;

public record GymResponse(
        Long id,
        String name,
        String address,
        Double latitude,
        Double longitude
) {}
