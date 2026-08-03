package com.ooredoo.turnover.repository;

import com.ooredoo.turnover.entity.Alert;
import com.ooredoo.turnover.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByStatus(String status);
    List<Alert> findBySeverity(String severity);
    List<Alert> findByStatusAndSeverity(String status, String severity);
    long countByStatus(String status);
    List<Alert> findByEmployeeOrderByCreatedAtDesc(Employee employee);
}
