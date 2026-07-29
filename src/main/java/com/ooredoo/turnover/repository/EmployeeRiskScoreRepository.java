package com.ooredoo.turnover.repository;

import com.ooredoo.turnover.entity.Employee;
import com.ooredoo.turnover.entity.EmployeeRiskScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeRiskScoreRepository extends JpaRepository<EmployeeRiskScore, Long> {

    List<EmployeeRiskScore> findByEmployeeOrderByCalculatedAtDesc(Employee employee);
}
