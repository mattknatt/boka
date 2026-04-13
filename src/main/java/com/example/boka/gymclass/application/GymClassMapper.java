package com.example.boka.gymclass.application;

import com.example.boka.gymclass.domain.GymClass;

public final class GymClassMapper {

    private GymClassMapper() {}

    public static GymClassResponse toResponse(GymClass gymClass) {
        if(gymClass == null) {
            return null;
        }

        // In this modular design, we might fetch instructor info via a port if needed.
        // For now, I'll just leave it empty or assume we only have IDs unless we join.

        return new GymClassResponse(
                gymClass.getId(),
                gymClass.getClassType() != null ? gymClass.getClassType().getName() : null,
                "Instructor", // Placeholder or fetch via User module
                String.valueOf(gymClass.getInstructorId()), // Placeholder
                gymClass.getGym() != null ? gymClass.getGym().getName() : "Gym",
                gymClass.getGym() != null ? gymClass.getGym().getAddress() : "Address",
                gymClass.getStartTime() != null ? gymClass.getStartTime().toString() : null,
                gymClass.getEndTime() != null ? gymClass.getEndTime().toString() : null,
                gymClass.getCapacity(),
                gymClass.getAvailableSpots() != null ? gymClass.getAvailableSpots() : gymClass.getCapacity(),
                gymClass.getStatus() != null ? gymClass.getStatus().name() : null
        );
    }
}
