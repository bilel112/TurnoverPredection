package com.ooredoo.turnover.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponseDTO {
    private Long employeeId;
    private Integer score;
    private String riskLevel;
    private String riskLabel;
    private String summary;
    private String aiSummary;
    private List<RecommendationItemDTO> recommendations;
}
