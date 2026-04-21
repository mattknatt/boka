package com.example.boka.gymclass.application;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateGymClassRequest(
        @NotNull Long classTypeId,
        @NotNull Long instructorId,
        @NotNull Long gymId,
        @NotNull @FutureOrPresent LocalDateTime startTime,
        @NotNull @Future LocalDateTime endTime,
        @NotNull @Min(1) Integer capacity
) {}
