package com.example.vietstage_web_be.service;

public interface IEmailService {
    void sendOtpEmail(String to, String otpCode, String subject, String messageTemplate) throws Exception;
}
