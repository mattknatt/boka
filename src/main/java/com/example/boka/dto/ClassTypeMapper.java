package com.example.boka.dto;

import com.example.boka.entity.ClassType;

public final class ClassTypeMapper {

    private ClassTypeMapper() {}

    public static ClassTypeResponse toResponse(ClassType classType) {
        return new ClassTypeResponse(
                classType.getId(),
                classType.getName(),
                classType.getDescription(),
                classType.getDefaultCapacity(),
                classType.getDurationMinutes(),
                classType.getIsActive()
        );
    }

    public static ClassType toEntity(ClassTypeRequest request) {
        ClassType classType = new ClassType();
        classType.setName(request.name());
        classType.setDescription(request.description());
        classType.setDefaultCapacity(request.defaultCapacity());
        classType.setDurationMinutes(request.durationMinutes());
        return classType;
    }
}