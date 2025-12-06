package com.restohub.adminapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.service.AuthenticationService;
import com.restohub.adminapi.service.PartnerRegistrationService;
import com.restohub.adminapi.service.PasswordResetService;
import com.restohub.adminapi.util.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private PasswordResetService passwordResetService;

    @MockBean
    private PartnerRegistrationService partnerRegistrationService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    // ========== POST /auth/login ==========

    @Test
    void testLogin_Success() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        LoginResponse response = new LoginResponse("access-token", "refresh-token", "MANAGER", 300L);

        doReturn(response).when(authenticationService).login(anyString(), anyString());

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
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.exceptionName").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").value("Неверный email или пароль"));

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

        doReturn(response).when(authenticationService).refresh(anyString());

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

    @Test
    void testRefresh_ExpiredRefreshToken_Returns401() throws Exception {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("expired-refresh-token");

        doThrow(new RuntimeException("REFRESH_TOKEN_EXPIRED")).when(authenticationService).refresh(anyString());

        // Act & Assert
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.exceptionName").value("REFRESH_TOKEN_EXPIRED"))
                .andExpect(jsonPath("$.message").value("Refresh token истек"));

        verify(authenticationService, times(1)).refresh("expired-refresh-token");
    }

    @Test
    void testRefresh_InvalidRefreshToken_Returns401() throws Exception {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("invalid-refresh-token");

        doThrow(new RuntimeException("INVALID_REFRESH_TOKEN")).when(authenticationService).refresh(anyString());

        // Act & Assert
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.exceptionName").value("INVALID_REFRESH_TOKEN"))
                .andExpect(jsonPath("$.message").value("Невалидный refresh token"));

        verify(authenticationService, times(1)).refresh("invalid-refresh-token");
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

    // ========== POST /auth/register ==========

    @Test
    void testRegister_Success() throws Exception {
        // Arrange
        RegisterPartnerRequest request = new RegisterPartnerRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("password123");
        request.setConfirmPassword("password123");
        request.setAgreeToTerms(true);

        RegisterPartnerResponse response = new RegisterPartnerResponse(
                "Код подтверждения отправлен на ваш email",
                "newuser@example.com"
        );

        doReturn(response).when(partnerRegistrationService).registerPartner(any(RegisterPartnerRequest.class));

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Код подтверждения отправлен на ваш email"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"));

        verify(partnerRegistrationService, times(1)).registerPartner(any(RegisterPartnerRequest.class));
    }

    // ========== POST /auth/verify-email ==========

    @Test
    void testVerifyEmail_Success() throws Exception {
        // Arrange
        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setEmail("newuser@example.com");
        request.setCode("1234");

        VerifyEmailResponse response = new VerifyEmailResponse(
                "Email успешно подтвержден. Регистрация завершена.",
                1L
        );

        doReturn(response).when(partnerRegistrationService).verifyEmail(any(VerifyEmailRequest.class));

        // Act & Assert
        mockMvc.perform(post("/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email успешно подтвержден. Регистрация завершена."))
                .andExpect(jsonPath("$.userId").value(1L));

        verify(partnerRegistrationService, times(1)).verifyEmail(any(VerifyEmailRequest.class));
    }

    // ========== POST /auth/resend-verification-code ==========

    @Test
    void testResendVerificationCode_Success() throws Exception {
        // Arrange
        ResendVerificationCodeRequest request = new ResendVerificationCodeRequest();
        request.setEmail("newuser@example.com");

        MessageResponse response = new MessageResponse("Код подтверждения отправлен на ваш email");

        doReturn(response).when(partnerRegistrationService).resendVerificationCode(any(ResendVerificationCodeRequest.class));

        // Act & Assert
        mockMvc.perform(post("/auth/resend-verification-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Код подтверждения отправлен на ваш email"));

        verify(partnerRegistrationService, times(1)).resendVerificationCode(any(ResendVerificationCodeRequest.class));
    }

    // ========== GET /auth/terms ==========

    @Test
    void testGetTerms_Success() throws Exception {
        // Arrange
        TermsResponse response = new TermsResponse("Текст оферты (заглушка)...");

        doReturn(response).when(partnerRegistrationService).getTerms();

        // Act & Assert
        mockMvc.perform(get("/auth/terms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.terms").value("Текст оферты (заглушка)..."));

        verify(partnerRegistrationService, times(1)).getTerms();
    }
}
