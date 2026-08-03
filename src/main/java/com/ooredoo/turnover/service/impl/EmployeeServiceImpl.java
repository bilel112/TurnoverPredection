package com.ooredoo.turnover.service.impl;

import com.ooredoo.turnover.dto.EmployeeDTO;
import com.ooredoo.turnover.entity.Employee;
import com.ooredoo.turnover.repository.EmployeeRepository;
import com.ooredoo.turnover.service.DynamicTurnoverScoringService;
import com.ooredoo.turnover.service.EmployeeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DynamicTurnoverScoringService scoringService;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, DynamicTurnoverScoringService scoringService) {
        this.employeeRepository = employeeRepository;
        this.scoringService = scoringService;
    }

    @Override
    public EmployeeDTO save(EmployeeDTO dto) {
        Employee employee = toEntity(dto);
        employee = employeeRepository.save(employee);
        // After creating a new employee, trigger initial scoring
        if (scoringService != null) {
            try {
                scoringService.calculateAndPersistForEmployee(employee.getId());
            } catch (Exception ex) {
                System.err.println("Failed to run initial scoring for employee " + employee.getId() + ": " + ex.getMessage());
            }
        }
        return toDto(employee);
    }

    @Override
    public EmployeeDTO update(EmployeeDTO dto) {
        if (dto.getId() == null) return save(dto);

        var opt = employeeRepository.findById(dto.getId());
        if (opt.isEmpty()) {
            // create new if not exists
            return save(dto);
        }

        Employee existing = opt.get();
        Employee updatedEntity = toEntity(dto);

        boolean significant = hasSignificantChange(existing, updatedEntity);

        // copy updated fields into existing (only fields mapped in toEntity)
        existing.setEmployeeNumber(updatedEntity.getEmployeeNumber());
        existing.setAge(updatedEntity.getAge());
        existing.setDepartment(updatedEntity.getDepartment());
        existing.setJobRole(updatedEntity.getJobRole());
        existing.setMonthlyIncome(updatedEntity.getMonthlyIncome());
        existing.setYearsAtCompany(updatedEntity.getYearsAtCompany());
        existing.setDistanceFromHome(updatedEntity.getDistanceFromHome());
        existing.setOvertime(updatedEntity.getOvertime());
        existing.setJobSatisfaction(updatedEntity.getJobSatisfaction());
        existing.setEnvironmentSatisfaction(updatedEntity.getEnvironmentSatisfaction());
        existing.setAttrition(updatedEntity.getAttrition());
        existing.setBusinessTravel(updatedEntity.getBusinessTravel());
        existing.setMaritalStatus(updatedEntity.getMaritalStatus());
        existing.setJobLevel(updatedEntity.getJobLevel());
        existing.setTotalWorkingYears(updatedEntity.getTotalWorkingYears());
        existing.setYearsInCurrentRole(updatedEntity.getYearsInCurrentRole());
        existing.setYearsWithCurrManager(updatedEntity.getYearsWithCurrManager());
        existing.setYearsSinceLastPromotion(updatedEntity.getYearsSinceLastPromotion());
        existing.setStockOptionLevel(updatedEntity.getStockOptionLevel());
        existing.setNumCompaniesWorked(updatedEntity.getNumCompaniesWorked());
        existing.setTrainingTimesLastYear(updatedEntity.getTrainingTimesLastYear());
        existing.setWorkLifeBalance(updatedEntity.getWorkLifeBalance());
        existing.setEducationField(updatedEntity.getEducationField());
        existing.setEducation(updatedEntity.getEducation());
        existing.setGender(updatedEntity.getGender());
        existing.setDailyRate(updatedEntity.getDailyRate());
        existing.setHourlyRate(updatedEntity.getHourlyRate());
        existing.setPercentSalaryHike(updatedEntity.getPercentSalaryHike());
        existing.setPerformanceRating(updatedEntity.getPerformanceRating());

        Employee saved = employeeRepository.save(existing);

        if (significant && scoringService != null) {
            try {
                scoringService.calculateAndPersistForEmployee(saved.getId());
            } catch (Exception ex) {
                System.err.println("Failed to calculate scoring after employee update " + saved.getId() + ": " + ex.getMessage());
            }
        }

        return toDto(saved);
    }

    private boolean hasSignificantChange(Employee before, Employee after) {
        if (before == null || after == null) return true;
        // fields considered by scoring
        if (!equalsInt(before.getYearsAtCompany(), after.getYearsAtCompany())) return true;
        if (!equalsInt(before.getNumCompaniesWorked(), after.getNumCompaniesWorked())) return true;
        if (!equalsBool(before.getOvertime(), after.getOvertime())) return true;
        if (!equalsDouble(before.getMonthlyIncome(), after.getMonthlyIncome())) return true;
        if (!equalsInt(before.getJobSatisfaction(), after.getJobSatisfaction())) return true;
        if (!equalsInt(before.getEnvironmentSatisfaction(), after.getEnvironmentSatisfaction())) return true;
        if (!equalsInt(before.getYearsSinceLastPromotion(), after.getYearsSinceLastPromotion())) return true;
        if (!equalsInt(before.getStockOptionLevel(), after.getStockOptionLevel())) return true;
        return false;
    }

    private boolean equalsInt(Integer a, Integer b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.intValue() == b.intValue();
    }

    private boolean equalsDouble(Double a, Double b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return Double.compare(a, b) == 0;
    }

    private boolean equalsBool(Boolean a, Boolean b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.booleanValue() == b.booleanValue();
    }

    @Override
    public Optional<EmployeeDTO> findById(Long id) {
        return employeeRepository.findById(id).map(this::toDto);
    }

    @Override
    public List<EmployeeDTO> findAll() {
        return employeeRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<EmployeeDTO> findAllPaginated(Pageable pageable) {
        return employeeRepository.findAll(pageable).map(this::toDto);
    }

    @Override
    public void deleteById(Long id) {
        employeeRepository.deleteById(id);
    }

    @Override
    public long count() {
        return employeeRepository.count();
    }

    // ==================== Mappers ====================

    private Employee toEntity(EmployeeDTO dto) {
        Employee e = new Employee();
        e.setId(dto.getId());
        e.setEmployeeNumber(dto.getEmployeeNumber());
        e.setAge(dto.getAge());
        e.setDepartment(dto.getDepartment());
        e.setJobRole(dto.getJobRole());
        e.setMonthlyIncome(dto.getMonthlyIncome());
        e.setYearsAtCompany(dto.getYearsAtCompany());
        e.setDistanceFromHome(dto.getDistanceFromHome());
        e.setOvertime(dto.getOvertime());
        e.setJobSatisfaction(dto.getJobSatisfaction());
        e.setEnvironmentSatisfaction(dto.getEnvironmentSatisfaction());
        e.setAttrition(dto.getAttrition());

        e.setBusinessTravel(dto.getBusinessTravel());
        e.setMaritalStatus(dto.getMaritalStatus());
        e.setJobLevel(dto.getJobLevel());
        e.setTotalWorkingYears(dto.getTotalWorkingYears());
        e.setYearsInCurrentRole(dto.getYearsInCurrentRole());
        e.setYearsWithCurrManager(dto.getYearsWithCurrManager());
        e.setYearsSinceLastPromotion(dto.getYearsSinceLastPromotion());
        e.setStockOptionLevel(dto.getStockOptionLevel());
        e.setNumCompaniesWorked(dto.getNumCompaniesWorked());
        e.setTrainingTimesLastYear(dto.getTrainingTimesLastYear());
        e.setWorkLifeBalance(dto.getWorkLifeBalance());
        e.setEducationField(dto.getEducationField());
        e.setEducation(dto.getEducation());
        e.setGender(dto.getGender());

        e.setDailyRate(dto.getDailyRate());
        e.setHourlyRate(dto.getHourlyRate());
        e.setPercentSalaryHike(dto.getPercentSalaryHike());
        e.setPerformanceRating(dto.getPerformanceRating());

        return e;
    }

    private EmployeeDTO toDto(Employee e) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(e.getId());
        dto.setEmployeeNumber(e.getEmployeeNumber());
        dto.setAge(e.getAge());
        dto.setDepartment(e.getDepartment());
        dto.setJobRole(e.getJobRole());
        dto.setMonthlyIncome(e.getMonthlyIncome());
        dto.setYearsAtCompany(e.getYearsAtCompany());
        dto.setDistanceFromHome(e.getDistanceFromHome());
        dto.setOvertime(e.getOvertime());
        dto.setJobSatisfaction(e.getJobSatisfaction());
        dto.setEnvironmentSatisfaction(e.getEnvironmentSatisfaction());
        dto.setAttrition(e.getAttrition());

        dto.setBusinessTravel(e.getBusinessTravel());
        dto.setMaritalStatus(e.getMaritalStatus());
        dto.setJobLevel(e.getJobLevel());
        dto.setTotalWorkingYears(e.getTotalWorkingYears());
        dto.setYearsInCurrentRole(e.getYearsInCurrentRole());
        dto.setYearsWithCurrManager(e.getYearsWithCurrManager());
        dto.setYearsSinceLastPromotion(e.getYearsSinceLastPromotion());
        dto.setStockOptionLevel(e.getStockOptionLevel());
        dto.setNumCompaniesWorked(e.getNumCompaniesWorked());
        dto.setTrainingTimesLastYear(e.getTrainingTimesLastYear());
        dto.setWorkLifeBalance(e.getWorkLifeBalance());
        dto.setEducationField(e.getEducationField());
        dto.setEducation(e.getEducation());
        dto.setGender(e.getGender());

        dto.setDailyRate(e.getDailyRate());
        dto.setHourlyRate(e.getHourlyRate());
        dto.setPercentSalaryHike(e.getPercentSalaryHike());
        dto.setPerformanceRating(e.getPerformanceRating());

        return dto;
    }
}