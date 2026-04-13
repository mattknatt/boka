package com.example.boka.user.infrastructure;

import com.example.boka.security.IdentityPort;
import com.example.boka.user.domain.AuthProvider;
import com.example.boka.user.domain.User;
import com.example.boka.user.domain.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class IdentityAdapter implements IdentityPort {

    private final UserRepository userRepository;

    @Override
    public Optional<IdentityDetails> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(user -> new IdentityDetails(
                        user.getId(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getPasswordHash(),
                        user.getRole().name()
                ));
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public IdentityDetails createLocalUser(String email, String firstName, String lastName, String passwordHash, String role) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPasswordHash(passwordHash);
        user.setRole(UserRole.valueOf(role));
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setIsActive(true);

        User saved = userRepository.save(user);
        return new IdentityDetails(saved.getId(), saved.getEmail(), saved.getFirstName(), saved.getLastName(), saved.getPasswordHash(), saved.getRole().name());
    }

    @Override
    @Transactional
    public void updateOAuth2ProviderInfo(String email, String providerId) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setAuthProvider(AuthProvider.GOOGLE);
            user.setProviderId(providerId);
            userRepository.save(user);
        });
    }
}
