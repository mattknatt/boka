package com.example.boka.gymclass.application;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;

import java.time.LocalDateTime;

public record UpdateGymClassRequest(
        Long classTypeId,
        Long instructorId,
        Long gymId,
        @FutureOrPresent LocalDateTime startTime,
        @Future LocalDateTime endTime,
        @Min(1) Integer capacity
) {}
