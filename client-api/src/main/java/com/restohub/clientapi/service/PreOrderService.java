package com.restohub.clientapi.service;

import com.restohub.clientapi.dto.CreatePreOrderRequest;
import com.restohub.clientapi.dto.CreatePreOrderResponse;
import com.restohub.clientapi.dto.BookingStatusResponse;
import com.restohub.clientapi.entity.*;
import com.restohub.clientapi.repository.*;
import com.restohub.clientapi.validation.PhoneValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class PreOrderService {
    
    private final PreOrderRepository preOrderRepository;
    private final PreOrderItemRepository preOrderItemRepository;
    private final PreOrderHistoryRepository preOrderHistoryRepository;
    private final RestaurantRepository restaurantRepository;
    private final BookingRepository bookingRepository;
    private final MenuItemRepository menuItemRepository;
    private final ClientRepository clientRepository;
    private final BookingStatusRepository bookingStatusRepository;
    
    @Autowired
    public PreOrderService(
            PreOrderRepository preOrderRepository,
            PreOrderItemRepository preOrderItemRepository,
            PreOrderHistoryRepository preOrderHistoryRepository,
            RestaurantRepository restaurantRepository,
            BookingRepository bookingRepository,
            MenuItemRepository menuItemRepository,
            ClientRepository clientRepository,
            BookingStatusRepository bookingStatusRepository) {
        this.preOrderRepository = preOrderRepository;
        this.preOrderItemRepository = preOrderItemRepository;
        this.preOrderHistoryRepository = preOrderHistoryRepository;
        this.restaurantRepository = restaurantRepository;
        this.bookingRepository = bookingRepository;
        this.menuItemRepository = menuItemRepository;
        this.clientRepository = clientRepository;
        this.bookingStatusRepository = bookingStatusRepository;
    }
    
    @Transactional
    public CreatePreOrderResponse createPreOrder(CreatePreOrderRequest request) {
        // Проверка ресторана
        Restaurant restaurant = restaurantRepository.findByIdAndIsActiveTrue(request.getRestaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
        
        // Проверка бронирования (если указано)
        Booking booking = null;
        if (request.getBookingId() != null) {
            booking = bookingRepository.findById(request.getBookingId())
                    .orElseThrow(() -> new RuntimeException("Booking not found"));
            
            // Проверка принадлежности бронирования к ресторану
            if (!booking.getRestaurant().getId().equals(request.getRestaurantId())) {
                throw new RuntimeException("Booking does not belong to restaurant");
            }
        }
        
        // Проверка блюд
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CreatePreOrderRequest.PreOrderItemRequest itemRequest : request.getItems()) {
            MenuItem menuItem = menuItemRepository.findByIdAndIsActiveTrue(itemRequest.getMenuItemId())
                    .orElseThrow(() -> new RuntimeException("Menu item not found: " + itemRequest.getMenuItemId()));
            
            if (!menuItem.getRestaurant().getId().equals(request.getRestaurantId())) {
                throw new RuntimeException("Menu item does not belong to restaurant");
            }
            
            if (!menuItem.getIsAvailable()) {
                throw new RuntimeException("Menu item is not available: " + itemRequest.getMenuItemId());
            }
            
            totalAmount = totalAmount.add(menuItem.getPrice().multiply(
                    BigDecimal.valueOf(itemRequest.getQuantity())));
        }
        
        // Проверка даты/времени
        LocalDate orderDate = LocalDate.parse(request.getDate());
        LocalTime orderTime = LocalTime.parse(request.getTime());
        LocalDateTime orderDateTime = LocalDateTime.of(orderDate, orderTime);
        if (orderDateTime.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Pre-order date/time cannot be in the past");
        }
        
        // Нормализация телефона
        String normalizedPhone = PhoneValidator.normalizePhone(request.getClientPhone());
        
        // Работа с клиентом
        Client client = clientRepository.findByPhone(normalizedPhone).orElse(null);
        
        if (client == null) {
            // Создаем нового клиента
            client = new Client();
            client.setPhone(normalizedPhone);
            client.setFirstName(request.getClientFirstName());
            client.setFirstBookingDate(null);
            client.setLastBookingDate(null);
            client.setTotalBookings(0);
            client.setTotalPreOrders(1);
            client.setCreatedAt(LocalDateTime.now());
            client.setUpdatedAt(LocalDateTime.now());
            client = clientRepository.save(client);
        } else {
            // Обновляем существующего клиента
            if (request.getClientFirstName() != null && !request.getClientFirstName().trim().isEmpty()) {
                client.setFirstName(request.getClientFirstName());
            }
            // Обновляем lastBookingDate только если есть бронирования
            if (client.getTotalBookings() > 0) {
                client.setLastBookingDate(LocalDateTime.now());
            }
            client.setTotalPreOrders(client.getTotalPreOrders() + 1);
            client.setUpdatedAt(LocalDateTime.now());
            client = clientRepository.save(client);
        }
        
        // Получаем статус PENDING
        BookingStatus pendingStatus = bookingStatusRepository.findByCodeAndIsActiveTrue("PENDING")
                .orElseThrow(() -> new RuntimeException("PENDING status not found"));
        
        // Создаем предзаказ
        PreOrder preOrder = new PreOrder();
        preOrder.setRestaurant(restaurant);
        preOrder.setBooking(booking);
        preOrder.setClient(client);
        preOrder.setClientName(request.getClientName());
        preOrder.setDate(orderDate);
        preOrder.setTime(orderTime);
        preOrder.setTotalAmount(totalAmount);
        preOrder.setSpecialRequests(request.getSpecialRequests());
        preOrder.setBookingStatus(pendingStatus);
        preOrder.setWhatsappMessageId(request.getWhatsappMessageId());
        preOrder.setCreatedAt(LocalDateTime.now());
        preOrder.setUpdatedAt(LocalDateTime.now());
        
        preOrder = preOrderRepository.save(preOrder);
        
        // Создаем позиции предзаказа
        for (CreatePreOrderRequest.PreOrderItemRequest itemRequest : request.getItems()) {
            MenuItem menuItem = menuItemRepository.findByIdAndIsActiveTrue(itemRequest.getMenuItemId())
                    .orElse(null);
            
            if (menuItem != null) {
                PreOrderItem item = new PreOrderItem();
                item.setPreOrder(preOrder);
                item.setMenuItem(menuItem);
                item.setQuantity(itemRequest.getQuantity());
                item.setPrice(menuItem.getPrice());
                item.setTotalPrice(menuItem.getPrice().multiply(
                        BigDecimal.valueOf(itemRequest.getQuantity())));
                item.setSpecialRequests(itemRequest.getSpecialRequests());
                item.setCreatedAt(LocalDateTime.now());
                item.setUpdatedAt(LocalDateTime.now());
                
                preOrderItemRepository.save(item);
            }
        }
        
        // Создаем запись в истории
        PreOrderHistory history = new PreOrderHistory();
        history.setPreOrder(preOrder);
        history.setBookingStatus(pendingStatus);
        history.setChangedAt(LocalDateTime.now());
        history.setChangedBy(null);
        history.setComment(null);
        history.setCreatedAt(LocalDateTime.now());
        preOrderHistoryRepository.save(history);
        
        // TODO: Отправка уведомления менеджерам через WhatsApp бот
        
        return CreatePreOrderResponse.builder()
                .id(preOrder.getId())
                .restaurantId(preOrder.getRestaurant().getId())
                .bookingId(preOrder.getBooking() != null ? preOrder.getBooking().getId() : null)
                .date(preOrder.getDate())
                .time(preOrder.getTime())
                .clientId(preOrder.getClient().getId())
                .clientName(preOrder.getClientName())
                .totalAmount(preOrder.getTotalAmount())
                .itemsCount(request.getItems().size())
                .status(BookingStatusResponse.builder()
                        .code(preOrder.getBookingStatus().getCode())
                        .name(preOrder.getBookingStatus().getName())
                        .build())
                .createdAt(preOrder.getCreatedAt())
                .build();
    }
}

