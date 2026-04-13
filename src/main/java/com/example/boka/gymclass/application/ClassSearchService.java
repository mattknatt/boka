package com.example.boka.gymclass.application;

import com.example.boka.gymclass.domain.ClassType;
import com.example.boka.gymclass.domain.ClassStatus;
import com.example.boka.gymclass.infrastructure.ClassTypeRepository;
import com.example.boka.gymclass.infrastructure.GymClassRepository;
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

    public Page<GymClassResponse> searchClasses(String query, Pageable pageable) {
        List<ClassType> matchingTypes = classTypeRepository
                .findByNameContainingIgnoreCaseAndIsActiveTrue(query);

        if (matchingTypes.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> typeIds = matchingTypes.stream()
                .map(ClassType::getId)
                .toList();

        return gymClassRepository
                .findByClassTypeIdInAndStatusAndStartTimeAfter(
                        typeIds, ClassStatus.SCHEDULED, LocalDateTime.now(), pageable)
                .map(GymClassMapper::toResponse);
    }
}
