package com.trainingcenter.management.service;

import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.trainingcenter.management.entity.*;
import com.trainingcenter.management.repository.EnrollmentRepository;
import com.trainingcenter.management.repository.PaymentRepository;
import com.trainingcenter.management.repository.TrainingSessionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WebhookService {

    private final PaymentRepository paymentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final TrainingSessionRepository trainingSessionRepository;

    private static final Logger logger = LoggerFactory.getLogger(WebhookService.class);

    @Transactional
    public void handleCheckoutSessionCompleted(Event event) {
        Session checkoutSession = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
        if (checkoutSession == null) {
            throw new IllegalArgumentException("Invalid checkout session payload");
        }

        String checkoutSessionId = checkoutSession.getId();

        Payment payment = paymentRepository.findByStripeCheckoutSessionId(checkoutSessionId).orElse(null);
        if (payment == null) {
            throw new IllegalStateException("Payment not found for checkout session: " + checkoutSessionId);
        }

        // Check if already succeeded (idempotency)
        if (payment.getStatus() == PaymentStatus.SUCCEEDED || payment.getStatus() == PaymentStatus.FAILED) {
            logger.info("Payment already processed for checkout session: {}", checkoutSessionId);
            return;
        }

        if (!"paid".equalsIgnoreCase(checkoutSession.getPaymentStatus())) {
            logger.warn("Checkout session {} completed without paid status: {}", checkoutSessionId, checkoutSession.getPaymentStatus());
            return;
        }

        // Update payment status to SUCCEEDED
        payment.setStatus(PaymentStatus.SUCCEEDED);
        payment.setStripePaymentIntentId(checkoutSession.getPaymentIntent());
        paymentRepository.save(payment);

        // Get TrainingSession from payment
        TrainingSession trainingSession = payment.getTrainingSession();
        Student student = payment.getStudent();

        // Check if enrollment already exists
        if (enrollmentRepository.existsByStudentAndTrainingSession(student, trainingSession)) {
            logger.info("Enrollment already exists for student {} in training session {}", student.getId(), trainingSession.getId());
            return;
        }

        // Check available seats
        if (trainingSession.getAvailableSeats() <= 0) {
            logger.warn("No available seats for training session {}", trainingSession.getId());
            return;
        }

        // Create enrollment
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setTrainingSession(trainingSession);

        int updatedRows = trainingSessionRepository.decrementAvailableSeatsIfAvailable(trainingSession.getId());
        if (updatedRows == 0) {
            logger.warn("No available seats for training session {}", trainingSession.getId());
            return;
        }

        try {
            enrollmentRepository.save(enrollment);
            logger.info("Enrollment created successfully for student {} and session {}", student.getId(), trainingSession.getId());
        } catch (DataIntegrityViolationException ex) {
            // Race condition - another request already created this enrollment
            // This is fine, just restore the seats
            trainingSessionRepository.incrementAvailableSeats(trainingSession.getId());
            logger.warn("Race condition detected for enrollment creation, seats restored");
        }
    }

    @Transactional
    public void handleCheckoutSessionFailed(Event event) {
        Session checkoutSession = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
        if (checkoutSession == null) {
            throw new IllegalArgumentException("Invalid checkout session payload");
        }

        String checkoutSessionId = checkoutSession.getId();

        Payment payment = paymentRepository.findByStripeCheckoutSessionId(checkoutSessionId).orElse(null);
        if (payment == null) {
            logger.warn("Payment not found for checkout session: {}", checkoutSessionId);
            return;
        }

        // Check if already processed
        if (payment.getStatus() == PaymentStatus.SUCCEEDED || payment.getStatus() == PaymentStatus.FAILED) {
            logger.info("Payment already processed for checkout session: {}", checkoutSessionId);
            return;
        }

        // Update payment status to FAILED
        payment.setStatus(PaymentStatus.FAILED);
        payment.setStripePaymentIntentId(checkoutSession.getPaymentIntent());
        paymentRepository.save(payment);

        logger.info("Payment failed for checkout session: {}", checkoutSessionId);
    }
}
