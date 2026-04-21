package com.example.boka.gymclass.infrastructure;

import com.example.boka.gymclass.InstructorProviderPort;
import com.example.boka.gymclass.application.AdminGymClassResponse;
import com.example.boka.gymclass.application.AdminGymClassService;
import com.example.boka.gymclass.application.ClassTypeResponse;
import com.example.boka.gymclass.application.CreateGymClassRequest;
import com.example.boka.gymclass.application.UpdateGymClassRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminGymClassController {

    private final AdminGymClassService adminGymClassService;
    private final InstructorProviderPort instructorProviderPort;

    @GetMapping("/classes")
    public Page<AdminGymClassResponse> getClasses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String status
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startTime"));
        return adminGymClassService.getAllClasses(status, pageable);
    }

    @PostMapping("/classes")
    public ResponseEntity<?> createClass(@Valid @RequestBody CreateGymClassRequest request) {
        try {
            return ResponseEntity.ok(adminGymClassService.createClass(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/classes/{id}")
    public ResponseEntity<?> updateClass(@PathVariable Long id, @Valid @RequestBody UpdateGymClassRequest request) {
        try {
            return ResponseEntity.ok(adminGymClassService.updateClass(id, request));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/classes/{id}")
    public ResponseEntity<?> cancelClass(@PathVariable Long id) {
        try {
            adminGymClassService.cancelClass(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/instructors")
    public List<InstructorProviderPort.InstructorDetails> getInstructors() {
        return instructorProviderPort.getAllInstructors();
    }

    @GetMapping("/class-types")
    public List<ClassTypeResponse> getClassTypes() {
        return adminGymClassService.getActiveClassTypes();
    }
}
