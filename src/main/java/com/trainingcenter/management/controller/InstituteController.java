package com.trainingcenter.management.controller;

import com.trainingcenter.management.dto.InstituteRequestDTO;
import com.trainingcenter.management.dto.InstituteResponseDTO;
import com.trainingcenter.management.service.InstituteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/institutes")
@RequiredArgsConstructor
public class InstituteController { 

    private final InstituteService instituteService;

    @PostMapping
    public ResponseEntity<InstituteResponseDTO> create(@Valid @RequestBody InstituteRequestDTO request) {
        return new ResponseEntity<>(instituteService.createInstitute(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InstituteResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(instituteService.getInstituteById(id));
    }

    @GetMapping
    public ResponseEntity<List<InstituteResponseDTO>> getAll() {
        return ResponseEntity.ok(instituteService.getAllInstitutes());
    }

    @PutMapping("/{id}")
    public ResponseEntity<InstituteResponseDTO> update(@PathVariable Long id, @Valid @RequestBody InstituteRequestDTO request) {
        return ResponseEntity.ok(instituteService.updateInstitute(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        instituteService.deleteInstitute(id);
        return ResponseEntity.noContent().build();
    }
}

