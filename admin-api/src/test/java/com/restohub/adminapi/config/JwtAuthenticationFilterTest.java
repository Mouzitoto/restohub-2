package com.restohub.adminapi.config;

import com.restohub.adminapi.util.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private JwtAuthenticationFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtTokenProvider);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        SecurityContextHolder.clearContext();
    }

    @Test
    void testDoFilterInternal_ExpiredToken_Returns401WithTokenExpired() throws Exception {
        // Arrange
        String expiredToken = "expired-token";
        request.addHeader("Authorization", "Bearer " + expiredToken);

        when(jwtTokenProvider.isTokenExpired(expiredToken)).thenReturn(true);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertTrue(response.getContentType().contains("application/json"), "Content-Type should be application/json");
        String content = response.getContentAsString();
        assertTrue(content.contains("TOKEN_EXPIRED"), "Response should contain TOKEN_EXPIRED");
        assertTrue(content.contains("Токен истек"), "Response should contain error message");
        
        verify(jwtTokenProvider, times(1)).isTokenExpired(expiredToken);
        verify(jwtTokenProvider, never()).validateToken(anyString());
    }

    @Test
    void testDoFilterInternal_ValidToken_SetsAuthentication() throws Exception {
        // Arrange
        String validToken = "valid-token";
        String email = "test@example.com";
        String role = "ADMIN";
        
        request.addHeader("Authorization", "Bearer " + validToken);

        when(jwtTokenProvider.isTokenExpired(validToken)).thenReturn(false);
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getEmailFromToken(validToken)).thenReturn(email);
        when(jwtTokenProvider.getRoleFromToken(validToken)).thenReturn(role);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(email, SecurityContextHolder.getContext().getAuthentication().getName());
        assertTrue(SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        
        verify(jwtTokenProvider, times(1)).isTokenExpired(validToken);
        verify(jwtTokenProvider, times(1)).validateToken(validToken);
        verify(jwtTokenProvider, times(1)).getEmailFromToken(validToken);
        verify(jwtTokenProvider, times(1)).getRoleFromToken(validToken);
    }

    @Test
    void testDoFilterInternal_NoAuthorizationHeader_ContinuesFilterChain() throws Exception {
        // Arrange - нет заголовка Authorization

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtTokenProvider, never()).isTokenExpired(anyString());
        verify(jwtTokenProvider, never()).validateToken(anyString());
    }

    @Test
    void testDoFilterInternal_InvalidTokenFormat_ContinuesFilterChain() throws Exception {
        // Arrange
        request.addHeader("Authorization", "InvalidFormat token");

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtTokenProvider, never()).isTokenExpired(anyString());
        verify(jwtTokenProvider, never()).validateToken(anyString());
    }

    @Test
    void testDoFilterInternal_ExpiredJwtException_Returns401WithTokenExpired() throws Exception {
        // Arrange
        String expiredToken = "expired-token";
        request.addHeader("Authorization", "Bearer " + expiredToken);

        when(jwtTokenProvider.isTokenExpired(expiredToken)).thenThrow(new io.jsonwebtoken.ExpiredJwtException(null, null, "Token expired"));

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertTrue(response.getContentType().contains("application/json"), "Content-Type should be application/json");
        String content = response.getContentAsString();
        assertTrue(content.contains("TOKEN_EXPIRED"), "Response should contain TOKEN_EXPIRED");
        assertTrue(content.contains("Токен истек"), "Response should contain error message");
        
        verify(jwtTokenProvider, times(1)).isTokenExpired(expiredToken);
    }
}

