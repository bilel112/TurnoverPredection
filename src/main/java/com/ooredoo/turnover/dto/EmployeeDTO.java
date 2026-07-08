package com.ooredoo.turnover.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {

    private Long id;

    private Integer employeeNumber;
    private Integer age;
    private String department;
    private String jobRole;
    private Double monthlyIncome;
    private Integer yearsAtCompany;
    private Integer distanceFromHome;
    private Boolean overtime;

    private Integer jobSatisfaction;
    private Integer environmentSatisfaction;
    private Boolean attrition;

    // Colonnes importantes selon l'EDA
    private String businessTravel;
    private String maritalStatus;
    private Integer jobLevel;
    private Integer totalWorkingYears;
    private Integer yearsInCurrentRole;
    private Integer yearsWithCurrManager;
    private Integer yearsSinceLastPromotion;
    private Integer stockOptionLevel;
    private Integer numCompaniesWorked;
    private Integer trainingTimesLastYear;
    private Integer workLifeBalance;
    private String educationField;
    private Integer education;
    private String gender;

    // Champs optionnels
    private Integer dailyRate;
    private Integer hourlyRate;
    private Integer percentSalaryHike;
    private Integer performanceRating;
}