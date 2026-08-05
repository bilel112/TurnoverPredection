package com.ooredoo.turnover.service.impl;

import com.ooredoo.turnover.dto.EmployeeDTO;
import com.ooredoo.turnover.service.DynamicTurnoverScoreResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecommendationServiceImplTest {

    @Test
    void shouldReturnThreeRecommendationsForLowRisk() {
        RecommendationServiceImpl service = new RecommendationServiceImpl(null, null, null, "", "", "");
        DynamicTurnoverScoreResult result = new DynamicTurnoverScoreResult();
        result.setScore(20);
        result.setRiskLevel("LOW");

        List<String> priorities = service.determineRecommendationPriorities(new EmployeeDTO(), result);

        assertEquals(3, priorities.size());
        assertEquals(List.of("low", "low", "low"), priorities);
    }

    @Test
    void shouldReturnFourRecommendationsForMediumRisk() {
        RecommendationServiceImpl service = new RecommendationServiceImpl(null, null, null, "", "", "");
        DynamicTurnoverScoreResult result = new DynamicTurnoverScoreResult();
        result.setScore(50);
        result.setRiskLevel("MEDIUM");

        List<String> priorities = service.determineRecommendationPriorities(new EmployeeDTO(), result);

        assertEquals(4, priorities.size());
        assertEquals(List.of("low", "low", "medium", "medium"), priorities);
    }

    @Test
    void shouldReturnFiveRecommendationsForHighRisk() {
        RecommendationServiceImpl service = new RecommendationServiceImpl(null, null, null, "", "", "");
        DynamicTurnoverScoreResult result = new DynamicTurnoverScoreResult();
        result.setScore(80);
        result.setRiskLevel("HIGH");

        List<String> priorities = service.determineRecommendationPriorities(new EmployeeDTO(), result);

        assertEquals(5, priorities.size());
        assertEquals(List.of("low", "medium", "medium", "high", "high"), priorities);
    }
}
