package com.trainingcenter.management.repository;

import com.trainingcenter.management.entity.Enrollment;
import com.trainingcenter.management.entity.SessionStatus;
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

    Long countByTrainingSessionId(Long trainingSessionId);

    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId AND e.trainingSession.status = :status")
    List<Enrollment> findByStudentIdAndTrainingSessionStatus(@Param("studentId") Long studentId,
                                                              @Param("status") com.trainingcenter.management.entity.SessionStatus status);

    boolean existsByStudentIdAndTrainingSession_CourseId(Long studentId, Long courseId);

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

    @Query(value = "SELECT " +
            "EXTRACT(MONTH FROM e.created_at) AS month, " +
            "COUNT(*) AS registrations " +
            "FROM enrollments e " +
            "JOIN training_sessions ts ON e.training_session_id = ts.id " +
            "JOIN classrooms cr ON ts.classroom_id = cr.id " +
            "WHERE cr.institute_id = :instituteId " +
            "AND EXTRACT(YEAR FROM e.created_at) = :year " +
            "GROUP BY EXTRACT(MONTH FROM e.created_at) " +
            "ORDER BY EXTRACT(MONTH FROM e.created_at)", nativeQuery = true)
    List<Object[]> getMonthlyRegistrationsByInstituteAndYear(
            @Param("instituteId") Long instituteId,
            @Param("year") Integer year);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.student.id = :studentId AND e.trainingSession.course.tenant.id = :tenantId")
    long countByStudentIdAndTenantId(@Param("studentId") Long studentId, @Param("tenantId") Long tenantId);

    @Query("SELECT e FROM Enrollment e " +
            "JOIN e.trainingSession ts " +
            "JOIN ts.classRoom cr " +
            "JOIN cr.institute i " +
            "WHERE e.student.id = :studentId AND i.id = :instituteId")
    List<Enrollment> findEnrollmentsByStudentAndInstitute(
            @Param("studentId") Long studentId,
            @Param("instituteId") Long instituteId);

    @Query("SELECT e.trainingSession.id, COUNT(e.id) " +
            "FROM Enrollment e " +
            "WHERE e.trainingSession.id IN :sessionIds " +
            "GROUP BY e.trainingSession.id")
    List<Object[]> countBySessionIds(@Param("sessionIds") List<Long> sessionIds);

    @Query("SELECT e.trainingSession FROM Enrollment e " +
            "WHERE e.student.id = :studentId " +
            "AND e.trainingSession.status <> :cancelledStatus")
    List<TrainingSession> findTrainingSessionsByStudentId(@Param("studentId") Long studentId,
                                                          @Param("cancelledStatus") SessionStatus cancelledStatus);

    @Query("SELECT e.trainingSession FROM Enrollment e " +
            "WHERE e.student.id = :studentId AND e.trainingSession.course.id = :courseId " +
            "ORDER BY e.trainingSession.startDate DESC")
    List<TrainingSession> findTrainingSessionsByStudentAndCourse(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId);

    @Query("SELECT DISTINCT e.student FROM Enrollment e " +
            "JOIN e.trainingSession ts " +
            "JOIN ts.course c " +
            "WHERE c.tenant.id = :tenantId")
    List<Student> findActiveStudentsByTenantId(@Param("tenantId") Long tenantId);

    @Query("SELECT COUNT(DISTINCT e.student.id) FROM Enrollment e " +
            "JOIN e.trainingSession ts " +
            "JOIN ts.course c " +
            "WHERE c.tenant.id = :tenantId")
    long countActiveStudentsByTenantId(@Param("tenantId") Long tenantId);
}
