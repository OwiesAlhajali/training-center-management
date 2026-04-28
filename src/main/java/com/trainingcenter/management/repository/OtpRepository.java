package com.trainingcenter.management.repository;

import com.trainingcenter.management.entity.OtpEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpEntry, Long> {
    
    Optional<OtpEntry> findTopByEmailAndUsedFalseOrderByExpiryDateDesc(String email);

    @Modifying
    @Transactional
    @Query("DELETE FROM OtpEntry o WHERE o.expiryDate < :now")
    void deleteExpiredOtps(LocalDateTime now);
}