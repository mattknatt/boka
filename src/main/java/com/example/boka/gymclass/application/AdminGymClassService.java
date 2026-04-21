package com.example.boka.gymclass.application;

import com.example.boka.booking.BookingProviderPort;
import com.example.boka.common.ResourceNotFoundException;
import com.example.boka.gym.domain.GymInfoRepository;
import com.example.boka.gymclass.InstructorProviderPort;
import com.example.boka.gymclass.domain.ClassStatus;
import com.example.boka.gymclass.domain.ClassType;
import com.example.boka.gymclass.domain.ClassTypeRepository;
import com.example.boka.gymclass.domain.GymClass;
import com.example.boka.gymclass.domain.GymClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminGymClassService {

    private final GymClassRepository gymClassRepository;
    private final ClassTypeRepository classTypeRepository;
    private final GymInfoRepository gymInfoRepository;
    private final InstructorProviderPort instructorProviderPort;
    private final BookingProviderPort bookingProviderPort;

    @Transactional(readOnly = true)
    public Page<AdminGymClassResponse> getAllClasses(String status, Pageable pageable) {
        Page<GymClass> page;
        if (status != null && !status.isBlank()) {
            page = gymClassRepository.findByStatus(ClassStatus.valueOf(status.toUpperCase()), pageable);
        } else {
            page = gymClassRepository.findAll(pageable);
        }
        return enrich(page);
    }

    @Transactional(readOnly = true)
    public List<ClassTypeResponse> getActiveClassTypes() {
        return classTypeRepository.findAll().stream()
                .filter(ct -> Boolean.TRUE.equals(ct.getIsActive()))
                .map(ct -> new ClassTypeResponse(ct.getId(), ct.getName(), ct.getDefaultCapacity(), ct.getDurationMinutes()))
                .toList();
    }

    @Transactional
    public AdminGymClassResponse createClass(CreateGymClassRequest req) {
        ClassType classType = classTypeRepository.findById(req.classTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("ClassType", "id", req.classTypeId()));

        instructorProviderPort.findInstructorById(req.instructorId())
                .orElseThrow(() -> new IllegalArgumentException("No instructor found with id " + req.instructorId()));

        gymInfoRepository.findById(req.gymId())
                .orElseThrow(() -> new ResourceNotFoundException("Gym", "id", req.gymId()));

        if (!req.startTime().isBefore(req.endTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        GymClass gymClass = new GymClass();
        gymClass.setClassType(classType);
        gymClass.setInstructorId(req.instructorId());
        gymClass.setGymId(req.gymId());
        gymClass.setStartTime(req.startTime());
        gymClass.setEndTime(req.endTime());
        gymClass.setCapacity(req.capacity());
        gymClass.setStatus(ClassStatus.SCHEDULED);

        GymClass saved = gymClassRepository.save(gymClass);
        return toResponse(saved, 0);
    }

    @Transactional
    public AdminGymClassResponse updateClass(Long id, UpdateGymClassRequest req) {
        GymClass gymClass = gymClassRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GymClass", "id", id));

        if (gymClass.getStatus() == ClassStatus.CANCELLED) {
            throw new IllegalStateException("Cannot update a cancelled class");
        }

        if (req.classTypeId() != null) {
            ClassType classType = classTypeRepository.findById(req.classTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("ClassType", "id", req.classTypeId()));
            gymClass.setClassType(classType);
        }

        if (req.instructorId() != null) {
            instructorProviderPort.findInstructorById(req.instructorId())
                    .orElseThrow(() -> new IllegalArgumentException("No instructor found with id " + req.instructorId()));
            gymClass.setInstructorId(req.instructorId());
        }

        if (req.gymId() != null) {
            gymInfoRepository.findById(req.gymId())
                    .orElseThrow(() -> new ResourceNotFoundException("Gym", "id", req.gymId()));
            gymClass.setGymId(req.gymId());
        }

        if (req.startTime() != null) gymClass.setStartTime(req.startTime());
        if (req.endTime() != null) gymClass.setEndTime(req.endTime());
        if (req.capacity() != null) gymClass.setCapacity(req.capacity());

        if (!gymClass.getStartTime().isBefore(gymClass.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        GymClass saved = gymClassRepository.save(gymClass);
        int bookingCount = bookingProviderPort.getBookingCounts(Set.of(id)).getOrDefault(id, 0);
        return toResponse(saved, bookingCount);
    }

    @Transactional
    public void cancelClass(Long id) {
        GymClass gymClass = gymClassRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GymClass", "id", id));

        if (gymClass.getStatus() == ClassStatus.CANCELLED) {
            throw new IllegalStateException("Class is already cancelled");
        }

        gymClass.setStatus(ClassStatus.CANCELLED);
        gymClassRepository.save(gymClass);
    }

    private Page<AdminGymClassResponse> enrich(Page<GymClass> page) {
        Set<Long> classIds = page.getContent().stream()
                .map(GymClass::getId)
                .collect(Collectors.toSet());

        Map<Long, Integer> bookingCounts = classIds.isEmpty()
                ? Map.of()
                : bookingProviderPort.getBookingCounts(classIds);

        Set<Long> instructorIds = page.getContent().stream()
                .map(GymClass::getInstructorId)
                .collect(Collectors.toSet());

        Map<Long, InstructorProviderPort.InstructorDetails> instructors =
                instructorProviderPort.getInstructorDetails(instructorIds);

        return page.map(gc -> {
            int bookingCount = bookingCounts.getOrDefault(gc.getId(), 0);
            InstructorProviderPort.InstructorDetails instructor = instructors.get(gc.getInstructorId());
            String instructorName = instructor != null
                    ? instructor.firstName() + " " + instructor.lastName()
                    : "Unknown";

            return new AdminGymClassResponse(
                    gc.getId(),
                    gc.getClassType() != null ? gc.getClassType().getId() : null,
                    gc.getClassType() != null ? gc.getClassType().getName() : null,
                    gc.getInstructorId(),
                    instructorName,
                    gc.getGymId(),
                    gc.getGym() != null ? gc.getGym().getName() : null,
                    gc.getStartTime() != null ? gc.getStartTime().toString() : null,
                    gc.getEndTime() != null ? gc.getEndTime().toString() : null,
                    gc.getCapacity(),
                    bookingCount,
                    Math.max(0, gc.getCapacity() - bookingCount),
                    gc.getStatus() != null ? gc.getStatus().name() : null
            );
        });
    }

    private AdminGymClassResponse toResponse(GymClass gc, int bookingCount) {
        InstructorProviderPort.InstructorDetails instructor =
                instructorProviderPort.findInstructorById(gc.getInstructorId()).orElse(null);
        String instructorName = instructor != null
                ? instructor.firstName() + " " + instructor.lastName()
                : "Unknown";

        return new AdminGymClassResponse(
                gc.getId(),
                gc.getClassType() != null ? gc.getClassType().getId() : null,
                gc.getClassType() != null ? gc.getClassType().getName() : null,
                gc.getInstructorId(),
                instructorName,
                gc.getGymId(),
                gc.getGym() != null ? gc.getGym().getName() : null,
                gc.getStartTime() != null ? gc.getStartTime().toString() : null,
                gc.getEndTime() != null ? gc.getEndTime().toString() : null,
                gc.getCapacity(),
                bookingCount,
                Math.max(0, gc.getCapacity() - bookingCount),
                gc.getStatus() != null ? gc.getStatus().name() : null
        );
    }
}
