package com.restohub.adminapi.service;

import com.restohub.adminapi.entity.PasswordResetCode;
import com.restohub.adminapi.entity.User;
import com.restohub.adminapi.repository.PasswordResetCodeRepository;
import com.restohub.adminapi.repository.RefreshTokenRepository;
import com.restohub.adminapi.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PasswordResetService {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);
    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRATION_MINUTES = 15;
    
    private final UserRepository userRepository;
    private final PasswordResetCodeRepository passwordResetCodeRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom random = new SecureRandom();
    
    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetCodeRepository passwordResetCodeRepository,
            EmailService emailService,
            PasswordEncoder passwordEncoder,
            RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.passwordResetCodeRepository = passwordResetCodeRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
    }
    
    @Transactional
    public void requestPasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmailAndIsActiveTrue(email);
        
        // Всегда возвращаем успешный ответ для безопасности
        if (userOpt.isEmpty()) {
            logger.info("Password reset requested for non-existent email: {}", email);
            return;
        }
        
        User user = userOpt.get();
        
        // Удаляем старые неиспользованные коды для этого пользователя
        List<PasswordResetCode> oldCodes = passwordResetCodeRepository.findByUserAndUsedFalse(user);
        passwordResetCodeRepository.deleteAll(oldCodes);
        
        // Генерируем 6-значный код
        String code = generateResetCode();
        String hashedCode = passwordEncoder.encode(code);
        
        // Сохраняем код в БД
        PasswordResetCode resetCode = new PasswordResetCode();
        resetCode.setUser(user);
        resetCode.setCode(hashedCode);
        resetCode.setEmail(user.getEmail());
        resetCode.setExpiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES));
        resetCode.setUsed(false);
        passwordResetCodeRepository.save(resetCode);
        
        // Отправляем email
        emailService.sendPasswordResetCode(user.getEmail(), code);
        
        logger.info("Password reset code generated for user: {}", user.getEmail());
    }
    
    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        Optional<User> userOpt = userRepository.findByEmailAndIsActiveTrue(email);
        
        if (userOpt.isEmpty()) {
            throw new RuntimeException("USER_NOT_FOUND");
        }
        
        User user = userOpt.get();
        
        // Ищем активный код восстановления
        Optional<PasswordResetCode> codeOpt = passwordResetCodeRepository.findActiveByUser(user, LocalDateTime.now());
        
        if (codeOpt.isEmpty()) {
            throw new RuntimeException("INVALID_RESET_CODE");
        }
        
        PasswordResetCode resetCode = codeOpt.get();
        
        // Проверяем, не истек ли код
        if (resetCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            resetCode.setUsed(true);
            resetCode.setUsedAt(LocalDateTime.now());
            passwordResetCodeRepository.save(resetCode);
            throw new RuntimeException("RESET_CODE_EXPIRED");
        }
        
        // Проверяем, не использован ли код
        if (resetCode.getUsed()) {
            throw new RuntimeException("RESET_CODE_ALREADY_USED");
        }
        
        // Проверяем код
        if (!passwordEncoder.matches(code, resetCode.getCode())) {
            throw new RuntimeException("INVALID_RESET_CODE");
        }
        
        // Обновляем пароль
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Помечаем код как использованный
        resetCode.setUsed(true);
        resetCode.setUsedAt(LocalDateTime.now());
        passwordResetCodeRepository.save(resetCode);
        
        // Удаляем все refresh tokens пользователя (принудительный выход из всех устройств)
        refreshTokenRepository.deleteByUser(user);
        
        logger.info("Password reset successful for user: {}", user.getEmail());
    }
    
    private String generateResetCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}

