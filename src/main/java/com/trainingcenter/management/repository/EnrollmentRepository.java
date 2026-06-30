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

    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId AND e.trainingSession.status = :status")
    List<Enrollment> findByStudentIdAndTrainingSessionStatus(@Param("studentId") Long studentId,
                                                              @Param("status") com.trainingcenter.management.entity.SessionStatus status);

    boolean existsByStudentIdAndTrainingSession_CourseId(Long studentId,Long courseId);

    @Query("SELECT e.trainingSession.course.id, COUNT(DISTINCT e.student.id) " +
            "FROM Enrollment e " +
            "WHERE e.trainingSession.teacher.id = :teacherId " +
            "GROUP BY e.trainingSession.course.id")
    List<Object[]> countStudentsByTeacherPerCourse(@Param("teacherId") Long teacherId);

    @Query("""
            SELECT e.trainingSession.id, COUNT(e.id)
            FROM Enrollment e
            GROUP BY e.trainingSession.id
            ORDER BY COUNT(e.id) DESC
            """)
    List<Object[]> findTopEnrolledTrainingSessions();

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

