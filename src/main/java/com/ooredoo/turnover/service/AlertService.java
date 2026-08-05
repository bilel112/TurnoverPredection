package com.ooredoo.turnover.service;

import com.ooredoo.turnover.entity.Alert;

import java.util.List;

public interface AlertService {
    Alert createAlertForEmployee(Long employeeId, String title, String message, Integer score, String reasons, String severity);
    boolean hasActiveAlert(Long employeeId, String title, String severity);
    List<Alert> listAlerts(String status, String severity);
    long countByStatus(String status);
    Alert markAsRead(Long alertId);
    Alert resolveAlert(Long alertId);
}
