package com.example.boka.security;

import java.util.Optional;

public interface IdentityPort {

    record IdentityDetails(Long id, String email, String firstName, String lastName, String passwordHash, String role) {}

    Optional<IdentityDetails> findByEmail(String email);

    boolean existsByEmail(String email);

    IdentityDetails createLocalUser(String email, String firstName, String lastName, String passwordHash, String role);

    void updateOAuth2ProviderInfo(String email, String providerId);
}
