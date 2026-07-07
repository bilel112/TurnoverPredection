package com.ooredoo.turnover.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer employeeNumber;

    private Integer age;

    private String department;

    private String jobRole;

    private Double monthlyIncome;

    private Integer yearsAtCompany;

    private Integer distanceFromHome;

    private Boolean overtime;

    private Integer jobSatisfaction;           // 1-4
    private Integer environmentSatisfaction;   // 1-4

    private Boolean attrition;                 // True = Yes (départ)

    // === Colonnes recommandées par l'EDA ===
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
    private Integer education;                 // 1-5

    // Colonnes moins prioritaires mais utiles pour analyses futures
    private Integer dailyRate;
    private Integer hourlyRate;
    private Integer percentSalaryHike;
    private Integer performanceRating;
    private String gender;
}