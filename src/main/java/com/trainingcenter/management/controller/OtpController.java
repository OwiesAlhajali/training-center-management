package com.trainingcenter.management.controller;

import com.trainingcenter.management.dto.ResetPasswordRequestDTO;
import com.trainingcenter.management.service.OtpService;
import com.trainingcenter.management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/otp")
@RequiredArgsConstructor
public class OtpController {
    private final OtpService otpService;
    private final UserService userService;

    @PostMapping("/send")
    public ResponseEntity<String> send(@RequestParam String email) {
        otpService.generateAndSendOtp(email);
        return ResponseEntity.ok("The code has been sent successfully");
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam String email, @RequestParam String code) {
        otpService.verifyOtp(email, code);
        return ResponseEntity.ok("Verified successfully");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequestDTO request) {
        otpService.ensureOtpVerified(request.getEmail());
        userService.resetPassword(request.getEmail(), request.getNewPassword());
        return ResponseEntity.ok("Password has been reset successfully");
    }
}


