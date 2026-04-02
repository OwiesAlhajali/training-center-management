package com.trainingcenter.management.service;

import com.trainingcenter.management.dto.QuizRequestDTO;
import com.trainingcenter.management.dto.QuizResponseDTO;
import com.trainingcenter.management.entity.Quiz;
import com.trainingcenter.management.entity.TrainingSession;
import com.trainingcenter.management.exception.BadRequestException;
import com.trainingcenter.management.exception.ResourceNotFoundException;
import com.trainingcenter.management.repository.GradeRepository;
import com.trainingcenter.management.repository.QuizRepository;
import com.trainingcenter.management.repository.TrainingSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final TrainingSessionRepository trainingSessionRepository;
    private final GradeRepository gradeRepository;

    // Create
    @Transactional
    public QuizResponseDTO createQuiz(QuizRequestDTO request) {

        TrainingSession session = getTrainingSessionOrThrow(request.getTrainingSessionId());

        validatePassingScore(request);
        validateUniqueName(request);

        Quiz quiz = mapToEntity(request, session);

        Quiz savedQuiz = quizRepository.save(quiz);

        return mapToResponse(savedQuiz);
    }

    // Delete
    @Transactional
    public void deleteQuiz(Long quizId) {

        Quiz quiz = getQuizOrThrow(quizId);

        validateNoGradesExist(quizId);

        quizRepository.delete(quiz);
    }

    // Update
    @Transactional
    public QuizResponseDTO updateQuiz(Long quizId, QuizRequestDTO request) {

        Quiz quiz = getQuizOrThrow(quizId);

        // Not allow if Quiz has Grade
        validateNoGradesExist(quizId);

        validatePassingScore(request);

        // Making sure the Quiz is Unique
        if (!quiz.getName().equals(request.getName())) {
            validateUniqueName(request);
        }

        quiz.setName(request.getName());
        quiz.setMaxScore(request.getMaxScore());
        quiz.setPassingScore(request.getPassingScore());

        Quiz updatedQuiz = quizRepository.save(quiz);

        return mapToResponse(updatedQuiz);
    }

    // get by id
    @Transactional(readOnly = true)
    public QuizResponseDTO getQuizById(Long quizId) {

        Quiz quiz = getQuizOrThrow(quizId);

        return mapToResponse(quiz);
    }

    // get by Session id
    @Transactional(readOnly = true)
    public List<QuizResponseDTO> getQuizzesByTrainingSession(Long sessionId) {

        // tranining Session Exists ?
        TrainingSession session = getTrainingSessionOrThrow(sessionId);

        // fetch quizes
        List<Quiz> quizzes = quizRepository.findByTrainingSessionId(sessionId);

        // map to Dto
        return quizzes.stream()
                .map(this::mapToResponse)
                .toList();
    }

    // helper method

    private TrainingSession getTrainingSessionOrThrow(Long id) {
        return trainingSessionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Training session not found with id: " + id)
                );
    }

    private Quiz getQuizOrThrow(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Quiz not found with id: " + id)
                );
    }

    private void validatePassingScore(QuizRequestDTO request) {
        if (request.getPassingScore().compareTo(request.getMaxScore()) > 0) {
            throw new BadRequestException("Passing score cannot be greater than max score");
        }
    }

    private void validateUniqueName(QuizRequestDTO request) {
        boolean exists = quizRepository.existsByNameAndTrainingSessionId(
                request.getName(),
                request.getTrainingSessionId()
        );

        if (exists) {
            throw new BadRequestException(
                    "Quiz with name '" + request.getName() + "' already exists in this training session"
            );
        }
    }

    private void validateNoGradesExist(Long quizId) {
        if (gradeRepository.existsByQuizId(quizId)) {
            throw new BadRequestException("Operation not allowed: quiz already has grades");
        }
    }

    private Quiz mapToEntity(QuizRequestDTO request, TrainingSession session) {
        Quiz quiz = new Quiz();
        quiz.setName(request.getName());
        quiz.setMaxScore(request.getMaxScore());
        quiz.setPassingScore(request.getPassingScore());
        quiz.setTrainingSession(session);
        return quiz;
    }

    private QuizResponseDTO mapToResponse(Quiz quiz) {
        return QuizResponseDTO.builder()
                .id(quiz.getId())
                .name(quiz.getName())
                .maxScore(quiz.getMaxScore())
                .passingScore(quiz.getPassingScore())
                .createdAt(quiz.getCreatedAt())
                .trainingSessionId(quiz.getTrainingSession().getId())
                .build();
    }
}
// The Code Dosent Work , we need to add grade Entity and import the Grade Repository ,
// after adding the Grade feat just import it HERE ..