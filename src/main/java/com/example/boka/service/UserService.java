package com.example.boka.service;

import com.example.boka.dto.UserMapper;
import com.example.boka.dto.UserResponse;
import com.example.boka.dto.UserUpdateRequest;
import com.example.boka.entity.User;
import com.example.boka.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<UserResponse> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserMapper::toResponse);
    }

    @Transactional
    public UserResponse updateUser(String email, UserUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserMapper.updateFromRequest(user, request);

        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }

        return UserMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }
}
