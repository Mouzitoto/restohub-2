package com.restohub.adminapi.controller;

import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.entity.*;
import com.restohub.adminapi.repository.*;
import com.restohub.adminapi.service.InvoicePdfService;
import com.restohub.adminapi.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
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
    private InvoicePdfService invoicePdfService;

    @MockBean
    private RestaurantSubscriptionRepository subscriptionRepository;

    @MockBean
    private SubscriptionPaymentRepository paymentRepository;

    @MockBean
    private SubscriptionTypeRepository subscriptionTypeRepository;

    @Test
    @WithMockUser(roles = "MANAGER")
    void testCreateSubscription_Success() throws Exception {
        // Arrange
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setSubscriptionTypeId(1L);

        SubscriptionResponse response = new SubscriptionResponse();
        response.setId(1L);
        response.setRestaurantId(1L);
        response.setPaymentReference("SUB-2024-123456");
        response.setStatus("DRAFT");

        // Настройка мока для валидатора
        SubscriptionType subscriptionType = new SubscriptionType();
        subscriptionType.setId(1L);
        subscriptionType.setIsActive(true);
        when(subscriptionTypeRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(java.util.Optional.of(subscriptionType));

        when(subscriptionService.createSubscription(eq(1L), any(CreateSubscriptionRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/r/1/subscription")
                        .contentType("application/json")
                        .content("{\"subscriptionTypeId\": 1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentReference").value("SUB-2024-123456"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    @WithMockUser(roles = "1C")
    void testActivateSubscription_WithApiKey_Success() throws Exception {
        // Arrange
        ActivateSubscriptionRequest request = new ActivateSubscriptionRequest();
        request.setPaymentReference("SUB-2024-123456");
        request.setAmount(new java.math.BigDecimal("10000.00"));
        request.setPaymentDate(LocalDateTime.now());
        request.setExternalTransactionId("TXN-123");

        SubscriptionResponse response = new SubscriptionResponse();
        response.setId(1L);
        response.setStatus("ACTIVATED");

        when(subscriptionService.activateSubscription(any(ActivateSubscriptionRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/subscriptions/activate")
                        .header("X-API-Key", "test-api-key")
                        .contentType("application/json")
                        .content("{\"paymentReference\":\"SUB-2024-123456\",\"amount\":10000.00,\"paymentDate\":\"2024-01-01T12:00:00\",\"externalTransactionId\":\"TXN-123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVATED"));
    }

    @Test
    void testActivateSubscription_WithoutApiKey_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/subscriptions/activate")
                        .contentType("application/json")
                        .content("{\"paymentReference\":\"SUB-2024-123456\"}"))
                .andExpect(status().isForbidden()); // 403 вместо 401, так как SecurityConfig требует роль
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetInvoice_Success() throws Exception {
        // Arrange
        RestaurantSubscription subscription = new RestaurantSubscription();
        subscription.setId(1L);
        subscription.setPaymentReference("SUB-2024-123456");
        
        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);
        subscription.setRestaurant(restaurant);

        when(subscriptionRepository.findById(1L))
                .thenReturn(Optional.of(subscription));
        when(invoicePdfService.generateInvoice(any(RestaurantSubscription.class)))
                .thenReturn(new byte[]{1, 2, 3});

        // Act & Assert
        mockMvc.perform(get("/r/1/subscriptions/1/invoice"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetPaidInvoice_Success() throws Exception {
        // Arrange
        RestaurantSubscription subscription = new RestaurantSubscription();
        subscription.setId(1L);
        subscription.setPaymentReference("SUB-2024-123456");
        subscription.setStatus(SubscriptionStatus.ACTIVATED);
        
        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);
        subscription.setRestaurant(restaurant);

        SubscriptionPayment payment = new SubscriptionPayment();
        payment.setStatus(PaymentStatus.SUCCESS);

        when(subscriptionRepository.findById(1L))
                .thenReturn(Optional.of(subscription));
        when(paymentRepository.findBySubscriptionId(1L))
                .thenReturn(java.util.Collections.singletonList(payment));
        when(invoicePdfService.generatePaidInvoice(any(RestaurantSubscription.class), any(SubscriptionPayment.class)))
                .thenReturn(new byte[]{1, 2, 3});

        // Act & Assert
        mockMvc.perform(get("/r/1/subscriptions/1/paid-invoice"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetRestaurantSubscriptions_Success() throws Exception {
        // Arrange
        SubscriptionListItemResponse item1 = new SubscriptionListItemResponse();
        item1.setId(1L);
        item1.setRestaurantId(1L);
        item1.setStatus("ACTIVATED");
        item1.setPaymentReference("SUB-2024-123456");
        item1.setIsActive(true);

        SubscriptionListItemResponse item2 = new SubscriptionListItemResponse();
        item2.setId(2L);
        item2.setRestaurantId(1L);
        item2.setStatus("DRAFT");
        item2.setPaymentReference("SUB-2024-123457");
        item2.setIsActive(false);

        java.util.List<SubscriptionListItemResponse> subscriptions = java.util.Arrays.asList(item1, item2);

        when(subscriptionService.getRestaurantSubscriptions(1L))
                .thenReturn(subscriptions);

        // Act & Assert
        mockMvc.perform(get("/r/1/subscriptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].status").value("ACTIVATED"))
                .andExpect(jsonPath("$[0].paymentReference").value("SUB-2024-123456"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].status").value("DRAFT"))
                .andExpect(jsonPath("$[1].paymentReference").value("SUB-2024-123457"));
    }
}
