package com.trainingcenter.management.repository;

import com.trainingcenter.management.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    Optional<Tenant> findByKey(String key);
    boolean existsByKey(String key);
}