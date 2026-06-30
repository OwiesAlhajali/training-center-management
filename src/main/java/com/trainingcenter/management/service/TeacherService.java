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
import com.trainingcenter.management.repository.EnrollmentRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
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
    private final EnrollmentRepository enrollmentRepository;
    private final ImageService imageService;

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


    @Transactional(readOnly = true)
    public TeacherResponseDTO getTeacherById(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Teacher not found with ID: " + id));

        return mapToResponse(teacher);
    }

    @Transactional(readOnly = true)
    public List<TeacherResponseDTO> getAllTeachers() {
        return teacherRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TeacherResponseDTO> searchTeachers(String keyword) {
        return teacherRepository.searchByUsernameOrName(keyword)
                .stream()
                .map(this::mapToResponse)
                .toList();
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

    /**
     * Uploads a teacher profile image and stores the Cloudinary URL in User.image.
     */
    @Transactional
    public TeacherResponseDTO updateProfileImage(Long id, MultipartFile file) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + id));

        User user = teacher.getUser();

        String imageUrl = imageService.uploadImage(file);
        user.setImage(imageUrl);
        userRepository.save(user);

        return mapToResponse(teacher);
    }

    @Transactional(readOnly = true)
    public List<TeacherCourseProgressDTO> getTeacherCourseProgress(Long teacherId) {
        ensureTeacherExists(teacherId);

        List<Object[]> progressRows = trainingSessionRepository.getTeacherCourseProgress(
                teacherId,
                SessionStatus.COMPLETED,
                SessionStatus.CANCELLED
        );

        // get number of students per course for this teacher
        Map<Long, Long> studentCounts = enrollmentRepository.countStudentsByTeacherPerCourse(teacherId).stream()
                .collect(Collectors.toMap(r -> ((Number) r[0]).longValue(), r -> ((Number) r[1]).longValue()));

        return progressRows.stream()
                .map(row -> mapToCourseProgress(row, studentCounts))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WeeklyScheduleItemDTO> getTeacherWeeklySchedule(Long teacherId) {
        ensureTeacherExists(teacherId);

        LocalDate startOfWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        return lectureRepository.findTeacherWeeklySchedule(teacherId, startOfWeek, endOfWeek).stream()
                .map(this::mapLectureToWeeklySchedule)
                .toList();
    }

    private TeacherCourseProgressDTO mapToCourseProgress(Object[] row, Map<Long, Long> studentCounts) {
        Long courseId = ((Number) row[0]).longValue();
        Long completed = ((Number) row[2]).longValue();
        Long total = ((Number) row[3]).longValue();
        double percentage = total == 0 ? 0.0 : Math.round(((completed * 100.0) / total) * 100.0) / 100.0;

        Long students = studentCounts.getOrDefault(courseId, 0L);

        return TeacherCourseProgressDTO.builder()
                .courseId(courseId)
                .courseName((String) row[1])
                .completedSessions(completed)
                .totalSessions(total)
                .progressPercentage(percentage)
                .numberOfStudents(students)
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
        User user = teacher.getUser();
        // compute total number of students across this teacher's courses (sum of per-course counts)
        Long totalStudents = enrollmentRepository.countStudentsByTeacherPerCourse(teacher.getId()).stream()
                .mapToLong(r -> ((Number) r[1]).longValue())
                .sum();

        return TeacherResponseDTO.builder()
                .id(teacher.getId())
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .contactInfo(user.getContactInfo())
                .image(user.getImage())
                .firstName(teacher.getFirstName())
                .lastName(teacher.getLastName())
                .specialization(teacher.getSpecialization())
                .certificates(teacher.getCertificates())
                .address(teacher.getAddress())
                .cv(teacher.getCv())
                .experienceYears(teacher.getExperienceYears())
                .numberOfStudents(totalStudents)
                .build();
    }
}