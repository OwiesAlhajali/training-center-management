package com.trainingcenter.management.repository;

import com.trainingcenter.management.entity.Institute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InstituteRepository extends JpaRepository<Institute, Long> {
    List<Institute> findByTenantId(Long tenantId);
}