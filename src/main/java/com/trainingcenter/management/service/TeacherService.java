package com.trainingcenter.management.service;

import com.trainingcenter.management.dto.TeacherRequestDTO;
import com.trainingcenter.management.dto.TeacherResponseDTO;
import com.trainingcenter.management.entity.Teacher;
import com.trainingcenter.management.entity.User;
import com.trainingcenter.management.exception.DuplicateResourceException;
import com.trainingcenter.management.exception.ResourceNotFoundException;
import com.trainingcenter.management.repository.TeacherRepository;
import com.trainingcenter.management.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;

    public TeacherResponseDTO createTeacher(TeacherRequestDTO requestDTO) {

        User user = userRepository.findById(requestDTO.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with ID: " + requestDTO.getUserId()));

        if (teacherRepository.existsByUserId(user.getId())) {
            throw new DuplicateResourceException ("User is already a teacher with ID: " + user.getId());
        }

        Teacher teacher = Teacher.builder()
                .user(user)
                .firstName(requestDTO.getFirstName())
                .lastName(requestDTO.getLastName())
                .specialization(requestDTO.getSpecialization())
                .certificates(requestDTO.getCertificates())
                .address(requestDTO.getAddress())
                .cv(requestDTO.getCv())
                .experienceYears(requestDTO.getExperienceYears())
                .build();

        user.setUserType(User.UserType.TEACHER);

        return mapToResponse(teacherRepository.save(teacher));
    }


    public TeacherResponseDTO getTeacherById(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Teacher not found with ID: " + id));

        return mapToResponse(teacher);
    }

    public List<TeacherResponseDTO> getAllTeachers() {
        return teacherRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    public TeacherResponseDTO updateTeacher(Long id, TeacherRequestDTO requestDTO) {

        Teacher existing = teacherRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Teacher not found with ID: " + id));

        existing.setFirstName(requestDTO.getFirstName());
        existing.setLastName(requestDTO.getLastName());
        existing.setSpecialization(requestDTO.getSpecialization());
        existing.setCertificates(requestDTO.getCertificates());
        existing.setAddress(requestDTO.getAddress());
        existing.setCv(requestDTO.getCv());
        existing.setExperienceYears(requestDTO.getExperienceYears());

        return mapToResponse(teacherRepository.save(existing));
    }


    public void deleteTeacher(Long id) {

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + id));

        teacherRepository.delete(teacher);
    }


    private TeacherResponseDTO mapToResponse(Teacher teacher) {
        return TeacherResponseDTO.builder()
                .id(teacher.getId())
                .userId(teacher.getUser().getId())
                .firstName(teacher.getFirstName())
                .lastName(teacher.getLastName())
                .specialization(teacher.getSpecialization())
                .certificates(teacher.getCertificates())
                .address(teacher.getAddress())
                .cv(teacher.getCv())
                .experienceYears(teacher.getExperienceYears())
                .build();
    }
}