package com.example.vietstage_web_be.service;

import jakarta.mail.MessagingException;

public interface IEmailService {
    void sendOtpEmail(String to, String otpCode, String subject, String messageTemplate) throws MessagingException;
}
