package com.trainingcenter.management.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import com.trainingcenter.management.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/webhook/stripe")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @PostMapping
    public ResponseEntity<?> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        if (sigHeader == null || payload == null) {
            log.warn("Missing Stripe signature or payload");
            return ResponseEntity.badRequest().build();
        }

        Event event;
        try {
            // Verify Stripe signature
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (Exception e) {
            if (e instanceof SignatureVerificationException) {
                log.error("Signature verification failed: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            log.error("Invalid JSON or error parsing webhook payload: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }

        // Handle specific event types
        try {
            if ("payment_intent.succeeded".equals(event.getType())) {
                log.info("Processing payment_intent.succeeded event: {}", event.getId());
                webhookService.handlePaymentIntentSucceeded(event);
            } else if ("payment_intent.payment_failed".equals(event.getType())) {
                log.info("Processing payment_intent.payment_failed event: {}", event.getId());
                webhookService.handlePaymentIntentFailed(event);
            }
        } catch (Exception e) {
            log.error("Error processing webhook event: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok().build();
    }
}
