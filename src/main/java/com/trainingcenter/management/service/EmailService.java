package com.trainingcenter.management.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendOtpEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("كود التحقق - Training Center");
        message.setText("كود التحقق الخاص بك هو: " + code + "\nتنتهي صلاحية الكود بعد 5 دقائق.");
        mailSender.send(message);
    }
}