package com.trainingcenter.management.service;

import com.trainingcenter.management.dto.RegisterRequestDTO;
import com.trainingcenter.management.dto.RegisterResponseDTO;
import com.trainingcenter.management.entity.Register;
import com.trainingcenter.management.entity.Student;
import com.trainingcenter.management.entity.Tenant;
import com.trainingcenter.management.exception.DuplicateResourceException;
import com.trainingcenter.management.exception.ResourceNotFoundException;
import com.trainingcenter.management.repository.RegisterRepository;
import com.trainingcenter.management.repository.StudentRepository;
import com.trainingcenter.management.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class RegisterService {

    private final RegisterRepository registerRepository;
    private final StudentRepository studentRepository;
    private final TenantRepository tenantRepository;

    public RegisterResponseDTO createRegister(RegisterRequestDTO dto) {

        //  Validate Student
        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Student not found with id: " + dto.getStudentId())
                );

        //  Validate Tenant
        Tenant tenant = tenantRepository.findById(dto.getTenantId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Tenant not found with id: " + dto.getTenantId())
                );

        // Check duplicate
        boolean exists = registerRepository
                .existsByStudentIdAndTenantId(dto.getStudentId(), dto.getTenantId());

        if (exists) {
            throw new DuplicateResourceException(
                    "Register already exists for studentId=" + dto.getStudentId()
                            + " and tenantId=" + dto.getTenantId()
            );
        }

        // Create Register
        Register register = Register.builder()
                .student(student)
                .tenant(tenant)
                .balance(BigDecimal.ZERO)
                .build();

        // Save
        Register saved = registerRepository.save(register);

        //Response
        return mapToResponse(saved);
    }
    // read by Id
    public RegisterResponseDTO getById(Long id) {

        Register register = registerRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Register not found with id: " + id)
                );

        return mapToResponse(register);
    }

    // Mapping

    private RegisterResponseDTO mapToResponse(Register register) {
        return RegisterResponseDTO.builder()
                .id(register.getId())
                .studentId(register.getStudent().getId())
                .tenantId(register.getTenant().getId())
                .balance(register.getBalance())
                .build();
    }
}