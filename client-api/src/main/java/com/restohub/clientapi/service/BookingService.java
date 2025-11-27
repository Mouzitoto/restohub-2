package com.restohub.clientapi.service;

import com.restohub.clientapi.dto.*;
import com.restohub.clientapi.entity.*;
import com.restohub.clientapi.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class BookingService {
    
    private final BookingRepository bookingRepository;
    private final BookingStatusRepository bookingStatusRepository;
    private final BookingHistoryRepository bookingHistoryRepository;
    private final BookingPreOrderRepository bookingPreOrderRepository;
    private final TableRepository tableRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final WhatsAppService whatsAppService;
    
    @Autowired
    public BookingService(
            BookingRepository bookingRepository,
            BookingStatusRepository bookingStatusRepository,
            BookingHistoryRepository bookingHistoryRepository,
            BookingPreOrderRepository bookingPreOrderRepository,
            TableRepository tableRepository,
            MenuItemRepository menuItemRepository,
            RestaurantRepository restaurantRepository,
            WhatsAppService whatsAppService) {
        this.bookingRepository = bookingRepository;
        this.bookingStatusRepository = bookingStatusRepository;
        this.bookingHistoryRepository = bookingHistoryRepository;
        this.bookingPreOrderRepository = bookingPreOrderRepository;
        this.tableRepository = tableRepository;
        this.menuItemRepository = menuItemRepository;
        this.restaurantRepository = restaurantRepository;
        this.whatsAppService = whatsAppService;
    }
    
    @Transactional
    public CreateBookingResponse createBooking(Long restaurantId, CreateBookingRequest request) {
        // Проверка ресторана
        Restaurant restaurant = restaurantRepository.findByIdAndIsActiveTrue(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
        
        // Проверка стола
        RestaurantTable table = tableRepository.findByIdAndIsActiveTrue(request.getTableId())
                .orElseThrow(() -> new RuntimeException("Table not found"));
        
        // Проверка принадлежности стола к ресторану
        if (!table.getRoom().getFloor().getRestaurant().getId().equals(restaurantId)) {
            throw new RuntimeException("Table does not belong to restaurant");
        }
        
        // Проверка вместимости стола
        if (request.getPersonCount() > table.getCapacity()) {
            throw new RuntimeException("Person count exceeds table capacity");
        }
        
        // Проверка даты/времени
        LocalDate bookingDate = LocalDate.parse(request.getDate());
        LocalTime bookingTime = LocalTime.parse(request.getTime());
        LocalDateTime bookingDateTime = LocalDateTime.of(bookingDate, bookingTime);
        if (bookingDateTime.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Booking date/time cannot be in the past");
        }
        
        // Валидация позиций предзаказа
        if (request.getPreOrderItems() != null && !request.getPreOrderItems().isEmpty()) {
            for (CreateBookingRequest.PreOrderItemRequest item : request.getPreOrderItems()) {
                MenuItem menuItem = menuItemRepository.findByIdAndIsActiveTrue(item.getMenuItemId())
                        .orElseThrow(() -> new RuntimeException("Menu item not found: " + item.getMenuItemId()));
                
                if (!menuItem.getRestaurant().getId().equals(restaurantId)) {
                    throw new RuntimeException("Menu item does not belong to restaurant");
                }
                
                if (!menuItem.getIsAvailable()) {
                    throw new RuntimeException("Menu item is not available: " + item.getMenuItemId());
                }
            }
        }
        
        // Получаем статус DRAFT
        BookingStatus draftStatus = bookingStatusRepository.findByCodeAndIsActiveTrue("DRAFT")
                .orElseThrow(() -> new RuntimeException("DRAFT status not found"));
        
        // Создаем бронирование
        Booking booking = new Booking();
        booking.setRestaurant(restaurant);
        booking.setTable(table);
        booking.setClient(null); // Будет установлен после подтверждения
        booking.setClientName(request.getClientName());
        booking.setDate(bookingDate);
        booking.setTime(bookingTime);
        booking.setPersonCount(request.getPersonCount());
        booking.setSpecialRequests(request.getSpecialRequests());
        booking.setBookingStatus(draftStatus);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());
        
        booking = bookingRepository.save(booking);
        
        // Создаем позиции предзаказа
        if (request.getPreOrderItems() != null && !request.getPreOrderItems().isEmpty()) {
            for (CreateBookingRequest.PreOrderItemRequest itemRequest : request.getPreOrderItems()) {
                MenuItem menuItem = menuItemRepository.findByIdAndIsActiveTrue(itemRequest.getMenuItemId())
                        .orElse(null);
                
                if (menuItem != null) {
                    BookingPreOrder preOrder = new BookingPreOrder();
                    preOrder.setBooking(booking);
                    preOrder.setMenuItem(menuItem);
                    preOrder.setQuantity(itemRequest.getQuantity());
                    preOrder.setPrice(menuItem.getPrice());
                    preOrder.setTotalPrice(menuItem.getPrice().multiply(
                            java.math.BigDecimal.valueOf(itemRequest.getQuantity())));
                    preOrder.setSpecialRequests(itemRequest.getSpecialRequests());
                    preOrder.setCreatedAt(LocalDateTime.now());
                    preOrder.setUpdatedAt(LocalDateTime.now());
                    
                    bookingPreOrderRepository.save(preOrder);
                }
            }
        }
        
        // Создаем запись в истории
        BookingHistory history = new BookingHistory();
        history.setBooking(booking);
        history.setBookingStatus(draftStatus);
        history.setChangedAt(LocalDateTime.now());
        history.setChangedBy(null);
        history.setComment(null);
        history.setCreatedAt(LocalDateTime.now());
        
        bookingHistoryRepository.save(history);
        
        // Генерируем WhatsApp ссылку
        String whatsappUrl = whatsAppService.generateBookingUrl(booking.getId());
        String message = whatsAppService.getBookingMessage(booking.getId());
        
        return CreateBookingResponse.builder()
                .id(booking.getId())
                .restaurantId(restaurantId)
                .tableId(booking.getTable().getId())
                .date(booking.getDate())
                .time(booking.getTime())
                .personCount(booking.getPersonCount())
                .clientName(booking.getClientName())
                .specialRequests(booking.getSpecialRequests())
                .status(BookingStatusResponse.builder()
                        .code(booking.getBookingStatus().getCode())
                        .name(booking.getBookingStatus().getName())
                        .build())
                .whatsappUrl(whatsappUrl)
                .message(message)
                .createdAt(booking.getCreatedAt())
                .build();
    }
}

