package com.trainingcenter.management.repository;

import com.trainingcenter.management.entity.Payment;
import com.trainingcenter.management.entity.PaymentStatus;
import com.trainingcenter.management.entity.Student;
import com.trainingcenter.management.entity.TrainingSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);
    Optional<Payment> findByStudentAndTrainingSessionAndStatus(Student student, TrainingSession trainingSession, PaymentStatus status);
}
