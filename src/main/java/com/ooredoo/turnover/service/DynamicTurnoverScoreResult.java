package com.ooredoo.turnover.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DynamicTurnoverScoreResult {

    private Integer score;
    private String riskLevel;
    private String riskLabel;
    private List<String> reasons;
    private LocalDateTime calculatedAt;
}
