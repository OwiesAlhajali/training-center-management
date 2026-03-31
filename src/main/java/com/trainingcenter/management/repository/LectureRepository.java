package com.trainingcenter.management.repository;

import com.trainingcenter.management.entity.Lecture;
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

    List<Lecture> findByTrainingSession_Id(Long sessionId);

    void deleteByTrainingSession_Id(Long sessionId);
}
