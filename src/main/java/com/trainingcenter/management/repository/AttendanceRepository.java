package com.trainingcenter.management.repository;

import com.trainingcenter.management.entity.Attendance;
import com.trainingcenter.management.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByLectureId(Long lectureId);
    List<Attendance> findByStudentId(Long studentId);
    void deleteByLectureId(Long lectureId);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.id = :studentId " +
           "AND a.lecture.trainingSession.id = :sessionId AND a.status = 'PRESENT'")
    long countPresentLectures(@Param("studentId") Long studentId, @Param("sessionId") Long sessionId);

    @Query("SELECT COUNT(DISTINCT a.lecture.id) FROM Attendance a WHERE a.lecture.trainingSession.id = :sessionId")
    long countTotalProcessedLectures(@Param("sessionId") Long sessionId);

    @Query("""
            SELECT a FROM Attendance a
            JOIN FETCH a.lecture l
            JOIN FETCH l.trainingSession ts
            JOIN FETCH ts.course c
            WHERE a.student.id = :studentId AND a.status = :status
            """)
    List<Attendance> findDetailedByStudentAndStatus(@Param("studentId") Long studentId,
                                                    @Param("status") AttendanceStatus status);

    @Query("""
            SELECT a.lecture.trainingSession.id,
                   SUM(CASE WHEN a.status = :presentStatus THEN 1 ELSE 0 END),
                   COUNT(a)
            FROM Attendance a
            WHERE a.student.id = :studentId
            GROUP BY a.lecture.trainingSession.id
            """)
    List<Object[]> getStudentSessionAttendanceStats(@Param("studentId") Long studentId,
                                                    @Param("presentStatus") AttendanceStatus presentStatus);
}
