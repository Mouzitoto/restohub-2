package com.restohub.adminapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.service.AuthenticationService;
import com.restohub.adminapi.service.PasswordResetService;
import com.restohub.adminapi.util.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Тесты для AuthenticationController.
 * 
 * ВАЖНО: Эти тесты используют standaloneSetup, поэтому Spring Security НЕ загружается.
 * Для полного тестирования SecurityConfig нужно использовать @WebMvcTest или @SpringBootTest.
 * 
 * Для проверки SecurityConfig создайте интеграционные тесты с @SpringBootTest.
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private PasswordResetService passwordResetService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthenticationController authenticationController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authenticationController)
                .setControllerAdvice(new TestExceptionHandler())
                .setValidator(null)
                .build();
        objectMapper = new ObjectMapper();
    }

    // ========== POST /auth/login ==========

    @Test
    void testLogin_Success() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        LoginResponse response = new LoginResponse("access-token", "refresh-token", "MANAGER", 300L);

        when(authenticationService.login(anyString(), anyString())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.role").value("MANAGER"))
                .andExpect(jsonPath("$.expiresIn").value(300));

        verify(authenticationService, times(1)).login("test@example.com", "password123");
    }

    @Test
    void testLogin_InvalidCredentials() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrong-password");

        when(authenticationService.login(anyString(), anyString()))
                .thenThrow(new RuntimeException("INVALID_CREDENTIALS"));

        // Act & Assert
        // В standalone режиме исключение обрабатывается TestExceptionHandler
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // TestExceptionHandler возвращает 400 для RuntimeException

        verify(authenticationService, times(1)).login("test@example.com", "wrong-password");
    }

    // ========== POST /auth/logout ==========

    @Test
    void testLogout_Success() throws Exception {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-token-123");

        doNothing().when(authenticationService).logout(anyString());

        // Act & Assert
        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Выход выполнен успешно"));

        verify(authenticationService, times(1)).logout("refresh-token-123");
    }

    // ========== POST /auth/refresh ==========

    @Test
    void testRefresh_Success() throws Exception {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-token-123");

        RefreshTokenResponse response = new RefreshTokenResponse("new-access-token", "new-refresh-token", 300L);

        when(authenticationService.refresh(anyString())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"))
                .andExpect(jsonPath("$.expiresIn").value(300));

        verify(authenticationService, times(1)).refresh("refresh-token-123");
    }

    // ========== POST /auth/forgot-password ==========

    @Test
    void testForgotPassword_Success() throws Exception {
        // Arrange
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        doNothing().when(passwordResetService).requestPasswordReset(anyString());

        // Act & Assert
        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Код восстановления отправлен на email"));

        verify(passwordResetService, times(1)).requestPasswordReset("test@example.com");
    }

    // ========== POST /auth/reset-password ==========

    @Test
    void testResetPassword_Success() throws Exception {
        // Arrange
        AuthResetPasswordRequest request = new AuthResetPasswordRequest();
        request.setEmail("test@example.com");
        request.setCode("123456");
        request.setNewPassword("newPassword123");

        doNothing().when(passwordResetService).resetPassword(anyString(), anyString(), anyString());

        // Act & Assert
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Пароль успешно изменен"));

        verify(passwordResetService, times(1)).resetPassword("test@example.com", "123456", "newPassword123");
    }
}

