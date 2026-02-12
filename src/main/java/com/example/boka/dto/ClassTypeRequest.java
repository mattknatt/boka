package com.example.boka.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ClassTypeRequest(

        @NotBlank String name,

        @Size(max = 500) String description,

        @NotNull @Positive Integer defaultCapacity,

        @NotNull @Positive Integer durationMinutes
) {}