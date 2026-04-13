package com.example.boka.user.application;

import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        String firstName,
        String lastName,
        String phoneNumber,

        @Size(min = 8, max = 100) String password
) {
}
