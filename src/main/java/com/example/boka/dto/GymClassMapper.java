package com.example.boka.dto;

import com.example.boka.entity.GymClass;

public final class GymClassMapper {

    private GymClassMapper() {}

    public static GymClassResponse toResponse(GymClass gymClass) {
        return new GymClassResponse(
                gymClass.getId(),
                gymClass.getClassType().getName(),
                gymClass.getInstructor().getFirstName(),
                gymClass.getInstructor().getLastName(),
                gymClass.getStartTime(),
                gymClass.getEndTime(),
                gymClass.getCapacity(),
                gymClass.getAvailableSpots(),
                gymClass.getStatus(),
                gymClass.getCreatedAt()
        );
    }
}