package com.ooredoo.turnover.service.impl;

import com.ooredoo.turnover.entity.Alert;
import com.ooredoo.turnover.entity.Employee;
import com.ooredoo.turnover.repository.AlertRepository;
import com.ooredoo.turnover.repository.EmployeeRepository;
import com.ooredoo.turnover.service.AlertService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;
    private final EmployeeRepository employeeRepository;

    public AlertServiceImpl(AlertRepository alertRepository, EmployeeRepository employeeRepository) {
        this.alertRepository = alertRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Alert createAlertForEmployee(Long employeeId, String title, String message, Integer score, String reasons, String severity) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));

        Alert alert = Alert.builder()
                .employee(employee)
                .title(title)
                .message(message)
                .score(score)
                .reasons(reasons)
                .severity(severity)
                .status("NEW")
                .createdAt(LocalDateTime.now())
                .build();

        return alertRepository.save(alert);
    }

    @Override
    public boolean hasActiveAlert(Long employeeId, String title, String severity) {
        Employee employee = employeeRepository.findById(employeeId).orElse(null);
        if (employee == null) {
            return false;
        }

        return alertRepository.findByEmployeeOrderByCreatedAtDesc(employee).stream()
                .anyMatch(alert -> {
                    String normalizedTitle = title == null ? null : title.trim();
                    String normalizedSeverity = severity == null ? null : severity.trim().toUpperCase();
                    String alertStatus = alert.getStatus() == null ? "" : alert.getStatus().trim().toUpperCase();
                    boolean sameTitle = normalizedTitle == null || normalizedTitle.equals(alert.getTitle());
                    boolean sameSeverity = normalizedSeverity == null || normalizedSeverity.equalsIgnoreCase(alert.getSeverity());
                    return sameTitle && sameSeverity && !"RESOLVED".equals(alertStatus);
                });
    }

    @Override
    public List<Alert> listAlerts(String status, String severity) {
        boolean hasStatus = status != null && !status.isBlank();
        boolean hasSeverity = severity != null && !severity.isBlank();
        if (hasStatus && hasSeverity) {
            return alertRepository.findByStatusAndSeverity(status, severity);
        }
        if (hasStatus) {
            return alertRepository.findByStatus(status);
        }
        if (hasSeverity) {
            return alertRepository.findBySeverity(severity);
        }
        return alertRepository.findAll();
    }

    @Override
    public long countByStatus(String status) {
        if (status == null || status.isBlank()) {
            return alertRepository.count();
        }
        return alertRepository.countByStatus(status);
    }

    @Override
    public Alert markAsRead(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));
        alert.setStatus("READ");
        return alertRepository.save(alert);
    }

    @Override
    public Alert resolveAlert(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));
        alert.setStatus("RESOLVED");
        alert.setResolvedAt(LocalDateTime.now());
        return alertRepository.save(alert);
    }
}
