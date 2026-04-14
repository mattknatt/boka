package com.example.boka.gymclass;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface GymClassProviderPort {

    record GymClassDetails(
        Long id,
        String classTypeName,
        LocalDateTime startTime,
        String gymName
    ) {}

    Map<Long, GymClassDetails> getGymClassDetails(List<Long> gymClassIds);
}
