package com.example.boka.booking;

import java.time.LocalDateTime;

public record UserBookingResponse(
    Long bookingId,
    Long gymClassId,
    String classTypeName,
    LocalDateTime startTime,
    String gymName,
    String status
) {}
