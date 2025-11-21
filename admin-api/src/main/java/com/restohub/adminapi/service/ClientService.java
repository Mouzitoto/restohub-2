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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ClientService {
    
    private final ClientRepository clientRepository;
    private final RestaurantRepository restaurantRepository;
    private final BookingRepository bookingRepository;
    private final BookingPreOrderRepository bookingPreOrderRepository;
    private final TableRepository tableRepository;
    private final MenuItemRepository menuItemRepository;
    
    @Autowired
    public ClientService(
            ClientRepository clientRepository,
            RestaurantRepository restaurantRepository,
            BookingRepository bookingRepository,
            BookingPreOrderRepository bookingPreOrderRepository,
            TableRepository tableRepository,
            MenuItemRepository menuItemRepository) {
        this.clientRepository = clientRepository;
        this.restaurantRepository = restaurantRepository;
        this.bookingRepository = bookingRepository;
        this.bookingPreOrderRepository = bookingPreOrderRepository;
        this.tableRepository = tableRepository;
        this.menuItemRepository = menuItemRepository;
    }
    
    public PaginationResponse<List<ClientListItemResponse>> getClients(
            Long restaurantId, Integer limit, Integer offset, String search, String sortBy, String sortOrder) {
        
        // Проверка существования ресторана
        restaurantRepository.findByIdAndIsActiveTrue(restaurantId)
                .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
        
        // Построение спецификации
        Specification<Client> spec = Specification.where(
                (root, query, cb) -> {
                    // Подзапрос для проверки наличия бронирований/предзаказов в этом ресторане
                    var bookingSubquery = query.subquery(Long.class);
                    var bookingRoot = bookingSubquery.from(Booking.class);
                    bookingSubquery.select(cb.literal(1L))
                            .where(cb.and(
                                    cb.equal(bookingRoot.get("client").get("id"), root.get("id")),
                                    cb.equal(bookingRoot.get("restaurant").get("id"), restaurantId)
                            ));
                    
                    return cb.exists(bookingSubquery);
                }
        );
        
        // Поиск по телефону или имени
        if (search != null && !search.trim().isEmpty()) {
            String searchPattern = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> 
                cb.or(
                    cb.like(cb.lower(root.get("phone")), searchPattern),
                    cb.like(cb.lower(root.get("firstName")), searchPattern)
                )
            );
        }
        
        // Сортировка
        Sort sort = buildSort(sortBy, sortOrder);
        Pageable pageable = PageRequest.of(offset / limit, limit, sort);
        
        Page<Client> page = clientRepository.findAll(spec, pageable);
        
        List<ClientListItemResponse> items = page.getContent().stream()
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
    
    public ClientResponse getClient(Long restaurantId, Long clientId) {
        // Проверка существования ресторана
        restaurantRepository.findByIdAndIsActiveTrue(restaurantId)
                .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
        
        // Получение клиента
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("CLIENT_NOT_FOUND"));
        
        // Проверка, что у клиента есть бронирования в этом ресторане
        List<Booking> restaurantBookings = bookingRepository.findByRestaurantId(restaurantId).stream()
                .filter(b -> b.getClient() != null && b.getClient().getId().equals(clientId))
                .collect(Collectors.toList());
        
        if (restaurantBookings.isEmpty()) {
            throw new RuntimeException("CLIENT_NOT_FOUND");
        }
        
        return toResponse(client, restaurantId);
    }
    
    public PaginationResponse<List<BookingListItemResponse>> getClientBookings(
            Long restaurantId, Long clientId, Integer limit, Integer offset) {
        
        // Проверка существования ресторана и клиента
        restaurantRepository.findByIdAndIsActiveTrue(restaurantId)
                .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
        
        clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("CLIENT_NOT_FOUND"));
        
        // Получение бронирований клиента в этом ресторане
        List<Booking> bookings = bookingRepository.findByRestaurantId(restaurantId).stream()
                .filter(b -> b.getClient() != null && b.getClient().getId().equals(clientId))
                .sorted((b1, b2) -> b2.getDate().compareTo(b1.getDate()))
                .collect(Collectors.toList());
        
        // Применение пагинации
        int start = offset;
        int end = Math.min(offset + limit, bookings.size());
        List<Booking> pagedBookings = bookings.subList(start, end);
        
        List<BookingListItemResponse> items = pagedBookings.stream()
                .map(this::toBookingListItemResponse)
                .collect(Collectors.toList());
        
        PaginationResponse.PaginationInfo pagination = new PaginationResponse.PaginationInfo(
                (long) bookings.size(),
                limit,
                offset,
                end < bookings.size()
        );
        
        return new PaginationResponse<>(items, pagination);
    }
    
    public PaginationResponse<List<PreOrderListItemResponse>> getClientPreOrders(
            Long restaurantId, Long clientId, Integer limit, Integer offset) {
        
        // Проверка существования ресторана и клиента
        restaurantRepository.findByIdAndIsActiveTrue(restaurantId)
                .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
        
        clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("CLIENT_NOT_FOUND"));
        
        // Получение предзаказов клиента через бронирования
        List<Booking> bookings = bookingRepository.findByRestaurantId(restaurantId).stream()
                .filter(b -> b.getClient() != null && b.getClient().getId().equals(clientId))
                .collect(Collectors.toList());
        
        List<BookingPreOrder> preOrders = bookings.stream()
                .flatMap(b -> bookingPreOrderRepository.findByBookingId(b.getId()).stream())
                .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                .collect(Collectors.toList());
        
        // Применение пагинации
        int start = offset;
        int end = Math.min(offset + limit, preOrders.size());
        List<BookingPreOrder> pagedPreOrders = preOrders.subList(start, end);
        
        List<PreOrderListItemResponse> items = pagedPreOrders.stream()
                .map(this::toPreOrderListItemResponse)
                .collect(Collectors.toList());
        
        PaginationResponse.PaginationInfo pagination = new PaginationResponse.PaginationInfo(
                (long) preOrders.size(),
                limit,
                offset,
                end < preOrders.size()
        );
        
        return new PaginationResponse<>(items, pagination);
    }
    
    private ClientListItemResponse toListItemResponse(Client client) {
        ClientListItemResponse response = new ClientListItemResponse();
        response.setId(client.getId());
        response.setPhone(client.getPhone());
        response.setFirstName(client.getFirstName());
        response.setTotalBookings(client.getTotalBookings());
        response.setTotalPreOrders(client.getTotalPreOrders());
        response.setFirstBookingDate(client.getFirstBookingDate() != null ? client.getFirstBookingDate().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        response.setLastBookingDate(client.getLastBookingDate() != null ? client.getLastBookingDate().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        response.setCreatedAt(client.getCreatedAt() != null ? client.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        return response;
    }
    
    private ClientResponse toResponse(Client client, Long restaurantId) {
        ClientResponse response = new ClientResponse();
        response.setId(client.getId());
        response.setPhone(client.getPhone());
        response.setFirstName(client.getFirstName());
        response.setTotalBookings(client.getTotalBookings());
        response.setTotalPreOrders(client.getTotalPreOrders());
        response.setFirstBookingDate(client.getFirstBookingDate() != null ? client.getFirstBookingDate().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        response.setLastBookingDate(client.getLastBookingDate() != null ? client.getLastBookingDate().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        response.setCreatedAt(client.getCreatedAt() != null ? client.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        response.setUpdatedAt(client.getUpdatedAt() != null ? client.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        
        // Расчет статистики
        ClientResponse.ClientStatistics statistics = calculateStatistics(client, restaurantId);
        response.setStatistics(statistics);
        
        return response;
    }
    
    private ClientResponse.ClientStatistics calculateStatistics(Client client, Long restaurantId) {
        ClientResponse.ClientStatistics stats = new ClientResponse.ClientStatistics();
        
        // Получение всех бронирований клиента в этом ресторане
        List<Booking> bookings = bookingRepository.findByRestaurantId(restaurantId).stream()
                .filter(b -> b.getClient() != null && b.getClient().getId().equals(client.getId()))
                .collect(Collectors.toList());
        
        if (bookings.isEmpty()) {
            return stats;
        }
        
        // Среднее количество персон
        double avgPersons = bookings.stream()
                .mapToInt(Booking::getPersonCount)
                .average()
                .orElse(0.0);
        stats.setAverageBookingPersons((int) Math.round(avgPersons));
        
        // Любимый стол
        Map<Long, Long> tableCounts = bookings.stream()
                .collect(Collectors.groupingBy(b -> b.getTable().getId(), Collectors.counting()));
        
        tableCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresent(entry -> {
                    RestaurantTable table = tableRepository.findById(entry.getKey()).orElse(null);
                    if (table != null) {
                        stats.setFavoriteTableId(table.getId());
                        stats.setFavoriteTableNumber(table.getTableNumber());
                    }
                });
        
        // Получение всех предзаказов клиента
        List<BookingPreOrder> preOrders = bookings.stream()
                .flatMap(b -> bookingPreOrderRepository.findByBookingId(b.getId()).stream())
                .collect(Collectors.toList());
        
        // Любимое блюдо
        Map<Long, Long> menuItemCounts = preOrders.stream()
                .collect(Collectors.groupingBy(p -> p.getMenuItem().getId(), Collectors.counting()));
        
        menuItemCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresent(entry -> {
                    MenuItem menuItem = menuItemRepository.findById(entry.getKey()).orElse(null);
                    if (menuItem != null) {
                        stats.setFavoriteMenuItemId(menuItem.getId());
                        stats.setFavoriteMenuItemName(menuItem.getName());
                    }
                });
        
        // Средняя сумма предзаказа
        if (!preOrders.isEmpty()) {
            // Группировка по бронированиям для расчета суммы каждого предзаказа
            Map<Long, BigDecimal> bookingTotals = preOrders.stream()
                    .collect(Collectors.groupingBy(
                            p -> p.getBooking().getId(),
                            Collectors.reducing(
                                    BigDecimal.ZERO,
                                    BookingPreOrder::getTotalPrice,
                                    BigDecimal::add
                            )
                    ));
            
            BigDecimal avgAmount = bookingTotals.values().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(bookingTotals.size()), 2, RoundingMode.HALF_UP);
            stats.setAveragePreOrderAmount(avgAmount);
        }
        
        return stats;
    }
    
    private BookingListItemResponse toBookingListItemResponse(Booking booking) {
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
        
        response.setCreatedAt(booking.getCreatedAt() != null ? booking.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        response.setUpdatedAt(booking.getUpdatedAt() != null ? booking.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        return response;
    }
    
    private PreOrderListItemResponse toPreOrderListItemResponse(BookingPreOrder preOrder) {
        PreOrderListItemResponse response = new PreOrderListItemResponse();
        Booking booking = preOrder.getBooking();
        response.setId(preOrder.getId());
        response.setRestaurantId(booking.getRestaurant().getId());
        response.setBookingId(booking.getId());
        response.setDate(booking.getDate());
        response.setTime(booking.getTime());
        response.setClientName(booking.getClientName());
        
        // Расчет общей суммы предзаказа
        List<BookingPreOrder> allPreOrders = bookingPreOrderRepository.findByBookingId(booking.getId());
        BigDecimal totalAmount = allPreOrders.stream()
                .map(BookingPreOrder::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.setTotalAmount(totalAmount);
        
        response.setSpecialRequests(preOrder.getSpecialRequests());
        
        if (booking.getBookingStatus() != null) {
            PreOrderListItemResponse.BookingStatusInfo statusInfo = new PreOrderListItemResponse.BookingStatusInfo();
            statusInfo.setCode(booking.getBookingStatus().getCode());
            statusInfo.setName(booking.getBookingStatus().getName());
            response.setStatus(statusInfo);
        }
        
        response.setItemsCount(allPreOrders.size());
        response.setCreatedAt(preOrder.getCreatedAt() != null ? preOrder.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        response.setUpdatedAt(preOrder.getUpdatedAt() != null ? preOrder.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        return response;
    }
    
    private Sort buildSort(String sortBy, String sortOrder) {
        String field = sortBy != null ? sortBy : "lastBookingDate";
        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        
        // Маппинг полей
        switch (field) {
            case "phone":
                return Sort.by(direction, "phone");
            case "firstName":
                return Sort.by(direction, "firstName");
            case "totalBookings":
                return Sort.by(direction, "totalBookings");
            case "totalPreOrders":
                return Sort.by(direction, "totalPreOrders");
            case "lastBookingDate":
                return Sort.by(direction, "lastBookingDate");
            case "firstBookingDate":
                return Sort.by(direction, "firstBookingDate");
            case "createdAt":
                return Sort.by(direction, "createdAt");
            default:
                return Sort.by(Sort.Direction.DESC, "lastBookingDate");
        }
    }
}

