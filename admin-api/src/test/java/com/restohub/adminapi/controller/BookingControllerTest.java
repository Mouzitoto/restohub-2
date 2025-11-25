package com.restohub.adminapi.controller;

import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

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
class BookingControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    // ========== GET /r/{id}/booking - список бронирований ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetBookings_Success() throws Exception {
        // Arrange
        BookingListItemResponse item1 = new BookingListItemResponse();
        item1.setId(1L);
        item1.setRestaurantId(1L);
        item1.setTableId(1L);
        item1.setTableNumber("1");
        item1.setBookingDate(LocalDate.of(2024, 1, 15));
        item1.setBookingTime(LocalTime.of(19, 0));
        item1.setPersonCount(4);
        item1.setClientName("Иван Иванов");
        item1.setSpecialRequests("У окна");
        
        BookingListItemResponse.BookingStatusInfo statusInfo = new BookingListItemResponse.BookingStatusInfo();
        statusInfo.setCode("APPROVED");
        statusInfo.setName("Одобрено");
        item1.setStatus(statusInfo);
        item1.setCreatedAt(Instant.now());
        item1.setUpdatedAt(Instant.now());

        List<BookingListItemResponse> items = Arrays.asList(item1);
        PaginationResponse.PaginationInfo pagination = new PaginationResponse.PaginationInfo(1L, 50, 0, false);
        PaginationResponse<List<BookingListItemResponse>> response = new PaginationResponse<>(items, pagination);

        doReturn(response).when(bookingService).getBookings(eq(1L), eq(50), eq(0), isNull(), isNull(), isNull(), isNull(), isNull(), eq("bookingDate"), eq("desc"));

        // Act & Assert
        mockMvc.perform(get("/r/1/booking")
                        .param("limit", "50")
                        .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].tableNumber").value("1"))
                .andExpect(jsonPath("$.data[0].clientName").value("Иван Иванов"))
                .andExpect(jsonPath("$.pagination.total").value(1L));

        verify(bookingService, times(1)).getBookings(eq(1L), eq(50), eq(0), isNull(), isNull(), isNull(), isNull(), isNull(), eq("bookingDate"), eq("desc"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetBookings_WithFilters() throws Exception {
        // Arrange
        List<BookingListItemResponse> items = Arrays.asList();
        PaginationResponse.PaginationInfo pagination = new PaginationResponse.PaginationInfo(0L, 50, 0, false);
        PaginationResponse<List<BookingListItemResponse>> response = new PaginationResponse<>(items, pagination);

        doReturn(response).when(bookingService).getBookings(
                eq(1L), 
                eq(50), 
                eq(0), 
                eq("APPROVED"), 
                eq(LocalDate.of(2024, 1, 1)), 
                eq(LocalDate.of(2024, 1, 31)), 
                eq(1L), 
                eq("+79991234567"), 
                eq("bookingDate"), 
                eq("desc"));

        // Act & Assert
        mockMvc.perform(get("/r/1/booking")
                        .param("limit", "50")
                        .param("offset", "0")
                        .param("statusCode", "APPROVED")
                        .param("dateFrom", "2024-01-01")
                        .param("dateTo", "2024-01-31")
                        .param("tableId", "1")
                        .param("clientPhone", "+79991234567"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.pagination").exists())
                .andExpect(jsonPath("$.pagination.total").value(0L));

        verify(bookingService, times(1)).getBookings(
                eq(1L), 
                eq(50), 
                eq(0), 
                eq("APPROVED"), 
                eq(LocalDate.of(2024, 1, 1)), 
                eq(LocalDate.of(2024, 1, 31)), 
                eq(1L), 
                eq("+79991234567"), 
                eq("bookingDate"), 
                eq("desc"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetBookings_RestaurantNotFound() throws Exception {
        // Arrange
        doThrow(new RuntimeException("RESTAURANT_NOT_FOUND")).when(bookingService).getBookings(eq(999L), eq(50), eq(0), isNull(), isNull(), isNull(), isNull(), isNull(), eq("bookingDate"), eq("desc"));

        // Act & Assert
        mockMvc.perform(get("/r/999/booking"))
                .andExpect(status().isNotFound());

        verify(bookingService, times(1)).getBookings(eq(999L), eq(50), eq(0), isNull(), isNull(), isNull(), isNull(), isNull(), eq("bookingDate"), eq("desc"));
    }

    // ========== GET /r/{id}/booking/{bookingId} - детали бронирования ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetBooking_Success() throws Exception {
        // Arrange
        BookingResponse response = new BookingResponse();
        response.setId(1L);
        response.setRestaurantId(1L);
        response.setTableId(1L);
        
        BookingResponse.TableInfo tableInfo = new BookingResponse.TableInfo();
        tableInfo.setId(1L);
        tableInfo.setTableNumber("1");
        tableInfo.setCapacity(4);
        
        BookingResponse.RoomInfo roomInfo = new BookingResponse.RoomInfo();
        roomInfo.setId(1L);
        roomInfo.setName("Зал 1");
        
        BookingResponse.FloorInfo floorInfo = new BookingResponse.FloorInfo();
        floorInfo.setId(1L);
        floorInfo.setFloorNumber("1");
        
        roomInfo.setFloor(floorInfo);
        tableInfo.setRoom(roomInfo);
        response.setTable(tableInfo);
        
        response.setBookingDate(LocalDate.of(2024, 1, 15));
        response.setBookingTime(LocalTime.of(19, 0));
        response.setPersonCount(4);
        response.setClientName("Иван Иванов");
        response.setClientId(1L);
        response.setSpecialRequests("У окна");
        
        BookingResponse.BookingStatusInfo statusInfo = new BookingResponse.BookingStatusInfo();
        statusInfo.setId(3L);
        statusInfo.setCode("APPROVED");
        statusInfo.setName("Одобрено");
        response.setStatus(statusInfo);
        
        response.setHistory(Arrays.asList());
        response.setCreatedAt(Instant.now());
        response.setUpdatedAt(Instant.now());

        doReturn(response).when(bookingService).getBooking(1L, 1L);

        // Act & Assert
        mockMvc.perform(get("/r/1/booking/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.table.tableNumber").value("1"))
                .andExpect(jsonPath("$.clientName").value("Иван Иванов"))
                .andExpect(jsonPath("$.status.code").value("APPROVED"));

        verify(bookingService, times(1)).getBooking(1L, 1L);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetBooking_NotFound() throws Exception {
        // Arrange
        doThrow(new RuntimeException("BOOKING_NOT_FOUND")).when(bookingService).getBooking(1L, 999L);

        // Act & Assert
        mockMvc.perform(get("/r/1/booking/999"))
                .andExpect(status().isNotFound());

        verify(bookingService, times(1)).getBooking(1L, 999L);
    }

    // ========== PUT /r/{id}/booking/{bookingId}/cancel - отмена бронирования ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testCancelBooking_Success() throws Exception {
        // Arrange
        BookingResponse response = new BookingResponse();
        response.setId(1L);
        response.setRestaurantId(1L);
        
        BookingResponse.BookingStatusInfo statusInfo = new BookingResponse.BookingStatusInfo();
        statusInfo.setId(5L);
        statusInfo.setCode("CANCELLED");
        statusInfo.setName("Отменено");
        response.setStatus(statusInfo);
        response.setUpdatedAt(Instant.now());

        doReturn(response).when(bookingService).cancelBooking(1L, 1L);

        // Act & Assert
        mockMvc.perform(put("/r/1/booking/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status.code").value("CANCELLED"));

        verify(bookingService, times(1)).cancelBooking(1L, 1L);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testCancelBooking_NotFound() throws Exception {
        // Arrange
        doThrow(new RuntimeException("BOOKING_NOT_FOUND")).when(bookingService).cancelBooking(1L, 999L);

        // Act & Assert
        mockMvc.perform(put("/r/1/booking/999/cancel"))
                .andExpect(status().isNotFound());

        verify(bookingService, times(1)).cancelBooking(1L, 999L);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testCancelBooking_AlreadyCancelled() throws Exception {
        // Arrange
        doThrow(new RuntimeException("BOOKING_ALREADY_CANCELLED_OR_REJECTED")).when(bookingService).cancelBooking(1L, 1L);

        // Act & Assert
        mockMvc.perform(put("/r/1/booking/1/cancel"))
                .andExpect(status().isBadRequest());

        verify(bookingService, times(1)).cancelBooking(1L, 1L);
    }
}

