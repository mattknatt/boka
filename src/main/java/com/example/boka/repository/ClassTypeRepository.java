package com.example.boka.repository;

import com.example.boka.entity.ClassType;

import java.util.Set;

public interface ClassTypeRepository {
    void save(ClassType classType);
    boolean delete(ClassType classType);
    ClassType findById(Long id);
    ClassType findByName(String name);
    Set<ClassType> findAll();
    Set<ClassType> findByDuration(int duration);
    Set<ClassType> findByCapacity(int capacity);
}
