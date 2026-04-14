package com.example.boka.gymclass;

import java.util.Map;
import java.util.Set;

public interface InstructorProviderPort {

    record InstructorDetails(Long id, String firstName, String lastName) {}

    /**
     * Returns a map of InstructorId to their name details.
     */
    Map<Long, InstructorDetails> getInstructorDetails(Set<Long> instructorIds);
}
