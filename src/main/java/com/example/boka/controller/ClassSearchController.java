package com.example.boka.controller;

import com.example.boka.dto.GymClassResponse;
import com.example.boka.service.ClassSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class ClassSearchController {

    private final ClassSearchService classSearchService;

    /**
     * Paginated search: find classes by name
     */
    @GetMapping("/search")
    public ResponseEntity<Page<GymClassResponse>> searchClasses(
            @RequestParam String query,
            @PageableDefault(size = 8) Pageable pageable
    ) {
        Page<GymClassResponse> results = classSearchService.searchClasses(query, pageable);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache().mustRevalidate())
                .body(results);
    }
}
