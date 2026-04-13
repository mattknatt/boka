package com.example.boka.dto;

public record GymResponse(
        Long id,
        String name,
        String address,
        Double latitude,
        Double longitude
) {}
