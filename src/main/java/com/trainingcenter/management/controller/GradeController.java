package com.trainingcenter.management.controller;

import com.trainingcenter.management.dto.GradeRequestDTO;
import com.trainingcenter.management.dto.GradeResponseDTO;
import com.trainingcenter.management.service.GradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    // Create
    @PostMapping("/grades")
    public ResponseEntity<GradeResponseDTO> creatGrade(
            @Valid @RequestBody GradeRequestDTO request
    ) {
        GradeResponseDTO response = gradeService.createGrade(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Update
    @PutMapping("/grades/{id}")
    public ResponseEntity<GradeResponseDTO> updateGrade(
            @PathVariable Long id,
            @Valid @RequestBody GradeRequestDTO request
    ) {
        GradeResponseDTO response = gradeService.updateGrade(id, request);
        return ResponseEntity.ok(response);
    }

    // Get Grade by Student and Quiz
    @GetMapping("/grades/student/{studentId}/quiz/{quizId}")
    public ResponseEntity<GradeResponseDTO> getGradeByStudentAndQuiz(
            @PathVariable Long studentId,
            @PathVariable Long quizId
    ) {
        GradeResponseDTO response = gradeService.getGradeByStudentAndQuiz(studentId, quizId);
        return ResponseEntity.ok(response);
    }

    // Get Grades by Student
    @GetMapping("/grades/student/{studentId}")
    public ResponseEntity<List<GradeResponseDTO>> getGradesByStudent(
            @PathVariable Long studentId
    ) {
        List<GradeResponseDTO> grades = gradeService.getGradesByStudent(studentId);
        return ResponseEntity.ok(grades);
    }

    // Get Grades by Quiz
    @GetMapping("/grades/quiz/{quizId}")
    public ResponseEntity<List<GradeResponseDTO>> getGradesByQuiz(
            @PathVariable Long quizId
    ) {
        List<GradeResponseDTO> grades = gradeService.getGradesByQuiz(quizId);
        return ResponseEntity.ok(grades);
    }

    // Delete Grade
    @DeleteMapping("/grades/{id}")
    public ResponseEntity<Void> deleteGrade(
            @PathVariable Long id
    ) {
        gradeService.deleteGrade(id);
        return ResponseEntity.noContent().build();
    }
}
