package com.trainingcenter.management.repository;

import com.trainingcenter.management.entity.Institute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InstituteRepository extends JpaRepository<Institute, Long> {
    List<Institute> findByUserId(Long userId);
    
    @Query("SELECT i.tenant.id FROM Institute i WHERE i.id = :instituteId")
    Long findTenantIdByInstituteId(@Param("instituteId") Long instituteId);
}
