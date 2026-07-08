package com.ooredoo.turnover.service.impl;

import com.ooredoo.turnover.dto.EmployeeDTO;
import com.ooredoo.turnover.entity.Employee;
import com.ooredoo.turnover.repository.EmployeeRepository;
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

    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public EmployeeDTO save(EmployeeDTO dto) {
        Employee employee = toEntity(dto);
        employee = employeeRepository.save(employee);
        return toDto(employee);
    }

    @Override
    public EmployeeDTO update(EmployeeDTO dto) {
        return save(dto);
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