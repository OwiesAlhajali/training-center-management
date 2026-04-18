package com.trainingcenter.management.service;
import com.trainingcenter.management.dto.StudentCompletionPercentageDTO;
import com.trainingcenter.management.dto.StudentRequestDTO;
import com.trainingcenter.management.dto.StudentResponseDTO;
import com.trainingcenter.management.dto.StudentTrainingHoursDTO;
import com.trainingcenter.management.dto.WeeklyScheduleItemDTO;
import com.trainingcenter.management.entity.Attendance;
import com.trainingcenter.management.entity.AttendanceStatus;
import com.trainingcenter.management.entity.Lecture;
import com.trainingcenter.management.entity.Student;
import com.trainingcenter.management.entity.User;
import com.trainingcenter.management.exception.BadRequestException;
import com.trainingcenter.management.exception.DuplicateResourceException;
import com.trainingcenter.management.exception.ResourceNotFoundException;
import com.trainingcenter.management.repository.AttendanceRepository;
import com.trainingcenter.management.repository.LectureRepository;
import com.trainingcenter.management.repository.StudentRepository;
import com.trainingcenter.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AttendanceRepository attendanceRepository;
    private final LectureRepository lectureRepository;

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

    public StudentResponseDTO updateStudent(Long id, StudentRequestDTO request) {

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setGender(request.getGender());
        student.setBirthDate(request.getBirthDate());
        student.setAddress(request.getAddress());
        student.setInterest(request.getInterest());

        return mapToResponse(student);
    }

    public void deleteStudent(Long id) {

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        studentRepository.delete(student);
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
    public StudentCompletionPercentageDTO getStudentCompletionPercentage(Long studentId) {
        ensureStudentExists(studentId);

        List<Object[]> stats = attendanceRepository.getStudentSessionAttendanceStats(studentId, AttendanceStatus.PRESENT);
        long totalSessions = stats.size();
        long completedSessions = stats.stream()
                .filter(row -> {
                    long present = ((Number) row[1]).longValue();
                    long total = ((Number) row[2]).longValue();
                    return total > 0 && present == total;
                })
                .count();

        double percentage = totalSessions == 0
                ? 0.0
                : Math.round(((completedSessions * 100.0) / totalSessions) * 100.0) / 100.0;

        return StudentCompletionPercentageDTO.builder()
                .studentId(studentId)
                .completedSessions(completedSessions)
                .totalSessions(totalSessions)
                .completionPercentage(percentage)
                .build();
    }

    @Transactional(readOnly = true)
    public List<WeeklyScheduleItemDTO> getStudentWeeklySchedule(Long studentId) {
        ensureStudentExists(studentId);

        LocalDate startOfWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        return lectureRepository.findStudentWeeklySchedule(studentId, startOfWeek, endOfWeek).stream()
                .map(this::mapLectureToWeeklySchedule)
                .toList();
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

    private void ensureStudentExists(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found");
        }
    }

    //MAPPER
    private StudentResponseDTO mapToResponse(Student student) {

        return StudentResponseDTO.builder()
                .id(student.getId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .gender(student.getGender())
                .birthDate(student.getBirthDate())
                .enrollmentDate(student.getEnrollmentDate())
                .address(student.getAddress())
                .interest(student.getInterest())
                .userId(student.getUser().getId())
                .build();
    }
}