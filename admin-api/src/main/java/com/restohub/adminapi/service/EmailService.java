package com.restohub.adminapi.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.from:no-reply@restohub.com}")
    private String fromEmail;
    
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    public void sendPasswordResetCode(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Восстановление пароля Resto-Hub");
            message.setText("Ваш код восстановления пароля: " + code + "\n\n" +
                    "Код действителен в течение 15 минут.\n\n" +
                    "Внимание: не передавайте этот код третьим лицам.");
            
            mailSender.send(message);
            logger.info("Password reset code sent to email: {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send password reset code to email: {}", toEmail, e);
            // Не пробрасываем исключение, чтобы не раскрывать информацию о существовании пользователя
        }
    }
}

