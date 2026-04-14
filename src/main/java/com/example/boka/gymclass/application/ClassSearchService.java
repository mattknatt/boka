package com.example.boka.gymclass.application;

import com.example.boka.booking.BookingProviderPort;
import com.example.boka.gymclass.InstructorProviderPort;
import com.example.boka.gymclass.domain.ClassType;
import com.example.boka.gymclass.domain.ClassStatus;
import com.example.boka.gymclass.domain.GymClass;
import com.example.boka.gymclass.domain.ClassTypeRepository;
import com.example.boka.gymclass.domain.GymClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassSearchService {

    private final ClassTypeRepository classTypeRepository;
    private final GymClassRepository gymClassRepository;
    private final BookingProviderPort bookingProviderPort;
    private final InstructorProviderPort instructorProviderPort;

    public Page<GymClassResponse> searchClasses(String query, Pageable pageable) {
        List<ClassType> matchingTypes = classTypeRepository
                .findByNameContainingIgnoreCaseAndIsActiveTrue(query);

        if (matchingTypes.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> typeIds = matchingTypes.stream()
                .map(ClassType::getId)
                .toList();

        Page<GymClass> gymClasses = gymClassRepository
                .findByClassTypeIdInAndStatusAndStartTimeAfter(
                        typeIds, ClassStatus.SCHEDULED, LocalDateTime.now(), pageable);

        // 1. Fetch actual booking counts via Port
        Set<Long> classIds = gymClasses.getContent().stream()
                .map(GymClass::getId)
                .collect(Collectors.toSet());
        Map<Long, Integer> bookingCounts = bookingProviderPort.getBookingCounts(classIds);

        // 2. Fetch instructor details via Port
        Set<Long> instructorIds = gymClasses.getContent().stream()
                .map(GymClass::getInstructorId)
                .collect(Collectors.toSet());
        Map<Long, InstructorProviderPort.InstructorDetails> instructors =
                instructorProviderPort.getInstructorDetails(instructorIds);

        // 3. Map to Response and enrich data
        return gymClasses.map(gc -> {
            int currentBookings = bookingCounts.getOrDefault(gc.getId(), 0);
            int availableSpots = Math.max(0, gc.getCapacity() - currentBookings);
            
            ClassStatus derivedStatus = gc.getStatus();
            if (derivedStatus == ClassStatus.SCHEDULED && availableSpots == 0) {
                derivedStatus = ClassStatus.FULL;
            }

            InstructorProviderPort.InstructorDetails instructor = instructors.get(gc.getInstructorId());
            return GymClassMapper.toResponse(gc, instructor, availableSpots, derivedStatus);
        });
    }
}
