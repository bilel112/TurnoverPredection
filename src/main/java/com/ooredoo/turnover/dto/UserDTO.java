package com.ooredoo.turnover.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;
    private String username;
    private String email;
    private String roleName;        // ex: "ADMIN", "HR", "MANAGER"
    private String password;        // Optionnel, utilisé lors de la création/modification
}