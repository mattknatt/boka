package com.example.boka.repository;

import com.example.boka.entity.ClassStatus;
import com.example.boka.entity.GymClass;

import java.time.LocalDateTime;
import java.util.List;

public interface GymClassInterface {
    void save(GymClass gymClass);
    boolean delete(GymClass gymClass);
    GymClass findById(Long id);
    List<GymClass> findByDate(String date);
    List<GymClass> findAll();
    List<GymClass> findByStatus(ClassStatus status);
    List<GymClass> findByInstructorId(Long id);
    List<GymClass> findAvailableClasses();
    List<GymClass> findUpcomingClasses(LocalDateTime time);


}
