package com.restohub.adminapi.service;

import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.entity.*;
import com.restohub.adminapi.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {
    
    private final BookingRepository bookingRepository;
    private final RestaurantRepository restaurantRepository;
    private final BookingStatusRepository bookingStatusRepository;
    private final BookingHistoryRepository bookingHistoryRepository;
    private final UserRepository userRepository;
    private final UserRestaurantRepository userRestaurantRepository;
    
    @Autowired
    public BookingService(
            BookingRepository bookingRepository,
            RestaurantRepository restaurantRepository,
            BookingStatusRepository bookingStatusRepository,
            BookingHistoryRepository bookingHistoryRepository,
            UserRepository userRepository,
            UserRestaurantRepository userRestaurantRepository) {
        this.bookingRepository = bookingRepository;
        this.restaurantRepository = restaurantRepository;
        this.bookingStatusRepository = bookingStatusRepository;
        this.bookingHistoryRepository = bookingHistoryRepository;
        this.userRepository = userRepository;
        this.userRestaurantRepository = userRestaurantRepository;
    }
    
    public PaginationResponse<List<BookingListItemResponse>> getBookings(
            Long restaurantId,
            Integer limit,
            Integer offset,
            String statusCode,
            LocalDate dateFrom,
            LocalDate dateTo,
            Long tableId,
            String clientPhone,
            String sortBy,
            String sortOrder) {
        
        // Проверка существования ресторана
        restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
        
        // Построение спецификации
        Specification<Booking> spec = Specification.where(
                (root, query, cb) -> {
                    // JOIN с table -> room -> floor -> restaurant
                    var floorJoin = root.join("table").join("room").join("floor");
                    return cb.equal(floorJoin.get("restaurant").get("id"), restaurantId);
                }
        );
        
        // Фильтр по статусу
        if (statusCode != null && !statusCode.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> {
                var statusSubquery = query.subquery(Long.class);
                var statusRoot = statusSubquery.from(BookingStatus.class);
                statusSubquery.select(statusRoot.get("id"))
                        .where(cb.and(
                                cb.equal(statusRoot.get("code"), statusCode),
                                cb.equal(statusRoot.get("isActive"), true)
                        ));
                return cb.equal(root.get("bookingStatus").get("id"), statusSubquery);
            });
        }
        
        // Фильтр по дате
        if (dateFrom != null) {
            spec = spec.and((root, query, cb) -> 
                cb.greaterThanOrEqualTo(root.get("date"), dateFrom)
            );
        }
        if (dateTo != null) {
            spec = spec.and((root, query, cb) -> 
                cb.lessThanOrEqualTo(root.get("date"), dateTo)
            );
        }
        
        // Фильтр по столу
        if (tableId != null) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("table").get("id"), tableId)
            );
        }
        
        // Фильтр по телефону клиента
        if (clientPhone != null && !clientPhone.trim().isEmpty()) {
            String phonePattern = "%" + clientPhone.trim() + "%";
            spec = spec.and((root, query, cb) -> {
                var clientSubquery = query.subquery(Long.class);
                var clientRoot = clientSubquery.from(Client.class);
                clientSubquery.select(cb.literal(1L))
                        .where(cb.and(
                                cb.equal(clientRoot.get("id"), root.get("client").get("id")),
                                cb.like(clientRoot.get("phone"), phonePattern)
                        ));
                return cb.exists(clientSubquery);
            });
        }
        
        // Сортировка
        Sort sort = buildSort(sortBy, sortOrder);
        Pageable pageable = PageRequest.of(offset / limit, limit, sort);
        
        Page<Booking> page = bookingRepository.findAll(spec, pageable);
        
        List<BookingListItemResponse> items = page.getContent().stream()
                .map(this::toListItemResponse)
                .collect(Collectors.toList());
        
        PaginationResponse.PaginationInfo pagination = new PaginationResponse.PaginationInfo(
                page.getTotalElements(),
                limit,
                offset,
                (offset + limit) < page.getTotalElements()
        );
        
        return new PaginationResponse<>(items, pagination);
    }
    
    public BookingResponse getBooking(Long restaurantId, Long bookingId) {
        // Проверка существования ресторана
        restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
        
        // Получение бронирования с проверкой принадлежности к ресторану
        Booking booking = bookingRepository.findByIdAndRestaurantId(bookingId, restaurantId)
                .orElseThrow(() -> new RuntimeException("BOOKING_NOT_FOUND"));
        
        return toResponse(booking);
    }
    
    @Transactional
    public BookingResponse cancelBooking(Long restaurantId, Long bookingId) {
        // Проверка существования ресторана
        restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
        
        // Получение бронирования с проверкой принадлежности к ресторану
        Booking booking = bookingRepository.findByIdAndRestaurantId(bookingId, restaurantId)
                .orElseThrow(() -> new RuntimeException("BOOKING_NOT_FOUND"));
        
        // Проверка текущего статуса
        String currentStatusCode = booking.getBookingStatus().getCode();
        if ("CANCELLED".equals(currentStatusCode) || "REJECTED".equals(currentStatusCode)) {
            throw new RuntimeException("BOOKING_ALREADY_CANCELLED_OR_REJECTED");
        }
        
        if (!"PENDING".equals(currentStatusCode) && !"APPROVED".equals(currentStatusCode)) {
            throw new RuntimeException("BOOKING_CANNOT_BE_CANCELLED");
        }
        
        // Получение статуса CANCELLED
        BookingStatus cancelledStatus = bookingStatusRepository.findByCodeAndIsActiveTrue("CANCELLED")
                .orElseThrow(() -> new RuntimeException("STATUS_NOT_FOUND"));
        
        // Обновление статуса
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
        
        return toResponse(booking);
    }
    
    /**
     * Изменение статуса бронирования (для WhatsApp бота)
     */
    @Transactional
    public BookingResponse changeStatus(Long bookingId, String status, Long managerId) {
        // Получение бронирования
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("BOOKING_NOT_FOUND"));
        
        // Проверка текущего статуса (должен быть PENDING)
        String currentStatusCode = booking.getBookingStatus().getCode();
        if (!"PENDING".equals(currentStatusCode)) {
            throw new RuntimeException("BOOKING_NOT_IN_PENDING_STATUS");
        }
        
        // Валидация статуса
        if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) {
            throw new RuntimeException("INVALID_STATUS");
        }
        
        // Проверка существования менеджера
        User manager = userRepository.findByIdAndIsActiveTrue(managerId)
                .orElseThrow(() -> new RuntimeException("MANAGER_NOT_FOUND"));
        
        // Проверка принадлежности менеджера к ресторану
        Long restaurantId = booking.getRestaurant().getId();
        boolean hasAccess = userRestaurantRepository.findByRestaurantId(restaurantId).stream()
                .anyMatch(ur -> ur.getUser().getId().equals(managerId));
        if (!hasAccess) {
            throw new RuntimeException("MANAGER_DOES_NOT_HAVE_ACCESS_TO_RESTAURANT");
        }
        
        // Получение нового статуса
        BookingStatus newStatus = bookingStatusRepository.findByCodeAndIsActiveTrue(status)
                .orElseThrow(() -> new RuntimeException("STATUS_NOT_FOUND"));
        
        // Обновление статуса
        booking.setBookingStatus(newStatus);
        booking.setUpdatedAt(java.time.LocalDateTime.now());
        booking = bookingRepository.save(booking);
        
        // Запись в историю
        BookingHistory history = new BookingHistory();
        history.setBooking(booking);
        history.setBookingStatus(newStatus);
        history.setChangedAt(java.time.LocalDateTime.now());
        history.setChangedBy(manager);
        history.setComment(null);
        history.setCreatedAt(java.time.LocalDateTime.now());
        bookingHistoryRepository.save(history);
        
        return toResponse(booking);
    }
    
    private BookingListItemResponse toListItemResponse(Booking booking) {
        BookingListItemResponse response = new BookingListItemResponse();
        response.setId(booking.getId());
        response.setRestaurantId(booking.getRestaurant().getId());
        response.setTableId(booking.getTable().getId());
        response.setTableNumber(booking.getTable().getTableNumber());
        response.setBookingDate(booking.getDate());
        response.setBookingTime(booking.getTime());
        response.setPersonCount(booking.getPersonCount());
        response.setClientName(booking.getClientName());
        response.setSpecialRequests(booking.getSpecialRequests());
        
        if (booking.getBookingStatus() != null) {
            BookingListItemResponse.BookingStatusInfo statusInfo = new BookingListItemResponse.BookingStatusInfo();
            statusInfo.setCode(booking.getBookingStatus().getCode());
            statusInfo.setName(booking.getBookingStatus().getName());
            response.setStatus(statusInfo);
        }
        
        response.setCreatedAt(booking.getCreatedAt() != null ? 
                booking.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant() : null);
        response.setUpdatedAt(booking.getUpdatedAt() != null ? 
                booking.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant() : null);
        
        return response;
    }
    
    private BookingResponse toResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setRestaurantId(booking.getRestaurant().getId());
        response.setTableId(booking.getTable().getId());
        
        // Информация о столе
        BookingResponse.TableInfo tableInfo = new BookingResponse.TableInfo();
        tableInfo.setId(booking.getTable().getId());
        tableInfo.setTableNumber(booking.getTable().getTableNumber());
        tableInfo.setCapacity(booking.getTable().getCapacity());
        
        // Информация о помещении
        BookingResponse.RoomInfo roomInfo = new BookingResponse.RoomInfo();
        roomInfo.setId(booking.getTable().getRoom().getId());
        roomInfo.setName(booking.getTable().getRoom().getName());
        
        // Информация об этаже
        BookingResponse.FloorInfo floorInfo = new BookingResponse.FloorInfo();
        floorInfo.setId(booking.getTable().getRoom().getFloor().getId());
        floorInfo.setFloorNumber(booking.getTable().getRoom().getFloor().getFloorNumber().toString());
        
        roomInfo.setFloor(floorInfo);
        tableInfo.setRoom(roomInfo);
        response.setTable(tableInfo);
        
        response.setBookingDate(booking.getDate());
        response.setBookingTime(booking.getTime());
        response.setPersonCount(booking.getPersonCount());
        response.setClientName(booking.getClientName());
        if (booking.getClient() != null) {
            response.setClientId(booking.getClient().getId());
        }
        response.setSpecialRequests(booking.getSpecialRequests());
        
        // Информация о статусе
        if (booking.getBookingStatus() != null) {
            BookingResponse.BookingStatusInfo statusInfo = new BookingResponse.BookingStatusInfo();
            statusInfo.setId(booking.getBookingStatus().getId());
            statusInfo.setCode(booking.getBookingStatus().getCode());
            statusInfo.setName(booking.getBookingStatus().getName());
            response.setStatus(statusInfo);
        }
        
        // История изменений (от старых к новым)
        List<BookingHistory> historyList = bookingHistoryRepository.findByBookingId(booking.getId()).stream()
                .sorted((h1, h2) -> h1.getChangedAt().compareTo(h2.getChangedAt()))
                .collect(Collectors.toList());
        List<BookingResponse.BookingHistoryItem> historyItems = historyList.stream()
                .map(this::toHistoryItem)
                .collect(Collectors.toList());
        response.setHistory(historyItems);
        
        response.setCreatedAt(booking.getCreatedAt() != null ? 
                booking.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant() : null);
        response.setUpdatedAt(booking.getUpdatedAt() != null ? 
                booking.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant() : null);
        
        return response;
    }
    
    private BookingResponse.BookingHistoryItem toHistoryItem(BookingHistory history) {
        BookingResponse.BookingHistoryItem item = new BookingResponse.BookingHistoryItem();
        item.setId(history.getId());
        
        BookingResponse.BookingStatusInfo statusInfo = new BookingResponse.BookingStatusInfo();
        statusInfo.setId(history.getBookingStatus().getId());
        statusInfo.setCode(history.getBookingStatus().getCode());
        statusInfo.setName(history.getBookingStatus().getName());
        item.setStatus(statusInfo);
        
        item.setChangedAt(history.getChangedAt() != null ? 
                history.getChangedAt().atZone(ZoneId.systemDefault()).toInstant() : null);
        
        if (history.getChangedBy() != null) {
            BookingResponse.UserInfo userInfo = new BookingResponse.UserInfo();
            userInfo.setId(history.getChangedBy().getId());
            userInfo.setEmail(history.getChangedBy().getEmail());
            item.setChangedBy(userInfo);
        }
        
        item.setComment(history.getComment());
        return item;
    }
    
    private Sort buildSort(String sortBy, String sortOrder) {
        String field = sortBy != null ? sortBy : "bookingDate";
        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        
        switch (field) {
            case "bookingDate":
                return Sort.by(direction, "date");
            case "createdAt":
                return Sort.by(direction, "createdAt");
            case "statusCode":
                return Sort.by(direction, "bookingStatus.code");
            default:
                return Sort.by(Sort.Direction.DESC, "date");
        }
    }
}

