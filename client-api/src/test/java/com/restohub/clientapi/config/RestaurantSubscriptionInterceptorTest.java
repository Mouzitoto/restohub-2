package com.restohub.clientapi.config;

import com.restohub.clientapi.service.SubscriptionCheckService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantSubscriptionInterceptorTest {
    
    @Mock
    private SubscriptionCheckService subscriptionCheckService;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    @InjectMocks
    private RestaurantSubscriptionInterceptor interceptor;
    
    private StringWriter stringWriter;
    private PrintWriter printWriter;
    
    @BeforeEach
    void setUp() throws Exception {
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        // Используем lenient() чтобы избежать UnnecessaryStubbingException для тестов, которые не используют getWriter()
        lenient().when(response.getWriter()).thenReturn(printWriter);
    }
    
    @Test
    void preHandle_WithActiveSubscription_ReturnsTrue() throws Exception {
        // Arrange
        when(request.getRequestURI()).thenReturn("/client-api/restaurants/1");
        when(subscriptionCheckService.hasActiveSubscription(1L)).thenReturn(true);
        
        // Act
        boolean result = interceptor.preHandle(request, response, null);
        
        // Assert
        assertTrue(result);
        verify(subscriptionCheckService, times(1)).hasActiveSubscription(1L);
        verify(response, never()).setStatus(anyInt());
    }
    
    @Test
    void preHandle_WithoutSubscription_Returns404() throws Exception {
        // Arrange
        when(request.getRequestURI()).thenReturn("/client-api/restaurants/1");
        when(subscriptionCheckService.hasActiveSubscription(1L)).thenReturn(false);
        
        // Act
        boolean result = interceptor.preHandle(request, response, null);
        
        // Assert
        assertFalse(result);
        verify(subscriptionCheckService, times(1)).hasActiveSubscription(1L);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_NOT_FOUND);
        verify(response, times(1)).setContentType("application/json");
        verify(response, times(1)).setCharacterEncoding("UTF-8");
        verify(response, times(1)).getWriter();
        
        // Проверяем содержимое ответа
        printWriter.flush();
        String responseBody = stringWriter.toString();
        assertTrue(responseBody.contains("RESTAURANT_NOT_FOUND"));
        assertTrue(responseBody.contains("Ресторан не найден или неактивен"));
    }
    
    @Test
    void preHandle_ListEndpoint_ReturnsTrue() throws Exception {
        // Arrange
        when(request.getRequestURI()).thenReturn("/client-api/restaurants");
        
        // Act
        boolean result = interceptor.preHandle(request, response, null);
        
        // Assert
        assertTrue(result);
        verify(subscriptionCheckService, never()).hasActiveSubscription(anyLong());
    }
    
    @Test
    void preHandle_NonRestaurantPath_ReturnsTrue() throws Exception {
        // Arrange
        when(request.getRequestURI()).thenReturn("/client-api/other/path");
        
        // Act
        boolean result = interceptor.preHandle(request, response, null);
        
        // Assert
        assertTrue(result);
        verify(subscriptionCheckService, never()).hasActiveSubscription(anyLong());
    }
    
    @Test
    void preHandle_RestaurantPathWithSubPath_ChecksSubscription() throws Exception {
        // Arrange
        when(request.getRequestURI()).thenReturn("/client-api/restaurants/1/menu");
        when(subscriptionCheckService.hasActiveSubscription(1L)).thenReturn(true);
        
        // Act
        boolean result = interceptor.preHandle(request, response, null);
        
        // Assert
        assertTrue(result);
        verify(subscriptionCheckService, times(1)).hasActiveSubscription(1L);
    }
    
    @Test
    void preHandle_ShortPath_RestaurantId() throws Exception {
        // Arrange
        when(request.getRequestURI()).thenReturn("/client-api/r/2");
        when(subscriptionCheckService.hasActiveSubscription(2L)).thenReturn(true);
        
        // Act
        boolean result = interceptor.preHandle(request, response, null);
        
        // Assert
        assertTrue(result);
        verify(subscriptionCheckService, times(1)).hasActiveSubscription(2L);
    }
    
    @Test
    void preHandle_ShortPathWithSubPath_ChecksSubscription() throws Exception {
        // Arrange
        when(request.getRequestURI()).thenReturn("/client-api/r/3/table");
        when(subscriptionCheckService.hasActiveSubscription(3L)).thenReturn(false);
        
        // Act
        boolean result = interceptor.preHandle(request, response, null);
        
        // Assert
        assertFalse(result);
        verify(subscriptionCheckService, times(1)).hasActiveSubscription(3L);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
}

