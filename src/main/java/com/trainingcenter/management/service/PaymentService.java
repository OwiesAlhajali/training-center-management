package com.trainingcenter.management.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.checkout.SessionCreateParams;
import com.trainingcenter.management.entity.Payment;
import com.trainingcenter.management.entity.PaymentStatus;
import com.trainingcenter.management.entity.Student;
import com.trainingcenter.management.entity.TrainingSession;
import com.trainingcenter.management.exception.BadRequestException;
import com.trainingcenter.management.exception.DuplicateResourceException;
import com.trainingcenter.management.exception.ResourceNotFoundException;
import com.trainingcenter.management.repository.EnrollmentRepository;
import com.trainingcenter.management.repository.PaymentRepository;
import com.trainingcenter.management.repository.StudentRepository;
import com.trainingcenter.management.repository.TrainingSessionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final StudentRepository studentRepository;
    private final TrainingSessionRepository trainingSessionRepository;
    private final EnrollmentRepository enrollmentRepository;

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${stripe.checkout.success-url:http://localhost:5173/payment/success}")
    private String checkoutSuccessUrl;

    @Value("${stripe.checkout.cancel-url:http://localhost:5173/payment/cancel}")
    private String checkoutCancelUrl;

    @Transactional
    public String initiatePayment(Long sessionId, Long studentId) throws StripeException {
        validateRequest(sessionId, studentId);
        validateStripeConfiguration();

        Stripe.apiKey = stripeSecretKey;

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        TrainingSession trainingSession = trainingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Training session not found"));

        validatePayableSession(trainingSession);

        if (enrollmentRepository.existsByStudentAndTrainingSession(student, trainingSession)) {
            throw new DuplicateResourceException("Student is already enrolled in this training session");
        }

        Optional<Payment> existingPayment = paymentRepository.findByStudentAndTrainingSession(student, trainingSession);
        if (existingPayment.isPresent()) {
            return handleExistingPayment(existingPayment.get(), student, trainingSession);
        }

        return createAndPersistCheckoutSession(student, trainingSession, null, false);
    }

    private String handleExistingPayment(Payment existingPayment, Student student, TrainingSession trainingSession) throws StripeException {
        if (existingPayment.getStatus() == PaymentStatus.SUCCEEDED) {
            throw new DuplicateResourceException("Payment already completed for this student and training session");
        }

        if (existingPayment.getStatus() == PaymentStatus.PENDING) {
            try {
                String checkoutUrl = resolveExistingCheckoutUrl(existingPayment.getStripeCheckoutSessionId());
                if (checkoutUrl != null) {
                    logger.info("Returning existing pending Stripe Checkout Session URL for student {} and session {}", student.getId(), trainingSession.getId());
                    return checkoutUrl;
                }
            } catch (StripeException ex) {
                logger.warn("Existing Stripe Checkout Session {} could not be reused, creating a replacement",
                        existingPayment.getStripeCheckoutSessionId(), ex);
            }

            logger.warn("Existing pending checkout session {} could not be reused, creating a replacement", existingPayment.getStripeCheckoutSessionId());
            return createAndPersistCheckoutSession(student, trainingSession, existingPayment, true);
        }

        logger.info("Recreating checkout session for payment in status {} for student {} and session {}",
                existingPayment.getStatus(), student.getId(), trainingSession.getId());
        return createAndPersistCheckoutSession(student, trainingSession, existingPayment, true);
    }

    private String createAndPersistCheckoutSession(Student student,
                                                   TrainingSession trainingSession,
                                                   Payment paymentToUpdate,
                                                   boolean refresh) throws StripeException {
        long amountInCents = toAmountInCents(trainingSession.getPrice());
        String idempotencyKey = refresh
                ? buildRefreshIdempotencyKey(student.getId(), trainingSession.getId(), paymentToUpdate)
                : buildCreationIdempotencyKey(student.getId(), trainingSession.getId());

        Session checkoutSession = Session.create(
                buildCheckoutSessionParams(student, trainingSession, amountInCents),
                RequestOptions.builder().setIdempotencyKey(idempotencyKey).build()
        );

        Payment payment = paymentToUpdate != null ? paymentToUpdate : new Payment();
        applyPaymentState(payment, student, trainingSession, checkoutSession.getId(), amountInCents);

        try {
            paymentRepository.save(payment);
        } catch (DataIntegrityViolationException ex) {
            logger.warn("Payment record collision detected for student {} and session {}. Reusing existing record.",
                    student.getId(), trainingSession.getId());

            Payment current = paymentRepository.findByStudentAndTrainingSession(student, trainingSession)
                    .orElseThrow(() -> new IllegalStateException("Payment record was not found after a uniqueness conflict"));

            if (current.getStatus() == PaymentStatus.SUCCEEDED) {
                throw new DuplicateResourceException("Payment already completed for this student and training session");
            }

            if (current.getStatus() == PaymentStatus.PENDING) {
                String checkoutUrl = resolveExistingCheckoutUrl(current.getStripeCheckoutSessionId());
                if (checkoutUrl != null) {
                    return checkoutUrl;
                }
            }

            applyPaymentState(current, student, trainingSession, checkoutSession.getId(), amountInCents);
            paymentRepository.save(current);
        }

        logger.info("Created Stripe Checkout Session {} for student {} and training session {}",
                checkoutSession.getId(), student.getId(), trainingSession.getId());
        return checkoutSession.getUrl();
    }

    private void validateRequest(Long sessionId, Long studentId) {
        if (sessionId == null || studentId == null || sessionId <= 0 || studentId <= 0) {
            throw new BadRequestException("studentId and sessionId are required");
        }
    }

    private void validateStripeConfiguration() {
        if (stripeSecretKey == null || stripeSecretKey.isBlank()) {
            throw new BadRequestException("Stripe secret key is not configured");
        }

        if (checkoutSuccessUrl == null || checkoutSuccessUrl.isBlank()) {
            throw new BadRequestException("Stripe success URL is not configured");
        }

        if (checkoutCancelUrl == null || checkoutCancelUrl.isBlank()) {
            throw new BadRequestException("Stripe cancel URL is not configured");
        }
    }

    private void validatePayableSession(TrainingSession trainingSession) {
        if (trainingSession.getStatus() == null) {
            throw new BadRequestException("Training session status is required");
        }

        if (trainingSession.getStatus() == com.trainingcenter.management.entity.SessionStatus.CANCELLED
                || trainingSession.getStatus() == com.trainingcenter.management.entity.SessionStatus.COMPLETED) {
            throw new BadRequestException("Training session is not available for payment");
        }

        if (trainingSession.getAvailableSeats() == null || trainingSession.getAvailableSeats() <= 0) {
            throw new BadRequestException("No available seats for this training session");
        }

        if (trainingSession.getPrice() == null || trainingSession.getPrice().signum() <= 0) {
            throw new BadRequestException("Training session price must be greater than zero");
        }
    }

    private long toAmountInCents(BigDecimal price) {
        return price.multiply(BigDecimal.valueOf(100)).longValueExact();
    }

    private SessionCreateParams buildCheckoutSessionParams(Student student, TrainingSession trainingSession, long amountInCents) {
        String productName = trainingSession.getCourse() != null && trainingSession.getCourse().getName() != null
                ? trainingSession.getCourse().getName() + " training session"
                : "Training session";

        return SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(checkoutSuccessUrl)
                .setCancelUrl(checkoutCancelUrl)
                .putMetadata("studentId", student.getId().toString())
                .putMetadata("trainingSessionId", trainingSession.getId().toString())
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd")
                                                .setUnitAmount(amountInCents)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(productName)
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();
    }

    private void applyPaymentState(Payment payment,
                                   Student student,
                                   TrainingSession trainingSession,
                                   String checkoutSessionId,
                                   long amountInCents) {
        payment.setStudent(student);
        payment.setTrainingSession(trainingSession);
        payment.setStripeCheckoutSessionId(checkoutSessionId);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setAmount(amountInCents);
        payment.setCurrency("usd");
    }

    private String resolveExistingCheckoutUrl(String checkoutSessionId) throws StripeException {
        if (checkoutSessionId == null || checkoutSessionId.isBlank()) {
            return null;
        }

        Session existingSession = Session.retrieve(checkoutSessionId);
        String url = existingSession.getUrl();
        return (url == null || url.isBlank()) ? null : url;
    }

    private String buildCreationIdempotencyKey(Long studentId, Long trainingSessionId) {
        return "checkout-" + studentId + "-" + trainingSessionId;
    }

    private String buildRefreshIdempotencyKey(Long studentId, Long trainingSessionId, Payment payment) {
        Long paymentId = payment != null && payment.getId() != null ? payment.getId() : 0L;
        return "checkout-refresh-" + studentId + "-" + trainingSessionId + "-" + paymentId + "-" + System.currentTimeMillis();
    }
}
