package com.trainingcenter.management.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.model.StripeObject;
import com.trainingcenter.management.entity.*;
import com.trainingcenter.management.exception.BadRequestException;
import com.trainingcenter.management.repository.EnrollmentRepository;
import com.trainingcenter.management.repository.PaymentRepository;
import com.trainingcenter.management.repository.TrainingSessionRepository;
import com.trainingcenter.management.dto.RegisterRequestDTO;
import com.trainingcenter.management.exception.DuplicateResourceException;
import com.trainingcenter.management.service.RegisterService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WebhookService {

    private final PaymentRepository paymentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final TrainingSessionRepository trainingSessionRepository;
    private final ObjectMapper objectMapper;
    private final RegisterService registerService;

    private static final Logger logger = LoggerFactory.getLogger(WebhookService.class);

    @Transactional
    public void handleCheckoutSessionCompleted(Event event) {
        CheckoutSessionDetails checkoutSession = resolveCheckoutSession(event);

        Payment payment = paymentRepository.findByStripeCheckoutSessionId(checkoutSession.sessionId())
                .orElse(null);
        if (payment == null) {
            logger.warn("Payment not found for checkout session {}", checkoutSession.sessionId());
            return;
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            logger.info("Payment already processed for checkout session {} with status {}", checkoutSession.sessionId(), payment.getStatus());
            return;
        }

        validateCheckoutMetadata(checkoutSession, payment);

        TrainingSession trainingSession = trainingSessionRepository.findByIdForUpdate(payment.getTrainingSession().getId())
                .orElseThrow(() -> new BadRequestException("Training session not found for payment"));
        Student student = payment.getStudent();

        if (enrollmentRepository.existsByStudentAndTrainingSession(student, trainingSession)) {
            markPaymentSucceeded(payment);
            logger.info("Enrollment already exists for student {} and training session {}", student.getId(), trainingSession.getId());
            return;
        }

        ensureRegisterExists(student, trainingSession);

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

            markPaymentSucceeded(payment);
        } catch (DataIntegrityViolationException ex) {
            if (enrollmentRepository.existsByStudentAndTrainingSession(student, trainingSession)) {
                markPaymentSucceeded(payment);
                logger.info("Enrollment already created by another transaction for student {} and training session {}", student.getId(), trainingSession.getId());
                return;
            }
            throw ex;
        }

        logger.info("Payment succeeded and enrollment created for checkout session {}", checkoutSession.sessionId());
    }

    private CheckoutSessionDetails resolveCheckoutSession(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

        if (deserializer.getObject().isPresent()) {
            StripeObject stripeObject = deserializer.getObject().get();
            if (stripeObject instanceof Session) {
                Session session = (Session) stripeObject;
                return CheckoutSessionDetails.fromSession(session);
            }
        }

        try {
            StripeObject stripeObject = deserializer.deserializeUnsafe();
            if (stripeObject instanceof Session) {
                Session session = (Session) stripeObject;
                logger.info("Resolved checkout session {} using Stripe unsafe deserialization for event {}", session.getId(), event.getId());
                return CheckoutSessionDetails.fromSession(session);
            }
        } catch (EventDataObjectDeserializationException ex) {
            logger.warn("Stripe SDK could not deserialize checkout session for event {}: {}", event.getId(), ex.getMessage());
        }

        String rawJson = deserializer.getRawJson();
        if (rawJson == null || rawJson.isBlank()) {
            throw new BadRequestException("Unable to deserialize checkout session");
        }

        try {
            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode idNode = root.path("id");
            String sessionId = idNode.isMissingNode() || idNode.isNull() ? null : idNode.asText();
            if (sessionId == null || sessionId.isBlank()) {
                throw new BadRequestException("Unable to deserialize checkout session");
            }

            Map<String, String> metadata = extractMetadata(root.path("metadata"));
            logger.info("Resolved checkout session {} from raw webhook payload for event {}", sessionId, event.getId());
            return new CheckoutSessionDetails(sessionId, metadata);
        } catch (Exception ex) {
            throw new BadRequestException("Unable to deserialize checkout session");
        }
    }

    private void validateCheckoutMetadata(CheckoutSessionDetails checkoutSession, Payment payment) {
        Map<String, String> metadata = checkoutSession.metadata();
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

    private void ensureRegisterExists(Student student, TrainingSession trainingSession) {
        if (trainingSession.getCourse() == null || trainingSession.getCourse().getTenant() == null) {
            return;
        }

        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setStudentId(student.getId());
        registerRequest.setTenantId(trainingSession.getCourse().getTenant().getId());

        try {
            registerService.createRegister(registerRequest);
        } catch (DuplicateResourceException e) {
            logger.info("Register already exists for student {} and tenant {}", student.getId(), registerRequest.getTenantId());
        } catch (Exception e) {
            logger.warn("Register creation failed for student {} and tenant {}, continuing payment flow: {}",
                    student.getId(), registerRequest.getTenantId(), e.getMessage());
        }
    }

    private void markPaymentSucceeded(Payment payment) {
        payment.setStatus(PaymentStatus.SUCCEEDED);
        paymentRepository.save(payment);
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

    private Map<String, String> extractMetadata(JsonNode metadataNode) {
        Map<String, String> metadata = new HashMap<>();
        if (metadataNode == null || !metadataNode.isObject()) {
            return metadata;
        }

        ObjectNode objectNode = (ObjectNode) metadataNode;
        objectNode.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            JsonNode value = entry.getValue();
            metadata.put(key, value != null && !value.isNull() ? value.asText() : null);
        });
        return metadata;
    }

    private static class CheckoutSessionDetails {
        private final String sessionId;
        private final Map<String, String> metadata;

        private CheckoutSessionDetails(String sessionId, Map<String, String> metadata) {
            this.sessionId = sessionId;
            this.metadata = metadata;
        }

        public String sessionId() {
            return sessionId;
        }

        public Map<String, String> metadata() {
            return metadata;
        }

        private static CheckoutSessionDetails fromSession(Session session) {
            return new CheckoutSessionDetails(session.getId(), session.getMetadata());
        }
    }
}
