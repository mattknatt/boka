package com.example.boka.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record GymClassCreateRequest(

        @NotNull
        Long classTypeId,

        @NotNull
        Long instructorId,

        @NotNull @Future
        LocalDateTime startTime,

        @NotNull @Future
        LocalDateTime endTime,

        @Positive
        Integer capacity // nullable — falls back to ClassType.defaultCapacity in service
) {}