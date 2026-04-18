package com.trainingcenter.management.repository;

import com.trainingcenter.management.entity.Lecture;
import com.trainingcenter.management.entity.ClassRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface LectureRepository extends JpaRepository<Lecture, Long> {

  
    @Query("SELECT COUNT(l) > 0 FROM Lecture l WHERE l.classRoom.id = :roomId " +
           "AND l.lectureDate = :date AND (:start < l.endTime AND :end > l.startTime)")
    boolean existsConflict(@Param("roomId") Long roomId, @Param("date") LocalDate date, 
                          @Param("start") LocalTime start, @Param("end") LocalTime end);

 
    @Query("SELECT COUNT(l) > 0 FROM Lecture l WHERE l.teacher.id = :teacherId " +
           "AND l.lectureDate = :date AND (:start < l.endTime AND :end > l.startTime)")
    boolean isTeacherBusy(@Param("teacherId") Long teacherId, @Param("date") LocalDate date, 
                         @Param("start") LocalTime start, @Param("end") LocalTime end);


	@Query("SELECT r FROM ClassRoom r WHERE r.capacity >= :minSeats " +
       "AND (:requiredDevice IS NULL OR LOWER(r.availableDevices) LIKE LOWER(CONCAT('%', :requiredDevice, '%'))) " +
       "AND NOT EXISTS (SELECT l FROM Lecture l WHERE l.classRoom.id = r.id " +
       "AND l.lectureDate = :date AND (:start < l.endTime AND :end > l.startTime))")
    List<ClassRoom> findAvailableRoomsWithFeatures(
        @Param("minSeats") Integer minSeats,
        @Param("requiredDevice") String requiredDevice,
        @Param("date") LocalDate date,
        @Param("start") LocalTime start,
        @Param("end") LocalTime end
    );


    List<Lecture> findByTrainingSession_Id(Long sessionId);

    @Query("""
            SELECT l FROM Lecture l
            JOIN FETCH l.trainingSession ts
            JOIN FETCH ts.course c
            JOIN FETCH l.classRoom cr
            JOIN FETCH l.teacher t
            WHERE l.trainingSession.id IN (
                SELECT e.trainingSession.id FROM Enrollment e WHERE e.student.id = :studentId
            )
            AND l.lectureDate BETWEEN :startDate AND :endDate
            ORDER BY l.lectureDate, l.startTime
            """)
    List<Lecture> findStudentWeeklySchedule(@Param("studentId") Long studentId,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    @Query("""
            SELECT l FROM Lecture l
            JOIN FETCH l.trainingSession ts
            JOIN FETCH ts.course c
            JOIN FETCH l.classRoom cr
            JOIN FETCH l.teacher t
            WHERE l.teacher.id = :teacherId
            AND l.lectureDate BETWEEN :startDate AND :endDate
            ORDER BY l.lectureDate, l.startTime
            """)
    List<Lecture> findTeacherWeeklySchedule(@Param("teacherId") Long teacherId,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    void deleteByTrainingSession_Id(Long sessionId);
}
