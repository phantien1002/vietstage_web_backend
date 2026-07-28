package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.service.IEmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements IEmailService {

    private final JavaMailSender javaMailSender;

    @org.springframework.beans.factory.annotation.Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendOtpEmail(String to, String otpCode, String subject, String messageTemplate) throws MessagingException {
        log.info("Sending OTP email to: {}", to);
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
        
        String htmlMsg = String.format(
                "<div style=\"font-family: Arial, sans-serif; padding: 20px;\">" +
                "<h2>VietStage - Thông báo xác thực</h2>" +
                "<p>%s</p>" +
                "<h3 style=\"color: #4CAF50; font-size: 24px; padding: 10px; border: 1px solid #4CAF50; display: inline-block;\">%s</h3>" +
                "<p>Mã này có hiệu lực trong vòng 5 phút.</p>" +
                "<p>Vui lòng không chia sẻ mã này cho bất kỳ ai.</p>" +
                "<br>" +
                "<p>Trân trọng,</p>" +
                "<p>Đội ngũ VietStage</p>" +
                "</div>",
                messageTemplate, otpCode);

        helper.setText(htmlMsg, true); // Set to true to indicate HTML content
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setFrom(fromEmail);

        javaMailSender.send(mimeMessage);
        log.info("OTP email sent successfully to: {}", to);
    }
}
