package com.ooredoo.turnover.service.impl;

import com.ooredoo.turnover.config.DynamicScoringProperties;
import com.ooredoo.turnover.entity.Employee;
import com.ooredoo.turnover.entity.EmployeeRiskScore;
import com.ooredoo.turnover.repository.EmployeeRepository;
import com.ooredoo.turnover.repository.EmployeeRiskScoreRepository;
import com.ooredoo.turnover.service.DynamicTurnoverScoreResult;
import com.ooredoo.turnover.service.DynamicTurnoverScoringService;
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

    @Autowired
    public DynamicTurnoverScoringServiceImpl(EmployeeRepository employeeRepository,
                                             EmployeeRiskScoreRepository employeeRiskScoreRepository,
                                             DynamicScoringProperties scoringProperties) {
        this.employeeRepository = employeeRepository;
        this.employeeRiskScoreRepository = employeeRiskScoreRepository;
        this.scoringProperties = scoringProperties;
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

        if (score >= 55) {
            return new DynamicTurnoverScoreResult(score, "HIGH", "Élevé", reasons, LocalDateTime.now());
        }
        if (score >= 30) {
            return new DynamicTurnoverScoreResult(score, "MEDIUM", "Moyen", reasons, LocalDateTime.now());
        }
        return new DynamicTurnoverScoreResult(score, "LOW", "Faible", reasons, LocalDateTime.now());
    }

    @Override
    public DynamicTurnoverScoreResult calculateAndPersistForEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));

        DynamicTurnoverScoreResult result = calculateScore(employee);

        EmployeeRiskScore scoreEntity = new EmployeeRiskScore();
        scoreEntity.setEmployee(employee);
        scoreEntity.setScore(result.getScore());
        scoreEntity.setRiskLevel(result.getRiskLevel());
        scoreEntity.setRiskLabel(result.getRiskLabel());
        scoreEntity.setCalculatedAt(LocalDateTime.now());
        scoreEntity.setReasons(String.join(";", result.getReasons()));

        employeeRiskScoreRepository.save(scoreEntity);
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
