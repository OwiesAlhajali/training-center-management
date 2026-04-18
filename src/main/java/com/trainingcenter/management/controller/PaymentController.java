package com.trainingcenter.management.controller;

import com.stripe.exception.StripeException;
import com.trainingcenter.management.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate/{sessionId}")
    public ResponseEntity<String> initiatePayment(@PathVariable Long sessionId) throws StripeException {
        String clientSecret = paymentService.initiatePayment(sessionId);
        return ResponseEntity.ok(clientSecret);
    }
}
