package com.restohub.adminapi.controller;

import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.service.AnalyticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentMatchers;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AnalyticsControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    // ========== GET /r/{id}/analytics/booking - аналитика бронирований ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetBookingAnalytics_Success() throws Exception {
        // Arrange
        BookingAnalyticsResponse response = new BookingAnalyticsResponse();
        response.setRestaurantId(1L);

        doReturn(response).when(analyticsService).getBookingAnalytics(eq(1L), ArgumentMatchers.nullable(LocalDate.class), ArgumentMatchers.nullable(LocalDate.class), eq("day"));

        // Act & Assert
        mockMvc.perform(get("/r/1/analytics/booking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restaurantId").value(1L));

        verify(analyticsService, times(1)).getBookingAnalytics(eq(1L), ArgumentMatchers.nullable(LocalDate.class), ArgumentMatchers.nullable(LocalDate.class), eq("day"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetBookingAnalytics_WithDateRange() throws Exception {
        // Arrange
        BookingAnalyticsResponse response = new BookingAnalyticsResponse();
        response.setRestaurantId(1L);

        LocalDate dateFrom = LocalDate.of(2024, 1, 1);
        LocalDate dateTo = LocalDate.of(2024, 1, 31);

        doReturn(response).when(analyticsService).getBookingAnalytics(eq(1L), eq(dateFrom), eq(dateTo), eq("day"));

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
    @WithMockUser(roles = "MANAGER")
    void testGetPreOrderAnalytics_Success() throws Exception {
        // Arrange
        PreOrderAnalyticsResponse response = new PreOrderAnalyticsResponse();
        response.setRestaurantId(1L);

        doReturn(response).when(analyticsService).getPreOrderAnalytics(eq(1L), ArgumentMatchers.nullable(LocalDate.class), ArgumentMatchers.nullable(LocalDate.class), eq("day"));

        // Act & Assert
        mockMvc.perform(get("/r/1/analytics/pre-order"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restaurantId").value(1L));

        verify(analyticsService, times(1)).getPreOrderAnalytics(eq(1L), ArgumentMatchers.nullable(LocalDate.class), ArgumentMatchers.nullable(LocalDate.class), eq("day"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetPreOrderAnalytics_WithDateRange() throws Exception {
        // Arrange
        PreOrderAnalyticsResponse response = new PreOrderAnalyticsResponse();
        response.setRestaurantId(1L);

        LocalDate dateFrom = LocalDate.of(2024, 1, 1);
        LocalDate dateTo = LocalDate.of(2024, 1, 31);

        doReturn(response).when(analyticsService).getPreOrderAnalytics(eq(1L), eq(dateFrom), eq(dateTo), eq("week"));

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
    @WithMockUser(roles = "MANAGER")
    void testGetClientAnalytics_Success() throws Exception {
        // Arrange
        ClientAnalyticsResponse response = new ClientAnalyticsResponse();
        response.setRestaurantId(1L);

        doReturn(response).when(analyticsService).getClientAnalytics(eq(1L), ArgumentMatchers.nullable(LocalDate.class), ArgumentMatchers.nullable(LocalDate.class));

        // Act & Assert
        mockMvc.perform(get("/r/1/analytics/client"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restaurantId").value(1L));

        verify(analyticsService, times(1)).getClientAnalytics(eq(1L), ArgumentMatchers.nullable(LocalDate.class), ArgumentMatchers.nullable(LocalDate.class));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetClientAnalytics_WithDateRange() throws Exception {
        // Arrange
        ClientAnalyticsResponse response = new ClientAnalyticsResponse();
        response.setRestaurantId(1L);

        LocalDate dateFrom = LocalDate.of(2024, 1, 1);
        LocalDate dateTo = LocalDate.of(2024, 1, 31);

        doReturn(response).when(analyticsService).getClientAnalytics(eq(1L), eq(dateFrom), eq(dateTo));

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
    @WithMockUser(roles = "MANAGER")
    void testGetOverview_Success() throws Exception {
        // Arrange
        AnalyticsOverviewResponse response = new AnalyticsOverviewResponse();
        response.setRestaurantId(1L);

        doReturn(response).when(analyticsService).getOverview(eq(1L), ArgumentMatchers.nullable(LocalDate.class), ArgumentMatchers.nullable(LocalDate.class));

        // Act & Assert
        mockMvc.perform(get("/r/1/analytics/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restaurantId").value(1L));

        verify(analyticsService, times(1)).getOverview(eq(1L), ArgumentMatchers.nullable(LocalDate.class), ArgumentMatchers.nullable(LocalDate.class));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetOverview_WithDateRange() throws Exception {
        // Arrange
        AnalyticsOverviewResponse response = new AnalyticsOverviewResponse();
        response.setRestaurantId(1L);

        LocalDate dateFrom = LocalDate.of(2024, 1, 1);
        LocalDate dateTo = LocalDate.of(2024, 1, 31);

        doReturn(response).when(analyticsService).getOverview(eq(1L), eq(dateFrom), eq(dateTo));

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
    @WithMockUser(roles = "MANAGER")
    void testExportData_Success() throws Exception {
        // Arrange
        String exportData = "{\"data\":[]}";

        doReturn(exportData).when(analyticsService).exportData(eq(1L), eq("booking"), eq("json"), ArgumentMatchers.nullable(LocalDate.class), ArgumentMatchers.nullable(LocalDate.class));

        // Act & Assert
        mockMvc.perform(get("/r/1/analytics/export")
                        .param("type", "booking")
                        .param("format", "json"))
                .andExpect(status().isOk())
                .andExpect(content().string(exportData));

        verify(analyticsService, times(1)).exportData(eq(1L), eq("booking"), eq("json"), ArgumentMatchers.nullable(LocalDate.class), ArgumentMatchers.nullable(LocalDate.class));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testExportData_WithDateRange() throws Exception {
        // Arrange
        String exportData = "id,name\n1,Test";

        LocalDate dateFrom = LocalDate.of(2024, 1, 1);
        LocalDate dateTo = LocalDate.of(2024, 1, 31);

        doReturn(exportData).when(analyticsService).exportData(eq(1L), eq("booking"), eq("csv"), eq(dateFrom), eq(dateTo));

        // Act & Assert
        mockMvc.perform(get("/r/1/analytics/export")
                        .param("type", "booking")
                        .param("format", "csv")
                        .param("dateFrom", "2024-01-01")
                        .param("dateTo", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(content().string(exportData));

        verify(analyticsService, times(1)).exportData(eq(1L), eq("booking"), eq("csv"), eq(dateFrom), eq(dateTo));
    }
}

