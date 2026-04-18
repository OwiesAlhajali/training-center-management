package com.trainingcenter.management.service;

import com.trainingcenter.management.dto.TenantRequestDTO;
import com.trainingcenter.management.dto.TenantResponseDTO;
import com.trainingcenter.management.entity.Tenant;
import com.trainingcenter.management.exception.DuplicateResourceException;
import com.trainingcenter.management.exception.ResourceNotFoundException;
import com.trainingcenter.management.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;

    public TenantResponseDTO createTenant(TenantRequestDTO requestDTO) {
        if (tenantRepository.existsByKey(requestDTO.getKey())) {
            throw new DuplicateResourceException("Tenant key already exists!");
        }

        Tenant tenant = Tenant.builder()
                .key(requestDTO.getKey())
                .name(requestDTO.getName())
                .address(requestDTO.getAddress())
                .build();

        return mapToResponse(tenantRepository.save(tenant));
    }

    public TenantResponseDTO getTenantById(Long id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with ID: " + id));
        return mapToResponse(tenant);
    }

    public List<TenantResponseDTO> getAllTenants() {
        return tenantRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TenantResponseDTO updateTenant(Long id, TenantRequestDTO requestDTO) {
        Tenant existingTenant = tenantRepository.findById(id)
             .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with ID: " + id));

        // update information
        existingTenant.setName(requestDTO.getName());
        existingTenant.setAddress(requestDTO.getAddress());
        //not update key because uniqe

        return mapToResponse(tenantRepository.save(existingTenant));
    }

    public void deleteTenant(Long id) {
        if (!tenantRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tenant not found with ID: " + id);
        }
        tenantRepository.deleteById(id);
    }

    // map Entity to DTO
    private TenantResponseDTO mapToResponse(Tenant tenant) {
        return TenantResponseDTO.builder()
                .id(tenant.getId())
                .key(tenant.getKey())
                .name(tenant.getName())
                .address(tenant.getAddress())
                .build();
    }
}
