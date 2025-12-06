package com.restohub.adminapi.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final String RESEND_API_URL = "https://api.resend.com/emails";
    
    private final RestTemplate restTemplate;
    private final Environment environment;
    
    @Value("${resend.from:no-reply@restohub.com}")
    private String fromEmail;
    
    private String apiKey;
    
    public EmailService(RestTemplate restTemplate, Environment environment) {
        this.restTemplate = restTemplate;
        this.environment = environment;
    }
    
    @PostConstruct
    public void init() {
        // Читаем API ключ из переменной окружения
        apiKey = environment.getProperty("RESEND_API_KEY");
        
        if (apiKey == null || apiKey.isEmpty()) {
            logger.warn("RESEND_API_KEY is not set! Email sending will fail.");
        } else {
            logger.info("EmailService initialized with Resend API. From email: {}", fromEmail);
        }
    }
    
    public void sendPasswordResetCode(String toEmail, String code) {
        try {
            String subject = "Восстановление пароля Resto-Hub";
            String text = "Ваш код восстановления пароля: " + code + "\n\n" +
                    "Код действителен в течение 15 минут.\n\n" +
                    "Внимание: не передавайте этот код третьим лицам.";
            
            sendEmail(toEmail, subject, text);
            logger.info("Password reset code sent to email: {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send password reset code to email: {}", toEmail, e);
            // Не пробрасываем исключение, чтобы не раскрывать информацию о существовании пользователя
        }
    }
    
    public void sendVerificationCode(String toEmail, String code) {
        try {
            String subject = "Подтверждение регистрации Resto-Hub";
            String text = "Добро пожаловать в Resto-Hub!\n\n" +
                    "Ваш код подтверждения email: " + code + "\n\n" +
                    "Код действителен в течение 15 минут.\n\n" +
                    "Внимание: не передавайте этот код третьим лицам.\n\n" +
                    "Если вы не регистрировались в Resto-Hub, проигнорируйте это письмо.";
            
            sendEmail(toEmail, subject, text);
            logger.info("Verification code sent to email: {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send verification code to email: {}", toEmail, e);
            // Не пробрасываем исключение, чтобы не раскрывать информацию о существовании пользователя
        }
    }
    
    private void sendEmail(String toEmail, String subject, String text) {
        if (apiKey == null || apiKey.isEmpty()) {
            logger.error("Cannot send email: RESEND_API_KEY is not configured");
            return;
        }
        
        ResendEmailRequest request = new ResendEmailRequest();
        request.setFrom(fromEmail);
        request.setTo(toEmail);
        request.setSubject(subject);
        request.setText(text);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        
        HttpEntity<ResendEmailRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<ResendEmailResponse> response = restTemplate.exchange(
                    RESEND_API_URL,
                    HttpMethod.POST,
                    entity,
                    ResendEmailResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.debug("Email sent successfully. Resend ID: {}", response.getBody().getId());
            } else {
                logger.warn("Unexpected response from Resend API: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Error calling Resend API", e);
            throw new RuntimeException("Failed to send email via Resend", e);
        }
    }
    
    // DTO классы для Resend API
    private static class ResendEmailRequest {
        @JsonProperty("from")
        private String from;
        
        @JsonProperty("to")
        private String to;
        
        @JsonProperty("subject")
        private String subject;
        
        @JsonProperty("text")
        private String text;
        
        public String getFrom() {
            return from;
        }
        
        public void setFrom(String from) {
            this.from = from;
        }
        
        public String getTo() {
            return to;
        }
        
        public void setTo(String to) {
            this.to = to;
        }
        
        public String getSubject() {
            return subject;
        }
        
        public void setSubject(String subject) {
            this.subject = subject;
        }
        
        public String getText() {
            return text;
        }
        
        public void setText(String text) {
            this.text = text;
        }
    }
    
    private static class ResendEmailResponse {
        @JsonProperty("id")
        private String id;
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
    }
}

