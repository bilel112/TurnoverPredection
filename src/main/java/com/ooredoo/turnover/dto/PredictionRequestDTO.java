package com.ooredoo.turnover.dto;

import lombok.Data;

@Data
public class PredictionRequestDTO {
    private Integer age;
    private Boolean overtime;
    private Integer monthlyIncome;
    private Integer distanceFromHome;
    private Integer jobSatisfaction;
    private Integer environmentSatisfaction;
    private Integer yearsAtCompany;
    private Integer totalWorkingYears;
    private Integer stockOptionLevel;
    private Integer jobLevel;
    private Integer yearsInCurrentRole;
    private Integer yearsWithCurrManager;
    private Integer yearsSinceLastPromotion;
    private Integer numCompaniesWorked;
    private Integer trainingTimesLastYear;
    private Integer workLifeBalance;
    private Integer education;
    private Integer performanceRating;
    private String maritalStatus;
    private String businessTravel;
    private String jobRole;
}
