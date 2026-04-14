package com.example.boka.booking.application;

import jakarta.validation.constraints.NotNull;

public record BookingRequest(
        @NotNull Long gymClassId
) {}
