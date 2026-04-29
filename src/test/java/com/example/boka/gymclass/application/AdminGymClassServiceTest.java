package com.example.boka.gymclass.application;

import com.example.boka.booking.BookingProviderPort;
import com.example.boka.common.ResourceNotFoundException;
import com.example.boka.gym.domain.GymInfo;
import com.example.boka.gym.domain.GymInfoRepository;
import com.example.boka.gymclass.InstructorProviderPort;
import com.example.boka.gymclass.domain.ClassStatus;
import com.example.boka.gymclass.domain.ClassType;
import com.example.boka.gymclass.domain.ClassTypeRepository;
import com.example.boka.gymclass.domain.GymClass;
import com.example.boka.gymclass.domain.GymClassRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminGymClassServiceTest {

    @Mock private GymClassRepository gymClassRepository;
    @Mock private ClassTypeRepository classTypeRepository;
    @Mock private GymInfoRepository gymInfoRepository;
    @Mock private InstructorProviderPort instructorProviderPort;
    @Mock private BookingProviderPort bookingProviderPort;

    @InjectMocks
    private AdminGymClassService adminGymClassService;

    private static final Long CLASS_ID    = 1L;
    private static final Long TYPE_ID     = 10L;
    private static final Long INSTRUCTOR_ID = 20L;
    private static final Long GYM_ID      = 30L;

    private static final LocalDateTime START = LocalDateTime.now().plusDays(1);
    private static final LocalDateTime END   = START.plusHours(1);

    // --- Helpers ---

    private ClassType classType() {
        ClassType ct = new ClassType();
        ct.setId(TYPE_ID);
        ct.setName("Yoga");
        ct.setDefaultCapacity(20);
        ct.setDurationMinutes(60);
        ct.setIsActive(true);
        return ct;
    }

    private GymClass gymClass(ClassStatus status) {
        GymClass gc = new GymClass();
        gc.setId(CLASS_ID);
        gc.setClassType(classType());
        gc.setInstructorId(INSTRUCTOR_ID);
        gc.setGymId(GYM_ID);
        gc.setStartTime(START);
        gc.setEndTime(END);
        gc.setCapacity(20);
        gc.setStatus(status);
        return gc;
    }

    private InstructorProviderPort.InstructorDetails instructor() {
        return new InstructorProviderPort.InstructorDetails(INSTRUCTOR_ID, "Jane", "Smith");
    }

    // --- getAllClasses ---

    @Test
    void getAllClasses_NoFilter_ReturnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 15);
        GymClass gc = gymClass(ClassStatus.SCHEDULED);
        when(gymClassRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(gc)));
        when(bookingProviderPort.getBookingCounts(anySet())).thenReturn(Map.of(CLASS_ID, 5));
        when(instructorProviderPort.getInstructorDetails(anySet())).thenReturn(Map.of(INSTRUCTOR_ID, instructor()));

        Page<AdminGymClassResponse> result = adminGymClassService.getAllClasses(null, pageable);

        assertEquals(1, result.getTotalElements());
        AdminGymClassResponse r = result.getContent().get(0);
        assertEquals(CLASS_ID, r.id());
        assertEquals("Yoga", r.classTypeName());
        assertEquals("Jane Smith", r.instructorName());
        assertEquals(5, r.currentBookings());
        assertEquals(15, r.availableSpots());
        assertEquals("SCHEDULED", r.status());
    }

    @Test
    void getAllClasses_WithStatusFilter_DelegatesToFindByStatus() {
        Pageable pageable = PageRequest.of(0, 15);
        GymClass gc = gymClass(ClassStatus.CANCELLED);
        when(gymClassRepository.findByStatus(ClassStatus.CANCELLED, pageable))
                .thenReturn(new PageImpl<>(List.of(gc)));
        when(bookingProviderPort.getBookingCounts(anySet())).thenReturn(Map.of());
        when(instructorProviderPort.getInstructorDetails(anySet())).thenReturn(Map.of());

        Page<AdminGymClassResponse> result = adminGymClassService.getAllClasses("CANCELLED", pageable);

        assertEquals(1, result.getTotalElements());
        verify(gymClassRepository).findByStatus(ClassStatus.CANCELLED, pageable);
        verify(gymClassRepository, never()).findAll(pageable);
    }

    @Test
    void getAllClasses_UnknownInstructor_FallsBackToUnknown() {
        Pageable pageable = PageRequest.of(0, 15);
        when(gymClassRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(gymClass(ClassStatus.SCHEDULED))));
        when(bookingProviderPort.getBookingCounts(anySet())).thenReturn(Map.of());
        when(instructorProviderPort.getInstructorDetails(anySet())).thenReturn(Map.of());

        AdminGymClassResponse r = adminGymClassService.getAllClasses(null, pageable).getContent().get(0);

        assertEquals("Unknown", r.instructorName());
    }

    // --- getActiveClassTypes ---

    @Test
    void getActiveClassTypes_ReturnsOnlyActiveTypes() {
        ClassType active = classType();
        ClassType inactive = new ClassType();
        inactive.setId(99L);
        inactive.setName("Inactive");
        inactive.setDefaultCapacity(10);
        inactive.setDurationMinutes(30);
        inactive.setIsActive(false);
        when(classTypeRepository.findAll()).thenReturn(List.of(active, inactive));

        List<ClassTypeResponse> result = adminGymClassService.getActiveClassTypes();

        assertEquals(1, result.size());
        assertEquals("Yoga", result.get(0).name());
    }

    // --- createClass ---

    @Test
    void createClass_Successful() {
        CreateGymClassRequest req = new CreateGymClassRequest(TYPE_ID, INSTRUCTOR_ID, GYM_ID, START, END, 20);
        when(classTypeRepository.findById(TYPE_ID)).thenReturn(Optional.of(classType()));
        when(instructorProviderPort.findInstructorById(INSTRUCTOR_ID)).thenReturn(Optional.of(instructor()));
        when(gymInfoRepository.findById(GYM_ID)).thenReturn(Optional.of(new GymInfo()));

        GymClass saved = gymClass(ClassStatus.SCHEDULED);
        when(gymClassRepository.save(any())).thenReturn(saved);
        when(instructorProviderPort.findInstructorById(INSTRUCTOR_ID)).thenReturn(Optional.of(instructor()));

        AdminGymClassResponse result = adminGymClassService.createClass(req);

        assertNotNull(result);
        assertEquals("SCHEDULED", result.status());
        assertEquals(TYPE_ID, result.classTypeId());
        assertEquals(INSTRUCTOR_ID, result.instructorId());

        ArgumentCaptor<GymClass> captor = ArgumentCaptor.forClass(GymClass.class);
        verify(gymClassRepository).save(captor.capture());
        assertEquals(ClassStatus.SCHEDULED, captor.getValue().getStatus());
        assertEquals(20, captor.getValue().getCapacity());
    }

    @Test
    void createClass_ClassTypeNotFound_ThrowsResourceNotFoundException() {
        CreateGymClassRequest req = new CreateGymClassRequest(TYPE_ID, INSTRUCTOR_ID, GYM_ID, START, END, 20);
        when(classTypeRepository.findById(TYPE_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminGymClassService.createClass(req));
        verify(gymClassRepository, never()).save(any());
    }

    @Test
    void createClass_InstructorNotFound_ThrowsIllegalArgumentException() {
        CreateGymClassRequest req = new CreateGymClassRequest(TYPE_ID, INSTRUCTOR_ID, GYM_ID, START, END, 20);
        when(classTypeRepository.findById(TYPE_ID)).thenReturn(Optional.of(classType()));
        when(instructorProviderPort.findInstructorById(INSTRUCTOR_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> adminGymClassService.createClass(req));
        verify(gymClassRepository, never()).save(any());
    }

    @Test
    void createClass_GymNotFound_ThrowsResourceNotFoundException() {
        CreateGymClassRequest req = new CreateGymClassRequest(TYPE_ID, INSTRUCTOR_ID, GYM_ID, START, END, 20);
        when(classTypeRepository.findById(TYPE_ID)).thenReturn(Optional.of(classType()));
        when(instructorProviderPort.findInstructorById(INSTRUCTOR_ID)).thenReturn(Optional.of(instructor()));
        when(gymInfoRepository.findById(GYM_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminGymClassService.createClass(req));
        verify(gymClassRepository, never()).save(any());
    }

    @Test
    void createClass_StartTimeAfterEndTime_ThrowsIllegalArgumentException() {
        CreateGymClassRequest req = new CreateGymClassRequest(TYPE_ID, INSTRUCTOR_ID, GYM_ID, END, START, 20);
        when(classTypeRepository.findById(TYPE_ID)).thenReturn(Optional.of(classType()));
        when(instructorProviderPort.findInstructorById(INSTRUCTOR_ID)).thenReturn(Optional.of(instructor()));
        when(gymInfoRepository.findById(GYM_ID)).thenReturn(Optional.of(new GymInfo()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> adminGymClassService.createClass(req));
        assertEquals("Start time must be before end time", ex.getMessage());
        verify(gymClassRepository, never()).save(any());
    }

    // --- updateClass ---

    @Test
    void updateClass_Successful_UpdatesAllFields() {
        LocalDateTime newStart = START.plusDays(1);
        LocalDateTime newEnd = newStart.plusHours(2);
        UpdateGymClassRequest req = new UpdateGymClassRequest(TYPE_ID, INSTRUCTOR_ID, GYM_ID, newStart, newEnd, 25);

        GymClass existing = gymClass(ClassStatus.SCHEDULED);
        when(gymClassRepository.findById(CLASS_ID)).thenReturn(Optional.of(existing));
        when(classTypeRepository.findById(TYPE_ID)).thenReturn(Optional.of(classType()));
        when(instructorProviderPort.findInstructorById(INSTRUCTOR_ID)).thenReturn(Optional.of(instructor()));
        when(gymInfoRepository.findById(GYM_ID)).thenReturn(Optional.of(new GymInfo()));
        when(gymClassRepository.save(any())).thenReturn(existing);
        when(bookingProviderPort.getBookingCounts(anySet())).thenReturn(Map.of(CLASS_ID, 3));
        when(instructorProviderPort.findInstructorById(INSTRUCTOR_ID)).thenReturn(Optional.of(instructor()));

        AdminGymClassResponse result = adminGymClassService.updateClass(CLASS_ID, req);

        assertNotNull(result);
        assertEquals(25, existing.getCapacity());
        assertEquals(newStart, existing.getStartTime());
        assertEquals(newEnd, existing.getEndTime());
    }

    @Test
    void updateClass_ClassNotFound_ThrowsResourceNotFoundException() {
        when(gymClassRepository.findById(CLASS_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> adminGymClassService.updateClass(CLASS_ID, new UpdateGymClassRequest(null, null, null, null, null, null)));
    }

    @Test
    void updateClass_CancelledClass_ThrowsIllegalStateException() {
        when(gymClassRepository.findById(CLASS_ID)).thenReturn(Optional.of(gymClass(ClassStatus.CANCELLED)));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> adminGymClassService.updateClass(CLASS_ID, new UpdateGymClassRequest(null, null, null, null, null, null)));
        assertEquals("Cannot update a cancelled class", ex.getMessage());
    }

    @Test
    void updateClass_InvalidInstructor_ThrowsIllegalArgumentException() {
        when(gymClassRepository.findById(CLASS_ID)).thenReturn(Optional.of(gymClass(ClassStatus.SCHEDULED)));
        when(instructorProviderPort.findInstructorById(INSTRUCTOR_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> adminGymClassService.updateClass(CLASS_ID,
                        new UpdateGymClassRequest(null, INSTRUCTOR_ID, null, null, null, null)));
    }

    @Test
    void updateClass_StartTimeEqualsEndTime_ThrowsIllegalArgumentException() {
        GymClass existing = gymClass(ClassStatus.SCHEDULED);
        when(gymClassRepository.findById(CLASS_ID)).thenReturn(Optional.of(existing));
        when(bookingProviderPort.getBookingCounts(anySet())).thenReturn(Map.of());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> adminGymClassService.updateClass(CLASS_ID,
                        new UpdateGymClassRequest(null, null, null, START, START, null)));
        assertEquals("Start time must be before end time", ex.getMessage());
    }

    @Test
    void updateClass_CapacityBelowBookingCount_ThrowsIllegalArgumentException() {
        GymClass existing = gymClass(ClassStatus.SCHEDULED);
        when(gymClassRepository.findById(CLASS_ID)).thenReturn(Optional.of(existing));
        when(bookingProviderPort.getBookingCounts(anySet())).thenReturn(Map.of(CLASS_ID, 15));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> adminGymClassService.updateClass(CLASS_ID,
                        new UpdateGymClassRequest(null, null, null, null, null, 10)));
        assertTrue(ex.getMessage().contains("Capacity cannot be lower than current confirmed bookings"));
        verify(gymClassRepository, never()).save(any());
    }

    // --- cancelClass ---

    @Test
    void cancelClass_Successful_SetsStatusToCancelledAndCancelsBookings() {
        GymClass gc = gymClass(ClassStatus.SCHEDULED);
        when(gymClassRepository.findById(CLASS_ID)).thenReturn(Optional.of(gc));
        when(gymClassRepository.save(any())).thenReturn(gc);

        adminGymClassService.cancelClass(CLASS_ID);

        assertEquals(ClassStatus.CANCELLED, gc.getStatus());
        verify(gymClassRepository).save(gc);
        verify(bookingProviderPort).cancelBookingsForClass(CLASS_ID);
    }

    @Test
    void cancelClass_ClassNotFound_ThrowsResourceNotFoundException() {
        when(gymClassRepository.findById(CLASS_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> adminGymClassService.cancelClass(CLASS_ID));
        verify(gymClassRepository, never()).save(any());
    }

    @Test
    void cancelClass_AlreadyCancelled_ThrowsIllegalStateException() {
        when(gymClassRepository.findById(CLASS_ID)).thenReturn(Optional.of(gymClass(ClassStatus.CANCELLED)));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> adminGymClassService.cancelClass(CLASS_ID));
        assertEquals("Class is already cancelled", ex.getMessage());
        verify(gymClassRepository, never()).save(any());
    }
}
