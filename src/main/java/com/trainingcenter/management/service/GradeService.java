package com.trainingcenter.management.service;

import com.trainingcenter.management.dto.GradeRequestDTO;
import com.trainingcenter.management.dto.GradeResponseDTO;
import com.trainingcenter.management.entity.Grade;
import com.trainingcenter.management.entity.Quiz;
import com.trainingcenter.management.entity.Student;
import com.trainingcenter.management.exception.BadRequestException;
import com.trainingcenter.management.exception.ResourceNotFoundException;
import com.trainingcenter.management.repository.GradeRepository;
import com.trainingcenter.management.repository.QuizRepository;
import com.trainingcenter.management.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;
    private final QuizRepository quizRepository;

    // Create
    @Transactional
    public GradeResponseDTO createGrade (GradeRequestDTO request) {

        Student student = getStudentOrThrow(request.getStudentId());
        Quiz quiz = getQuizOrThrow(request.getQuizId());

        validateScore(request.getScore(), quiz.getMaxScore());

        // Check if grade already exists
        Grade grade = gradeRepository.findByStudentIdAndQuizId(request.getStudentId(), request.getQuizId())
                .orElse(new Grade());

        grade.setScore(request.getScore());
        grade.setStudent(student);
        grade.setQuiz(quiz);

        Grade savedGrade = gradeRepository.save(grade);

        return mapToResponse(savedGrade);
    }

    // Update Grade
    @Transactional
    public GradeResponseDTO updateGrade(Long id, GradeRequestDTO request) {

        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Grade not found with id: " + id)
                );

        validateScore(request.getScore(), grade.getQuiz().getMaxScore());

        grade.setScore(request.getScore());

        Grade updatedGrade = gradeRepository.save(grade);

        return mapToResponse(updatedGrade);
    }


    // Get Grade by Student and Quiz
    @Transactional(readOnly = true)
    public GradeResponseDTO getGradeByStudentAndQuiz(Long studentId, Long quizId) {

        Grade grade = gradeRepository.findByStudentIdAndQuizId(studentId, quizId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Grade not found for student " + studentId + " and quiz " + quizId)
                );

        return mapToResponse(grade);
    }

    // Get Grades by Student
    @Transactional(readOnly = true)
    public List<GradeResponseDTO> getGradesByStudent(Long studentId) {

        getStudentOrThrow(studentId); // student exists ?

        List<Grade> grades = gradeRepository.findByStudentId(studentId);

        return grades.stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Get Grades by Quiz
    @Transactional(readOnly = true)
    public List<GradeResponseDTO> getGradesByQuiz(Long quizId) {

        getQuizOrThrow(quizId); // quiz exists ?

        List<Grade> grades = gradeRepository.findByQuizId(quizId);

        return grades.stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Delete Grade
    @Transactional
    public void deleteGrade(Long gradeId) {

        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Grade not found with id: " + gradeId)
                );

        gradeRepository.delete(grade);
    }


    // Helper methods

    private Student getStudentOrThrow(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Student not found with id: " + id)
                );
    }

    private Quiz getQuizOrThrow(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Quiz not found with id: " + id)
                );
    }

    private void validateScore(BigDecimal score, BigDecimal maxScore) {
        if (score.compareTo(maxScore) > 0) {
            throw new BadRequestException("Score cannot be greater than max score of the quiz");
        }
    }

    // Mapper
    private GradeResponseDTO mapToResponse(Grade grade) {
        return GradeResponseDTO.builder()
                .id(grade.getId())
                .score(grade.getScore())
                .studentId(grade.getStudent().getId())
                .quizId(grade.getQuiz().getId())
                .build();
    }
}
