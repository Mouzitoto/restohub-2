package com.restohub.adminapi.service;

import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.entity.EmailVerificationCode;
import com.restohub.adminapi.entity.Role;
import com.restohub.adminapi.entity.User;
import com.restohub.adminapi.repository.EmailVerificationCodeRepository;
import com.restohub.adminapi.repository.RoleRepository;
import com.restohub.adminapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PartnerRegistrationService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailVerificationCodeRepository emailVerificationCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    private static final int CODE_EXPIRATION_MINUTES = 15;
    private static final SecureRandom random = new SecureRandom();
    
    @Autowired
    public PartnerRegistrationService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            EmailVerificationCodeRepository emailVerificationCodeRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.emailVerificationCodeRepository = emailVerificationCodeRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }
    
    private String generateVerificationCode() {
        // Генерируем случайный 4-значный код
        return String.format("%04d", random.nextInt(10000));
    }
    
    @Transactional
    public RegisterPartnerResponse registerPartner(RegisterPartnerRequest request) {
        // Валидация
        validateRegisterRequest(request);
        
        // Проверка уникальности email
        if (userRepository.findByEmailAndIsActiveTrue(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email уже зарегистрирован");
        }
        
        // Пометить старые коды как использованные
        markOldCodesAsUsed(request.getEmail());
        
        // Генерируем случайный код подтверждения
        String verificationCode = generateVerificationCode();
        
        // Создать новый код подтверждения
        EmailVerificationCode code = new EmailVerificationCode();
        code.setUserEmail(request.getEmail());
        code.setCode(verificationCode);
        code.setPasswordHash(passwordEncoder.encode(request.getPassword())); // Сохраняем зашифрованный пароль
        code.setExpiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES));
        code.setUsed(false);
        emailVerificationCodeRepository.save(code);
        
        // Отправляем код подтверждения на email
        emailService.sendVerificationCode(request.getEmail(), verificationCode);
        
        return new RegisterPartnerResponse(
            "Код подтверждения отправлен на ваш email",
            request.getEmail()
        );
    }
    
    @Transactional
    public VerifyEmailResponse verifyEmail(VerifyEmailRequest request) {
        // Валидация
        if (request.getCode() == null || request.getCode().length() != 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Код должен содержать 4 символа");
        }
        
        // Поиск активного кода
        EmailVerificationCode code = emailVerificationCodeRepository
            .findFirstByUserEmailAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                request.getEmail(), LocalDateTime.now())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Код подтверждения не найден или истек"));
        
        // Проверка кода
        if (!code.getCode().equals(request.getCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неверный код подтверждения");
        }
        
        // Проверка, что пользователь еще не зарегистрирован
        if (userRepository.findByEmailAndIsActiveTrue(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email уже зарегистрирован");
        }
        
        // Получаем роль MANAGER
        Role managerRole = roleRepository.findByCodeAndIsActiveTrue("MANAGER")
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, "Роль MANAGER не найдена"));
        
        // Создаем пользователя с сохраненным паролем из кода
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(code.getPasswordHash()); // Используем сохраненный зашифрованный пароль
        user.setRole(managerRole);
        user.setIsActive(true);
        user.setEmailVerified(true);
        user = userRepository.save(user);
        
        // Пометить код как использованный
        code.setUsed(true);
        code.setUsedAt(LocalDateTime.now());
        emailVerificationCodeRepository.save(code);
        
        return new VerifyEmailResponse(
            "Email успешно подтвержден. Регистрация завершена.",
            user.getId()
        );
    }
    
    @Transactional
    public MessageResponse resendVerificationCode(ResendVerificationCodeRequest request) {
        // Проверка: пользователь не должен быть уже зарегистрирован и подтвержден
        User existingUser = userRepository.findByEmailAndIsActiveTrue(request.getEmail()).orElse(null);
        if (existingUser != null && existingUser.getEmailVerified()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email уже зарегистрирован и подтвержден");
        }
        
        // Найти последний неиспользованный код для получения пароля
        EmailVerificationCode existingCode = emailVerificationCodeRepository
            .findFirstByUserEmailAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                request.getEmail(), LocalDateTime.now().minusMinutes(CODE_EXPIRATION_MINUTES * 2))
            .orElse(null);
        
        String passwordHash;
        if (existingCode != null && existingCode.getPasswordHash() != null) {
            // Используем существующий пароль
            passwordHash = existingCode.getPasswordHash();
        } else {
            // Если кода нет, нужно получить пароль из запроса регистрации
            // Но в resend мы не получаем пароль, поэтому нужно либо хранить его отдельно,
            // либо требовать повторную регистрацию
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, 
                "Необходимо повторно зарегистрироваться с паролем");
        }
        
        // Пометить старые коды как использованные
        markOldCodesAsUsed(request.getEmail());
        
        // Генерируем новый случайный код подтверждения
        String verificationCode = generateVerificationCode();
        
        // Создать новый код подтверждения
        EmailVerificationCode code = new EmailVerificationCode();
        code.setUserEmail(request.getEmail());
        code.setCode(verificationCode);
        code.setPasswordHash(passwordHash); // Используем существующий пароль
        code.setExpiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES));
        code.setUsed(false);
        emailVerificationCodeRepository.save(code);
        
        // Отправляем код подтверждения на email
        emailService.sendVerificationCode(request.getEmail(), verificationCode);
        
        return new MessageResponse("Код подтверждения отправлен на ваш email");
    }
    
    public TermsResponse getTerms() {
        // Заглушка: возвращаем статический текст
        String terms = "Текст оферты (заглушка)\n\n" +
            "Настоящая оферта определяет условия использования платформы Resto-Hub...\n\n" +
            "[Здесь будет полный текст оферты]";
        return new TermsResponse(terms);
    }
    
    private void validateRegisterRequest(RegisterPartnerRequest request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email обязателен");
        }
        
        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пароль должен содержать минимум 8 символов");
        }
        
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пароли не совпадают");
        }
        
        if (request.getAgreeToTerms() == null || !request.getAgreeToTerms()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Необходимо согласие с офертой");
        }
    }
    
    private void markOldCodesAsUsed(String email) {
        List<EmailVerificationCode> oldCodes = emailVerificationCodeRepository
            .findByUserEmailAndUsedFalse(email);
        for (EmailVerificationCode oldCode : oldCodes) {
            oldCode.setUsed(true);
            oldCode.setUsedAt(LocalDateTime.now());
            emailVerificationCodeRepository.save(oldCode);
        }
    }
}

