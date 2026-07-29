package com.ooredoo.turnover.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "turnover.scoring")
@Data
public class DynamicScoringProperties {
    private int shortTenureWeight = 20;
    private int manyCompaniesWeight = 15;
    private int overtimeWeight = 12;
    private int lowSalaryWeight = 14;
    private int lowJobSatisfactionWeight = 12;
    private int lowEnvironmentSatisfactionWeight = 8;
    private int noRecentPromotionWeight = 10;
    private int lowStockOptionWeight = 9;
}
