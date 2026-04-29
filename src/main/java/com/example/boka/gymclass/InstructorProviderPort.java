package com.example.boka.gymclass;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface InstructorProviderPort {

    record InstructorDetails(Long id, String firstName, String lastName) {}

    Map<Long, InstructorDetails> getInstructorDetails(Set<Long> instructorIds);

    List<InstructorDetails> getAllInstructors();

    Optional<InstructorDetails> findInstructorById(Long id);
}
