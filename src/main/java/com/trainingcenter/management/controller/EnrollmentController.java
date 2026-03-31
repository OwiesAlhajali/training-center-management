package com.trainingcenter.management.controller;

import com.trainingcenter.management.dto.EnrollmentRequestDTO;
import com.trainingcenter.management.dto.EnrollmentResponseDTO;
import com.trainingcenter.management.dto.StudentResponseDTO;
import com.trainingcenter.management.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    // Create Enrollment
    @PostMapping
    public ResponseEntity<EnrollmentResponseDTO> createEnrollment(
            @RequestBody EnrollmentRequestDTO request) {

        EnrollmentResponseDTO response = enrollmentService.createEnrollment(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Delete Enrollment
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEnrollment(@PathVariable Long id) {

        enrollmentService.deleteEnrollment(id);
        return ResponseEntity.noContent().build();
    }

    // Get enrollments by session
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<List<EnrollmentResponseDTO>> getEnrollmentsBySession(
            @PathVariable Long sessionId) {

        List<EnrollmentResponseDTO> response =
                enrollmentService.getEnrollmentsBySession(sessionId);

        return ResponseEntity.ok(response);
    }

    // Get all unique enrolled students (for statistics)
    @GetMapping("/students/distinct")
    public ResponseEntity<List<StudentResponseDTO>> getAllUniqueEnrolledStudents() {

        List<StudentResponseDTO> response =
                enrollmentService.getAllUniqueEnrolledStudents();

        return ResponseEntity.ok(response);
    }
}
