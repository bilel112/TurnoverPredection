package com.ooredoo.turnover.service;

import com.ooredoo.turnover.dto.RecommendationResponseDTO;

public interface RecommendationService {
    RecommendationResponseDTO generateRecommendationsForEmployee(Long employeeId);
}
