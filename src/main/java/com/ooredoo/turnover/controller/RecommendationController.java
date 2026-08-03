package com.ooredoo.turnover.controller;

import com.ooredoo.turnover.dto.RecommendationResponseDTO;
import com.ooredoo.turnover.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping("/employees/{employeeId}")
    @PreAuthorize("hasAnyRole('HR','MANAGER','ADMIN')")
    public ResponseEntity<RecommendationResponseDTO> generateForEmployee(@PathVariable Long employeeId) {
        RecommendationResponseDTO response = recommendationService.generateRecommendationsForEmployee(employeeId);
        return ResponseEntity.ok(response);
    }
}
