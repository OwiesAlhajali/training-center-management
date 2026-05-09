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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstituteService {

    private static final DateTimeFormatter RESPONSE_TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");

    private final InstituteRepository instituteRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final EnrollmentRepository enrollmentRepository;

    public InstituteResponseDTO createInstitute(InstituteRequestDTO requestDTO) {
        User user = userRepository.findById(requestDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + requestDTO.getUserId()));

        Tenant tenant = tenantRepository.findById(requestDTO.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with ID: " + requestDTO.getTenantId()));

        LocalTime[] workingHours = parseWorkingHours(requestDTO.getWorkingHours());

        Institute institute = Institute.builder()
            .name(requestDTO.getName())
            .description(requestDTO.getDescription())
            .address(requestDTO.getLocation())
            .phoneNumber(requestDTO.getPhoneNumber())
            .email(requestDTO.getEmail())
            .workingDays(requestDTO.getWorkingDays() == null ? List.of() : requestDTO.getWorkingDays())
            .startTime(workingHours[0])
            .endTime(workingHours[1])
            .status(requestDTO.getStatus() == null ? com.trainingcenter.management.entity.InstituteStatus.ACTIVE : requestDTO.getStatus())
            .user(user)
            .tenant(tenant)
            .build();

        return mapToResponse(instituteRepository.save(institute));
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
        // mapToResponse accesses lazy associations; calling it inside the
        // transactional boundary ensures those properties are initialized
        // before JSON serialization outside the service.
        return mapToResponse(institute);
    }

    @Transactional(readOnly = true)
    public List<InstituteResponseDTO> getAllInstitutes() {
        return instituteRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public InstituteResponseDTO updateInstitute(Long id, InstituteRequestDTO requestDTO) {
        Institute existing = instituteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Institute not found with ID: " + id));

        User user = userRepository.findById(requestDTO.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + requestDTO.getUserId()));

        Tenant tenant = tenantRepository.findById(requestDTO.getTenantId())
            .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with ID: " + requestDTO.getTenantId()));

        LocalTime[] workingHours = parseWorkingHours(requestDTO.getWorkingHours());

        existing.setName(requestDTO.getName());
        existing.setDescription(requestDTO.getDescription());
        existing.setAddress(requestDTO.getLocation());
        existing.setPhoneNumber(requestDTO.getPhoneNumber());
        existing.setEmail(requestDTO.getEmail());
        existing.setWorkingDays(requestDTO.getWorkingDays() == null ? List.of() : requestDTO.getWorkingDays());
        existing.setStartTime(workingHours[0]);
        existing.setEndTime(workingHours[1]);
        existing.setStatus(requestDTO.getStatus() == null ? existing.getStatus() : requestDTO.getStatus());
        existing.setUser(user);
        existing.setTenant(tenant);

        return mapToResponse(instituteRepository.save(existing));
    }

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
                .workingHours((institute.getStartTime() != null && institute.getEndTime() != null)
                    ? RESPONSE_TIME_FORMAT.format(institute.getStartTime()) + " - " + RESPONSE_TIME_FORMAT.format(institute.getEndTime())
                    : null)
                .description(institute.getDescription())
                .location(institute.getAddress())
                .phoneNumber(institute.getPhoneNumber())
                .email(institute.getEmail())
                .workingDays(institute.getWorkingDays())
                .status(institute.getStatus())
                .ownerName(institute.getUser() != null ? institute.getUser().getUsername() : "No Owner")
                .tenantName(institute.getTenant() != null ? institute.getTenant().getName() : "No Tenant")
                .build();
    }

    private LocalTime[] parseWorkingHours(String workingHours) {
        if (workingHours == null || workingHours.isBlank()) {
            throw new BadRequestException("Working hours are required and must follow format HH:mm - HH:mm or hh:mm AM - hh:mm PM");
        }

        String[] parts = workingHours.trim().split("\\s*-\\s*");
        if (parts.length != 2) {
            throw new BadRequestException("Invalid working hours format. Expected: HH:mm - HH:mm or hh:mm AM - hh:mm PM");
        }

        LocalTime start = parseTime(parts[0].trim());
        LocalTime end = parseTime(parts[1].trim());

        if (!end.isAfter(start)) {
            throw new BadRequestException("End time must be after start time");
        }

        return new LocalTime[]{start, end};
    }

    private LocalTime parseTime(String value) {
        List<DateTimeFormatter> acceptedFormats = List.of(
                DateTimeFormatter.ofPattern("HH:mm"),
                DateTimeFormatter.ofPattern("H:mm"),
                DateTimeFormatter.ofPattern("hh:mm a"),
                DateTimeFormatter.ofPattern("h:mm a")
        );

        for (DateTimeFormatter formatter : acceptedFormats) {
            try {
                return LocalTime.parse(value.toUpperCase(), formatter);
            } catch (DateTimeParseException ignored) {
                // Try next accepted pattern.
            }
        }

        throw new BadRequestException("Invalid time value: " + value + ". Accepted formats: HH:mm or hh:mm AM/PM");
    }
}
