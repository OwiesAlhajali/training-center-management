package com.trainingcenter.management.repository;

import com.trainingcenter.management.entity.TrainingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TrainingSessionRepository extends JpaRepository<TrainingSession, Long> {

    @Query("SELECT ts FROM TrainingSession ts WHERE ts.classRoom.institute.id = :instituteId")
    List<TrainingSession> findByInstituteId(@Param("instituteId") Long instituteId);

    @Query("SELECT ts FROM TrainingSession ts WHERE ts.course.tenant.id = :tenantId")
    List<TrainingSession> findByTenantId(@Param("tenantId") Long tenantId);
}
