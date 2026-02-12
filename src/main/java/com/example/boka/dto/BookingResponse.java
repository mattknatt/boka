package com.example.boka.dto;

import com.example.boka.entity.BookingStatus;

import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        Long userId,
        String userFirstName,
        String userLastName,
        Long gymClassId,
        String classTypeName,
        LocalDateTime classStartTime,
        BookingStatus status,
        LocalDateTime bookedAt,
        LocalDateTime cancelledAt
) {}