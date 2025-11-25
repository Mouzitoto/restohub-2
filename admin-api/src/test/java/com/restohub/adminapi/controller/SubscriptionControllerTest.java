package com.restohub.adminapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.entity.SubscriptionType;
import com.restohub.adminapi.repository.SubscriptionTypeRepository;
import com.restohub.adminapi.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SubscriptionControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SubscriptionService subscriptionService;

    @MockBean
    private SubscriptionTypeRepository subscriptionTypeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // ========== GET /r/{id}/subscription - подписка ресторана ==========

    @Test
    @WithMockUser(roles = "ADMIN")
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

        doReturn(response).when(subscriptionService).getRestaurantSubscription(1L);

        // Act & Assert
        mockMvc.perform(get("/r/1/subscription"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.restaurantId").value(1L))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(subscriptionService, times(1)).getRestaurantSubscription(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetRestaurantSubscription_NotFound() throws Exception {
        // Arrange
        doThrow(new RuntimeException("SUBSCRIPTION_NOT_FOUND")).when(subscriptionService).getRestaurantSubscription(999L);

        // Act & Assert
        mockMvc.perform(get("/r/999/subscription"))
                .andExpect(status().isNotFound());

        verify(subscriptionService, times(1)).getRestaurantSubscription(999L);
    }

    // ========== PUT /r/{id}/subscription - обновление подписки ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateRestaurantSubscription_Success() throws Exception {
        // Arrange
        UpdateSubscriptionRequest request = new UpdateSubscriptionRequest();
        request.setSubscriptionTypeId(2L);
        request.setStartDate(LocalDate.of(2024, 2, 1));
        // Используем дату в будущем для валидации @Future
        request.setEndDate(LocalDate.now().plusYears(1));

        SubscriptionResponse response = new SubscriptionResponse();
        response.setId(1L);
        response.setRestaurantId(1L);
        SubscriptionResponse.SubscriptionTypeInfo subscriptionTypeInfo = new SubscriptionResponse.SubscriptionTypeInfo();
        subscriptionTypeInfo.setId(2L);
        subscriptionTypeInfo.setCode("PREMIUM");
        subscriptionTypeInfo.setName("Премиум");
        response.setSubscriptionType(subscriptionTypeInfo);
        response.setStartDate(LocalDate.of(2024, 2, 1));
        response.setEndDate(LocalDate.now().plusYears(1));
        response.setIsActive(true);
        response.setUpdatedAt(Instant.now());

        // Настраиваем мок репозитория для валидатора ValidSubscriptionTypeId
        SubscriptionType subscriptionType = new SubscriptionType();
        subscriptionType.setId(2L);
        subscriptionType.setCode("PREMIUM");
        subscriptionType.setName("Премиум");
        subscriptionType.setIsActive(true);
        doReturn(Optional.of(subscriptionType)).when(subscriptionTypeRepository).findByIdAndIsActiveTrue(2L);

        doReturn(response).when(subscriptionService).updateRestaurantSubscription(eq(1L), any(UpdateSubscriptionRequest.class));

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
    @WithMockUser(roles = "ADMIN")
    void testUpdateRestaurantSubscription_NotFound() throws Exception {
        // Arrange
        UpdateSubscriptionRequest request = new UpdateSubscriptionRequest();
        request.setSubscriptionTypeId(2L);

        // Настраиваем мок репозитория для валидатора ValidSubscriptionTypeId
        SubscriptionType subscriptionType = new SubscriptionType();
        subscriptionType.setId(2L);
        subscriptionType.setCode("PREMIUM");
        subscriptionType.setName("Премиум");
        subscriptionType.setIsActive(true);
        doReturn(Optional.of(subscriptionType)).when(subscriptionTypeRepository).findByIdAndIsActiveTrue(2L);

        doThrow(new RuntimeException("SUBSCRIPTION_NOT_FOUND")).when(subscriptionService).updateRestaurantSubscription(eq(999L), any(UpdateSubscriptionRequest.class));

        // Act & Assert
        mockMvc.perform(put("/r/999/subscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(subscriptionService, times(1)).updateRestaurantSubscription(eq(999L), any(UpdateSubscriptionRequest.class));
    }

    // ========== GET /subscription - список всех подписок ==========

    @Test
    @WithMockUser(roles = "ADMIN")
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

        doReturn(response).when(subscriptionService).getAllSubscriptions(eq(50), eq(0), isNull(), isNull(), isNull(), isNull(), eq("endDate"), eq("asc"));

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
    @WithMockUser(roles = "ADMIN")
    void testGetAllSubscriptions_WithFilters() throws Exception {
        // Arrange
        List<SubscriptionListItemResponse> items = Arrays.asList();
        PaginationResponse.PaginationInfo pagination = new PaginationResponse.PaginationInfo(0L, 50, 0, false);
        PaginationResponse<List<SubscriptionListItemResponse>> response = new PaginationResponse<>(items, pagination);

        doReturn(response).when(subscriptionService).getAllSubscriptions(eq(50), eq(0), eq(true), eq(1L), eq(1L), eq(true), eq("endDate"), eq("asc"));

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

