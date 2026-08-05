package com.ooredoo.turnover.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    private String title;

    @Column(length = 2000)
    private String message;

    private Integer score;

    @Column(length = 4000)
    private String reasons;

    private String severity; // e.g., LOW, MEDIUM, HIGH, CRITICAL

    private String status; // NEW, READ, RESOLVED

    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;
}
