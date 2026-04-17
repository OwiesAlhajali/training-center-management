package com.trainingcenter.management.controller;

import com.trainingcenter.management.dto.LoginRequestDTO;
import com.trainingcenter.management.dto.LoginResponseDTO;
import com.trainingcenter.management.entity.User;
import com.trainingcenter.management.exception.ResourceNotFoundException;
import com.trainingcenter.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class LoginController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Simple login endpoint
     * In production, replace with JWT token generation
     * For now, returns user details on successful authentication
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResourceNotFoundException("Invalid credentials");
        }

        LoginResponseDTO response = LoginResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .userType(user.getUserType())
                .message("Login successful")
                .build();

        return ResponseEntity.ok(response);
    }
}

