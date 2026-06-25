package com.trainingcenter.management.repository;

import com.trainingcenter.management.entity.Institute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface InstituteRepository extends JpaRepository<Institute, Long> {
    List<Institute> findByUserId(Long userId);
    
    @Query("SELECT i.tenant.id FROM Institute i WHERE i.id = :instituteId")
    Long findTenantIdByInstituteId(@Param("instituteId") Long instituteId);

    
@Query("SELECT COUNT(DISTINCT u.id) " +
       "FROM User u " +
       "WHERE u.userType IN ('STUDENT', 'TEACHER') " +
       "AND ( " +
       "  EXISTS (SELECT 1 FROM Enrollment e " +
       "           JOIN e.trainingSession ts " +
       "           JOIN ts.classRoom cr " +
       "           WHERE e.student.user.id = u.id " +
       "           AND cr.institute.id = :instituteId) " +
       "  OR " +
       "  EXISTS (SELECT 1 FROM TrainingSession ts " +
       "           JOIN ts.classRoom cr " +
       "           WHERE ts.teacher.user.id = u.id " +
       "           AND cr.institute.id = :instituteId) " +
       ")")
long countTotalUsersByInstitute(@Param("instituteId") Long instituteId);
    
}
