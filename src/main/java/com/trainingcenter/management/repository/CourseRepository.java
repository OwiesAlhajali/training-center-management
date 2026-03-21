package com.trainingcenter.management.repository;

import com.trainingcenter.management.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByTenantId(Long tenantId);
    List<Course> findByCategoryIdAndTenantId(Long categoryId, Long tenantId);
    List<Course> findByNameContainingIgnoreCaseAndTenantId(String name, Long tenantId);
}
