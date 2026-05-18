package com.trainingcenter.management.service;

import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.trainingcenter.management.entity.*;
import com.trainingcenter.management.exception.BadRequestException;
import com.trainingcenter.management.repository.EnrollmentRepository;
import com.trainingcenter.management.repository.PaymentRepository;
import com.trainingcenter.management.repository.TrainingSessionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

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
            throw new BadRequestException("Unable to deserialize checkout session");
        }

        String checkoutSessionId = checkoutSession.getId();
        Payment payment = paymentRepository.findByStripeCheckoutSessionId(checkoutSessionId).orElse(null);
        if (payment == null) {
            logger.warn("Payment not found for checkout session {}", checkoutSessionId);
            return;
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            logger.info("Payment already processed for checkout session {} with status {}", checkoutSessionId, payment.getStatus());
            return;
        }

        validateCheckoutMetadata(checkoutSession, payment);

        TrainingSession trainingSession = trainingSessionRepository.findByIdForUpdate(payment.getTrainingSession().getId())
                .orElseThrow(() -> new BadRequestException("Training session not found for payment"));
        Student student = payment.getStudent();

        if (enrollmentRepository.existsByStudentAndTrainingSession(student, trainingSession)) {
            payment.setStatus(PaymentStatus.SUCCEEDED);
            paymentRepository.save(payment);
            logger.info("Enrollment already exists for student {} and training session {}", student.getId(), trainingSession.getId());
            return;
        }

        if (trainingSession.getAvailableSeats() == null || trainingSession.getAvailableSeats() <= 0) {
            logger.error("No available seats for training session {} during webhook processing", trainingSession.getId());
            throw new IllegalStateException("No available seats for training session");
        }

        try {
            Enrollment enrollment = new Enrollment();
            enrollment.setStudent(student);
            enrollment.setTrainingSession(trainingSession);

            trainingSession.setAvailableSeats(trainingSession.getAvailableSeats() - 1);
            enrollmentRepository.save(enrollment);
            trainingSessionRepository.save(trainingSession);

            payment.setStatus(PaymentStatus.SUCCEEDED);
            paymentRepository.save(payment);
        } catch (DataIntegrityViolationException ex) {
            if (enrollmentRepository.existsByStudentAndTrainingSession(student, trainingSession)) {
                payment.setStatus(PaymentStatus.SUCCEEDED);
                paymentRepository.save(payment);
                logger.info("Enrollment already created by another transaction for student {} and training session {}", student.getId(), trainingSession.getId());
                return;
            }
            throw ex;
        }

        logger.info("Payment succeeded and enrollment created for checkout session {}", checkoutSessionId);
    }

    private void validateCheckoutMetadata(Session checkoutSession, Payment payment) {
        Map<String, String> metadata = checkoutSession.getMetadata();
        if (metadata == null) {
            throw new BadRequestException("Checkout session metadata is missing");
        }

        Long metadataStudentId = parseMetadataId(metadata.get("studentId"), "studentId");
        Long metadataTrainingSessionId = parseMetadataId(metadata.get("trainingSessionId"), "trainingSessionId");

        if (!metadataStudentId.equals(payment.getStudent().getId())
                || !metadataTrainingSessionId.equals(payment.getTrainingSession().getId())) {
            throw new BadRequestException("Checkout session metadata does not match the stored payment");
        }
    }

    private Long parseMetadataId(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(fieldName + " is missing from checkout metadata");
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new BadRequestException(fieldName + " is invalid in checkout metadata");
        }
    }
}
