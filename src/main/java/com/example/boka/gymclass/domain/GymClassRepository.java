package com.example.boka.gymclass.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GymClassRepository extends JpaRepository<GymClass, Long> {

    Page<GymClass> findByClassTypeIdInAndStatusAndStartTimeAfter(
            List<Long> classTypeIds, ClassStatus status, LocalDateTime time, Pageable pageable
    );
}
