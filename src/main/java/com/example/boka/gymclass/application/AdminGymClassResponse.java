package com.example.boka.gymclass.application;

public record AdminGymClassResponse(
        Long id,
        Long classTypeId,
        String classTypeName,
        Long instructorId,
        String instructorName,
        Long gymId,
        String gymName,
        String startTime,
        String endTime,
        Integer capacity,
        Integer currentBookings,
        Integer availableSpots,
        String status
) {}
