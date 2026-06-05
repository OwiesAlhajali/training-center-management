package com.trainingcenter.management.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {
    private static final String RESEND_EMAIL_URL = "https://api.resend.com/emails";

    private final RestTemplate restTemplate;

    @Value("${RESEND_API_KEY}")
    private String resendApiKey;

    @Value("${RESEND_FROM_EMAIL}")
    private String senderEmail;

    public void sendOtpEmail(String to, String code) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("from", senderEmail);
        requestBody.put("to", to);
        requestBody.put("subject", "Verification code- Training Center");
        requestBody.put("html", "<p>Your verification code is: <strong>" + code + "</strong></p><p>The code expires after 5 minutes.</p>");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(resendApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            restTemplate.postForEntity(RESEND_EMAIL_URL, requestEntity, String.class);
        } catch (RestClientException ex) {
            throw new RuntimeException("Failed to send OTP email via Resend", ex);
        }
    }
}