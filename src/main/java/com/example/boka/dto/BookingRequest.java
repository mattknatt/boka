package com.example.boka.dto;

import jakarta.validation.constraints.NotNull;

public record BookingRequest(

        @NotNull Long gymClassId
) {}