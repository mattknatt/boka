package com.example.boka.controller;

import com.example.boka.dto.GymClassResponse;
import com.example.boka.service.ClassSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class ClassSearchController {

    private final ClassSearchService classSearchService;

    /**
     * Smart search: "I want to build muscle" → returns strength/weightlifting classes
     */
    @GetMapping("/search")
    public ResponseEntity<List<GymClassResponse>> searchClasses(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<GymClassResponse> results = classSearchService.searchClasses(query, limit);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache().mustRevalidate())
                .body(results);
    }
}