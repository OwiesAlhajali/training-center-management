package com.trainingcenter.management.repository;

import com.trainingcenter.management.entity.Payment;
import com.trainingcenter.management.entity.PaymentStatus;
import com.trainingcenter.management.entity.Student;
import com.trainingcenter.management.entity.TrainingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByStripeCheckoutSessionId(String stripeCheckoutSessionId);
    Optional<Payment> findByStudentAndTrainingSession(Student student, TrainingSession trainingSession);
    Optional<Payment> findByStudentAndTrainingSessionAndStatus(Student student, TrainingSession trainingSession, PaymentStatus status);

    @Query(value = "SELECT " +
            "EXTRACT(MONTH FROM p.createdAt) AS month, " +
            "SUM(p.amount) AS totalRevenue, " +
            "COUNT(*) AS totalPayments " +
            "FROM Payment p " +
            "JOIN p.trainingSession ts " +
            "JOIN ts.classRoom cr " +
            "WHERE cr.institute.id = :instituteId " +
            "AND p.status = 'SUCCEEDED' " +
            "AND EXTRACT(YEAR FROM p.createdAt) = :year " +
            "GROUP BY EXTRACT(MONTH FROM p.createdAt) " +
            "ORDER BY EXTRACT(MONTH FROM p.createdAt)")
    List<Object[]> getMonthlyFinancialPerformanceByInstituteAndYear(
            @Param("instituteId") Long instituteId,
            @Param("year") Integer year);
}
