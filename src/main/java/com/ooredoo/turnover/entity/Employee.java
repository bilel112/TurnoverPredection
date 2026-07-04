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

    private Integer employeeNumber;   // Important pour le dataset

    private Integer age;

    private String department;

    private String jobRole;

    private Double monthlyIncome;

    private Integer yearsAtCompany;

    private Integer distanceFromHome;

    private Boolean overtime;

    private Integer jobSatisfaction;           // 1 à 4
    private Integer environmentSatisfaction;   // 1 à 4

    private Boolean attrition;                 // True = départ (Yes)

    // Tu pourras ajouter d'autres champs plus tard
}