package com.trainingcenter.management.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.trainingcenter.management.entity.*;
import com.trainingcenter.management.exception.ResourceNotFoundException;
import com.trainingcenter.management.repository.PaymentRepository;
import com.trainingcenter.management.repository.StudentRepository;
import com.trainingcenter.management.repository.TrainingSessionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final StudentRepository studentRepository;
    private final TrainingSessionRepository trainingSessionRepository;

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${stripe.checkout.success-url:http://localhost:3000/payment/success}")
    private String checkoutSuccessUrl;

    @Value("${stripe.checkout.cancel-url:http://localhost:3000/payment/cancel}")
    private String checkoutCancelUrl;

    @Transactional
    public String initiatePayment(Long sessionId, Long studentId) throws StripeException {
        if (studentId == null) {
            throw new ResourceNotFoundException("Student not found");
        }

        // Get student directly
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        // Fetch training session
        TrainingSession session = trainingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Training session not found"));

        // Set Stripe API key
        Stripe.apiKey = stripeSecretKey;

        // Check for existing pending payment
        Optional<Payment> existingPayment = paymentRepository.findByStudentAndTrainingSessionAndStatus(student, session, PaymentStatus.PENDING);
        if (existingPayment.isPresent()) {
            Session existingSession = Session.retrieve(existingPayment.get().getStripePaymentIntentId());
            logger.info("Returning existing pending Stripe Checkout Session URL for student {} and session {}", studentId, sessionId);
            return existingSession.getUrl();
        }

        Long amountInCents = session.getPrice().multiply(new java.math.BigDecimal(100)).longValue();

        SessionCreateParams params = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setSuccessUrl(checkoutSuccessUrl)
            .setCancelUrl(checkoutCancelUrl)
            .addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setQuantity(1L)
                    .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("usd")
                            .setUnitAmount(amountInCents)
                            .setProductData(
                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName("Training Session #" + sessionId)
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .putMetadata("studentId", student.getId().toString())
            .putMetadata("sessionId", sessionId.toString())
                .build();

        Session checkoutSession = Session.create(params);

        // Save Payment
        Payment payment = Payment.builder()
                .student(student)
                .trainingSession(session)
            .stripePaymentIntentId(checkoutSession.getId())
                .status(PaymentStatus.PENDING)
            .amount(amountInCents)
                .currency("usd")
                .build();

        paymentRepository.save(payment);
        logger.info("Created new Stripe Checkout Session {} for student {} and session {}", checkoutSession.getId(), studentId, sessionId);

        return checkoutSession.getUrl();
    }
}
