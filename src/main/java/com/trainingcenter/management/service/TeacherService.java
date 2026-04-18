package com.trainingcenter.management.service;

import com.trainingcenter.management.dto.TeacherRequestDTO;
import com.trainingcenter.management.dto.TeacherResponseDTO;
import com.trainingcenter.management.dto.TeacherCourseProgressDTO;
import com.trainingcenter.management.dto.WeeklyScheduleItemDTO;
import com.trainingcenter.management.entity.Lecture;
import com.trainingcenter.management.entity.SessionStatus;
import com.trainingcenter.management.entity.Teacher;
import com.trainingcenter.management.entity.User;
import com.trainingcenter.management.exception.BadRequestException;
import com.trainingcenter.management.exception.DuplicateResourceException;
import com.trainingcenter.management.exception.ResourceNotFoundException;
import com.trainingcenter.management.repository.LectureRepository;
import com.trainingcenter.management.repository.TeacherRepository;
import com.trainingcenter.management.repository.TrainingSessionRepository;
import com.trainingcenter.management.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TrainingSessionRepository trainingSessionRepository;
    private final LectureRepository lectureRepository;

    @Transactional
    public TeacherResponseDTO createTeacher(TeacherRequestDTO requestDTO) {

        User user;

        // New unified flow: create account + teacher profile from one request.
        if (requestDTO.getUserId() == null) {
            if (isBlank(requestDTO.getUsername()) || isBlank(requestDTO.getEmail())
                    || isBlank(requestDTO.getPassword()) || isBlank(requestDTO.getConfirmPassword())) {
                throw new BadRequestException("username, email, password and confirmPassword are required");
            }

            if (!requestDTO.getPassword().equals(requestDTO.getConfirmPassword())) {
                throw new BadRequestException("Password and confirm password do not match");
            }

            if (userRepository.findByUsername(requestDTO.getUsername()).isPresent()) {
                throw new DuplicateResourceException("Username already exists");
            }

            if (userRepository.existsByEmail(requestDTO.getEmail())) {
                throw new DuplicateResourceException("Email already exists");
            }

            user = new User();
            user.setUsername(requestDTO.getUsername());
            user.setEmail(requestDTO.getEmail());
            user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
            user.setUserType(User.UserType.TEACHER);
            user.setContactInfo(requestDTO.getPhone());

            user = userRepository.save(user);
        } else {
            // Backward-compatible flow: attach teacher profile to an existing user.
            user = userRepository.findById(requestDTO.getUserId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("User not found with ID: " + requestDTO.getUserId()));

            if (teacherRepository.existsByUserId(user.getId())) {
                throw new DuplicateResourceException("User is already a teacher with ID: " + user.getId());
            }

            user.setUserType(User.UserType.TEACHER);
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

        return mapToResponse(teacherRepository.save(teacher));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
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

    public List<TeacherCourseProgressDTO> getTeacherCourseProgress(Long teacherId) {
        ensureTeacherExists(teacherId);

        return trainingSessionRepository.getTeacherCourseProgress(
                        teacherId,
                        SessionStatus.COMPLETED,
                        SessionStatus.CANCELLED
                ).stream()
                .map(this::mapToCourseProgress)
                .toList();
    }

    public List<WeeklyScheduleItemDTO> getTeacherWeeklySchedule(Long teacherId) {
        ensureTeacherExists(teacherId);

        LocalDate startOfWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        return lectureRepository.findTeacherWeeklySchedule(teacherId, startOfWeek, endOfWeek).stream()
                .map(this::mapLectureToWeeklySchedule)
                .toList();
    }

    private TeacherCourseProgressDTO mapToCourseProgress(Object[] row) {
        Long completed = ((Number) row[2]).longValue();
        Long total = ((Number) row[3]).longValue();
        double percentage = total == 0 ? 0.0 : Math.round(((completed * 100.0) / total) * 100.0) / 100.0;

        return TeacherCourseProgressDTO.builder()
                .courseId((Long) row[0])
                .courseName((String) row[1])
                .completedSessions(completed)
                .totalSessions(total)
                .progressPercentage(percentage)
                .build();
    }

    private WeeklyScheduleItemDTO mapLectureToWeeklySchedule(Lecture lecture) {
        return WeeklyScheduleItemDTO.builder()
                .day(lecture.getLectureDate().getDayOfWeek().name())
                .courseName(lecture.getTrainingSession().getCourse().getName())
                .startTime(lecture.getStartTime())
                .endTime(lecture.getEndTime())
                .teacherName(lecture.getTeacher().getFirstName() + " " + lecture.getTeacher().getLastName())
                .room(lecture.getClassRoom().getNumber())
                .build();
    }

    private void ensureTeacherExists(Long teacherId) {
        if (!teacherRepository.existsById(teacherId)) {
            throw new ResourceNotFoundException("Teacher not found with ID: " + teacherId);
        }
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