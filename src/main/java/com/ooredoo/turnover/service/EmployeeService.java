package com.ooredoo.turnover.service;

import com.ooredoo.turnover.dto.EmployeeDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface EmployeeService {

    EmployeeDTO save(EmployeeDTO dto);

    EmployeeDTO update(EmployeeDTO dto);

    Optional<EmployeeDTO> findById(Long id);

    List<EmployeeDTO> findAll();

    Page<EmployeeDTO> findAllPaginated(Pageable pageable);

    void deleteById(Long id);

    long count();
}