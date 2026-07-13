package com.ooredoo.turnover.service;

import com.ooredoo.turnover.dto.UserDTO;
import java.util.List;
import java.util.Optional;

public interface UserService {

    UserDTO save(UserDTO userDTO, String rawPassword);

    UserDTO update(UserDTO userDTO);

    Optional<UserDTO> findById(Long id);

    Optional<UserDTO> findByUsername(String username);

    List<UserDTO> findAll();

    void deleteById(Long id);

    boolean existsByUsername(String username);
}