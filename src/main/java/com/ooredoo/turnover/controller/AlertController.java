package com.ooredoo.turnover.controller;

import com.ooredoo.turnover.entity.Alert;
import com.ooredoo.turnover.entity.AlertConfig;
import com.ooredoo.turnover.service.AlertConfigService;
import com.ooredoo.turnover.service.AlertReportService;
import com.ooredoo.turnover.service.AlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;
    private final AlertConfigService alertConfigService;
    private final AlertReportService alertReportService;

    public AlertController(AlertService alertService, AlertConfigService alertConfigService, AlertReportService alertReportService) {
        this.alertService = alertService;
        this.alertConfigService = alertConfigService;
        this.alertReportService = alertReportService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('HR','MANAGER','ADMIN')")
    public ResponseEntity<List<Alert>> listAlerts(@RequestParam(required = false) String status,
                                                  @RequestParam(required = false) String severity) {
        return ResponseEntity.ok(alertService.listAlerts(status, severity));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('HR','MANAGER','ADMIN')")
    public ResponseEntity<Map<String, Long>> summary() {
        Map<String, Long> result = new HashMap<>();
        result.put("total", alertService.countByStatus(null));
        result.put("newCount", alertService.countByStatus("NEW"));
        result.put("readCount", alertService.countByStatus("READ"));
        result.put("resolvedCount", alertService.countByStatus("RESOLVED"));
        return ResponseEntity.ok(result);
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

    @GetMapping("/config")
    @PreAuthorize("hasAnyRole('HR','MANAGER','ADMIN')")
    public ResponseEntity<AlertConfig> getConfig() {
        return ResponseEntity.ok(alertConfigService.getConfig());
    }

    @PutMapping("/config")
    @PreAuthorize("hasAnyRole('HR','MANAGER','ADMIN')")
    public ResponseEntity<AlertConfig> updateConfig(@RequestBody ConfigRequest req) {
        AlertConfig existing = alertConfigService.getConfig();
        if (req.getHighThreshold() != null) existing.setHighThreshold(req.getHighThreshold());
        if (req.getMediumThreshold() != null) existing.setMediumThreshold(req.getMediumThreshold());
        if (req.getTrendDelta() != null) existing.setTrendDelta(req.getTrendDelta());
        if (req.getEvaluationIntervalMs() != null) existing.setEvaluationIntervalMs(req.getEvaluationIntervalMs());
        return ResponseEntity.ok(alertConfigService.updateConfig(existing));
    }

    @PostMapping("/report")
    @PreAuthorize("hasAnyRole('HR','MANAGER','ADMIN')")
    public ResponseEntity<Map<String, String>> sendReport() {
        alertReportService.sendHighRiskAlertReport();
        return ResponseEntity.ok(Map.of("message", "Rapport envoyé à bilelbrino3@gmail.com"));
    }

    public static class ConfigRequest {
        private Integer highThreshold;
        private Integer mediumThreshold;
        private Integer trendDelta;
        private Long evaluationIntervalMs;

        public Integer getHighThreshold() { return highThreshold; }
        public void setHighThreshold(Integer highThreshold) { this.highThreshold = highThreshold; }
        public Integer getMediumThreshold() { return mediumThreshold; }
        public void setMediumThreshold(Integer mediumThreshold) { this.mediumThreshold = mediumThreshold; }
        public Integer getTrendDelta() { return trendDelta; }
        public void setTrendDelta(Integer trendDelta) { this.trendDelta = trendDelta; }
        public Long getEvaluationIntervalMs() { return evaluationIntervalMs; }
        public void setEvaluationIntervalMs(Long evaluationIntervalMs) { this.evaluationIntervalMs = evaluationIntervalMs; }
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
