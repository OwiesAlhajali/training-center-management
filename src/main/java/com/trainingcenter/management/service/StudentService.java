package com.trainingcenter.management.service;
import com.trainingcenter.management.dto.StudentCompletionPercentageDTO;
import com.trainingcenter.management.dto.StudentRequestDTO;
import com.trainingcenter.management.dto.StudentResponseDTO;
import com.trainingcenter.management.dto.StudentUpdateRequestDTO;
import com.trainingcenter.management.dto.StudentTrainingHoursDTO;
import com.trainingcenter.management.dto.WeeklyScheduleItemDTO;
import com.trainingcenter.management.entity.Attendance;
import com.trainingcenter.management.entity.AttendanceStatus;
import com.trainingcenter.management.entity.Lecture;
import com.trainingcenter.management.entity.Student;
import com.trainingcenter.management.entity.TrainingSession;
import com.trainingcenter.management.entity.User;
import com.trainingcenter.management.entity.Enrollment;
import com.trainingcenter.management.entity.SessionStatus;
import com.trainingcenter.management.exception.BadRequestException;
import com.trainingcenter.management.exception.DuplicateResourceException;
import com.trainingcenter.management.exception.ResourceNotFoundException;
import com.trainingcenter.management.repository.AttendanceRepository;
import com.trainingcenter.management.repository.LectureRepository;
import com.trainingcenter.management.repository.StudentRepository;
import com.trainingcenter.management.repository.UserRepository;
import com.trainingcenter.management.repository.EnrollmentRepository;
import com.trainingcenter.management.repository.InstituteRepository;
import com.trainingcenter.management.repository.RegisterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final InstituteRepository instituteRepository; 
    private final EnrollmentRepository enrollmentRepository;  
    private final RegisterRepository registerRepository;     
    private final PasswordEncoder passwordEncoder;
    private final AttendanceRepository attendanceRepository;
    private final LectureRepository lectureRepository;
    private final ImageService imageService;

    public StudentResponseDTO createStudent(StudentRequestDTO request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Password and confirm password do not match");
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new DuplicateResourceException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUserType(User.UserType.STUDENT);
        user.setContactInfo(request.getContactInfo());
        user.setImage(request.getImage());

        User savedUser = userRepository.save(user);

        if (studentRepository.existsByUserId(savedUser.getId())) {
            throw new DuplicateResourceException("Student profile already exists for this user");
        }

        Student student = new Student();
        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setGender(request.getGender());
        student.setBirthDate(request.getBirthDate());
        student.setAddress(request.getAddress());
        student.setInterest(request.getInterest());
        student.setEnrollmentDate(LocalDate.now());

        student.setUser(savedUser);

        Student savedStudent = studentRepository.save(student);

        return mapToResponse(savedStudent);
    }

    @Transactional(readOnly = true)
    public StudentResponseDTO getStudentById(Long id) {

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        return mapToResponse(student);
    }

    @Transactional(readOnly = true)
    public List<StudentResponseDTO> getAllStudents() {

        return studentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StudentResponseDTO> searchStudents(String keyword) {
        return studentRepository.searchByUsernameOrName(keyword)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public StudentResponseDTO updateStudent(Long id, StudentUpdateRequestDTO request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        User user = student.getUser();

        if (!user.getUsername().equals(request.getUsername()) && userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new DuplicateResourceException("Username already exists");
        }

        user.setUsername(request.getUsername());
        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setGender(request.getGender());
        student.setBirthDate(request.getBirthDate());
        student.setAddress(request.getAddress());
        student.setBio(request.getBio());

        if (request.getProfilePicture() != null && !request.getProfilePicture().isEmpty()) {
            String imageUrl = imageService.uploadImage(request.getProfilePicture());
            user.setImage(imageUrl);
        }

        userRepository.save(user);
        Student updatedStudent = studentRepository.save(student);
        return mapToResponse(updatedStudent);
    }
 
    @Transactional
    public void deleteStudentRegisterForInstitute(Long studentId, Long instituteId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        if (!instituteRepository.existsById(instituteId)) {
            throw new ResourceNotFoundException("Institute not found with id: " + instituteId);
        }

        List<Enrollment> enrollmentsInInstitute = enrollmentRepository.findEnrollmentsByStudentAndInstitute(studentId, instituteId);

        boolean hasActiveOrUpcoming = enrollmentsInInstitute.stream()
                .anyMatch(e -> {
                    SessionStatus status = e.getTrainingSession().getStatus();
                    return status == SessionStatus.ACTIVE || status == SessionStatus.UPCOMING;
                });

        if (hasActiveOrUpcoming) {
            throw new BadRequestException("Cannot delete register: Student has active or upcoming enrollments in this institute.");
        }

        Long tenantId = getTenantIdByInstitute(instituteId);
        registerRepository.deleteByStudentIdAndTenantId(studentId, tenantId);
    }

    // Helper method
    private Long getTenantIdByInstitute(Long instituteId) {
        return instituteRepository.findTenantIdByInstituteId(instituteId);
    }

    /**
     * Uploads a student profile image and stores the Cloudinary URL in User.image.
     */
    public StudentResponseDTO updateProfileImage(Long id, MultipartFile file) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        User user = student.getUser();

        String imageUrl = imageService.uploadImage(file);
        user.setImage(imageUrl);
        userRepository.save(user);

        return mapToResponse(student);
    }

    @Transactional(readOnly = true)
    public StudentTrainingHoursDTO getStudentTrainingHours(Long studentId) {
        ensureStudentExists(studentId);

        List<Attendance> presentAttendances = attendanceRepository.findDetailedByStudentAndStatus(
                studentId,
                AttendanceStatus.PRESENT
        );

        double totalMinutes = presentAttendances.stream()
                .map(Attendance::getLecture)
                .mapToLong(lecture -> java.time.Duration.between(lecture.getStartTime(), lecture.getEndTime()).toMinutes())
                .sum();

        double totalHours = Math.round((totalMinutes / 60.0) * 100.0) / 100.0;

        return StudentTrainingHoursDTO.builder()
                .studentId(studentId)
                .totalHours(totalHours)
                .build();
    }

    @Transactional(readOnly = true)
    public List<StudentCompletionPercentageDTO> getStudentCompletionPercentage(Long studentId) {
        ensureStudentExists(studentId);

        List<TrainingSession> sessions = enrollmentRepository.findTrainingSessionsByStudentId(
                studentId, SessionStatus.CANCELLED);

        if (sessions.isEmpty()) {
            return List.of();
        }

        List<Long> sessionIds = sessions.stream().map(TrainingSession::getId).toList();

        Map<Long, Long> totalLecturesMap = lectureRepository.countBySessionIds(sessionIds).stream()
                .collect(Collectors.toMap(r -> ((Number) r[0]).longValue(), r -> ((Number) r[1]).longValue()));

        Map<Long, Long> presentMap = attendanceRepository.countPresentBySessionIds(
                        studentId, AttendanceStatus.PRESENT, sessionIds).stream()
                .collect(Collectors.toMap(r -> ((Number) r[0]).longValue(), r -> ((Number) r[1]).longValue()));

        return sessions.stream().map(session -> {
            Long total = totalLecturesMap.getOrDefault(session.getId(), 0L);
            Long attended = presentMap.getOrDefault(session.getId(), 0L);
            double percentage = total == 0 ? 0.0 : Math.round(((attended * 100.0) / total) * 100.0) / 100.0;

            return StudentCompletionPercentageDTO.builder()
                    .studentId(studentId)
                    .trainingSessionId(session.getId())
                    .courseName(session.getCourse().getName())
                    .totalLectures(total)
                    .lecturesAttended(attended)
                    .image(session.getImage())
                    .startDate(session.getStartDate())
                    .attendancePercentage(percentage)
                    .build();
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<WeeklyScheduleItemDTO> getStudentWeeklySchedule(Long studentId, LocalDate date) {
        ensureStudentExists(studentId);

        LocalDate refDate = date != null ? date : LocalDate.now();
        LocalDate startOfWeek = refDate.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        return lectureRepository.findStudentWeeklySchedule(studentId, startOfWeek, endOfWeek).stream()
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

    private void ensureStudentExists(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found");
        }
    }

    // Mapper
    private StudentResponseDTO mapToResponse(Student student) {
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
}
