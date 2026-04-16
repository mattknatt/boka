package com.example.boka.gymclass;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface GymClassProviderPort {

    record GymClassDetails(
        Long id,
        String classTypeName,
        LocalDateTime startTime,
        String gymName
    ) {}

    Map<Long, GymClassDetails> getGymClassDetails(List<Long> gymClassIds);

    /** Acquires a pessimistic write lock on the gym class row and returns its capacity. */
    Optional<Integer> lockAndGetCapacity(Long gymClassId);
}
