package com.example.boka.security;

import java.util.Optional;

public interface IdentityPort {

    record IdentityDetails(Long id, String email, String firstName, String lastName, String passwordHash, String role) {}

    Optional<IdentityDetails> findByEmail(String email);

    boolean existsByEmail(String email);

    IdentityDetails createLocalUser(String email, String firstName, String lastName, String passwordHash, String role);

    /**
     * Atomically creates a new user or updates an existing user's OAuth2 provider info.
     */
    IdentityDetails createOrUpdateOAuth2User(String email, String firstName, String lastName, String providerId);
}
