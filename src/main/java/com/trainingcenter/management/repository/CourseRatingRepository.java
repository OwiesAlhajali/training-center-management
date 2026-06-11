package com.trainingcenter.management.repository;

import com.trainingcenter.management.entity.CourseRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRatingRepository extends JpaRepository<CourseRating, Long> {

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    List<CourseRating> findByCourseId(Long courseId);

    @Query("SELECT AVG(r.rating) FROM CourseRating r WHERE r.course.id = :courseId")
    Double findAverageRatingByCourseId(@Param("courseId") Long courseId);
}