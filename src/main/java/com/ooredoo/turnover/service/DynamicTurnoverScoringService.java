package com.ooredoo.turnover.service;

import com.ooredoo.turnover.entity.Employee;

import java.util.List;

public interface DynamicTurnoverScoringService {

    DynamicTurnoverScoreResult calculateScore(Employee employee);

    DynamicTurnoverScoreResult calculateAndPersistForEmployee(Long employeeId);

    List<DynamicTurnoverScoreResult> getHistoryForEmployee(Long employeeId);
}
