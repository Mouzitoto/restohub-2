package com.restohub.adminapi.controller;

import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.service.AnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsControllerTest {

    @Mock
    private AnalyticsService analyticsService;

    @InjectMocks
    private AnalyticsController analyticsController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(analyticsController)
                .setControllerAdvice(new TestExceptionHandler())
                .setValidator(null)
                .build();
    }

    // ========== GET /r/{id}/analytics/booking - аналитика бронирований ==========

    @Test
    void testGetBookingAnalytics_Success() throws Exception {
        // Arrange
        BookingAnalyticsResponse response = new BookingAnalyticsResponse();
        response.setRestaurantId(1L);
        // Добавляем другие поля если они есть в BookingAnalyticsResponse

        when(analyticsService.getBookingAnalytics(eq(1L), isNull(), isNull(), eq("day")))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/r/1/analytics/booking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restaurantId").value(1L));

        verify(analyticsService, times(1)).getBookingAnalytics(eq(1L), isNull(), isNull(), eq("day"));
    }

    @Test
    void testGetBookingAnalytics_WithDateRange() throws Exception {
        // Arrange
        BookingAnalyticsResponse response = new BookingAnalyticsResponse();
        response.setRestaurantId(1L);

        LocalDate dateFrom = LocalDate.of(2024, 1, 1);
        LocalDate dateTo = LocalDate.of(2024, 1, 31);

        when(analyticsService.getBookingAnalytics(eq(1L), eq(dateFrom), eq(dateTo), eq("day")))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/r/1/analytics/booking")
                        .param("dateFrom", "2024-01-01")
                        .param("dateTo", "2024-01-31")
                        .param("groupBy", "day"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restaurantId").value(1L));

        verify(analyticsService, times(1)).getBookingAnalytics(eq(1L), eq(dateFrom), eq(dateTo), eq("day"));
    }

    // ========== GET /r/{id}/analytics/pre-order - аналитика предзаказов ==========

    @Test
    void testGetPreOrderAnalytics_Success() throws Exception {
        // Arrange
        PreOrderAnalyticsResponse response = new PreOrderAnalyticsResponse();
        response.setRestaurantId(1L);

        when(analyticsService.getPreOrderAnalytics(eq(1L), isNull(), isNull(), eq("day")))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/r/1/analytics/pre-order"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restaurantId").value(1L));

        verify(analyticsService, times(1)).getPreOrderAnalytics(eq(1L), isNull(), isNull(), eq("day"));
    }

    @Test
    void testGetPreOrderAnalytics_WithDateRange() throws Exception {
        // Arrange
        PreOrderAnalyticsResponse response = new PreOrderAnalyticsResponse();
        response.setRestaurantId(1L);

        LocalDate dateFrom = LocalDate.of(2024, 1, 1);
        LocalDate dateTo = LocalDate.of(2024, 1, 31);

        when(analyticsService.getPreOrderAnalytics(eq(1L), eq(dateFrom), eq(dateTo), eq("week")))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/r/1/analytics/pre-order")
                        .param("dateFrom", "2024-01-01")
                        .param("dateTo", "2024-01-31")
                        .param("groupBy", "week"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restaurantId").value(1L));

        verify(analyticsService, times(1)).getPreOrderAnalytics(eq(1L), eq(dateFrom), eq(dateTo), eq("week"));
    }

    // ========== GET /r/{id}/analytics/client - аналитика клиентов ==========

    @Test
    void testGetClientAnalytics_Success() throws Exception {
        // Arrange
        ClientAnalyticsResponse response = new ClientAnalyticsResponse();
        response.setRestaurantId(1L);

        when(analyticsService.getClientAnalytics(eq(1L), isNull(), isNull()))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/r/1/analytics/client"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restaurantId").value(1L));

        verify(analyticsService, times(1)).getClientAnalytics(eq(1L), isNull(), isNull());
    }

    @Test
    void testGetClientAnalytics_WithDateRange() throws Exception {
        // Arrange
        ClientAnalyticsResponse response = new ClientAnalyticsResponse();
        response.setRestaurantId(1L);

        LocalDate dateFrom = LocalDate.of(2024, 1, 1);
        LocalDate dateTo = LocalDate.of(2024, 1, 31);

        when(analyticsService.getClientAnalytics(eq(1L), eq(dateFrom), eq(dateTo)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/r/1/analytics/client")
                        .param("dateFrom", "2024-01-01")
                        .param("dateTo", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restaurantId").value(1L));

        verify(analyticsService, times(1)).getClientAnalytics(eq(1L), eq(dateFrom), eq(dateTo));
    }

    // ========== GET /r/{id}/analytics/overview - обзорная аналитика ==========

    @Test
    void testGetOverview_Success() throws Exception {
        // Arrange
        AnalyticsOverviewResponse response = new AnalyticsOverviewResponse();
        response.setRestaurantId(1L);

        when(analyticsService.getOverview(eq(1L), isNull(), isNull()))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/r/1/analytics/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restaurantId").value(1L));

        verify(analyticsService, times(1)).getOverview(eq(1L), isNull(), isNull());
    }

    @Test
    void testGetOverview_WithDateRange() throws Exception {
        // Arrange
        AnalyticsOverviewResponse response = new AnalyticsOverviewResponse();
        response.setRestaurantId(1L);

        LocalDate dateFrom = LocalDate.of(2024, 1, 1);
        LocalDate dateTo = LocalDate.of(2024, 1, 31);

        when(analyticsService.getOverview(eq(1L), eq(dateFrom), eq(dateTo)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/r/1/analytics/overview")
                        .param("dateFrom", "2024-01-01")
                        .param("dateTo", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restaurantId").value(1L));

        verify(analyticsService, times(1)).getOverview(eq(1L), eq(dateFrom), eq(dateTo));
    }

    // ========== GET /r/{id}/analytics/export - экспорт данных ==========

    @Test
    void testExportData_Success() throws Exception {
        // Arrange
        String exportData = "{\"data\":[]}";

        when(analyticsService.exportData(eq(1L), eq("booking"), eq("json"), isNull(), isNull()))
                .thenReturn(exportData);

        // Act & Assert
        mockMvc.perform(get("/r/1/analytics/export")
                        .param("type", "booking")
                        .param("format", "json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());

        verify(analyticsService, times(1)).exportData(eq(1L), eq("booking"), eq("json"), isNull(), isNull());
    }

    @Test
    void testExportData_WithDateRange() throws Exception {
        // Arrange
        String exportData = "id,name\n1,Test";

        LocalDate dateFrom = LocalDate.of(2024, 1, 1);
        LocalDate dateTo = LocalDate.of(2024, 1, 31);

        when(analyticsService.exportData(eq(1L), eq("booking"), eq("csv"), eq(dateFrom), eq(dateTo)))
                .thenReturn(exportData);

        // Act & Assert
        mockMvc.perform(get("/r/1/analytics/export")
                        .param("type", "booking")
                        .param("format", "csv")
                        .param("dateFrom", "2024-01-01")
                        .param("dateTo", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());

        verify(analyticsService, times(1)).exportData(eq(1L), eq("booking"), eq("csv"), eq(dateFrom), eq(dateTo));
    }
}

