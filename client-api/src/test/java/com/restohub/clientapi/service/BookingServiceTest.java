package com.restohub.clientapi.service;

import com.restohub.clientapi.dto.CreateBookingRequest;
import com.restohub.clientapi.dto.CreateBookingResponse;
import com.restohub.clientapi.entity.*;
import com.restohub.clientapi.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    
    @Mock
    private BookingRepository bookingRepository;
    
    @Mock
    private BookingStatusRepository bookingStatusRepository;
    
    @Mock
    private BookingHistoryRepository bookingHistoryRepository;
    
    @Mock
    private BookingPreOrderRepository bookingPreOrderRepository;
    
    @Mock
    private TableRepository tableRepository;
    
    @Mock
    private MenuItemRepository menuItemRepository;
    
    @Mock
    private RestaurantRepository restaurantRepository;
    
    @Mock
    private WhatsAppService whatsAppService;
    
    @InjectMocks
    private BookingService bookingService;
    
    private Restaurant restaurant;
    private RestaurantTable table;
    private BookingStatus draftStatus;
    private CreateBookingRequest request;
    
    @BeforeEach
    void setUp() {
        restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setIsActive(true);
        
        Floor floor = new Floor();
        floor.setRestaurant(restaurant);
        
        Room room = new Room();
        room.setFloor(floor);
        
        table = new RestaurantTable();
        table.setId(1L);
        table.setRoom(room);
        table.setCapacity(4);
        table.setIsActive(true);
        
        draftStatus = new BookingStatus();
        draftStatus.setId(1L);
        draftStatus.setCode("DRAFT");
        draftStatus.setName("Черновик");
        
        request = CreateBookingRequest.builder()
                .tableId(1L)
                .date(LocalDate.now().plusDays(1).toString())
                .time("19:00:00")
                .personCount(2)
                .clientName("Test Client")
                .build();
    }
    
    @Test
    void testCreateBooking() {
        // Given
        when(restaurantRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(Optional.of(restaurant));
        when(tableRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(Optional.of(table));
        when(bookingStatusRepository.findByCodeAndIsActiveTrue("DRAFT"))
                .thenReturn(Optional.of(draftStatus));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(1L);
            return booking;
        });
        when(whatsAppService.generateBookingUrl(anyLong())).thenReturn("https://wa.me/79991234567?text=BOOKING:1");
        when(whatsAppService.getBookingMessage(anyLong())).thenReturn("BOOKING:1");
        
        // When
        CreateBookingResponse response = bookingService.createBooking(1L, request);
        
        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getRestaurantId());
        assertNotNull(response.getWhatsappUrl());
        verify(bookingRepository).save(any(Booking.class));
        verify(bookingHistoryRepository).save(any(BookingHistory.class));
    }
    
    @Test
    void testCreateBookingRestaurantNotFound() {
        // Given
        when(restaurantRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(RuntimeException.class, () -> bookingService.createBooking(1L, request));
    }
    
    @Test
    void testCreateBookingTableNotFound() {
        // Given
        when(restaurantRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(Optional.of(restaurant));
        when(tableRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(RuntimeException.class, () -> bookingService.createBooking(1L, request));
    }
    
    @Test
    void testCreateBookingPersonCountExceedsCapacity() {
        // Given
        request.setPersonCount(10); // Больше capacity (4)
        when(restaurantRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(Optional.of(restaurant));
        when(tableRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(Optional.of(table));
        
        // When & Then
        assertThrows(RuntimeException.class, () -> bookingService.createBooking(1L, request));
    }
}

