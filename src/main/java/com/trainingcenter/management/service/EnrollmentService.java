package com.trainingcenter.management.service;
import com.trainingcenter.management.dto.EnrollmentRequestDTO;
import com.trainingcenter.management.dto.EnrollmentResponseDTO;
import com.trainingcenter.management.dto.StudentResponseDTO;
import com.trainingcenter.management.entity.Enrollment;
import com.trainingcenter.management.entity.Student;
import com.trainingcenter.management.entity.TrainingSession;
import com.trainingcenter.management.entity.User;
import com.trainingcenter.management.exception.DuplicateResourceException;
import com.trainingcenter.management.exception.ResourceNotFoundException;
import com.trainingcenter.management.repository.EnrollmentRepository;
import com.trainingcenter.management.repository.StudentRepository;
import com.trainingcenter.management.repository.TrainingSessionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor

public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final TrainingSessionRepository trainingSessionRepository;

    @Transactional
    public EnrollmentResponseDTO createEnrollment(EnrollmentRequestDTO request) {

        // Fetch Student
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        // Fetch Session
        TrainingSession session = trainingSessionRepository.findById(request.getTrainingSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Training session not found"));

        // Check duplication
        if (enrollmentRepository.existsByStudentAndTrainingSession(student, session)) {
            throw new DuplicateResourceException("Student already enrolled");
        }

        //  Check seats
        if (session.getAvailableSeats() <= 0) {
            throw new IllegalStateException("No available seats");
        }

        // 5. Create enrollment
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setTrainingSession(session);

        // 6. Decrease seats
        session.setAvailableSeats(session.getAvailableSeats() - 1);

        try {
            Enrollment saved = enrollmentRepository.save(enrollment);
            return mapToDTO(saved);

        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException("Student already enrolled (race condition)");
        }
    }

    @Transactional
    public void deleteEnrollment(Long enrollmentId) {

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        TrainingSession session = enrollment.getTrainingSession();

        // increase seats back
        session.setAvailableSeats(session.getAvailableSeats() + 1);

        enrollmentRepository.delete(enrollment);
    }

    public List<EnrollmentResponseDTO> getEnrollmentsBySession(Long sessionId) {

        // Validate session exists
        if (!trainingSessionRepository.existsById(sessionId)) {
            throw new ResourceNotFoundException("Training session not found");
        }

        List<Enrollment> enrollments = enrollmentRepository.findByTrainingSessionId(sessionId);

        return enrollments.stream()
                .map(this::mapToDTO)
                .toList();
    }
    public List<StudentResponseDTO> getAllUniqueEnrolledStudents() {
        return enrollmentRepository.findDistinctStudents()
                .stream()
                .map(this::mapStudentToDTO)
                .toList();
    }

    // Mapping
    private StudentResponseDTO mapStudentToDTO(Student s) {
        User user = s.getUser();
        return StudentResponseDTO.builder()
                .id(s.getId())
                .firstName(s.getFirstName())
                .lastName(s.getLastName())
                .gender(s.getGender())
                .birthDate(s.getBirthDate())
                .address(s.getAddress())
                .interest(s.getInterest())
                .enrollmentDate(s.getEnrollmentDate())
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .contactInfo(user.getContactInfo())
                .image(user.getImage())
                .build();
    }

    // Mapping
    private EnrollmentResponseDTO mapToDTO(Enrollment e) {
        return new EnrollmentResponseDTO(
                e.getId(),
                e.getStudent().getId(),
                e.getTrainingSession().getId(),
                e.getCreatedAt()
        );
    }
    //  read for the student id and for all student related to the TraningSession
}