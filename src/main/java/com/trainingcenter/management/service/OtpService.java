package com.trainingcenter.management.service;

import com.trainingcenter.management.entity.OtpEntry;
import com.trainingcenter.management.exception.BadRequestException;
import com.trainingcenter.management.repository.OtpRepository;
import com.trainingcenter.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final OtpRepository otpRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;

    @Transactional
    public void generateAndSendOtp(String email) {


        if (!userRepository.existsByEmail(email)) {
        throw new BadRequestException("This email address is not registered with us.");
        }


        otpRepository.deleteExpiredOtps(LocalDateTime.now());
        

        SecureRandom random = new SecureRandom();
        String code = String.format("%06d", random.nextInt(999999));
        
        OtpEntry otp = OtpEntry.builder()
                .email(email)
                .code(code)
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .build();
        
        otpRepository.save(otp);
        emailService.sendOtpEmail(email, code);
    }

    @Transactional
    public void verifyOtp(String email, String code) {
        OtpEntry otp = otpRepository.findTopByEmailAndUsedFalseOrderByExpiryDateDesc(email)
                .orElseThrow(() -> new BadRequestException("No active code found"));

        if (otp.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("The code has expired");
        }

        if (!otp.getCode().equals(code)) {
            throw new BadRequestException("The verification code is invalid");
        }

        otp.setUsed(true);
        otpRepository.save(otp);
    }
}