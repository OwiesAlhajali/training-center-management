package com.trainingcenter.management.controller;

import com.trainingcenter.management.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/otp")
@RequiredArgsConstructor
public class OtpController {
    private final OtpService otpService;

    @PostMapping("/send")
    public ResponseEntity<String> send(@RequestParam String email) {
        otpService.generateAndSendOtp(email);
        return ResponseEntity.ok("تم إرسال الكود بنجاح");
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam String email, @RequestParam String code) {
        otpService.verifyOtp(email, code);
        return ResponseEntity.ok("تم التحقق بنجاح");
    }
}