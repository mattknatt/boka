package com.example.boka.user.infrastructure;

import com.example.boka.gymclass.InstructorProviderPort;
import com.example.boka.user.domain.User;
import com.example.boka.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InstructorProviderAdapter implements InstructorProviderPort {

    private final UserRepository userRepository;

    @Override
    public Map<Long, InstructorDetails> getInstructorDetails(Set<Long> instructorIds) {
        return userRepository.findAllById(instructorIds).stream()
                .collect(Collectors.toMap(
                        User::getId,
                        user -> new InstructorDetails(user.getId(), user.getFirstName(), user.getLastName())
                ));
    }
}
