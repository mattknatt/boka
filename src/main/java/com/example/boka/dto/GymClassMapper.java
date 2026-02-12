package com.example.boka.dto;

import com.example.boka.entity.GymClass;

public final class GymClassMapper {

    private GymClassMapper() {}

    public static GymClassResponse toResponse(GymClass gymClass) {
        if(gymClass == null) {
            return null;
        }
        return new GymClassResponse(
                gymClass.getId(),
                gymClass.getClassType() != null ? gymClass.getClassType().getName() : null,
                gymClass.getInstructor() != null ? gymClass.getInstructor().getFirstName() : null,
                gymClass.getInstructor() != null ? gymClass.getInstructor().getLastName() : null,
                gymClass.getStartTime(),
                gymClass.getEndTime(),
                gymClass.getCapacity(),
                gymClass.getAvailableSpots(),
                gymClass.getStatus(),
                gymClass.getCreatedAt()
        );
    }
}