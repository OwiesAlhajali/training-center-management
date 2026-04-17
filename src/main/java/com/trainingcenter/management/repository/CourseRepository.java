package com.trainingcenter.management.repository;

import com.trainingcenter.management.entity.Course;
import com.trainingcenter.management.entity.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByTenantId(Long tenantId);
    List<Course> findByCategoryIdAndTenantId(Long categoryId, Long tenantId);
    List<Course> findByNameContainingIgnoreCaseAndTenantId(String name, Long tenantId);

    @Query("""
            SELECT c.id,
                   c.name,
                   c.description,
                   c.hours,
                   c.category.name,
                   c.tenant.name,
                   COUNT(ts)
            FROM TrainingSession ts
            JOIN ts.course c
            WHERE ts.status = :status
            GROUP BY c.id, c.name, c.description, c.hours, c.category.name, c.tenant.name
            """)
    List<Object[]> findActiveCourseSummariesBySessionStatus(@Param("status") SessionStatus status);
}
