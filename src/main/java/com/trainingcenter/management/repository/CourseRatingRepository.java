package com.trainingcenter.management.repository;

import com.trainingcenter.management.entity.CourseRating;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRatingRepository extends JpaRepository<CourseRating,Long> {
    boolean existsByStudentIdAndCourseId(Long studentId,Long courseId);
}
