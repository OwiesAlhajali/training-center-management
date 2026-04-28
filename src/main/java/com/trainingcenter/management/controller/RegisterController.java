/**package com.trainingcenter.management.controller;

import com.trainingcenter.management.dto.RegisterRequestDTO;
import com.trainingcenter.management.dto.RegisterResponseDTO;
import com.trainingcenter.management.service.RegisterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registers")
@RequiredArgsConstructor
public class RegisterController {

    private final RegisterService registerService;

    // Create

    @PostMapping
    public ResponseEntity<RegisterResponseDTO> createRegister(
            @Valid @RequestBody RegisterRequestDTO dto
    ) {
        RegisterResponseDTO response = registerService.createRegister(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Read

    @GetMapping("/{id}")
    public ResponseEntity<RegisterResponseDTO> getRegisterById(@PathVariable Long id) {
        RegisterResponseDTO response = registerService.getById(id);
        return ResponseEntity.ok(response);
    }
}**/
