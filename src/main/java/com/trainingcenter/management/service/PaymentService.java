package com.trainingcenter.management.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.trainingcenter.management.entity.*;
import com.trainingcenter.management.exception.ResourceNotFoundException;
import com.trainingcenter.management.repository.PaymentRepository;
import com.trainingcenter.management.repository.StudentRepository;
import com.trainingcenter.management.repository.TrainingSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final StudentRepository studentRepository;
    private final TrainingSessionRepository trainingSessionRepository;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Transactional
    public String initiatePayment(Long sessionId) throws StripeException {
        // Get current user from security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        // Get student
        Student student = studentRepository.findByUser(user);
        if (student == null) {
            throw new ResourceNotFoundException("Student not found for user");
        }

        // Fetch training session
        TrainingSession session = trainingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Training session not found"));

        // Check for existing pending payment
        Optional<Payment> existingPayment = paymentRepository.findByStudentAndTrainingSessionAndStatus(student, session, PaymentStatus.PENDING);
        if (existingPayment.isPresent()) {
            // Retrieve existing PaymentIntent and return its client secret
            PaymentIntent existingIntent = PaymentIntent.retrieve(existingPayment.get().getStripePaymentIntentId());
            return existingIntent.getClientSecret();
        }

        // Set Stripe API key
        Stripe.apiKey = stripeSecretKey;

        // Create PaymentIntent with metadata
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(session.getPrice().multiply(new java.math.BigDecimal(100)).longValue()) // amount in cents
                .setCurrency("usd")
                .putMetadata("studentId", student.getId().toString())
                .putMetadata("sessionId", sessionId.toString())
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        // Save Payment
        Payment payment = Payment.builder()
                .student(student)
                .trainingSession(session)
                .stripePaymentIntentId(paymentIntent.getId())
                .status(PaymentStatus.PENDING)
                .amount(session.getPrice().multiply(new java.math.BigDecimal(100)).longValue())
                .currency("usd")
                .build();

        paymentRepository.save(payment);

        // Return client secret
        return paymentIntent.getClientSecret();
    }
}
