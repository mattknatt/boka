package com.example.boka.gymclass.application;

import com.example.boka.gymclass.domain.GymClass;

public final class GymClassMapper {

    private GymClassMapper() {}

    public static GymClassResponse toResponse(GymClass gymClass, InstructorProviderPort.InstructorDetails instructor) {
        if(gymClass == null) {
            return null;
        }

        return new GymClassResponse(
                gymClass.getId(),
                gymClass.getClassType() != null ? gymClass.getClassType().getName() : null,
                gymClass.getInstructorId(),
                instructor != null ? instructor.firstName() : null,
                instructor != null ? instructor.lastName() : null,
                gymClass.getGym() != null ? gymClass.getGym().getName() : null,
                gymClass.getGym() != null ? gymClass.getGym().getAddress() : null,
                gymClass.getStartTime() != null ? gymClass.getStartTime().toString() : null,
                gymClass.getEndTime() != null ? gymClass.getEndTime().toString() : null,
                gymClass.getCapacity(),
                gymClass.getAvailableSpots() != null ? gymClass.getAvailableSpots() : gymClass.getCapacity(),
                gymClass.getStatus() != null ? gymClass.getStatus().name() : null
        );
    }
}
