package com.restohub.adminapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionControllerTest {

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private SubscriptionController subscriptionController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Создаем фиктивный валидатор, который ничего не делает
        org.springframework.validation.Validator noOpValidator = new org.springframework.validation.Validator() {
            @Override
            public boolean supports(Class<?> clazz) {
                return false; // Не поддерживаем никакие классы, чтобы валидация не вызывалась
            }
            
            @Override
            public void validate(Object target, org.springframework.validation.Errors errors) {
                // Ничего не делаем - валидация отключена
            }
        };
        
        mockMvc = MockMvcBuilders.standaloneSetup(subscriptionController)
                .setControllerAdvice(new TestExceptionHandler())
                .setValidator(noOpValidator)
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    // ========== GET /r/{id}/subscription - подписка ресторана ==========

    @Test
    void testGetRestaurantSubscription_Success() throws Exception {
        // Arrange
        SubscriptionResponse response = new SubscriptionResponse();
        response.setId(1L);
        response.setRestaurantId(1L);
        SubscriptionResponse.SubscriptionTypeInfo subscriptionTypeInfo = new SubscriptionResponse.SubscriptionTypeInfo();
        subscriptionTypeInfo.setId(1L);
        subscriptionTypeInfo.setCode("BASIC");
        subscriptionTypeInfo.setName("Базовый");
        response.setSubscriptionType(subscriptionTypeInfo);
        response.setStartDate(LocalDate.of(2024, 1, 1));
        response.setEndDate(LocalDate.of(2024, 12, 31));
        response.setIsActive(true);
        response.setCreatedAt(Instant.now());
        response.setUpdatedAt(Instant.now());

        when(subscriptionService.getRestaurantSubscription(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/r/1/subscription"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.restaurantId").value(1L))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(subscriptionService, times(1)).getRestaurantSubscription(1L);
    }

    @Test
    void testGetRestaurantSubscription_NotFound() throws Exception {
        // Arrange
        when(subscriptionService.getRestaurantSubscription(999L))
                .thenThrow(new RuntimeException("SUBSCRIPTION_NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(get("/r/999/subscription"))
                .andExpect(status().isNotFound());

        verify(subscriptionService, times(1)).getRestaurantSubscription(999L);
    }

    // ========== PUT /r/{id}/subscription - обновление подписки ==========

    @Test
    void testUpdateRestaurantSubscription_Success() throws Exception {
        // Arrange
        UpdateSubscriptionRequest request = new UpdateSubscriptionRequest();
        request.setSubscriptionTypeId(2L);
        request.setStartDate(LocalDate.of(2024, 2, 1));
        request.setEndDate(LocalDate.of(2024, 12, 31));

        SubscriptionResponse response = new SubscriptionResponse();
        response.setId(1L);
        response.setRestaurantId(1L);
        SubscriptionResponse.SubscriptionTypeInfo subscriptionTypeInfo = new SubscriptionResponse.SubscriptionTypeInfo();
        subscriptionTypeInfo.setId(2L);
        subscriptionTypeInfo.setCode("PREMIUM");
        subscriptionTypeInfo.setName("Премиум");
        response.setSubscriptionType(subscriptionTypeInfo);
        response.setStartDate(LocalDate.of(2024, 2, 1));
        response.setEndDate(LocalDate.of(2024, 12, 31));
        response.setIsActive(true);
        response.setUpdatedAt(Instant.now());

        when(subscriptionService.updateRestaurantSubscription(eq(1L), any(UpdateSubscriptionRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/r/1/subscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.subscriptionType.id").value(2L));

        verify(subscriptionService, times(1)).updateRestaurantSubscription(eq(1L), any(UpdateSubscriptionRequest.class));
    }

    @Test
    void testUpdateRestaurantSubscription_NotFound() throws Exception {
        // Arrange
        UpdateSubscriptionRequest request = new UpdateSubscriptionRequest();
        request.setSubscriptionTypeId(2L);

        when(subscriptionService.updateRestaurantSubscription(eq(999L), any(UpdateSubscriptionRequest.class)))
                .thenThrow(new RuntimeException("SUBSCRIPTION_NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(put("/r/999/subscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(subscriptionService, times(1)).updateRestaurantSubscription(eq(999L), any(UpdateSubscriptionRequest.class));
    }

    // ========== GET /subscription - список всех подписок ==========

    @Test
    void testGetAllSubscriptions_Success() throws Exception {
        // Arrange
        SubscriptionListItemResponse item1 = new SubscriptionListItemResponse();
        item1.setId(1L);
        item1.setRestaurantId(1L);
        SubscriptionListItemResponse.SubscriptionTypeInfo subscriptionTypeInfo = new SubscriptionListItemResponse.SubscriptionTypeInfo();
        subscriptionTypeInfo.setId(1L);
        subscriptionTypeInfo.setCode("BASIC");
        subscriptionTypeInfo.setName("Базовый");
        item1.setSubscriptionType(subscriptionTypeInfo);
        item1.setStartDate(LocalDate.of(2024, 1, 1));
        item1.setEndDate(LocalDate.of(2024, 12, 31));
        item1.setIsActive(true);

        List<SubscriptionListItemResponse> items = Arrays.asList(item1);
        PaginationResponse.PaginationInfo pagination = new PaginationResponse.PaginationInfo(1L, 50, 0, false);
        PaginationResponse<List<SubscriptionListItemResponse>> response = new PaginationResponse<>(items, pagination);

        when(subscriptionService.getAllSubscriptions(eq(50), eq(0), isNull(), isNull(), isNull(), isNull(), eq("endDate"), eq("asc")))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/subscription")
                        .param("limit", "50")
                        .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.pagination.total").value(1L));

        verify(subscriptionService, times(1)).getAllSubscriptions(eq(50), eq(0), isNull(), isNull(), isNull(), isNull(), eq("endDate"), eq("asc"));
    }

    @Test
    void testGetAllSubscriptions_WithFilters() throws Exception {
        // Arrange
        List<SubscriptionListItemResponse> items = Arrays.asList();
        PaginationResponse.PaginationInfo pagination = new PaginationResponse.PaginationInfo(0L, 50, 0, false);
        PaginationResponse<List<SubscriptionListItemResponse>> response = new PaginationResponse<>(items, pagination);

        when(subscriptionService.getAllSubscriptions(eq(50), eq(0), eq(true), eq(1L), eq(1L), eq(true), eq("endDate"), eq("asc")))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/subscription")
                        .param("limit", "50")
                        .param("offset", "0")
                        .param("isActive", "true")
                        .param("restaurantId", "1")
                        .param("subscriptionTypeId", "1")
                        .param("expiringSoon", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.pagination.total").value(0L));

        verify(subscriptionService, times(1)).getAllSubscriptions(eq(50), eq(0), eq(true), eq(1L), eq(1L), eq(true), eq("endDate"), eq("asc"));
    }
}

