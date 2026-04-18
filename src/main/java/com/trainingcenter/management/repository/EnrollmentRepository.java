package com.trainingcenter.management.repository;

import com.trainingcenter.management.entity.Enrollment;
import com.trainingcenter.management.entity.Student;
import com.trainingcenter.management.entity.TrainingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByStudentAndTrainingSession(Student student, TrainingSession trainingSession);

    @Query("SELECT DISTINCT e.student FROM Enrollment e")
    List<Student> findDistinctStudents();

    @Query("SELECT e FROM Enrollment e WHERE e.trainingSession.id = :sessionId")
    List<Enrollment> findByTrainingSessionId(@Param("sessionId") Long sessionId);

    boolean existsByStudentIdAndTrainingSession_CourseId(Long studentId,Long courseId);

    @Query("""
            SELECT FUNCTION('MONTH', e.createdAt), COUNT(e)
            FROM Enrollment e
            WHERE e.trainingSession.classRoom.institute.id = :instituteId
              AND FUNCTION('YEAR', e.createdAt) = :year
            GROUP BY FUNCTION('MONTH', e.createdAt)
            ORDER BY FUNCTION('MONTH', e.createdAt)
            """)
    List<Object[]> getMonthlyRegistrationsByInstituteAndYear(@Param("instituteId") Long instituteId,
                                                              @Param("year") Integer year);
}

