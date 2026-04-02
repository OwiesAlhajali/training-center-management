package com.trainingcenter.management.controller;

import com.trainingcenter.management.dto.QuizRequestDTO;
import com.trainingcenter.management.dto.QuizResponseDTO;
import com.trainingcenter.management.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    // Create
    @PostMapping("/quizzes")
    public ResponseEntity<QuizResponseDTO> createQuiz(
            @Valid @RequestBody QuizRequestDTO request
    ) {
        QuizResponseDTO response = quizService.createQuiz(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Update
    @PutMapping("/quizzes/{id}")
    public ResponseEntity<QuizResponseDTO> updateQuiz(
            @PathVariable Long id,
            @Valid @RequestBody QuizRequestDTO request
    ) {
        QuizResponseDTO response = quizService.updateQuiz(id, request);
        return ResponseEntity.ok(response);
    }

    // Delete
    @DeleteMapping("/quizzes/{id}")
    public ResponseEntity<Void> deleteQuiz(
            @PathVariable Long id
    ) {
        quizService.deleteQuiz(id);
        return ResponseEntity.noContent().build();
    }

    // Get By id
    @GetMapping("/quizzes/{id}")
    public ResponseEntity<QuizResponseDTO> getQuizById(
            @PathVariable Long id
    ) {
        QuizResponseDTO response = quizService.getQuizById(id);
        return ResponseEntity.ok(response);
    }

    //Get Quizes by Training Session
    @GetMapping("/training-sessions/{sessionId}/quizzes")
    public ResponseEntity<List<QuizResponseDTO>> getQuizzesBySession(
            @PathVariable Long sessionId
    ) {
        List<QuizResponseDTO> quizzes = quizService.getQuizzesByTrainingSession(sessionId);
        return ResponseEntity.ok(quizzes);
    }
}