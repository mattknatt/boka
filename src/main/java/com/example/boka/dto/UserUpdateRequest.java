package com.example.boka.dto;

import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        String firstName,
        String lastName,
        String phoneNumber,

        @Size(min = 8, max = 100) String password
        //A blank password should simply be treated as "no update",
        // handle this in the service layer instead.
) {
}