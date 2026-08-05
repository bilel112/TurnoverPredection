package com.ooredoo.turnover.service.impl;

import com.ooredoo.turnover.config.DynamicScoringProperties;
import com.ooredoo.turnover.entity.Employee;
import com.ooredoo.turnover.entity.EmployeeRiskScore;
import com.ooredoo.turnover.repository.EmployeeRepository;
import com.ooredoo.turnover.repository.EmployeeRiskScoreRepository;
import com.ooredoo.turnover.service.DynamicTurnoverScoreResult;
import com.ooredoo.turnover.service.DynamicTurnoverScoringService;
import com.ooredoo.turnover.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class DynamicTurnoverScoringServiceImpl implements DynamicTurnoverScoringService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeRiskScoreRepository employeeRiskScoreRepository;
    private final DynamicScoringProperties scoringProperties;
    private final AlertService alertService;

    @Autowired
    public DynamicTurnoverScoringServiceImpl(EmployeeRepository employeeRepository,
                                             EmployeeRiskScoreRepository employeeRiskScoreRepository,
                                             DynamicScoringProperties scoringProperties,
                                             AlertService alertService) {
        this.employeeRepository = employeeRepository;
        this.employeeRiskScoreRepository = employeeRiskScoreRepository;
        this.scoringProperties = scoringProperties;
        this.alertService = alertService;
    }

    // Backwards-compatible constructor for tests and existing callers
    public DynamicTurnoverScoringServiceImpl(EmployeeRepository employeeRepository,
                                             EmployeeRiskScoreRepository employeeRiskScoreRepository,
                                             DynamicScoringProperties scoringProperties) {
        this(employeeRepository, employeeRiskScoreRepository, scoringProperties, null);
    }

    @Override
    public DynamicTurnoverScoreResult calculateScore(Employee employee) {
        List<String> reasons = new ArrayList<>();
        int score = 0;

        if (employee.getYearsAtCompany() != null && employee.getYearsAtCompany() <= 1) {
            score += scoringProperties.getShortTenureWeight();
            reasons.add("ancienneté faible");
        }

        if (employee.getNumCompaniesWorked() != null && employee.getNumCompaniesWorked() >= 4) {
            score += scoringProperties.getManyCompaniesWeight();
            reasons.add("nombre d'entreprises élevé");
        }

        if (employee.getOvertime() != null && Boolean.TRUE.equals(employee.getOvertime())) {
            score += scoringProperties.getOvertimeWeight();
            reasons.add("heures supplémentaires");
        }

        if (employee.getMonthlyIncome() != null && employee.getMonthlyIncome() < 3000) {
            score += scoringProperties.getLowSalaryWeight();
            reasons.add("salaire bas");
        }

        if (employee.getJobSatisfaction() != null && employee.getJobSatisfaction() <= 2) {
            score += scoringProperties.getLowJobSatisfactionWeight();
            reasons.add("satisfaction au travail faible");
        }

        if (employee.getEnvironmentSatisfaction() != null && employee.getEnvironmentSatisfaction() <= 2) {
            score += scoringProperties.getLowEnvironmentSatisfactionWeight();
            reasons.add("satisfaction environnement faible");
        }

        if (employee.getYearsSinceLastPromotion() != null && employee.getYearsSinceLastPromotion() >= 3) {
            score += scoringProperties.getNoRecentPromotionWeight();
            reasons.add("pas de promotion récente");
        }

        if (employee.getStockOptionLevel() != null && employee.getStockOptionLevel() <= 0) {
            score += scoringProperties.getLowStockOptionWeight();
            reasons.add("niveau d'options faibles");
        }

        if (score >= scoringProperties.getHighThreshold()) {
            return new DynamicTurnoverScoreResult(score, "HIGH", "Élevé", reasons, LocalDateTime.now());
        }
        if (score >= scoringProperties.getMediumThreshold()) {
            return new DynamicTurnoverScoreResult(score, "MEDIUM", "Moyen", reasons, LocalDateTime.now());
        }
        return new DynamicTurnoverScoreResult(score, "LOW", "Faible", reasons, LocalDateTime.now());
    }

    public String formatAlertMessage(int score, List<String> reasons) {
        String normalizedReasons = reasons == null || reasons.isEmpty()
                ? "Aucun facteur détecté"
                : String.join(";", reasons.stream().filter(reason -> reason != null && !reason.isBlank()).toList());
        return String.format("Score: %d | Reasons: %s", score, normalizedReasons);
    }

    @Override
    public DynamicTurnoverScoreResult calculateAndPersistForEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));

        DynamicTurnoverScoreResult result = calculateScore(employee);
        // Prepare new score entity
        EmployeeRiskScore scoreEntity = new EmployeeRiskScore();
        scoreEntity.setEmployee(employee);
        scoreEntity.setScore(result.getScore());
        scoreEntity.setRiskLevel(result.getRiskLevel());
        scoreEntity.setRiskLabel(result.getRiskLabel());
        scoreEntity.setCalculatedAt(LocalDateTime.now());
        String joinedReasons = String.join(";", result.getReasons());
        scoreEntity.setReasons(joinedReasons);

        // Save the score only if it's not a duplicate of the latest score.
        // IMPORTANT: do NOT return early here — alerts must be evaluated on every
        // scheduled run even when the score is unchanged, otherwise risk alerts
        // would never be created by the periodic scheduler.
        boolean scoreWasPersisted = false;
        List<EmployeeRiskScore> history = null;
        try {
            history = employeeRiskScoreRepository.findByEmployeeOrderByCalculatedAtDesc(employee);
            if (history != null && !history.isEmpty()) {
                EmployeeRiskScore latest = history.get(0);
                boolean sameScore = latest.getScore() != null && latest.getScore().intValue() == scoreEntity.getScore();
                boolean sameLevel = latest.getRiskLevel() != null && latest.getRiskLevel().equalsIgnoreCase(scoreEntity.getRiskLevel());
                boolean sameReasons = (latest.getReasons() == null && scoreEntity.getReasons() == null) || (latest.getReasons() != null && latest.getReasons().equals(scoreEntity.getReasons()));
                if (!sameScore || !sameLevel || !sameReasons) {
                    employeeRiskScoreRepository.save(scoreEntity);
                    scoreWasPersisted = true;
                }
            } else {
                employeeRiskScoreRepository.save(scoreEntity);
                scoreWasPersisted = true;
            }
        } catch (Exception ex) {
            System.err.println("Error checking previous scores for employee " + employeeId + ": " + ex.getMessage());
        }

        if (!scoreWasPersisted) {
            scoreEntity = history != null && !history.isEmpty() ? history.get(0) : scoreEntity;
        }

        // Create an alert for high risk employees (MVP behavior)
        if ("HIGH".equalsIgnoreCase(result.getRiskLevel())) {
            String message = formatAlertMessage(result.getScore(), result.getReasons());
            if (alertService != null) {
                try {
                    if (!alertService.hasActiveAlert(employeeId, "Risque élevé détecté", "HIGH")) {
                        alertService.createAlertForEmployee(employeeId, "Risque élevé détecté", message, scoreEntity.getScore(), String.join(";", result.getReasons()), "HIGH");
                    }
                } catch (Exception ex) {
                    // Log and continue; alerting should not break scoring
                    System.err.println("Failed to create alert for employee " + employeeId + ": " + ex.getMessage());
                }
            }
        }

        // Trend detection: compare with previous score and create an alert if increase >= trendDelta
        if (alertService != null) {
            try {
                var history2 = employeeRiskScoreRepository.findByEmployeeOrderByCalculatedAtDesc(employee);
                if (history2 != null && history2.size() >= 2) {
                    EmployeeRiskScore previous = history2.get(1); // 0 is current
                    int delta = scoreEntity.getScore() - previous.getScore();
                    if (delta >= scoringProperties.getTrendDelta()) {
                        String msg = String.format("Score augmenté de %d points (actuel %d → précédent %d). %s", delta, scoreEntity.getScore(), previous.getScore(), formatAlertMessage(scoreEntity.getScore(), result.getReasons()));
                        if (!alertService.hasActiveAlert(employeeId, "Tendance risque : augmentation rapide", "MEDIUM")) {
                            alertService.createAlertForEmployee(employeeId, "Tendance risque : augmentation rapide", msg, scoreEntity.getScore(), String.join(";", result.getReasons()), "MEDIUM");
                        }
                    }
                }
            } catch (Exception ex) {
                System.err.println("Failed to detect trend for employee " + employeeId + ": " + ex.getMessage());
            }
        }
        return result;
    }

    @Override
    public List<DynamicTurnoverScoreResult> getHistoryForEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));

        return employeeRiskScoreRepository.findByEmployeeOrderByCalculatedAtDesc(employee).stream()
                .map(score -> new DynamicTurnoverScoreResult(
                        score.getScore(),
                        score.getRiskLevel(),
                        score.getRiskLabel(),
                        score.getReasons() == null || score.getReasons().isBlank()
                                ? List.of()
                                : List.of(score.getReasons().split(";")),
                        score.getCalculatedAt()))
                .toList();
    }
}
