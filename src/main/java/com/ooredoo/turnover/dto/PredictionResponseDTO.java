package com.ooredoo.turnover.dto;

import lombok.Data;

@Data
public class PredictionResponseDTO {
    private String model;
    private Double probability;
    private boolean willLeave;
    private String riskLevel;
    private String message;
}
