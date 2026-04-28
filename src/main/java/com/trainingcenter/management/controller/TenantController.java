/**package com.trainingcenter.management.controller;

import com.trainingcenter.management.dto.TenantRequestDTO;
import com.trainingcenter.management.dto.TenantResponseDTO;
import com.trainingcenter.management.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @PostMapping
    public ResponseEntity<TenantResponseDTO> create(@Valid @RequestBody TenantRequestDTO request) {
        return new ResponseEntity<>(tenantService.createTenant(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TenantResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(tenantService.getTenantById(id));
    }

    @GetMapping
    public ResponseEntity<List<TenantResponseDTO>> getAll() {
        return ResponseEntity.ok(tenantService.getAllTenants());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TenantResponseDTO> update(@PathVariable Long id, @Valid @RequestBody TenantRequestDTO request) {
        return ResponseEntity.ok(tenantService.updateTenant(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tenantService.deleteTenant(id);
        return ResponseEntity.noContent().build();
    }
}
**/

