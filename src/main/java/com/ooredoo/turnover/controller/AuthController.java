package com.ooredoo.turnover.controller;

import com.ooredoo.turnover.dto.LoginRequest;
import com.ooredoo.turnover.dto.LoginResponse;
import com.ooredoo.turnover.dto.UserDTO;
import com.ooredoo.turnover.security.JwtTokenProvider;
import com.ooredoo.turnover.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Validated @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
                String token = tokenProvider.generateToken(authentication);
                UserDTO userDTO = userService.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable après authentification."));
                String normalizedRole = userDTO.getRoleName() != null ? userDTO.getRoleName().trim().toUpperCase() : null;
                LoginResponse response = new LoginResponse(token, userDTO.getUsername(), userDTO.getEmail(), normalizedRole);
                return ResponseEntity.ok()
                    .header("Authorization", "Bearer " + token)
                    .body(response);
        } catch (AuthenticationException ex) {
            return ResponseEntity.badRequest().body("Nom d'utilisateur ou mot de passe invalide.");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(Principal principal) {
        return userService.findByUsername(principal.getName())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
