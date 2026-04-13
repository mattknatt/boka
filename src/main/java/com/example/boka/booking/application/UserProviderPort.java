package com.example.boka.booking.application;

import java.util.Optional;

public interface UserProviderPort {

    record UserDetails(Long id, String email) {}

    Optional<UserDetails> findByEmail(String email);
}
