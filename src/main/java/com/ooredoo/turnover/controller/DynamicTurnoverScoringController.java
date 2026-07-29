package com.ooredoo.turnover.controller;

import com.ooredoo.turnover.service.DynamicTurnoverScoreResult;
import com.ooredoo.turnover.service.DynamicTurnoverScoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/turnover-scoring")
public class DynamicTurnoverScoringController {

    private final DynamicTurnoverScoringService dynamicTurnoverScoringService;

    public DynamicTurnoverScoringController(DynamicTurnoverScoringService dynamicTurnoverScoringService) {
        this.dynamicTurnoverScoringService = dynamicTurnoverScoringService;
    }

    @GetMapping("/employees/{employeeId}/score")
    @PreAuthorize("hasAnyRole('HR','MANAGER','ADMIN')")
    public ResponseEntity<DynamicTurnoverScoreResult> getScore(@PathVariable Long employeeId) {
        return ResponseEntity.ok(dynamicTurnoverScoringService.calculateAndPersistForEmployee(employeeId));
    }

    @GetMapping("/employees/{employeeId}/history")
    @PreAuthorize("hasAnyRole('HR','MANAGER','ADMIN')")
    public ResponseEntity<List<DynamicTurnoverScoreResult>> getHistory(@PathVariable Long employeeId) {
        return ResponseEntity.ok(dynamicTurnoverScoringService.getHistoryForEmployee(employeeId));
    }
}
