package com.ooredoo.turnover.service;

import com.ooredoo.turnover.dto.RoleDTO;
import java.util.List;
import java.util.Optional;

public interface RoleService {

    RoleDTO save(RoleDTO roleDTO);
    Optional<RoleDTO> findById(Long id);
    Optional<RoleDTO> findByName(String name);
    List<RoleDTO> findAll();
    void deleteById(Long id);
}