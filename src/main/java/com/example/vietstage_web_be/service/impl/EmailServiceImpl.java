package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.service.IEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements IEmailService {

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${sendgrid.api-key}")
    private String sendgridApiKey;

    @Override
    public void sendOtpEmail(String to, String otpCode, String subject, String messageTemplate) throws Exception {
        log.info("Sending OTP email via SendGrid API to: {}", to);

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

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(sendgridApiKey);

        Map<String, Object> body = new HashMap<>();
        
        Map<String, Object> personalization = new HashMap<>();
        personalization.put("to", List.of(Map.of("email", to)));
        personalization.put("subject", subject);
        
        body.put("personalizations", List.of(personalization));
        body.put("from", Map.of("email", fromEmail, "name", "VietStage"));
        body.put("content", List.of(Map.of("type", "text/html", "value", htmlMsg)));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity("https://api.sendgrid.com/v3/mail/send", request, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("OTP email sent successfully via SendGrid to: {}", to);
            } else {
                log.error("SendGrid returned status code: {}", response.getStatusCode());
                throw new Exception("SendGrid API error");
            }
        } catch (Exception e) {
            log.error("Failed to send OTP email via SendGrid: {}", e.getMessage());
            throw new Exception("Cannot send OTP email via SendGrid", e);
        }
    }
}
