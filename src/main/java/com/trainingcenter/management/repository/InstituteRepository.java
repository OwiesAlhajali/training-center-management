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

    
@Query("SELECT COUNT(DISTINCT e.student.id) " +
       "FROM Enrollment e " +
       "JOIN e.trainingSession ts " +
       "JOIN ts.classRoom cr " +
       "WHERE cr.institute.id = :instituteId")
long countStudentsByInstitute(@Param("instituteId") Long instituteId);
    
@Query("SELECT COUNT(DISTINCT t.id) " +
       "FROM Teacher t " +
       "JOIN TrainingSession ts ON ts.teacher.id = t.id " +
       "JOIN ts.classRoom cr " +
       "WHERE cr.institute.id = :instituteId")
long countTeachersByInstitute(@Param("instituteId") Long instituteId);
    
}
