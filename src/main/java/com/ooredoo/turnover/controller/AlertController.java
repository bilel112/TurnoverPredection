package com.ooredoo.turnover.controller;

import com.ooredoo.turnover.entity.Alert;
import com.ooredoo.turnover.service.AlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('HR','MANAGER','ADMIN')")
    public ResponseEntity<List<Alert>> listAlerts(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(alertService.listAlerts(status));
    }

    @PostMapping("/employees/{employeeId}")
    @PreAuthorize("hasAnyRole('HR','MANAGER','ADMIN')")
    public ResponseEntity<Alert> createAlert(@PathVariable Long employeeId, @RequestBody CreateAlertRequest req) {
        Alert a = alertService.createAlertForEmployee(employeeId, req.getTitle(), req.getMessage(), req.getSeverity());
        return ResponseEntity.ok(a);
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('HR','MANAGER','ADMIN')")
    public ResponseEntity<Alert> markRead(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.markAsRead(id));
    }

    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('HR','MANAGER','ADMIN')")
    public ResponseEntity<Alert> resolve(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.resolveAlert(id));
    }

    public static class CreateAlertRequest {
        private String title;
        private String message;
        private String severity;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
    }
}
