package com.restohub.adminapi.service;

import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.entity.*;
import com.restohub.adminapi.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PreOrderService {
    
    private final BookingPreOrderRepository bookingPreOrderRepository;
    private final BookingRepository bookingRepository;
    private final RestaurantRepository restaurantRepository;
    private final BookingStatusRepository bookingStatusRepository;
    private final BookingHistoryRepository bookingHistoryRepository;
    private final UserRepository userRepository;
    
    @Autowired
    public PreOrderService(
            BookingPreOrderRepository bookingPreOrderRepository,
            BookingRepository bookingRepository,
            RestaurantRepository restaurantRepository,
            BookingStatusRepository bookingStatusRepository,
            BookingHistoryRepository bookingHistoryRepository,
            UserRepository userRepository) {
        this.bookingPreOrderRepository = bookingPreOrderRepository;
        this.bookingRepository = bookingRepository;
        this.restaurantRepository = restaurantRepository;
        this.bookingStatusRepository = bookingStatusRepository;
        this.bookingHistoryRepository = bookingHistoryRepository;
        this.userRepository = userRepository;
    }
    
    public PaginationResponse<List<PreOrderListItemResponse>> getPreOrders(
            Long restaurantId,
            Integer limit,
            Integer offset,
            String statusCode,
            java.time.LocalDate dateFrom,
            java.time.LocalDate dateTo,
            Long bookingId,
            String clientPhone,
            String sortBy,
            String sortOrder) {
        
        // Проверка существования ресторана
        restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
        
        // Получаем все бронирования ресторана
        List<Booking> restaurantBookings = bookingRepository.findByRestaurantId(restaurantId);
        
        // Фильтруем бронирования по критериям
        List<Booking> filteredBookings = restaurantBookings.stream()
                .filter(booking -> {
                    // Фильтр по статусу
                    if (statusCode != null && !statusCode.trim().isEmpty()) {
                        if (!booking.getBookingStatus().getCode().equals(statusCode)) {
                            return false;
                        }
                    }
                    
                    // Фильтр по дате
                    if (dateFrom != null && booking.getDate().isBefore(dateFrom)) {
                        return false;
                    }
                    if (dateTo != null && booking.getDate().isAfter(dateTo)) {
                        return false;
                    }
                    
                    // Фильтр по bookingId
                    if (bookingId != null && !booking.getId().equals(bookingId)) {
                        return false;
                    }
                    
                    // Фильтр по телефону клиента
                    if (clientPhone != null && !clientPhone.trim().isEmpty()) {
                        if (booking.getClient() == null || 
                            !booking.getClient().getPhone().contains(clientPhone.trim())) {
                            return false;
                        }
                    }
                    
                    // Проверяем, что у бронирования есть предзаказы
                    List<BookingPreOrder> preOrders = bookingPreOrderRepository.findByBookingId(booking.getId());
                    return !preOrders.isEmpty();
                })
                .collect(Collectors.toList());
        
        // Сортировка
        filteredBookings.sort((b1, b2) -> {
            Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
            int comparison = 0;
            
            String field = sortBy != null ? sortBy : "date";
            switch (field) {
                case "date":
                    comparison = b1.getDate().compareTo(b2.getDate());
                    if (b1.getDate().equals(b2.getDate())) {
                        comparison = b1.getTime().compareTo(b2.getTime());
                    }
                    break;
                case "totalAmount":
                    BigDecimal amount1 = bookingPreOrderRepository.findByBookingId(b1.getId()).stream()
                            .map(BookingPreOrder::getTotalPrice)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal amount2 = bookingPreOrderRepository.findByBookingId(b2.getId()).stream()
                            .map(BookingPreOrder::getTotalPrice)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    comparison = amount1.compareTo(amount2);
                    break;
                case "createdAt":
                    comparison = b1.getCreatedAt().compareTo(b2.getCreatedAt());
                    break;
                case "statusCode":
                    comparison = b1.getBookingStatus().getCode().compareTo(b2.getBookingStatus().getCode());
                    break;
                default:
                    comparison = b1.getDate().compareTo(b2.getDate());
            }
            
            return direction == Sort.Direction.DESC ? -comparison : comparison;
        });
        
        // Применение пагинации
        int start = offset;
        int end = Math.min(offset + limit, filteredBookings.size());
        List<Booking> pagedBookings = filteredBookings.subList(start, end);
        
        // Преобразование в PreOrderListItemResponse
        List<PreOrderListItemResponse> items = pagedBookings.stream()
                .map(booking -> {
                    List<BookingPreOrder> preOrders = bookingPreOrderRepository.findByBookingId(booking.getId());
                    return toListItemResponse(booking, preOrders);
                })
                .collect(Collectors.toList());
        
        PaginationResponse.PaginationInfo pagination = new PaginationResponse.PaginationInfo(
                (long) filteredBookings.size(),
                limit,
                offset,
                end < filteredBookings.size()
        );
        
        return new PaginationResponse<>(items, pagination);
    }
    
    public PreOrderResponse getPreOrder(Long restaurantId, Long preOrderId) {
        // Проверка существования ресторана
        restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
        
        // Получаем предзаказ (это booking_pre_order запись)
        BookingPreOrder preOrder = bookingPreOrderRepository.findById(preOrderId)
                .orElseThrow(() -> new RuntimeException("PRE_ORDER_NOT_FOUND"));
        
        Booking booking = preOrder.getBooking();
        
        // Проверяем принадлежность к ресторану
        if (!booking.getRestaurant().getId().equals(restaurantId)) {
            throw new RuntimeException("PRE_ORDER_NOT_FOUND");
        }
        
        // Получаем все позиции предзаказа для этого бронирования
        List<BookingPreOrder> allPreOrders = bookingPreOrderRepository.findByBookingId(booking.getId());
        
        return toResponse(booking, allPreOrders);
    }
    
    @Transactional
    public PreOrderResponse cancelPreOrder(Long restaurantId, Long preOrderId) {
        // Проверка существования ресторана
        restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
        
        // Получаем предзаказ
        BookingPreOrder preOrder = bookingPreOrderRepository.findById(preOrderId)
                .orElseThrow(() -> new RuntimeException("PRE_ORDER_NOT_FOUND"));
        
        Booking booking = preOrder.getBooking();
        
        // Проверяем принадлежность к ресторану
        if (!booking.getRestaurant().getId().equals(restaurantId)) {
            throw new RuntimeException("PRE_ORDER_NOT_FOUND");
        }
        
        // Проверка текущего статуса
        String currentStatusCode = booking.getBookingStatus().getCode();
        if ("CANCELLED".equals(currentStatusCode) || "REJECTED".equals(currentStatusCode)) {
            throw new RuntimeException("PRE_ORDER_ALREADY_CANCELLED_OR_REJECTED");
        }
        
        if (!"PENDING".equals(currentStatusCode) && !"APPROVED".equals(currentStatusCode)) {
            throw new RuntimeException("PRE_ORDER_CANNOT_BE_CANCELLED");
        }
        
        // Получение статуса CANCELLED
        BookingStatus cancelledStatus = bookingStatusRepository.findByCodeAndIsActiveTrue("CANCELLED")
                .orElseThrow(() -> new RuntimeException("STATUS_NOT_FOUND"));
        
        // Обновление статуса бронирования (предзаказ связан с бронированием)
        booking.setBookingStatus(cancelledStatus);
        booking.setUpdatedAt(java.time.LocalDateTime.now());
        booking = bookingRepository.save(booking);
        
        // Получение текущего пользователя
        Long userId = null;
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String email = authentication.getName();
                User user = userRepository.findByEmailAndIsActiveTrue(email).orElse(null);
                if (user != null) {
                    userId = user.getId();
                }
            }
        } catch (Exception e) {
            // Игнорируем ошибки получения пользователя
        }
        
        // Запись в историю
        BookingHistory history = new BookingHistory();
        history.setBooking(booking);
        history.setBookingStatus(cancelledStatus);
        history.setChangedAt(java.time.LocalDateTime.now());
        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            history.setChangedBy(user);
        }
        history.setComment(null);
        history.setCreatedAt(java.time.LocalDateTime.now());
        bookingHistoryRepository.save(history);
        
        // Получаем все позиции предзаказа
        List<BookingPreOrder> allPreOrders = bookingPreOrderRepository.findByBookingId(booking.getId());
        
        return toResponse(booking, allPreOrders);
    }
    
    private PreOrderListItemResponse toListItemResponse(Booking booking, List<BookingPreOrder> preOrders) {
        PreOrderListItemResponse response = new PreOrderListItemResponse();
        
        // Используем ID первого предзаказа как ID предзаказа
        if (!preOrders.isEmpty()) {
            response.setId(preOrders.get(0).getId());
        }
        
        response.setRestaurantId(booking.getRestaurant().getId());
        response.setBookingId(booking.getId());
        response.setDate(booking.getDate());
        response.setTime(booking.getTime());
        response.setClientName(booking.getClientName());
        
        // Расчет общей суммы
        BigDecimal totalAmount = preOrders.stream()
                .map(BookingPreOrder::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.setTotalAmount(totalAmount);
        
        // Используем specialRequests из первого предзаказа или из booking
        if (!preOrders.isEmpty() && preOrders.get(0).getSpecialRequests() != null) {
            response.setSpecialRequests(preOrders.get(0).getSpecialRequests());
        } else {
            response.setSpecialRequests(booking.getSpecialRequests());
        }
        
        // Статус из бронирования
        if (booking.getBookingStatus() != null) {
            PreOrderListItemResponse.BookingStatusInfo statusInfo = new PreOrderListItemResponse.BookingStatusInfo();
            statusInfo.setCode(booking.getBookingStatus().getCode());
            statusInfo.setName(booking.getBookingStatus().getName());
            response.setStatus(statusInfo);
        }
        
        response.setItemsCount(preOrders.size());
        
        // Используем даты из первого предзаказа
        if (!preOrders.isEmpty()) {
            response.setCreatedAt(preOrders.get(0).getCreatedAt() != null ? 
                    preOrders.get(0).getCreatedAt().atZone(ZoneId.systemDefault()).toInstant() : null);
            response.setUpdatedAt(preOrders.get(0).getUpdatedAt() != null ? 
                    preOrders.get(0).getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant() : null);
        }
        
        return response;
    }
    
    private PreOrderResponse toResponse(Booking booking, List<BookingPreOrder> preOrders) {
        PreOrderResponse response = new PreOrderResponse();
        
        // Используем ID первого предзаказа как ID предзаказа
        if (!preOrders.isEmpty()) {
            response.setId(preOrders.get(0).getId());
        }
        
        response.setRestaurantId(booking.getRestaurant().getId());
        response.setBookingId(booking.getId());
        
        // Информация о бронировании
        PreOrderResponse.BookingInfo bookingInfo = new PreOrderResponse.BookingInfo();
        bookingInfo.setId(booking.getId());
        bookingInfo.setBookingDate(booking.getDate());
        bookingInfo.setBookingTime(booking.getTime());
        bookingInfo.setTableNumber(booking.getTable().getTableNumber());
        response.setBooking(bookingInfo);
        
        response.setDate(booking.getDate());
        response.setTime(booking.getTime());
        if (booking.getClient() != null) {
            response.setClientId(booking.getClient().getId());
        }
        response.setClientName(booking.getClientName());
        
        // Расчет общей суммы
        BigDecimal totalAmount = preOrders.stream()
                .map(BookingPreOrder::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.setTotalAmount(totalAmount);
        
        // Используем specialRequests из первого предзаказа или из booking
        if (!preOrders.isEmpty() && preOrders.get(0).getSpecialRequests() != null) {
            response.setSpecialRequests(preOrders.get(0).getSpecialRequests());
        } else {
            response.setSpecialRequests(booking.getSpecialRequests());
        }
        
        // Статус из бронирования
        if (booking.getBookingStatus() != null) {
            PreOrderResponse.BookingStatusInfo statusInfo = new PreOrderResponse.BookingStatusInfo();
            statusInfo.setId(booking.getBookingStatus().getId());
            statusInfo.setCode(booking.getBookingStatus().getCode());
            statusInfo.setName(booking.getBookingStatus().getName());
            response.setStatus(statusInfo);
        }
        
        // История изменений (из бронирования)
        List<BookingHistory> historyList = bookingHistoryRepository.findByBookingId(booking.getId()).stream()
                .sorted((h1, h2) -> h1.getChangedAt().compareTo(h2.getChangedAt()))
                .collect(Collectors.toList());
        List<PreOrderResponse.BookingHistoryItem> historyItems = historyList.stream()
                .map(this::toHistoryItem)
                .collect(Collectors.toList());
        response.setHistory(historyItems);
        
        // Позиции предзаказа
        List<PreOrderResponse.PreOrderItem> items = preOrders.stream()
                .map(this::toPreOrderItem)
                .collect(Collectors.toList());
        response.setItems(items);
        
        // Используем даты из первого предзаказа
        if (!preOrders.isEmpty()) {
            response.setCreatedAt(preOrders.get(0).getCreatedAt() != null ? 
                    preOrders.get(0).getCreatedAt().atZone(ZoneId.systemDefault()).toInstant() : null);
            response.setUpdatedAt(preOrders.get(0).getUpdatedAt() != null ? 
                    preOrders.get(0).getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant() : null);
        }
        
        return response;
    }
    
    private PreOrderResponse.BookingHistoryItem toHistoryItem(BookingHistory history) {
        PreOrderResponse.BookingHistoryItem item = new PreOrderResponse.BookingHistoryItem();
        item.setId(history.getId());
        
        PreOrderResponse.BookingStatusInfo statusInfo = new PreOrderResponse.BookingStatusInfo();
        statusInfo.setId(history.getBookingStatus().getId());
        statusInfo.setCode(history.getBookingStatus().getCode());
        statusInfo.setName(history.getBookingStatus().getName());
        item.setStatus(statusInfo);
        
        item.setChangedAt(history.getChangedAt() != null ? 
                history.getChangedAt().atZone(ZoneId.systemDefault()).toInstant() : null);
        
        if (history.getChangedBy() != null) {
            PreOrderResponse.UserInfo userInfo = new PreOrderResponse.UserInfo();
            userInfo.setId(history.getChangedBy().getId());
            userInfo.setEmail(history.getChangedBy().getEmail());
            item.setChangedBy(userInfo);
        }
        
        item.setComment(history.getComment());
        return item;
    }
    
    private PreOrderResponse.PreOrderItem toPreOrderItem(BookingPreOrder preOrder) {
        PreOrderResponse.PreOrderItem item = new PreOrderResponse.PreOrderItem();
        item.setId(preOrder.getId());
        item.setMenuItemId(preOrder.getMenuItem().getId());
        
        PreOrderResponse.MenuItemInfo menuItemInfo = new PreOrderResponse.MenuItemInfo();
        menuItemInfo.setId(preOrder.getMenuItem().getId());
        menuItemInfo.setName(preOrder.getMenuItem().getName());
        menuItemInfo.setPrice(preOrder.getPrice());
        item.setMenuItem(menuItemInfo);
        
        item.setQuantity(preOrder.getQuantity());
        item.setPrice(preOrder.getPrice());
        item.setTotalPrice(preOrder.getTotalPrice());
        item.setSpecialRequests(preOrder.getSpecialRequests());
        
        return item;
    }
}

