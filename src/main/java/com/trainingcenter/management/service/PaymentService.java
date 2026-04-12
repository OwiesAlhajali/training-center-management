package com.trainingcenter.management.service;

import com.stripe.Stripe;
import com.stripe.net.RequestOptions;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.trainingcenter.management.entity.*;
import com.trainingcenter.management.exception.ResourceNotFoundException;
import com.trainingcenter.management.repository.PaymentRepository;
import com.trainingcenter.management.repository.StudentRepository;
import com.trainingcenter.management.repository.TrainingSessionRepository;
import com.trainingcenter.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private final UserRepository userRepository;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${stripe.currency:usd}")
    private String stripeCurrency;

    @Value("${stripe.checkout.success-url:http://localhost:3000/payment/success?session_id={CHECKOUT_SESSION_ID}}")
    private String successUrl;

    @Value("${stripe.checkout.cancel-url:http://localhost:3000/payment/cancel}")
    private String cancelUrl;

    @Transactional
    public String initiatePayment(Long sessionId, String authenticatedIdentifier) throws StripeException {
        if (authenticatedIdentifier == null || authenticatedIdentifier.isBlank()) {
            throw new IllegalArgumentException("Authenticated user identifier is required");
        }

        if (stripeSecretKey == null || stripeSecretKey.isBlank()) {
            throw new IllegalArgumentException("Stripe secret key is not configured");
        }

        Stripe.apiKey = stripeSecretKey;

        User user = userRepository.findByUsername(authenticatedIdentifier)
                .or(() -> userRepository.findByEmail(authenticatedIdentifier))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Get student
        Student student = studentRepository.findByUser(user);
        if (student == null) {
            throw new ResourceNotFoundException("Student not found for user");
        }

        // Fetch training session
        TrainingSession session = trainingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Training session not found"));

        Long amountInSmallestUnit = session.getPrice().multiply(BigDecimal.valueOf(100)).longValue();

        // Check for existing pending checkout
        Optional<Payment> existingPayment = paymentRepository.findByStudentAndTrainingSessionAndStatus(student, session, PaymentStatus.PENDING);
        if (existingPayment.isPresent()) {
            Payment pendingPayment = existingPayment.get();
            Session existingSession = Session.retrieve(pendingPayment.getStripeCheckoutSessionId());

            // Reuse only active unpaid checkout links.
            if (existingSession != null
                    && existingSession.getUrl() != null
                    && "open".equalsIgnoreCase(existingSession.getStatus())
                    && "unpaid".equalsIgnoreCase(existingSession.getPaymentStatus())) {
                return existingSession.getUrl();
            }

            // If Stripe already marks it paid, avoid creating another payable link.
            if (existingSession != null && "paid".equalsIgnoreCase(existingSession.getPaymentStatus())) {
                throw new IllegalArgumentException("Payment already completed. Please wait for webhook processing.");
            }

            Session freshSession = createCheckoutSession(student, session, amountInSmallestUnit);
            pendingPayment.setStripeCheckoutSessionId(freshSession.getId());
            pendingPayment.setStripePaymentIntentId(null);
            pendingPayment.setAmount(amountInSmallestUnit);
            pendingPayment.setCurrency(stripeCurrency);
            paymentRepository.save(pendingPayment);
            return freshSession.getUrl();
        }

        Session checkoutSession = createCheckoutSession(student, session, amountInSmallestUnit);

        // Save Payment
        Payment payment = Payment.builder()
                .student(student)
                .trainingSession(session)
                .stripeCheckoutSessionId(checkoutSession.getId())
                .stripePaymentIntentId(null)
                .status(PaymentStatus.PENDING)
                .amount(amountInSmallestUnit)
                .currency(stripeCurrency)
                .build();

        paymentRepository.save(payment);

        // Return Checkout URL
        return checkoutSession.getUrl();
    }

    private Session createCheckoutSession(Student student, TrainingSession session, Long amountInSmallestUnit) throws StripeException {
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .putMetadata("studentId", student.getId().toString())
                .putMetadata("sessionId", session.getId().toString())
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(stripeCurrency)
                                .setUnitAmount(amountInSmallestUnit)
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(session.getCourse().getName())
                                        .build())
                                .build())
                        .build())
                .build();

        // Keep idempotency scoped per request so expired links can be replaced.
        RequestOptions requestOptions = RequestOptions.builder()
                .setIdempotencyKey("checkout-" + student.getId() + "-" + session.getId() + "-" + System.currentTimeMillis())
                .build();

        return Session.create(params, requestOptions);
    }
}
