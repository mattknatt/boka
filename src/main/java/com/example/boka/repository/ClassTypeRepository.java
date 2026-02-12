package com.example.boka.repository;

import com.example.boka.entity.ClassType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClassTypeRepository extends JpaRepository<ClassType, Long> {

    Optional<ClassType> findByName(String name);

    boolean existsByName(String name);

    List<ClassType> findByIsActiveTrue();
}