package com.restohub.adminapi.controller;

import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.service.AuthenticationService;
import com.restohub.adminapi.service.PasswordResetService;
import com.restohub.adminapi.util.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    
    private final AuthenticationService authenticationService;
    private final PasswordResetService passwordResetService;
    private final JwtTokenProvider jwtTokenProvider;
    
    public AuthenticationController(
            AuthenticationService authenticationService,
            PasswordResetService passwordResetService,
            JwtTokenProvider jwtTokenProvider) {
        this.authenticationService = authenticationService;
        this.passwordResetService = passwordResetService;
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authenticationService.login(request.getEmail(), request.getPassword());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if ("INVALID_CREDENTIALS".equals(e.getMessage())) {
                throw e;
            }
            throw new RuntimeException("MISSING_CREDENTIALS");
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authenticationService.logout(request.getRefreshToken());
        return ResponseEntity.ok(new MessageResponse("Выход выполнен успешно"));
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse response = authenticationService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getCurrentUser(Authentication authentication, HttpServletRequest request) {
        String email = authentication.getName();
        
        // Получаем токен из заголовка
        String authHeader = request.getHeader("Authorization");
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        
        String role = null;
        if (token != null) {
            role = jwtTokenProvider.getRoleFromToken(token);
        } else {
            // Если токен не найден, пытаемся получить роль из authorities
            if (authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
                String authority = authentication.getAuthorities().iterator().next().getAuthority();
                role = authority.replace("ROLE_", "");
            }
        }
        
        UserInfoResponse response = authenticationService.getCurrentUser(email, role);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok(new MessageResponse("Код восстановления отправлен на email"));
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getEmail(), request.getCode(), request.getNewPassword());
        return ResponseEntity.ok(new MessageResponse("Пароль успешно изменен"));
    }
}

