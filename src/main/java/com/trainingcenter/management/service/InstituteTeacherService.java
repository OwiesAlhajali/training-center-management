package com.trainingcenter.management.service;

import com.trainingcenter.management.dto.InstituteTeacherRequestDTO;
import com.trainingcenter.management.dto.InstituteTeacherResponseDTO;
import com.trainingcenter.management.entity.Institute;
import com.trainingcenter.management.entity.InstituteTeacher;
import com.trainingcenter.management.entity.Teacher;
import com.trainingcenter.management.entity.TeacherInstituteStatus;
import com.trainingcenter.management.exception.DuplicateResourceException;
import com.trainingcenter.management.exception.ResourceNotFoundException;
import com.trainingcenter.management.repository.InstituteRepository;
import com.trainingcenter.management.repository.InstituteTeacherRepository;
import com.trainingcenter.management.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InstituteTeacherService {

    private final InstituteTeacherRepository instituteTeacherRepository;
    private final InstituteRepository instituteRepository;
    private final TeacherRepository teacherRepository;

    @Transactional
    public InstituteTeacherResponseDTO assignTeacherToInstitute(InstituteTeacherRequestDTO requestDTO) {
        Institute institute = instituteRepository.findById(requestDTO.getInstituteId())
                .orElseThrow(() -> new ResourceNotFoundException("Institute not found with ID: " + requestDTO.getInstituteId()));

        Teacher teacher = teacherRepository.findById(requestDTO.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + requestDTO.getTeacherId()));

        if (instituteTeacherRepository.existsByInstituteIdAndTeacherId(requestDTO.getInstituteId(), requestDTO.getTeacherId())) {
            throw new DuplicateResourceException("Teacher is already assigned to this institute");
        }

        InstituteTeacher instituteTeacher = InstituteTeacher.builder()
                .institute(institute)
                .teacher(teacher)
                .status(requestDTO.getStatus() != null ? requestDTO.getStatus() : TeacherInstituteStatus.ACTIVE)
                .joinedDate(requestDTO.getJoinedDate() != null ? requestDTO.getJoinedDate() : LocalDate.now())
                .build();

        return mapToResponse(instituteTeacherRepository.save(instituteTeacher));
    }

    public List<InstituteTeacherResponseDTO> getTeachersByInstitute(Long instituteId) {
        if (!instituteRepository.existsById(instituteId)) {
            throw new ResourceNotFoundException("Institute not found with ID: " + instituteId);
        }

        return instituteTeacherRepository.findByInstituteId(instituteId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<InstituteTeacherResponseDTO> getActiveTeachersByInstitute(Long instituteId) {
        if (!instituteRepository.existsById(instituteId)) {
            throw new ResourceNotFoundException("Institute not found with ID: " + instituteId);
        }

        return instituteTeacherRepository.findByInstituteIdAndStatus(instituteId, TeacherInstituteStatus.ACTIVE).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<InstituteTeacherResponseDTO> getInstitutesByTeacher(Long teacherId) {
        if (!teacherRepository.existsById(teacherId)) {
            throw new ResourceNotFoundException("Teacher not found with ID: " + teacherId);
        }

        return instituteTeacherRepository.findByTeacherId(teacherId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public InstituteTeacherResponseDTO updateTeacherInstituteStatus(Long instituteId, Long teacherId, TeacherInstituteStatus status) {
        InstituteTeacher instituteTeacher = instituteTeacherRepository.findByInstituteIdAndTeacherId(instituteId, teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found in this institute"));

        instituteTeacher.setStatus(status);
        return mapToResponse(instituteTeacherRepository.save(instituteTeacher));
    }

    @Transactional
    public void removeTeacherFromInstitute(Long instituteId, Long teacherId) {
        InstituteTeacher instituteTeacher = instituteTeacherRepository.findByInstituteIdAndTeacherId(instituteId, teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found in this institute"));

        instituteTeacherRepository.delete(instituteTeacher);
    }

    private InstituteTeacherResponseDTO mapToResponse(InstituteTeacher instituteTeacher) {
        Teacher teacher = instituteTeacher.getTeacher();
        Institute institute = instituteTeacher.getInstitute();

        return InstituteTeacherResponseDTO.builder()
                .id(instituteTeacher.getId())
                .instituteId(institute.getId())
                .instituteName(institute.getName())
                .teacherId(teacher.getId())
                .teacherName(teacher.getFirstName() + " " + teacher.getLastName())
                .teacherUsername(teacher.getUser().getUsername())
                .teacherEmail(teacher.getUser().getEmail())
                .teacherSpecialization(teacher.getSpecialization())
                .teacherPhone(teacher.getUser().getContactInfo())
                .teacherImage(teacher.getUser().getImage())
                .status(instituteTeacher.getStatus())
                .joinedDate(instituteTeacher.getJoinedDate())
                .build();
    }
}
