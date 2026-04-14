package com.example.boka.gymclass.infrastructure;

import com.example.boka.gymclass.GymClassProviderPort;
import com.example.boka.gymclass.domain.GymClass;
import com.example.boka.gymclass.domain.GymClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GymClassProviderAdapter implements GymClassProviderPort {

    private final GymClassRepository gymClassRepository;

    @Override
    public Map<Long, GymClassDetails> getGymClassDetails(List<Long> gymClassIds) {
        List<GymClass> classes = gymClassRepository.findAllById(gymClassIds);

        return classes.stream().collect(Collectors.toMap(
            GymClass::getId,
            gc -> new GymClassDetails(
                gc.getId(),
                gc.getClassType() != null ? gc.getClassType().getName() : "Unknown",
                gc.getStartTime(),
                gc.getGym() != null ? gc.getGym().getName() : "Local Gym"
            )
        ));
    }
}
