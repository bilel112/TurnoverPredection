package com.ooredoo.turnover.service.impl;

import com.ooredoo.turnover.dto.UserDTO;
import com.ooredoo.turnover.entity.Role;
import com.ooredoo.turnover.entity.User;
import com.ooredoo.turnover.repository.RoleRepository;
import com.ooredoo.turnover.repository.UserRepository;
import com.ooredoo.turnover.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDTO save(UserDTO dto, String rawPassword) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username déjà utilisé");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(rawPassword));

        // Récupérer le rôle avec normalisation
        String roleName = dto.getRoleName() == null ? "" : dto.getRoleName().trim().toUpperCase();
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé : " + roleName));

        user.setRole(role);

        user = userRepository.save(user);
        return toDto(user);
    }

    @Override
    public UserDTO update(UserDTO dto) {
        User user = userRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id : " + dto.getId()));

        if (!user.getUsername().equals(dto.getUsername()) && userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username déjà utilisé");
        }

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());

        String roleName = dto.getRoleName() == null ? "" : dto.getRoleName().trim().toUpperCase();
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé : " + roleName));
        user.setRole(role);

        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        user = userRepository.save(user);
        return toDto(user);
    }

    @Override
    public Optional<UserDTO> findById(Long id) {
        return userRepository.findById(id).map(this::toDto);
    }

    @Override
    public Optional<UserDTO> findByUsername(String username) {
        return userRepository.findByUsername(username).map(this::toDto);
    }

    @Override
    public List<UserDTO> findAll() {
        return userRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    private UserDTO toDto(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRoleName(user.getRole().getName());
        return dto;
    }
}