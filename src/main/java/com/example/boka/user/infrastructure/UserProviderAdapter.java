package com.example.boka.user.infrastructure;

import com.example.boka.user.UserProviderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserProviderAdapter implements UserProviderPort {

    private final UserRepository userRepository;

    @Override
    public Optional<UserDetails> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(user -> new UserDetails(user.getId(), user.getEmail()));
    }
}
