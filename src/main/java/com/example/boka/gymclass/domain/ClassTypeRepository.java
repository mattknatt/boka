package com.example.boka.gymclass.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassTypeRepository extends JpaRepository<ClassType, Long> {
    List<ClassType> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);
}
