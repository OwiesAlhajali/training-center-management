package com.trainingcenter.management.service;

import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
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
    public void handlePaymentIntentSucceeded(Event event) {
        // Extract PaymentIntent from event
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
        if (paymentIntent == null) {
            return;
        }

        String paymentIntentId = paymentIntent.getId();
        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId).orElse(null);
        processSuccessfulPayment(payment, "PaymentIntent", paymentIntentId);
    }

    @Transactional
    public void handlePaymentIntentFailed(Event event) {
        // Extract PaymentIntent from event
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
        if (paymentIntent == null) {
            return;
        }

        String paymentIntentId = paymentIntent.getId();
        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId).orElse(null);
        processFailedPayment(payment, "PaymentIntent", paymentIntentId);
    }

    @Transactional
    public void handleCheckoutSessionCompleted(Event event) {
        Session checkoutSession = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
        if (checkoutSession == null) {
            return;
        }

        String sessionId = checkoutSession.getId();
        Payment payment = paymentRepository.findByStripePaymentIntentId(sessionId).orElse(null);
        processSuccessfulPayment(payment, "Checkout Session", sessionId);
    }

    @Transactional
    public void handleCheckoutSessionExpired(Event event) {
        Session checkoutSession = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
        if (checkoutSession == null) {
            return;
        }

        String sessionId = checkoutSession.getId();
        Payment payment = paymentRepository.findByStripePaymentIntentId(sessionId).orElse(null);
        processFailedPayment(payment, "Checkout Session", sessionId);
    }

    private void processSuccessfulPayment(Payment payment, String paymentType, String externalId) {
        if (payment == null) {
            logger.warn("Payment not found for {} ID: {}", paymentType, externalId);
            return;
        }

        // Check if already succeeded (idempotency)
        if (payment.getStatus() == PaymentStatus.SUCCEEDED || payment.getStatus() == PaymentStatus.FAILED) {
            logger.info("Payment already processed for {} ID: {}", paymentType, externalId);
            return;
        }

        // Update payment status to SUCCEEDED
        payment.setStatus(PaymentStatus.SUCCEEDED);
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

        // Decrease available seats
        trainingSession.setAvailableSeats(trainingSession.getAvailableSeats() - 1);

        try {
            enrollmentRepository.save(enrollment);
            trainingSessionRepository.save(trainingSession);
        } catch (DataIntegrityViolationException ex) {
            // Race condition - another request already created this enrollment
            // This is fine, just restore the seats
            trainingSession.setAvailableSeats(trainingSession.getAvailableSeats() + 1);
            trainingSessionRepository.save(trainingSession);
            logger.warn("Race condition detected for enrollment creation, seats restored");
        }
    }

    private void processFailedPayment(Payment payment, String paymentType, String externalId) {
        if (payment == null) {
            logger.warn("Payment not found for {} ID: {}", paymentType, externalId);
            return;
        }

        // Check if already processed
        if (payment.getStatus() == PaymentStatus.SUCCEEDED || payment.getStatus() == PaymentStatus.FAILED) {
            logger.info("Payment already processed for {} ID: {}", paymentType, externalId);
            return;
        }

        // Update payment status to FAILED
        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);

        logger.info("Payment failed for {} ID: {}", paymentType, externalId);
    }
}
