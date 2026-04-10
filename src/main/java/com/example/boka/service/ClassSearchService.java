package com.example.boka.service;

import com.example.boka.dto.GymClassMapper;
import com.example.boka.dto.GymClassResponse;
import com.example.boka.entity.ClassType;
import com.example.boka.entity.ClassStatus;
import com.example.boka.repository.ClassTypeRepository;
import com.example.boka.repository.GymClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassSearchService {

    private final ClassTypeRepository classTypeRepository;
    private final GymClassRepository gymClassRepository;

    /**
     * Simple search: find class types with names containing the query,
     * then return upcoming gym classes.
     */
    public List<GymClassResponse> searchClasses(String query, int maxResults) {
        // 1. Find matching class types
        List<ClassType> matchingTypes = classTypeRepository
                .findByNameContainingIgnoreCaseAndIsActiveTrue(query);

        // 2. Find upcoming scheduled gym classes for those types
        List<Long> typeIds = matchingTypes.stream()
                .map(ClassType::getId)
                .toList();

        return gymClassRepository
                .findByClassTypeIdInAndStatusAndStartTimeAfter(
                        typeIds, ClassStatus.SCHEDULED, LocalDateTime.now())
                .stream()
                .limit(maxResults)
                .map(GymClassMapper::toResponse)
                .toList();
    }
}
