package com.example.boka.gymclass.application;

public record GymClassResponse(
        Long id,
        String classTypeName,
        String instructorFirstName,
        String instructorLastName,
        String gymName,
        String gymAddress,
        String startTime,
        String endTime,
        Integer capacity,
        Integer availableSpots,
        String status
) {}
