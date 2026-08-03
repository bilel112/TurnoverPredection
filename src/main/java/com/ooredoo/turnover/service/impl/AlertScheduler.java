package com.ooredoo.turnover.service.impl;

import com.ooredoo.turnover.config.DynamicScoringProperties;
import com.ooredoo.turnover.entity.Employee;
import com.ooredoo.turnover.repository.EmployeeRepository;
import com.ooredoo.turnover.service.DynamicTurnoverScoringService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AlertScheduler {

    private final EmployeeRepository employeeRepository;
    private final DynamicTurnoverScoringService scoringService;
    private final DynamicScoringProperties scoringProperties;

    public AlertScheduler(EmployeeRepository employeeRepository,
                          DynamicTurnoverScoringService scoringService,
                          DynamicScoringProperties scoringProperties) {
        this.employeeRepository = employeeRepository;
        this.scoringService = scoringService;
        this.scoringProperties = scoringProperties;
    }

    // Runs periodically, interval configured via turnover.scoring.evaluationIntervalMs
    @Scheduled(fixedDelayString = "#{@dynamicScoringProperties.evaluationIntervalMs > 0 ? @dynamicScoringProperties.evaluationIntervalMs : 300000}")
    public void evaluateAllEmployees() {
        if (scoringProperties.getEvaluationIntervalMs() <= 0) {
            return;
        }
        try {
            List<Employee> employees = employeeRepository.findAll();
            for (Employee e : employees) {
                try {
                    scoringService.calculateAndPersistForEmployee(e.getId());
                } catch (Exception ex) {
                    System.err.println("Scoring failed for employee " + e.getId() + ": " + ex.getMessage());
                }
            }
        } catch (Exception ex) {
            System.err.println("AlertScheduler failed: " + ex.getMessage());
        }
    }
}
