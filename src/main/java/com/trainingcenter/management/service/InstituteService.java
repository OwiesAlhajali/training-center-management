package com.trainingcenter.management.service;

import com.trainingcenter.management.dto.InstituteRequestDTO;
import com.trainingcenter.management.dto.InstituteResponseDTO;
import com.trainingcenter.management.dto.MonthlyRegistrationStatDTO;
import com.trainingcenter.management.entity.Institute;
import com.trainingcenter.management.entity.Tenant;
import com.trainingcenter.management.entity.User;
import com.trainingcenter.management.exception.BadRequestException;
import com.trainingcenter.management.exception.ResourceNotFoundException;
import com.trainingcenter.management.repository.EnrollmentRepository;
import com.trainingcenter.management.repository.InstituteRepository;
import com.trainingcenter.management.repository.TenantRepository;
import com.trainingcenter.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstituteService {

    private final InstituteRepository instituteRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional
    public InstituteResponseDTO createInstitute(InstituteRequestDTO requestDTO) {
        User user = userRepository.findById(requestDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + requestDTO.getUserId()));

        String generatedKey = requestDTO.getName().toLowerCase().trim().replaceAll("\\s+", "-");

        Tenant tenant = Tenant.builder()
                .key(generatedKey)
                .name(requestDTO.getName())
                .address(requestDTO.getLocation())
                .build();

        Tenant savedTenant = tenantRepository.save(tenant);

        validateWorkingHours(requestDTO.getStartTime(), requestDTO.getEndTime());

        Institute institute = Institute.builder()
                .name(requestDTO.getName())
                .description(requestDTO.getDescription())
                .location(requestDTO.getLocation())
                .phoneNumber(requestDTO.getPhoneNumber())
                .email(requestDTO.getEmail())
                .workingDays(requestDTO.getWorkingDays() == null ? List.of() : requestDTO.getWorkingDays())
                .startTime(requestDTO.getStartTime())
                .endTime(requestDTO.getEndTime())
                .status(requestDTO.getStatus() == null ? com.trainingcenter.management.entity.InstituteStatus.ACTIVE : requestDTO.getStatus())
                .user(user)
                .tenant(savedTenant)
                .build();

        return mapToResponse(instituteRepository.save(institute));
    }

    @Transactional
    public InstituteResponseDTO updateInstitute(Long id, InstituteRequestDTO requestDTO) {
        Institute existing = instituteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Institute not found with ID: " + id));

        User user = userRepository.findById(requestDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + requestDTO.getUserId()));

        validateWorkingHours(requestDTO.getStartTime(), requestDTO.getEndTime());

        existing.setName(requestDTO.getName());
        existing.setDescription(requestDTO.getDescription());
        existing.setLocation(requestDTO.getLocation());
        existing.setPhoneNumber(requestDTO.getPhoneNumber());
        existing.setEmail(requestDTO.getEmail());
        existing.setWorkingDays(requestDTO.getWorkingDays() == null ? List.of() : requestDTO.getWorkingDays());
        existing.setStartTime(requestDTO.getStartTime());
        existing.setEndTime(requestDTO.getEndTime());
        existing.setStatus(requestDTO.getStatus() == null ? com.trainingcenter.management.entity.InstituteStatus.ACTIVE : requestDTO.getStatus());
        existing.setUser(user);

        if (existing.getTenant() != null) {
            existing.getTenant().setName(requestDTO.getName());
            existing.getTenant().setAddress(requestDTO.getLocation());
        }

        return mapToResponse(instituteRepository.save(existing));
    }

    @Transactional(readOnly = true)
    public List<InstituteResponseDTO> getInstitutesByTenant(Long tenantId) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new ResourceNotFoundException("Tenant not found");
        }
        return instituteRepository.findByTenantId(tenantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InstituteResponseDTO getInstituteById(Long id) {
        Institute institute = instituteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Institute not found"));
        return mapToResponse(institute);
    }

    @Transactional(readOnly = true)
    public List<InstituteResponseDTO> getAllInstitutes() {
        return instituteRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteInstitute(Long id) {
        if (!instituteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Institute not found");
        }
        instituteRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<MonthlyRegistrationStatDTO> getMonthlyRegistrations(Long instituteId, Integer year) {
        if (!instituteRepository.existsById(instituteId)) {
            throw new ResourceNotFoundException("Institute not found with ID: " + instituteId);
        }

        int targetYear = (year == null) ? Year.now().getValue() : year;
        List<Object[]> rows = enrollmentRepository.getMonthlyRegistrationsByInstituteAndYear(instituteId, targetYear);

        Map<Integer, Long> countsByMonth = new HashMap<>();
        for (Object[] row : rows) {
            countsByMonth.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }

        return java.util.stream.IntStream.rangeClosed(1, 12)
                .mapToObj(month -> MonthlyRegistrationStatDTO.builder()
                        .month(month)
                        .registrations(countsByMonth.getOrDefault(month, 0L))
                        .build())
                .toList();
    }

    private InstituteResponseDTO mapToResponse(Institute institute) {
        return InstituteResponseDTO.builder()
                .id(institute.getId())
                .name(institute.getName())
                .startTime(institute.getStartTime())
                .endTime(institute.getEndTime())
                .description(institute.getDescription())
                .location(institute.getLocation())
                .phoneNumber(institute.getPhoneNumber())
                .email(institute.getEmail())
                .workingDays(institute.getWorkingDays())
                .status(institute.getStatus() == null ? com.trainingcenter.management.entity.InstituteStatus.ACTIVE : institute.getStatus())
                .userId(institute.getUser() != null ? institute.getUser().getId() : null)
                .ownerName(institute.getUser() != null ? institute.getUser().getUsername() : "No Owner")
                .tenantId(institute.getTenant() != null ? institute.getTenant().getId() : null)
                .tenantName(institute.getTenant() != null ? institute.getTenant().getName() : "No Tenant")
                .tenantKey(institute.getTenant() != null ? institute.getTenant().getKey() : null)
                .build();
    }

    private void validateWorkingHours(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            throw new BadRequestException("Start time and end time are required");
        }
        if (!endTime.isAfter(startTime)) {
            throw new BadRequestException("End time must be after start time");
        }
    }
}