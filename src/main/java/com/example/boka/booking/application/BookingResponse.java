package com.example.boka.booking.application;

public record BookingResponse(
        Long id,
        Long gymClassId,
        String userEmail,
        String status
) {}
