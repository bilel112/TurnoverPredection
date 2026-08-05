package com.ooredoo.turnover.service.impl;

import com.ooredoo.turnover.config.DynamicScoringProperties;
import com.ooredoo.turnover.entity.Employee;
import com.ooredoo.turnover.repository.EmployeeRepository;
import com.ooredoo.turnover.service.DynamicTurnoverScoringService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

@Component
public class AlertScheduler {

    private final EmployeeRepository employeeRepository;
    private final DynamicTurnoverScoringService scoringService;
    private final DynamicScoringProperties scoringProperties;
    private final TaskScheduler taskScheduler;
    private ScheduledFuture<?> scheduledTask;

    public AlertScheduler(EmployeeRepository employeeRepository,
                          DynamicTurnoverScoringService scoringService,
                          DynamicScoringProperties scoringProperties,
                          TaskScheduler taskScheduler) {
        this.employeeRepository = employeeRepository;
        this.scoringService = scoringService;
        this.scoringProperties = scoringProperties;
        this.taskScheduler = taskScheduler;
    }

    @PostConstruct
    public void init() {
        refreshSchedule();
    }

    @PreDestroy
    public void shutdown() {
        cancelScheduledEvaluation();
    }

    public void refreshSchedule() {
        cancelScheduledEvaluation();
        long intervalMs = scoringProperties.getEvaluationIntervalMs();
        if (intervalMs <= 0) {
            return;
        }
        scheduledTask = taskScheduler.scheduleWithFixedDelay(this::evaluateAllEmployees, intervalMs);
    }

    public void cancelScheduledEvaluation() {
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            scheduledTask = null;
        }
    }

    public boolean shouldEvaluateNow(long nowMs, long lastEvaluatedAtMs, long intervalMs) {
        return intervalMs > 0 && (lastEvaluatedAtMs <= 0 || nowMs - lastEvaluatedAtMs >= intervalMs);
    }

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
