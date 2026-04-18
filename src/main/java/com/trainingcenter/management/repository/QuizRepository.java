package com.trainingcenter.management.repository;

import com.trainingcenter.management.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List ;
public interface QuizRepository extends JpaRepository<Quiz,Long > {

    boolean existsByNameAndTrainingSessionId (String name , Long TrainingSessionId) ;

    List<Quiz> findByTrainingSessionId( Long TrainingSessionId) ;
}
