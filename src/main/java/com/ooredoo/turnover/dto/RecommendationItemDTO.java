package com.ooredoo.turnover.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationItemDTO {
    private String priority;
    private String title;
    private String reason;
    private String action;
}
