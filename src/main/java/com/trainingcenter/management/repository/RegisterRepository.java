package com.trainingcenter.management.repository;

import com.trainingcenter.management.entity.Register;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegisterRepository extends JpaRepository<Register,Long> {
    boolean existsByStudentIdAndTenantId(Long studentId, Long tenantId);

    Long countDistinctByTenantId(Long tenantId);

    void deleteByStudentIdAndTenantId(Long studentId, Long tenantId);

    List<Register> findByTenantId(Long tenantId);
}
