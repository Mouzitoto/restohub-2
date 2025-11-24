package com.restohub.adminapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.service.BookingStatusService;
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
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BookingStatusControllerTest {

    @Mock
    private BookingStatusService bookingStatusService;

    @InjectMocks
    private BookingStatusController bookingStatusController;

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
        
        mockMvc = MockMvcBuilders.standaloneSetup(bookingStatusController)
                .setControllerAdvice(new TestExceptionHandler())
                .setValidator(noOpValidator)
                .build();
        objectMapper = new ObjectMapper();
    }

    // ========== POST /booking-status - создание статуса ==========

    @Test
    void testCreateBookingStatus_Success() throws Exception {
        // Arrange
        CreateBookingStatusRequest request = new CreateBookingStatusRequest();
        request.setCode("TEST_STATUS");
        request.setName("Тестовый статус");
        request.setDisplayOrder(1);

        BookingStatusResponse response = new BookingStatusResponse();
        response.setId(1L);
        response.setCode("TEST_STATUS");
        response.setName("Тестовый статус");
        response.setDisplayOrder(1);
        response.setIsActive(true);
        response.setCreatedAt(Instant.now());
        response.setUpdatedAt(Instant.now());

        when(bookingStatusService.createBookingStatus(any(CreateBookingStatusRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/booking-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.code").value("TEST_STATUS"))
                .andExpect(jsonPath("$.name").value("Тестовый статус"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(bookingStatusService, times(1)).createBookingStatus(any(CreateBookingStatusRequest.class));
    }

    // ========== GET /booking-status - список статусов ==========

    @Test
    void testGetBookingStatuses_Success() throws Exception {
        // Arrange
        BookingStatusListItemResponse item1 = new BookingStatusListItemResponse();
        item1.setId(1L);
        item1.setCode("PENDING");
        item1.setName("Ожидает подтверждения");
        item1.setDisplayOrder(1);
        item1.setIsActive(true);
        item1.setCreatedAt(Instant.now());

        List<BookingStatusListItemResponse> items = Arrays.asList(item1);
        PaginationResponse.PaginationInfo pagination = new PaginationResponse.PaginationInfo(1L, 100, 0, false);
        PaginationResponse<List<BookingStatusListItemResponse>> response = new PaginationResponse<>(items, pagination);

        when(bookingStatusService.getBookingStatuses(eq(100), eq(0), eq("displayOrder"), eq("asc")))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/booking-status")
                        .param("limit", "100")
                        .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].code").value("PENDING"))
                .andExpect(jsonPath("$.pagination.total").value(1L));

        verify(bookingStatusService, times(1)).getBookingStatuses(eq(100), eq(0), eq("displayOrder"), eq("asc"));
    }

    // ========== GET /booking-status/{statusId} - детали статуса ==========

    @Test
    void testGetBookingStatus_Success() throws Exception {
        // Arrange
        BookingStatusResponse response = new BookingStatusResponse();
        response.setId(1L);
        response.setCode("PENDING");
        response.setName("Ожидает подтверждения");
        response.setDisplayOrder(1);
        response.setIsActive(true);
        response.setCreatedAt(Instant.now());
        response.setUpdatedAt(Instant.now());

        when(bookingStatusService.getBookingStatus(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/booking-status/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.code").value("PENDING"))
                .andExpect(jsonPath("$.name").value("Ожидает подтверждения"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(bookingStatusService, times(1)).getBookingStatus(1L);
    }

    @Test
    void testGetBookingStatus_NotFound() throws Exception {
        // Arrange
        when(bookingStatusService.getBookingStatus(999L))
                .thenThrow(new RuntimeException("BOOKING_STATUS_NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(get("/booking-status/999"))
                .andExpect(status().isNotFound());

        verify(bookingStatusService, times(1)).getBookingStatus(999L);
    }

    // ========== PUT /booking-status/{statusId} - обновление статуса ==========

    @Test
    void testUpdateBookingStatus_Success() throws Exception {
        // Arrange
        UpdateBookingStatusDetailsRequest request = new UpdateBookingStatusDetailsRequest();
        request.setName("Обновленное название");
        request.setDisplayOrder(2);

        BookingStatusResponse response = new BookingStatusResponse();
        response.setId(1L);
        response.setCode("PENDING");
        response.setName("Обновленное название");
        response.setDisplayOrder(2);
        response.setIsActive(true);
        response.setUpdatedAt(Instant.now());

        when(bookingStatusService.updateBookingStatus(eq(1L), any(UpdateBookingStatusDetailsRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/booking-status/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Обновленное название"))
                .andExpect(jsonPath("$.displayOrder").value(2));

        verify(bookingStatusService, times(1)).updateBookingStatus(eq(1L), any(UpdateBookingStatusDetailsRequest.class));
    }

    @Test
    void testUpdateBookingStatus_NotFound() throws Exception {
        // Arrange
        UpdateBookingStatusDetailsRequest request = new UpdateBookingStatusDetailsRequest();
        request.setName("Обновленное название");

        when(bookingStatusService.updateBookingStatus(eq(999L), any(UpdateBookingStatusDetailsRequest.class)))
                .thenThrow(new RuntimeException("BOOKING_STATUS_NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(put("/booking-status/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(bookingStatusService, times(1)).updateBookingStatus(eq(999L), any(UpdateBookingStatusDetailsRequest.class));
    }

    // ========== DELETE /booking-status/{statusId} - удаление статуса ==========

    @Test
    void testDeleteBookingStatus_Success() throws Exception {
        // Arrange
        doNothing().when(bookingStatusService).deleteBookingStatus(1L);

        // Act & Assert
        mockMvc.perform(delete("/booking-status/1"))
                .andExpect(status().isNoContent());

        verify(bookingStatusService, times(1)).deleteBookingStatus(1L);
    }

    @Test
    void testDeleteBookingStatus_NotFound() throws Exception {
        // Arrange
        doThrow(new RuntimeException("BOOKING_STATUS_NOT_FOUND"))
                .when(bookingStatusService).deleteBookingStatus(999L);

        // Act & Assert
        mockMvc.perform(delete("/booking-status/999"))
                .andExpect(status().isNotFound());

        verify(bookingStatusService, times(1)).deleteBookingStatus(999L);
    }
}

