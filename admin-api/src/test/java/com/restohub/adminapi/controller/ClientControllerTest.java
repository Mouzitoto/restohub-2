package com.restohub.adminapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ClientControllerTest {

    @Mock
    private ClientService clientService;

    @InjectMocks
    private ClientController clientController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(clientController)
                .setControllerAdvice(new TestExceptionHandler())
                .setValidator(null)
                .build();
        objectMapper = new ObjectMapper();
    }

    // ========== GET /r/{id}/client - список клиентов ==========

    @Test
    void testGetClients_Success() throws Exception {
        // Arrange
        ClientListItemResponse item1 = new ClientListItemResponse();
        item1.setId(1L);
        item1.setPhone("+79991234567");
        item1.setFirstName("Иван");
        item1.setTotalBookings(5);
        item1.setTotalPreOrders(3);
        item1.setLastBookingDate(Instant.now());
        item1.setCreatedAt(Instant.now());

        List<ClientListItemResponse> items = Arrays.asList(item1);
        PaginationResponse.PaginationInfo pagination = new PaginationResponse.PaginationInfo(1L, 50, 0, false);
        PaginationResponse<List<ClientListItemResponse>> response = new PaginationResponse<>(items, pagination);

        when(clientService.getClients(eq(1L), eq(50), eq(0), isNull(), eq("lastBookingDate"), eq("desc")))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/r/1/client")
                        .param("limit", "50")
                        .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].phone").value("+79991234567"))
                .andExpect(jsonPath("$.data[0].firstName").value("Иван"))
                .andExpect(jsonPath("$.pagination.total").value(1L));

        verify(clientService, times(1)).getClients(eq(1L), eq(50), eq(0), isNull(), eq("lastBookingDate"), eq("desc"));
    }

    @Test
    void testGetClients_WithSearch() throws Exception {
        // Arrange
        List<ClientListItemResponse> items = Arrays.asList();
        PaginationResponse.PaginationInfo pagination = new PaginationResponse.PaginationInfo(0L, 50, 0, false);
        PaginationResponse<List<ClientListItemResponse>> response = new PaginationResponse<>(items, pagination);

        when(clientService.getClients(eq(1L), eq(50), eq(0), eq("Иван"), eq("lastBookingDate"), eq("desc")))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/r/1/client")
                        .param("limit", "50")
                        .param("offset", "0")
                        .param("search", "Иван"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.pagination.total").value(0L));

        verify(clientService, times(1)).getClients(eq(1L), eq(50), eq(0), eq("Иван"), eq("lastBookingDate"), eq("desc"));
    }

    @Test
    void testGetClients_RestaurantNotFound() throws Exception {
        // Arrange
        when(clientService.getClients(eq(999L), anyInt(), anyInt(), isNull(), anyString(), anyString()))
                .thenThrow(new RuntimeException("RESTAURANT_NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(get("/r/999/client"))
                .andExpect(status().isNotFound());

        verify(clientService, times(1)).getClients(eq(999L), anyInt(), anyInt(), isNull(), anyString(), anyString());
    }

    // ========== GET /r/{id}/client/{clientId} - детали клиента ==========

    @Test
    void testGetClient_Success() throws Exception {
        // Arrange
        ClientResponse response = new ClientResponse();
        response.setId(1L);
        response.setPhone("+79991234567");
        response.setFirstName("Иван");
        response.setTotalBookings(5);
        response.setTotalPreOrders(3);
        response.setLastBookingDate(Instant.now());
        response.setCreatedAt(Instant.now());
        response.setUpdatedAt(Instant.now());

        ClientResponse.ClientStatistics statistics = new ClientResponse.ClientStatistics();
        statistics.setAverageBookingPersons(4);
        statistics.setFavoriteTableId(1L);
        statistics.setFavoriteTableNumber("1");
        statistics.setFavoriteMenuItemId(1L);
        statistics.setFavoriteMenuItemName("Пицца Маргарита");
        statistics.setAveragePreOrderAmount(new java.math.BigDecimal("2500.00"));
        response.setStatistics(statistics);

        when(clientService.getClient(1L, 1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/r/1/client/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.phone").value("+79991234567"))
                .andExpect(jsonPath("$.firstName").value("Иван"))
                .andExpect(jsonPath("$.statistics.averageBookingPersons").value(4));

        verify(clientService, times(1)).getClient(1L, 1L);
    }

    @Test
    void testGetClient_NotFound() throws Exception {
        // Arrange
        when(clientService.getClient(1L, 999L))
                .thenThrow(new RuntimeException("CLIENT_NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(get("/r/1/client/999"))
                .andExpect(status().isNotFound());

        verify(clientService, times(1)).getClient(1L, 999L);
    }

    // ========== GET /r/{id}/client/{clientId}/booking - бронирования клиента ==========

    @Test
    void testGetClientBookings_Success() throws Exception {
        // Arrange
        BookingListItemResponse item1 = new BookingListItemResponse();
        item1.setId(1L);
        item1.setRestaurantId(1L);
        item1.setTableId(1L);
        item1.setTableNumber("1");
        item1.setBookingDate(java.time.LocalDate.of(2024, 1, 15));
        item1.setBookingTime(java.time.LocalTime.of(19, 0));
        item1.setPersonCount(4);
        item1.setClientName("Иван Иванов");
        
        BookingListItemResponse.BookingStatusInfo statusInfo = new BookingListItemResponse.BookingStatusInfo();
        statusInfo.setCode("APPROVED");
        statusInfo.setName("Одобрено");
        item1.setStatus(statusInfo);
        item1.setCreatedAt(Instant.now());
        item1.setUpdatedAt(Instant.now());

        List<BookingListItemResponse> items = Arrays.asList(item1);
        PaginationResponse.PaginationInfo pagination = new PaginationResponse.PaginationInfo(1L, 50, 0, false);
        PaginationResponse<List<BookingListItemResponse>> response = new PaginationResponse<>(items, pagination);

        when(clientService.getClientBookings(eq(1L), eq(1L), eq(50), eq(0)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/r/1/client/1/booking")
                        .param("limit", "50")
                        .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.pagination.total").value(1L));

        verify(clientService, times(1)).getClientBookings(eq(1L), eq(1L), eq(50), eq(0));
    }

    // ========== GET /r/{id}/client/{clientId}/pre-order - предзаказы клиента ==========

    @Test
    void testGetClientPreOrders_Success() throws Exception {
        // Arrange
        PreOrderListItemResponse item1 = new PreOrderListItemResponse();
        item1.setId(1L);
        item1.setRestaurantId(1L);
        item1.setBookingId(1L);
        item1.setDate(java.time.LocalDate.of(2024, 1, 15));
        item1.setTime(java.time.LocalTime.of(19, 0));
        item1.setClientName("Иван Иванов");
        item1.setTotalAmount(new java.math.BigDecimal("3500.00"));
        item1.setItemsCount(3);
        
        PreOrderListItemResponse.BookingStatusInfo statusInfo = new PreOrderListItemResponse.BookingStatusInfo();
        statusInfo.setCode("APPROVED");
        statusInfo.setName("Одобрено");
        item1.setStatus(statusInfo);
        item1.setCreatedAt(Instant.now());
        item1.setUpdatedAt(Instant.now());

        List<PreOrderListItemResponse> items = Arrays.asList(item1);
        PaginationResponse.PaginationInfo pagination = new PaginationResponse.PaginationInfo(1L, 50, 0, false);
        PaginationResponse<List<PreOrderListItemResponse>> response = new PaginationResponse<>(items, pagination);

        when(clientService.getClientPreOrders(eq(1L), eq(1L), eq(50), eq(0)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/r/1/client/1/pre-order")
                        .param("limit", "50")
                        .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.pagination.total").value(1L));

        verify(clientService, times(1)).getClientPreOrders(eq(1L), eq(1L), eq(50), eq(0));
    }
}

