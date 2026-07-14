package com.ooredoo.turnover.service.impl;

import com.ooredoo.turnover.dto.RoleDTO;
import com.ooredoo.turnover.entity.Role;
import com.ooredoo.turnover.repository.RoleRepository;
import com.ooredoo.turnover.service.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public RoleDTO save(RoleDTO dto) {
        Role role = new Role();
        role.setName(dto.getName().toUpperCase());
        role = roleRepository.save(role);
        return toDto(role);
    }

    @Override
    public Optional<RoleDTO> findById(Long id) {
        return roleRepository.findById(id).map(this::toDto);
    }

    @Override
    public Optional<RoleDTO> findByName(String name) {
        return roleRepository.findByName(name.toUpperCase()).map(this::toDto);
    }

    @Override
    public List<RoleDTO> findAll() {
        return roleRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public void deleteById(Long id) {
        roleRepository.deleteById(id);
    }

    private RoleDTO toDto(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        return dto;
    }
}