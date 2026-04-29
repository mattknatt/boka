package com.example.boka.user.infrastructure;

import com.example.boka.gymclass.InstructorProviderPort;
import com.example.boka.user.domain.User;
import com.example.boka.user.domain.UserRepository;
import com.example.boka.user.domain.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InstructorProviderAdapter implements InstructorProviderPort {

    private final UserRepository userRepository;

    @Override
    public Map<Long, InstructorDetails> getInstructorDetails(Set<Long> instructorIds) {
        return userRepository.findAllById(instructorIds).stream()
                .filter(user -> user.getRole() == UserRole.INSTRUCTOR)
                .collect(Collectors.toMap(
                        User::getId,
                        user -> new InstructorDetails(user.getId(), user.getFirstName(), user.getLastName())
                ));
    }

    @Override
    public List<InstructorDetails> getAllInstructors() {
        return userRepository.findByRole(UserRole.INSTRUCTOR).stream()
                .map(user -> new InstructorDetails(user.getId(), user.getFirstName(), user.getLastName()))
                .toList();
    }

    @Override
    public Optional<InstructorDetails> findInstructorById(Long id) {
        return userRepository.findById(id)
                .filter(user -> user.getRole() == UserRole.INSTRUCTOR)
                .map(user -> new InstructorDetails(user.getId(), user.getFirstName(), user.getLastName()));
    }
}
