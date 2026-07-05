// EmployeeRepository.java
package com.ooredoo.turnover.repository;

import com.ooredoo.turnover.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}