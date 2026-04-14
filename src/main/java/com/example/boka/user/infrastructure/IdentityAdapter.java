package com.example.boka.user.infrastructure;

import com.example.boka.security.IdentityPort;
import com.example.boka.user.domain.AuthProvider;
import com.example.boka.user.domain.User;
import com.example.boka.user.domain.UserRepository;
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
                .map(this::toDetails);
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

        return toDetails(userRepository.save(user));
    }

    @Override
    @Transactional
    public IdentityDetails createOrUpdateOAuth2User(String email, String firstName, String lastName, String providerId) {
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFirstName(firstName != null ? firstName : "");
            newUser.setLastName(lastName != null ? lastName : "");
            newUser.setRole(UserRole.MEMBER);
            newUser.setIsActive(true);
            return newUser;
        });

        user.setAuthProvider(AuthProvider.GOOGLE);
        user.setProviderId(providerId);

        return toDetails(userRepository.save(user));
    }

    private IdentityDetails toDetails(User user) {
        return new IdentityDetails(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPasswordHash(),
                user.getRole().name()
        );
    }
}
