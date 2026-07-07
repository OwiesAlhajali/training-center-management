package com.trainingcenter.management.service;

import com.trainingcenter.management.dto.TeacherLectureCountDTO;
import com.trainingcenter.management.dto.TeacherRequestDTO;
import com.trainingcenter.management.dto.TeacherResponseDTO;
import com.trainingcenter.management.dto.TeacherCourseProgressDTO;
import com.trainingcenter.management.dto.WeeklyScheduleItemDTO;
import com.trainingcenter.management.entity.Institute;
import com.trainingcenter.management.entity.InstituteTeacher;
import com.trainingcenter.management.entity.Lecture;
import com.trainingcenter.management.entity.SessionStatus;
import com.trainingcenter.management.entity.Teacher;
import com.trainingcenter.management.entity.TrainingSession;
import com.trainingcenter.management.entity.User;
import com.trainingcenter.management.exception.BadRequestException;
import com.trainingcenter.management.exception.DuplicateResourceException;
import com.trainingcenter.management.exception.ResourceNotFoundException;
import com.trainingcenter.management.repository.AttendanceRepository;
import com.trainingcenter.management.repository.LectureRepository;
import com.trainingcenter.management.repository.InstituteRepository;
import com.trainingcenter.management.repository.TeacherRepository;
import com.trainingcenter.management.repository.TrainingSessionRepository;
import com.trainingcenter.management.repository.UserRepository;
import com.trainingcenter.management.repository.EnrollmentRepository;
import com.trainingcenter.management.repository.InstituteTeacherRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
    private final AttendanceRepository attendanceRepository;
    private final ImageService imageService;
    private final InstituteRepository instituteRepository;
    private final InstituteTeacherRepository instituteTeacherRepository;

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

    @Transactional
    public TeacherResponseDTO createTeacherForInstitute(Long instituteId, TeacherRequestDTO requestDTO) {
        TeacherResponseDTO response = createTeacher(requestDTO);
        linkTeacherToInstitute(response.getId(), instituteId);
        return response;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void linkTeacherToInstitute(Long teacherId, Long instituteId) {
        Institute institute = instituteRepository.findById(instituteId)
                .orElseThrow(() -> new ResourceNotFoundException("Institute not found with ID: " + instituteId));

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + teacherId));

        if (instituteTeacherRepository.existsByInstituteIdAndTeacherId(instituteId, teacherId)) {
            throw new DuplicateResourceException("Teacher is already assigned to this institute");
        }

        InstituteTeacher instituteTeacher = InstituteTeacher.builder()
                .institute(institute)
                .teacher(teacher)
                .joinedDate(LocalDate.now())
                .build();

        instituteTeacherRepository.save(instituteTeacher);
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
    public List<TeacherResponseDTO> searchTeachers(String keyword, Long instituteId) {
        return teacherRepository.searchByUsernameOrNameAndInstituteId(keyword, instituteId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TeacherResponseDTO> getTeachersByInstituteId(Long instituteId) {
        return teacherRepository.findByInstituteId(instituteId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TeacherResponseDTO> searchTeachersByInstitute(String keyword, Long instituteId) {
        return teacherRepository.searchByInstituteId(keyword, instituteId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TeacherLectureCountDTO getTeacherLectureCount(Long teacherId) {
        ensureTeacherExists(teacherId);
        long count = lectureRepository.countByTeacher_Id(teacherId);
        return TeacherLectureCountDTO.builder()
                .teacherId(teacherId)
                .lectureCount(count)
                .build();
    }


    @Transactional
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

        User user = existing.getUser();

        if (requestDTO.getUsername() != null && !user.getUsername().equals(requestDTO.getUsername())) {
            if (userRepository.findByUsername(requestDTO.getUsername()).isPresent()) {
                throw new DuplicateResourceException("Username already exists");
            }
            user.setUsername(requestDTO.getUsername());
        }

        if (requestDTO.getEmail() != null && !user.getEmail().equals(requestDTO.getEmail())) {
            if (userRepository.findByEmail(requestDTO.getEmail()).isPresent()) {
                throw new DuplicateResourceException("Email already exists");
            }
            user.setEmail(requestDTO.getEmail());
        }

        if (requestDTO.getPhone() != null) {
            user.setContactInfo(requestDTO.getPhone());
        }

        userRepository.save(user);

        return mapToResponse(teacherRepository.save(existing));
    }

    @Transactional
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

        List<TrainingSession> sessions = trainingSessionRepository.findByTeacherIdAndStatusNot(
                teacherId, SessionStatus.CANCELLED);

        if (sessions.isEmpty()) {
            return List.of();
        }

        List<Long> sessionIds = sessions.stream().map(TrainingSession::getId).toList();

        Map<Long, Long> totalLecturesMap = lectureRepository.countBySessionIds(sessionIds).stream()
                .collect(Collectors.toMap(r -> ((Number) r[0]).longValue(), r -> ((Number) r[1]).longValue()));

        Map<Long, Long> lecturesGivenMap = attendanceRepository.countLecturesGivenBySessionIds(sessionIds).stream()
                .collect(Collectors.toMap(r -> ((Number) r[0]).longValue(), r -> ((Number) r[1]).longValue()));

        Map<Long, Long> enrollmentCountMap = enrollmentRepository.countBySessionIds(sessionIds).stream()
                .collect(Collectors.toMap(r -> ((Number) r[0]).longValue(), r -> ((Number) r[1]).longValue()));

        return sessions.stream().map(session -> {
            Long total = totalLecturesMap.getOrDefault(session.getId(), 0L);
            Long given = lecturesGivenMap.getOrDefault(session.getId(), 0L);
            double percentage = total == 0 ? 0.0 : Math.round(((given * 100.0) / total) * 100.0) / 100.0;

            return TeacherCourseProgressDTO.builder()
                    .trainingSessionId(session.getId())
                    .courseName(session.getCourse().getName())
                    .totalLectures(total)
                    .lecturesGiven(given)
                    .image(session.getImage())
                    .startDate(session.getStartDate())
                    .progressPercentage(percentage)
                    .numberOfStudents(enrollmentCountMap.getOrDefault(session.getId(), 0L))
                    .build();
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<WeeklyScheduleItemDTO> getTeacherWeeklySchedule(Long teacherId, LocalDate date) {
        ensureTeacherExists(teacherId);

        LocalDate refDate = date != null ? date : LocalDate.now();
        LocalDate startOfWeek = refDate.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        return lectureRepository.findTeacherWeeklySchedule(teacherId, startOfWeek, endOfWeek).stream()
                .map(this::mapLectureToWeeklySchedule)
                .toList();
    }

    private WeeklyScheduleItemDTO mapLectureToWeeklySchedule(Lecture lecture) {
        return WeeklyScheduleItemDTO.builder()
                .day(lecture.getLectureDate().getDayOfWeek().name())
                .lectureDate(lecture.getLectureDate())
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
