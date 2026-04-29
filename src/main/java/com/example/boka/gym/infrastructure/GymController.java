package com.example.boka.gym.infrastructure;

import com.example.boka.gym.application.GymResponse;
import com.example.boka.gym.application.GymService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Gyms", description = "List and locate gyms")
@RestController
@RequestMapping("/api/gyms")
@RequiredArgsConstructor
public class GymController {

    private final GymService gymService;

    @GetMapping
    public ResponseEntity<Page<GymResponse>> getAllGyms(
            @PageableDefault(size = 8) Pageable pageable
    ) {
        return ResponseEntity.ok(gymService.getAllGyms(pageable));
    }
}
