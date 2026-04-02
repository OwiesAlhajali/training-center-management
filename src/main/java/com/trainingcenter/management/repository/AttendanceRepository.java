package com.trainingcenter.management.repository;

import com.trainingcenter.management.entity.Attendance;
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
}
