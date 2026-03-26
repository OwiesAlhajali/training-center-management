package com.trainingcenter.management.service;

import com.trainingcenter.management.dto.InstituteRequestDTO;
import com.trainingcenter.management.dto.InstituteResponseDTO;
import com.trainingcenter.management.entity.Institute;
import com.trainingcenter.management.entity.Tenant;
import com.trainingcenter.management.entity.User;
import com.trainingcenter.management.exception.ResourceNotFoundException;
import com.trainingcenter.management.repository.InstituteRepository;
import com.trainingcenter.management.repository.TenantRepository;
import com.trainingcenter.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstituteService {

    private final InstituteRepository instituteRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository; 

    public InstituteResponseDTO createInstitute(InstituteRequestDTO requestDTO) {
        User user = userRepository.findById(requestDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + requestDTO.getUserId()));

        Tenant tenant = tenantRepository.findById(requestDTO.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with ID: " + requestDTO.getTenantId()));

        Institute institute = Institute.builder()
	 	.name(requestDTO.getName())
                .workingHours(requestDTO.getWorkingHours())
                .description(requestDTO.getDescription())
                .location(requestDTO.getLocation())
                .user(user)
                .tenant(tenant)
                .build();

        return mapToResponse(instituteRepository.save(institute));
    }

    public List<InstituteResponseDTO> getInstitutesByTenant(Long tenantId) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new ResourceNotFoundException("Tenant not found");
        }
        return instituteRepository.findByTenantId(tenantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public InstituteResponseDTO getInstituteById(Long id) {
        Institute institute = instituteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Institute not found"));
        return mapToResponse(institute);
    }

    public List<InstituteResponseDTO> getAllInstitutes() {
        return instituteRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public InstituteResponseDTO updateInstitute(Long id, InstituteRequestDTO requestDTO) {
        Institute existing = instituteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Institute not found with ID: " + id));

	existing.setName(requestDTO.getName());
        existing.setWorkingHours(requestDTO.getWorkingHours());
        existing.setDescription(requestDTO.getDescription());
        existing.setLocation(requestDTO.getLocation());

        return mapToResponse(instituteRepository.save(existing));
    }

    public void deleteInstitute(Long id) {
        if (!instituteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Institute not found");
        }
        instituteRepository.deleteById(id);
    }

    private InstituteResponseDTO mapToResponse(Institute institute) {
        return InstituteResponseDTO.builder()
                .id(institute.getId())
		.name(institute.getName())
                .workingHours(institute.getWorkingHours())
                .description(institute.getDescription())
                .location(institute.getLocation())
                .ownerName(institute.getUser() != null ? institute.getUser().getUsername() : "No Owner")
                .tenantName(institute.getTenant() != null ? institute.getTenant().getName() : "No Tenant")
                .build();
    }
}
