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
    public ResponseEntity<String> initiatePayment(
            @PathVariable Long sessionId,
            @RequestHeader(value = "X-User-Identifier", required = false) String userIdentifierHeader) throws StripeException {

        if (userIdentifierHeader == null || userIdentifierHeader.isBlank()) {
            throw new IllegalArgumentException("X-User-Identifier header is required");
        }

        String checkoutUrl = paymentService.initiatePayment(sessionId, userIdentifierHeader);
        return ResponseEntity.ok(checkoutUrl);
    }
}
