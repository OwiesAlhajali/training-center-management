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

import java.util.Objects;

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
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {

        if (sigHeader == null || sigHeader.isBlank() || payload == null || payload.isBlank()) {
            log.warn("Missing Stripe signature or payload");
            return ResponseEntity.badRequest().build();
        }

        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.error("Stripe webhook secret is not configured");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        Event event;
        try {
            // Verify Stripe signature
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Signature verification failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Invalid JSON or error parsing webhook payload: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }

        // Handle specific event types
        try {
            if (Objects.equals("checkout.session.completed", event.getType())) {
                log.info("Processing checkout.session.completed event: {}", event.getId());
                webhookService.handleCheckoutSessionCompleted(event);
            } else if (Objects.equals("checkout.session.async_payment_failed", event.getType())) {
                log.info("Processing checkout.session.async_payment_failed event: {}", event.getId());
                webhookService.handleCheckoutSessionFailed(event);
            } else {
                log.info("Ignoring unsupported Stripe event type: {}", event.getType());
            }
        } catch (IllegalArgumentException e) {
            log.error("Webhook validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error processing webhook event: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok().build();
    }
}
