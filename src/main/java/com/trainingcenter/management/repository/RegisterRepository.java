package com.trainingcenter.management.repository;

import com.trainingcenter.management.entity.Register;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegisterRepository extends JpaRepository<Register,Long> {
    boolean existsByStudentIdAndTenantId(Long studentId, Long tenantId);
}
