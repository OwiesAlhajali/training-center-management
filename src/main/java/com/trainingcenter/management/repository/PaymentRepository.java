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
            "EXTRACT(MONTH FROM p.created_at) AS month, " +
            "SUM(p.amount) AS total_revenue, " +
            "COUNT(*) AS total_payments " +
            "FROM payments p " +
            "JOIN training_sessions ts ON p.training_session_id = ts.id " +
            "JOIN classrooms cr ON ts.classroom_id = cr.id " +
            "WHERE cr.institute_id = :instituteId " +
            "AND p.status = 'SUCCEEDED' " +
            "AND EXTRACT(YEAR FROM p.created_at) = :year " +
            "GROUP BY EXTRACT(MONTH FROM p.created_at) " +
            "ORDER BY EXTRACT(MONTH FROM p.created_at)", nativeQuery = true)
    List<Object[]> getMonthlyFinancialPerformanceByInstituteAndYear(
            @Param("instituteId") Long instituteId,
            @Param("year") Integer year);
}
