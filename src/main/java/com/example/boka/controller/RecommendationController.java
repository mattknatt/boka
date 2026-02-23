package com.example.boka.controller;

import com.example.boka.dto.GymClassResponse;
import com.example.boka.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<GymClassResponse>> getRecommendations(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<GymClassResponse> recommendations = recommendationService.getRecommendationsForUser(userId, limit);
        return ResponseEntity.ok(recommendations);
    }
}
