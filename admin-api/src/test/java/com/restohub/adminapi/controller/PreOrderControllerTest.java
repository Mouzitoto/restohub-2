package com.restohub.adminapi.controller;

import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.service.PreOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PreOrderControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PreOrderService preOrderService;

    // ========== GET /r/{id}/pre-order - список предзаказов ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetPreOrders_Success() throws Exception {
        // Arrange
        PreOrderListItemResponse item1 = new PreOrderListItemResponse();
        item1.setId(1L);
        item1.setRestaurantId(1L);
        item1.setBookingId(1L);
        item1.setDate(LocalDate.of(2024, 1, 15));
        item1.setTime(LocalTime.of(19, 0));
        item1.setClientName("Иван Иванов");
        item1.setTotalAmount(new BigDecimal("3500.00"));
        item1.setSpecialRequests("Без лука");
        
        PreOrderListItemResponse.BookingStatusInfo statusInfo = new PreOrderListItemResponse.BookingStatusInfo();
        statusInfo.setCode("APPROVED");
        statusInfo.setName("Одобрено");
        item1.setStatus(statusInfo);
        item1.setItemsCount(3);
        item1.setCreatedAt(Instant.now());
        item1.setUpdatedAt(Instant.now());

        List<PreOrderListItemResponse> items = Arrays.asList(item1);
        PaginationResponse.PaginationInfo pagination = new PaginationResponse.PaginationInfo(1L, 50, 0, false);
        PaginationResponse<List<PreOrderListItemResponse>> response = new PaginationResponse<>(items, pagination);

        doReturn(response).when(preOrderService).getPreOrders(eq(1L), eq(50), eq(0), isNull(), isNull(), isNull(), isNull(), isNull(), eq("date"), eq("desc"));

        // Act & Assert
        mockMvc.perform(get("/r/1/pre-order")
                        .param("limit", "50")
                        .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].clientName").value("Иван Иванов"))
                .andExpect(jsonPath("$.data[0].totalAmount").value(3500.00))
                .andExpect(jsonPath("$.data[0].itemsCount").value(3))
                .andExpect(jsonPath("$.pagination.total").value(1L));

        verify(preOrderService, times(1)).getPreOrders(eq(1L), eq(50), eq(0), isNull(), isNull(), isNull(), isNull(), isNull(), eq("date"), eq("desc"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetPreOrders_WithFilters() throws Exception {
        // Arrange
        List<PreOrderListItemResponse> items = Arrays.asList();
        PaginationResponse.PaginationInfo pagination = new PaginationResponse.PaginationInfo(0L, 50, 0, false);
        PaginationResponse<List<PreOrderListItemResponse>> response = new PaginationResponse<>(items, pagination);

        doReturn(response).when(preOrderService).getPreOrders(eq(1L), eq(50), eq(0), eq("APPROVED"), 
                eq(LocalDate.of(2024, 1, 1)), eq(LocalDate.of(2024, 1, 31)), eq(1L), eq("+79991234567"), eq("date"), eq("desc"));

        // Act & Assert
        mockMvc.perform(get("/r/1/pre-order")
                        .param("limit", "50")
                        .param("offset", "0")
                        .param("statusCode", "APPROVED")
                        .param("dateFrom", "2024-01-01")
                        .param("dateTo", "2024-01-31")
                        .param("bookingId", "1")
                        .param("clientPhone", "+79991234567"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.pagination.total").value(0L));

        verify(preOrderService, times(1)).getPreOrders(eq(1L), eq(50), eq(0), eq("APPROVED"), 
                eq(LocalDate.of(2024, 1, 1)), eq(LocalDate.of(2024, 1, 31)), eq(1L), eq("+79991234567"), eq("date"), eq("desc"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetPreOrders_RestaurantNotFound() throws Exception {
        // Arrange
        doThrow(new RuntimeException("RESTAURANT_NOT_FOUND")).when(preOrderService).getPreOrders(eq(999L), eq(50), eq(0), isNull(), isNull(), isNull(), isNull(), isNull(), eq("date"), eq("desc"));

        // Act & Assert
        mockMvc.perform(get("/r/999/pre-order"))
                .andExpect(status().isNotFound());

        verify(preOrderService, times(1)).getPreOrders(eq(999L), eq(50), eq(0), isNull(), isNull(), isNull(), isNull(), isNull(), eq("date"), eq("desc"));
    }

    // ========== GET /r/{id}/pre-order/{preOrderId} - детали предзаказа ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetPreOrder_Success() throws Exception {
        // Arrange
        PreOrderResponse response = new PreOrderResponse();
        response.setId(1L);
        response.setRestaurantId(1L);
        response.setBookingId(1L);
        
        PreOrderResponse.BookingInfo bookingInfo = new PreOrderResponse.BookingInfo();
        bookingInfo.setId(1L);
        bookingInfo.setBookingDate(LocalDate.of(2024, 1, 15));
        bookingInfo.setBookingTime(LocalTime.of(19, 0));
        bookingInfo.setTableNumber("1");
        response.setBooking(bookingInfo);
        
        response.setDate(LocalDate.of(2024, 1, 15));
        response.setTime(LocalTime.of(19, 0));
        response.setClientId(1L);
        response.setClientName("Иван Иванов");
        response.setTotalAmount(new BigDecimal("3500.00"));
        response.setSpecialRequests("Без лука");
        
        PreOrderResponse.BookingStatusInfo statusInfo = new PreOrderResponse.BookingStatusInfo();
        statusInfo.setId(3L);
        statusInfo.setCode("APPROVED");
        statusInfo.setName("Одобрено");
        response.setStatus(statusInfo);
        
        PreOrderResponse.PreOrderItem item = new PreOrderResponse.PreOrderItem();
        item.setId(1L);
        item.setMenuItemId(1L);
        item.setQuantity(2);
        item.setPrice(new BigDecimal("1000.00"));
        item.setTotalPrice(new BigDecimal("2000.00"));
        
        PreOrderResponse.MenuItemInfo menuItemInfo = new PreOrderResponse.MenuItemInfo();
        menuItemInfo.setId(1L);
        menuItemInfo.setName("Пицца Маргарита");
        menuItemInfo.setPrice(new BigDecimal("1000.00"));
        item.setMenuItem(menuItemInfo);
        
        response.setItems(Arrays.asList(item));
        response.setHistory(Arrays.asList());
        response.setCreatedAt(Instant.now());
        response.setUpdatedAt(Instant.now());

        doReturn(response).when(preOrderService).getPreOrder(1L, 1L);

        // Act & Assert
        mockMvc.perform(get("/r/1/pre-order/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.clientName").value("Иван Иванов"))
                .andExpect(jsonPath("$.totalAmount").value(3500.00))
                .andExpect(jsonPath("$.status.code").value("APPROVED"))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(1));

        verify(preOrderService, times(1)).getPreOrder(1L, 1L);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetPreOrder_NotFound() throws Exception {
        // Arrange
        doThrow(new RuntimeException("PRE_ORDER_NOT_FOUND")).when(preOrderService).getPreOrder(1L, 999L);

        // Act & Assert
        mockMvc.perform(get("/r/1/pre-order/999"))
                .andExpect(status().isNotFound());

        verify(preOrderService, times(1)).getPreOrder(1L, 999L);
    }

    // ========== PUT /r/{id}/pre-order/{preOrderId}/cancel - отмена предзаказа ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testCancelPreOrder_Success() throws Exception {
        // Arrange
        PreOrderResponse response = new PreOrderResponse();
        response.setId(1L);
        response.setRestaurantId(1L);
        
        PreOrderResponse.BookingStatusInfo statusInfo = new PreOrderResponse.BookingStatusInfo();
        statusInfo.setId(5L);
        statusInfo.setCode("CANCELLED");
        statusInfo.setName("Отменено");
        response.setStatus(statusInfo);
        response.setUpdatedAt(Instant.now());

        doReturn(response).when(preOrderService).cancelPreOrder(1L, 1L);

        // Act & Assert
        mockMvc.perform(put("/r/1/pre-order/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status.code").value("CANCELLED"));

        verify(preOrderService, times(1)).cancelPreOrder(1L, 1L);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testCancelPreOrder_NotFound() throws Exception {
        // Arrange
        doThrow(new RuntimeException("PRE_ORDER_NOT_FOUND")).when(preOrderService).cancelPreOrder(1L, 999L);

        // Act & Assert
        mockMvc.perform(put("/r/1/pre-order/999/cancel"))
                .andExpect(status().isNotFound());

        verify(preOrderService, times(1)).cancelPreOrder(1L, 999L);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testCancelPreOrder_AlreadyCancelled() throws Exception {
        // Arrange
        doThrow(new RuntimeException("PRE_ORDER_ALREADY_CANCELLED_OR_REJECTED")).when(preOrderService).cancelPreOrder(1L, 1L);

        // Act & Assert
        mockMvc.perform(put("/r/1/pre-order/1/cancel"))
                .andExpect(status().isBadRequest());

        verify(preOrderService, times(1)).cancelPreOrder(1L, 1L);
    }
}

