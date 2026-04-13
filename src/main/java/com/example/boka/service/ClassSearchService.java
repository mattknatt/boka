package com.example.boka.service;

import com.example.boka.dto.GymClassMapper;
import com.example.boka.dto.GymClassResponse;
import com.example.boka.entity.ClassType;
import com.example.boka.entity.ClassStatus;
import com.example.boka.repository.ClassTypeRepository;
import com.example.boka.repository.GymClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassSearchService {

    private final ClassTypeRepository classTypeRepository;
    private final GymClassRepository gymClassRepository;

    /**
     * Paginated search: find upcoming gym classes by name
     */
    public Page<GymClassResponse> searchClasses(String query, Pageable pageable) {
        // 1. Find matching class types
        List<ClassType> matchingTypes = classTypeRepository
                .findByNameContainingIgnoreCaseAndIsActiveTrue(query);

        // 2. If no types match, return empty page
        if (matchingTypes.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> typeIds = matchingTypes.stream()
                .map(ClassType::getId)
                .toList();

        // 3. Find upcoming scheduled gym classes for those types with pagination
        return gymClassRepository
                .findByClassTypeIdInAndStatusAndStartTimeAfter(
                        typeIds, ClassStatus.SCHEDULED, LocalDateTime.now(), pageable)
                .map(GymClassMapper::toResponse);
    }
}
