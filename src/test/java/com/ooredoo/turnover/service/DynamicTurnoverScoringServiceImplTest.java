package com.ooredoo.turnover.service;

import com.ooredoo.turnover.config.DynamicScoringProperties;
import com.ooredoo.turnover.entity.Employee;
import com.ooredoo.turnover.repository.EmployeeRepository;
import com.ooredoo.turnover.repository.EmployeeRiskScoreRepository;
import com.ooredoo.turnover.entity.EmployeeRiskScore;
import com.ooredoo.turnover.service.impl.DynamicTurnoverScoringServiceImpl;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DynamicTurnoverScoringServiceImplTest {

    @Test
    void shouldReturnHighRiskWhenEmployeeHasStrongTurnoverSignals() {
        Employee employee = new Employee();
        employee.setOvertime(true);
        employee.setMonthlyIncome(2100.0);
        employee.setYearsAtCompany(0);
        employee.setTotalWorkingYears(1);
        employee.setYearsInCurrentRole(0);
        employee.setYearsWithCurrManager(0);
        employee.setYearsSinceLastPromotion(0);
        employee.setStockOptionLevel(0);
        employee.setJobSatisfaction(1);
        employee.setEnvironmentSatisfaction(1);
        employee.setDistanceFromHome(27);
        employee.setNumCompaniesWorked(6);

        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        EmployeeRiskScoreRepository scoreRepository = mock(EmployeeRiskScoreRepository.class);

        DynamicScoringProperties properties = new DynamicScoringProperties();
        DynamicTurnoverScoringServiceImpl service = new DynamicTurnoverScoringServiceImpl(employeeRepository, scoreRepository, properties);
        DynamicTurnoverScoreResult result = service.calculateScore(employee);

        assertTrue(result.getScore() >= 50);
        assertEquals("HIGH", result.getRiskLevel());
    }

    @Test
    void shouldUseCustomWeightsWhenProvided() {
        Employee employee = new Employee();
        employee.setYearsAtCompany(0);
        employee.setNumCompaniesWorked(0);
        employee.setOvertime(false);
        employee.setMonthlyIncome(5000.0);
        employee.setJobSatisfaction(3);
        employee.setEnvironmentSatisfaction(3);
        employee.setYearsSinceLastPromotion(1);
        employee.setStockOptionLevel(1);

        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        EmployeeRiskScoreRepository scoreRepository = mock(EmployeeRiskScoreRepository.class);

        DynamicScoringProperties properties = new DynamicScoringProperties();
        properties.setShortTenureWeight(50);
        properties.setManyCompaniesWeight(0);
        properties.setOvertimeWeight(0);
        properties.setLowSalaryWeight(0);
        properties.setLowJobSatisfactionWeight(0);
        properties.setLowEnvironmentSatisfactionWeight(0);
        properties.setNoRecentPromotionWeight(0);
        properties.setLowStockOptionWeight(0);

        DynamicTurnoverScoringServiceImpl service = new DynamicTurnoverScoringServiceImpl(employeeRepository, scoreRepository, properties);
        DynamicTurnoverScoreResult result = service.calculateScore(employee);

        assertEquals(50, result.getScore());
        assertEquals("MEDIUM", result.getRiskLevel());
    }

    @Test
    void shouldReturnStoredReasonsInHistory() {
        Employee employee = new Employee();
        employee.setId(1L);

        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        EmployeeRiskScoreRepository scoreRepository = mock(EmployeeRiskScoreRepository.class);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        EmployeeRiskScore scoreEntity = new EmployeeRiskScore();
        scoreEntity.setScore(45);
        scoreEntity.setRiskLevel("MEDIUM");
        scoreEntity.setRiskLabel("Moyen");
        scoreEntity.setCalculatedAt(LocalDateTime.now());
        scoreEntity.setReasons("ancienneté faible;heures supplémentaires");

        when(scoreRepository.findByEmployeeOrderByCalculatedAtDesc(employee)).thenReturn(List.of(scoreEntity));

        DynamicTurnoverScoringServiceImpl service = new DynamicTurnoverScoringServiceImpl(employeeRepository, scoreRepository, new DynamicScoringProperties());
        List<DynamicTurnoverScoreResult> history = service.getHistoryForEmployee(1L);

        assertEquals(1, history.size());
        assertEquals(List.of("ancienneté faible", "heures supplémentaires"), history.get(0).getReasons());
    }
}
