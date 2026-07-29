package com.ooredoo.turnover.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "employee_risk_scores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRiskScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private Integer score;

    @Column(nullable = false, length = 20)
    private String riskLevel;

    @Column(nullable = false, length = 50)
    private String riskLabel;

    @Column(nullable = false)
    private LocalDateTime calculatedAt;

    @Column(length = 1000)
    private String reasons;
}
