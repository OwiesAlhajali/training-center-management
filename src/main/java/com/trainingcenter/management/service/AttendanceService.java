package com.trainingcenter.management.service;

import com.trainingcenter.management.dto.*;
import com.trainingcenter.management.entity.*;
import com.trainingcenter.management.exception.*;
import com.trainingcenter.management.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final LectureRepository lectureRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional
    public void markBulkAttendance(BulkAttendanceRequestDTO request) {
        Lecture lecture = lectureRepository.findById(request.getLectureId())
                .orElseThrow(() -> new ResourceNotFoundException("Lecture not found"));

        attendanceRepository.deleteByLectureId(lecture.getId());

        List<Attendance> attendances = request.getRecords().stream().map(record -> {
            Student student = studentRepository.findById(record.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + record.getStudentId()));

            if (!enrollmentRepository.existsByStudentAndTrainingSession(student, lecture.getTrainingSession())) {
                throw new RuntimeException("Student " + student.getId() + " is not enrolled in this session");
            }

            return Attendance.builder()
                    .student(student)
                    .lecture(lecture)
                    .status(record.getStatus())
                    .checkInTime(LocalDateTime.now())
                    .build();
        }).collect(Collectors.toList());

        attendanceRepository.saveAll(attendances);
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponseDTO> getAttendanceByLecture(Long lectureId) {
        return attendanceRepository.findByLectureId(lectureId).stream()
                .map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponseDTO> getAttendanceByStudent(Long studentId) {
        return attendanceRepository.findByStudentId(studentId).stream()
                .map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    private AttendanceResponseDTO mapToResponseDTO(Attendance attendance) {
        Long studentId = attendance.getStudent().getId();
        Long sessionId = attendance.getLecture().getTrainingSession().getId();

        return AttendanceResponseDTO.builder()
                .id(attendance.getId())
                .studentId(studentId)
                .studentFullName(attendance.getStudent().getFirstName() + " " + attendance.getStudent().getLastName())
                .lectureId(attendance.getLecture().getId())
                .lectureDate(attendance.getLecture().getLectureDate().toString())
                .status(attendance.getStatus())
                .checkInTime(attendance.getCheckInTime())
                .attendancePercentage(calculateRate(studentId, sessionId))
                .build();
    }

    private Double calculateRate(Long studentId, Long sessionId) {
        long present = attendanceRepository.countPresentLectures(studentId, sessionId);
        long total = attendanceRepository.countTotalProcessedLectures(sessionId);
        return (total == 0) ? 0.0 : Math.round(((double) present / total * 100) * 10.0) / 10.0;
    }
}
