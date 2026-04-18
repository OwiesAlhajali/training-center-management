package com.trainingcenter.management.repository;

import com.trainingcenter.management.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GradeRepository extends JpaRepository<Grade, Long> {

    boolean existsByStudentIdAndQuizId(Long studentId, Long quizId);

    Optional<Grade> findByStudentIdAndQuizId(Long studentId, Long quizId);

    List<Grade> findByStudentId(Long studentId);

    List<Grade> findByQuizId(Long quizId);

    boolean existsByQuizId(Long quizId);
}
