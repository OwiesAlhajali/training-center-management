package com.trainingcenter.management.service;

import com.trainingcenter.management.dto.InstituteRequestDTO;
import com.trainingcenter.management.dto.InstituteResponseDTO;
import com.trainingcenter.management.dto.MonthlyFinancialPerformanceDTO;
import com.trainingcenter.management.dto.MonthlyRegistrationStatDTO;
import com.trainingcenter.management.dto.StudentResponseDTO;
import com.trainingcenter.management.entity.Institute;
import com.trainingcenter.management.entity.Student;
import com.trainingcenter.management.entity.Tenant;
import com.trainingcenter.management.entity.User;
import com.trainingcenter.management.entity.InstituteStatus;
import com.trainingcenter.management.entity.SessionStatus;  
import com.trainingcenter.management.exception.BadRequestException;
import com.trainingcenter.management.exception.ResourceNotFoundException;
import com.trainingcenter.management.repository.EnrollmentRepository;
import com.trainingcenter.management.repository.InstituteRepository;
import com.trainingcenter.management.repository.PaymentRepository;
import com.trainingcenter.management.repository.TenantRepository;
import com.trainingcenter.management.repository.UserRepository;
import com.trainingcenter.management.repository.RegisterRepository;
import com.trainingcenter.management.repository.TrainingSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstituteService {

    

    private final InstituteRepository instituteRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final RegisterRepository registerRepository;
    private final PaymentRepository paymentRepository;
    private final TrainingSessionRepository sessionRepository;

    @Transactional
    public InstituteResponseDTO createInstitute(InstituteRequestDTO requestDTO) {
       User user = userRepository.findById(requestDTO.getUserId())
               .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + requestDTO.getUserId()));

   
       Tenant tenant = new Tenant();
    
 
       String instituteName = requestDTO.getName().trim();
       String[] words = instituteName.split("\\s+");
       String tenantName = (words.length >= 2) ? words[0] + " " + words[1]  : instituteName;

    tenant.setKey(generateUniqueTenantKey());
       tenant.setName(tenantName);
       tenant.setAddress(requestDTO.getLocation());  

 
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
           .status(requestDTO.getStatus() == null ? InstituteStatus.ACTIVE : requestDTO.getStatus())
           .user(user)
           .tenant(savedTenant)         
           .build();

       return mapToResponse(instituteRepository.save(institute));
   }

    private String generateUniqueTenantKey() {
        String key;
        do {
            key = "TC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (tenantRepository.existsByKey(key));
        return key;
    }

  @Transactional(readOnly = true)
  public List<InstituteResponseDTO> getInstitutesByUser(Long userId) {
       if (!userRepository.existsById(userId)) {
           throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
    
        return instituteRepository.findByUserId(userId).stream()
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
    
    @Transactional(readOnly = true)
    public Long getStudentsCountByTenant(Long tenantId) {

          if (!tenantRepository.existsById(tenantId)) {
                throw new ResourceNotFoundException(
                "Tenant not found with ID: " + tenantId);
           }

         return registerRepository.countDistinctByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public List<StudentResponseDTO> getStudentsByTenant(Long tenantId) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new ResourceNotFoundException("Tenant not found with ID: " + tenantId);
        }

        return registerRepository.findByTenantId(tenantId).stream()
                .map(register -> mapStudentToResponse(register.getStudent()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StudentResponseDTO> getActiveStudentsByTenant(Long tenantId) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new ResourceNotFoundException("Tenant not found with ID: " + tenantId);
        }

        return enrollmentRepository.findActiveStudentsByTenantId(tenantId).stream()
                .map(this::mapStudentToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Long getActiveStudentsCountByTenant(Long tenantId) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new ResourceNotFoundException("Tenant not found with ID: " + tenantId);
        }

        return enrollmentRepository.countActiveStudentsByTenantId(tenantId);
    }

    @Transactional
    public InstituteResponseDTO updateInstitute(Long id, InstituteRequestDTO requestDTO) {
        Institute existing = instituteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Institute not found with ID: " + id));

        User user = userRepository.findById(requestDTO.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + requestDTO.getUserId()));

        Tenant tenant = tenantRepository.findById(requestDTO.getTenantId())
            .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with ID: " + requestDTO.getTenantId()));

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

    @Transactional(readOnly = true)
    public long getActiveTrainingSessionsCountByInstitute(Long instituteId) {
       if (!instituteRepository.existsById(instituteId)) {
           throw new ResourceNotFoundException("Institute not found with ID: " + instituteId);
       }
    
       return sessionRepository.countActiveSessionsByInstitute(instituteId, SessionStatus.ACTIVE);
    }
    
    @Transactional(readOnly = true)
    public long getTotalUsersCountByInstitute(Long instituteId) {
        if (!instituteRepository.existsById(instituteId)) {
            throw new ResourceNotFoundException("Institute not found with ID: " + instituteId);
        }
    
       return instituteRepository.countTotalUsersByInstitute(instituteId);
    }

    @Transactional(readOnly = true)
    public List<MonthlyFinancialPerformanceDTO> getMonthlyFinancialPerformance(Long instituteId, Integer year) {
        if (!instituteRepository.existsById(instituteId)) {
            throw new ResourceNotFoundException("Institute not found with ID: " + instituteId);
        }

        int targetYear = (year == null) ? Year.now().getValue() : year;
        List<Object[]> rows = paymentRepository.getMonthlyFinancialPerformanceByInstituteAndYear(instituteId, targetYear);

        Map<Integer, MonthlyFinancialPerformanceDTO> performanceByMonth = new HashMap<>();
        for (Object[] row : rows) {
            Integer month = ((Number) row[0]).intValue();
            Long totalRevenue = ((Number) row[1]).longValue();
            Long totalPayments = ((Number) row[2]).longValue();
            performanceByMonth.put(month, new MonthlyFinancialPerformanceDTO(month, totalRevenue, totalPayments));
        }

        return java.util.stream.IntStream.rangeClosed(1, 12)
                .mapToObj(month -> performanceByMonth.getOrDefault(
                        month,
                        new MonthlyFinancialPerformanceDTO(month, 0L, 0L)))
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
                .build();
    }

    private StudentResponseDTO mapStudentToResponse(Student student) {
        User user = student.getUser();
        return StudentResponseDTO.builder()
                .id(student.getId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .gender(student.getGender())
                .birthDate(student.getBirthDate())
                .enrollmentDate(student.getEnrollmentDate())
                .address(student.getAddress())
                .bio(student.getBio())
                .interest(student.getInterest())
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .contactInfo(user.getContactInfo())
                .image(user.getImage())
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
