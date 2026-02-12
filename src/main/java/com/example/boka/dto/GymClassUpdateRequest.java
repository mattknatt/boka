package com.example.boka.dto;

import com.example.boka.entity.ClassStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record GymClassUpdateRequest(

        @Future
        LocalDateTime startTime,

        @Future
        LocalDateTime endTime,

        @Positive
        Integer capacity,

        ClassStatus status
) {}