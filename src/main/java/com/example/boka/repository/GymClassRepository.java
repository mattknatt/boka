package com.example.boka.repository;

import com.example.boka.entity.ClassStatus;
import com.example.boka.entity.GymClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface GymClassRepository extends JpaRepository<GymClass, Long> {

    List<GymClass> findByStatus(ClassStatus status);

    List<GymClass> findByInstructorId(Long instructorId);

    List<GymClass> findByClassTypeId(Long classTypeId);

    List<GymClass> findByStartTimeAfter(LocalDateTime time);

    List<GymClass> findByStartTimeBetween(LocalDateTime from, LocalDateTime to);

    List<GymClass> findByStatusAndStartTimeAfter(ClassStatus status, LocalDateTime time);
}