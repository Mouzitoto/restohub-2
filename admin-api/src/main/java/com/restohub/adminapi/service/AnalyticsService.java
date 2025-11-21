package com.restohub.adminapi.service;

import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.entity.*;
import com.restohub.adminapi.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {
    
    private final RestaurantRepository restaurantRepository;
    private final BookingRepository bookingRepository;
    private final BookingPreOrderRepository bookingPreOrderRepository;
    private final ClientRepository clientRepository;
    private final TableRepository tableRepository;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public AnalyticsService(
            RestaurantRepository restaurantRepository,
            BookingRepository bookingRepository,
            BookingPreOrderRepository bookingPreOrderRepository,
            ClientRepository clientRepository,
            TableRepository tableRepository,
            ObjectMapper objectMapper) {
        this.restaurantRepository = restaurantRepository;
        this.bookingRepository = bookingRepository;
        this.bookingPreOrderRepository = bookingPreOrderRepository;
        this.clientRepository = clientRepository;
        this.tableRepository = tableRepository;
        this.objectMapper = objectMapper;
    }
    
    public BookingAnalyticsResponse getBookingAnalytics(
            Long restaurantId, LocalDate dateFrom, LocalDate dateTo, String groupBy) {
        
        // Проверка существования ресторана
        restaurantRepository.findByIdAndIsActiveTrue(restaurantId)
                .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
        
        // Валидация дат
        final LocalDate finalDateFrom = dateFrom != null ? dateFrom : LocalDate.now().minusDays(30);
        final LocalDate finalDateTo = dateTo != null ? dateTo : LocalDate.now();
        if (finalDateFrom.isAfter(finalDateTo)) {
            throw new RuntimeException("INVALID_DATE_RANGE");
        }
        
        // Получение всех бронирований ресторана за период
        List<Booking> bookings = bookingRepository.findByRestaurantId(restaurantId).stream()
                .filter(b -> !b.getDate().isBefore(finalDateFrom) && !b.getDate().isAfter(finalDateTo))
                .collect(Collectors.toList());
        
        // Расчет общей статистики
        BookingAnalyticsResponse.Summary summary = new BookingAnalyticsResponse.Summary();
        summary.setTotal((long) bookings.size());
        
        // Группировка по статусам
        Map<String, Long> byStatus = bookings.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getBookingStatus().getCode(),
                        Collectors.counting()
                ));
        summary.setByStatus(byStatus);
        
        // Среднее количество персон
        double avgPersons = bookings.stream()
                .mapToInt(Booking::getPersonCount)
                .average()
                .orElse(0.0);
        summary.setAveragePersons(avgPersons);
        
        // Конверсия (APPROVED / total)
        long approved = byStatus.getOrDefault("APPROVED", 0L);
        summary.setConversionRate(bookings.isEmpty() ? 0.0 : (double) approved / bookings.size());
        
        // Группировка по периодам
        List<BookingAnalyticsResponse.ChartItem> chart = groupBookingsByPeriod(bookings, groupBy);
        
        // Популярные столы
        List<BookingAnalyticsResponse.PopularTable> popularTables = getPopularTables(bookings, restaurantId);
        
        BookingAnalyticsResponse response = new BookingAnalyticsResponse();
        response.setRestaurantId(restaurantId);
        BookingAnalyticsResponse.Period period = new BookingAnalyticsResponse.Period();
        period.setDateFrom(finalDateFrom);
        period.setDateTo(finalDateTo);
        response.setPeriod(period);
        response.setSummary(summary);
        response.setChart(chart);
        response.setPopularTables(popularTables);
        
        return response;
    }
    
    public PreOrderAnalyticsResponse getPreOrderAnalytics(
            Long restaurantId, LocalDate dateFrom, LocalDate dateTo, String groupBy) {
        
        // Проверка существования ресторана
        restaurantRepository.findByIdAndIsActiveTrue(restaurantId)
                .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
        
        // Валидация дат
        final LocalDate finalDateFrom = dateFrom != null ? dateFrom : LocalDate.now().minusDays(30);
        final LocalDate finalDateTo = dateTo != null ? dateTo : LocalDate.now();
        if (finalDateFrom.isAfter(finalDateTo)) {
            throw new RuntimeException("INVALID_DATE_RANGE");
        }
        
        // Получение всех предзаказов через бронирования
        List<Booking> bookings = bookingRepository.findByRestaurantId(restaurantId).stream()
                .filter(b -> !b.getDate().isBefore(finalDateFrom) && !b.getDate().isAfter(finalDateTo))
                .collect(Collectors.toList());
        
        List<BookingPreOrder> preOrders = bookings.stream()
                .flatMap(b -> bookingPreOrderRepository.findByBookingId(b.getId()).stream())
                .collect(Collectors.toList());
        
        // Группировка предзаказов по бронированиям
        Map<Long, List<BookingPreOrder>> preOrdersByBooking = preOrders.stream()
                .collect(Collectors.groupingBy(p -> p.getBooking().getId()));
        
        // Расчет общей статистики
        PreOrderAnalyticsResponse.Summary summary = new PreOrderAnalyticsResponse.Summary();
        summary.setTotal((long) preOrdersByBooking.size());
        
        // Группировка по статусам
        Map<String, Long> byStatus = bookings.stream()
                .filter(b -> preOrdersByBooking.containsKey(b.getId()))
                .collect(Collectors.groupingBy(
                        b -> b.getBookingStatus().getCode(),
                        Collectors.counting()
                ));
        summary.setByStatus(byStatus);
        
        // Общая выручка и средний чек
        BigDecimal totalRevenue = preOrdersByBooking.values().stream()
                .map(items -> items.stream()
                        .map(BookingPreOrder::getTotalPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.setTotalRevenue(totalRevenue);
        summary.setAverageCheck(preOrdersByBooking.isEmpty() ? BigDecimal.ZERO :
                totalRevenue.divide(BigDecimal.valueOf(preOrdersByBooking.size()), 2, RoundingMode.HALF_UP));
        
        // Конверсия
        long approved = byStatus.getOrDefault("APPROVED", 0L);
        summary.setConversionRate(preOrdersByBooking.isEmpty() ? 0.0 :
                (double) approved / preOrdersByBooking.size());
        
        // Группировка по периодам
        List<PreOrderAnalyticsResponse.ChartItem> chart = groupPreOrdersByPeriod(bookings, preOrdersByBooking, groupBy);
        
        // Популярные блюда
        List<PreOrderAnalyticsResponse.PopularItem> popularItems = getPopularItems(preOrders, restaurantId);
        
        PreOrderAnalyticsResponse response = new PreOrderAnalyticsResponse();
        response.setRestaurantId(restaurantId);
        PreOrderAnalyticsResponse.Period period = new PreOrderAnalyticsResponse.Period();
        period.setDateFrom(finalDateFrom);
        period.setDateTo(finalDateTo);
        response.setPeriod(period);
        response.setSummary(summary);
        response.setChart(chart);
        response.setPopularItems(popularItems);
        
        return response;
    }
    
    public ClientAnalyticsResponse getClientAnalytics(Long restaurantId, LocalDate dateFrom, LocalDate dateTo) {
        // Проверка существования ресторана
        restaurantRepository.findByIdAndIsActiveTrue(restaurantId)
                .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
        
        // Валидация дат
        final LocalDate finalDateFrom = dateFrom != null ? dateFrom : LocalDate.now().minusDays(30);
        final LocalDate finalDateTo = dateTo != null ? dateTo : LocalDate.now();
        if (finalDateFrom.isAfter(finalDateTo)) {
            throw new RuntimeException("INVALID_DATE_RANGE");
        }
        
        // Получение всех клиентов, связанных с рестораном
        List<Booking> bookings = bookingRepository.findByRestaurantId(restaurantId).stream()
                .filter(b -> b.getClient() != null)
                .filter(b -> !b.getDate().isBefore(finalDateFrom) && !b.getDate().isAfter(finalDateTo))
                .collect(Collectors.toList());
        
        Set<Long> clientIds = bookings.stream()
                .map(b -> b.getClient().getId())
                .collect(Collectors.toSet());
        
        List<Client> clients = clientRepository.findAllById(clientIds);
        
        // Расчет статистики
        ClientAnalyticsResponse.Summary summary = new ClientAnalyticsResponse.Summary();
        summary.setTotal((long) clients.size());
        
        // Новые клиенты (первое бронирование в периоде)
        long newClients = bookings.stream()
                .filter(b -> b.getClient() != null)
                .collect(Collectors.groupingBy(b -> b.getClient().getId()))
                .values().stream()
                .filter(clientBookings -> clientBookings.size() == 1)
                .count();
        summary.setNewClients(newClients);
        summary.setReturningClients(clients.size() - newClients);
        
        // Средние значения
        double avgBookings = clients.stream()
                .mapToInt(Client::getTotalBookings)
                .average()
                .orElse(0.0);
        summary.setAverageBookingsPerClient(avgBookings);
        
        double avgPreOrders = clients.stream()
                .mapToInt(Client::getTotalPreOrders)
                .average()
                .orElse(0.0);
        summary.setAveragePreOrdersPerClient(avgPreOrders);
        
        // Топ клиентов
        List<ClientAnalyticsResponse.TopClient> topClients = getTopClients(clients, restaurantId);
        
        ClientAnalyticsResponse response = new ClientAnalyticsResponse();
        response.setRestaurantId(restaurantId);
        ClientAnalyticsResponse.Period period = new ClientAnalyticsResponse.Period();
        period.setDateFrom(finalDateFrom);
        period.setDateTo(finalDateTo);
        response.setPeriod(period);
        response.setSummary(summary);
        response.setTopClients(topClients);
        
        return response;
    }
    
    public AnalyticsOverviewResponse getOverview(Long restaurantId, LocalDate dateFrom, LocalDate dateTo) {
        // Проверка существования ресторана
        restaurantRepository.findByIdAndIsActiveTrue(restaurantId)
                .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
        
        // Валидация дат
        final LocalDate finalDateFrom = dateFrom != null ? dateFrom : LocalDate.now().minusDays(30);
        final LocalDate finalDateTo = dateTo != null ? dateTo : LocalDate.now();
        if (finalDateFrom.isAfter(finalDateTo)) {
            throw new RuntimeException("INVALID_DATE_RANGE");
        }
        
        // Получение данных
        List<Booking> bookings = bookingRepository.findByRestaurantId(restaurantId).stream()
                .filter(b -> !b.getDate().isBefore(finalDateFrom) && !b.getDate().isAfter(finalDateTo))
                .collect(Collectors.toList());
        
        List<BookingPreOrder> preOrders = bookings.stream()
                .flatMap(b -> bookingPreOrderRepository.findByBookingId(b.getId()).stream())
                .collect(Collectors.toList());
        
        Map<Long, List<BookingPreOrder>> preOrdersByBooking = preOrders.stream()
                .collect(Collectors.groupingBy(p -> p.getBooking().getId()));
        
        Set<Long> clientIds = bookings.stream()
                .filter(b -> b.getClient() != null)
                .map(b -> b.getClient().getId())
                .collect(Collectors.toSet());
        
        List<Client> clients = clientRepository.findAllById(clientIds);
        
        // Статистика бронирований
        AnalyticsOverviewResponse.BookingsInfo bookingsInfo = new AnalyticsOverviewResponse.BookingsInfo();
        bookingsInfo.setTotal((long) bookings.size());
        long approvedBookings = bookings.stream()
                .filter(b -> "APPROVED".equals(b.getBookingStatus().getCode()))
                .count();
        bookingsInfo.setApproved(approvedBookings);
        bookingsInfo.setConversionRate(bookings.isEmpty() ? 0.0 : (double) approvedBookings / bookings.size());
        
        // Статистика предзаказов
        AnalyticsOverviewResponse.PreOrdersInfo preOrdersInfo = new AnalyticsOverviewResponse.PreOrdersInfo();
        preOrdersInfo.setTotal((long) preOrdersByBooking.size());
        long approvedPreOrders = bookings.stream()
                .filter(b -> preOrdersByBooking.containsKey(b.getId()))
                .filter(b -> "APPROVED".equals(b.getBookingStatus().getCode()))
                .count();
        preOrdersInfo.setApproved(approvedPreOrders);
        
        BigDecimal totalRevenue = preOrdersByBooking.values().stream()
                .map(items -> items.stream()
                        .map(BookingPreOrder::getTotalPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        preOrdersInfo.setTotalRevenue(totalRevenue);
        preOrdersInfo.setAverageCheck(preOrdersByBooking.isEmpty() ? BigDecimal.ZERO :
                totalRevenue.divide(BigDecimal.valueOf(preOrdersByBooking.size()), 2, RoundingMode.HALF_UP));
        preOrdersInfo.setConversionRate(preOrdersByBooking.isEmpty() ? 0.0 :
                (double) approvedPreOrders / preOrdersByBooking.size());
        
        // Статистика клиентов
        AnalyticsOverviewResponse.ClientsInfo clientsInfo = new AnalyticsOverviewResponse.ClientsInfo();
        clientsInfo.setTotal((long) clients.size());
        long newClients = bookings.stream()
                .filter(b -> b.getClient() != null)
                .collect(Collectors.groupingBy(b -> b.getClient().getId()))
                .values().stream()
                .filter(clientBookings -> clientBookings.size() == 1)
                .count();
        clientsInfo.setNewClients(newClients);
        clientsInfo.setReturningClients(clients.size() - newClients);
        
        // Популярные блюда
        List<AnalyticsOverviewResponse.PopularItem> popularItems = getPopularItemsForOverview(preOrders);
        
        // Популярные столы
        List<AnalyticsOverviewResponse.PopularTable> popularTables = getPopularTablesForOverview(bookings);
        
        AnalyticsOverviewResponse response = new AnalyticsOverviewResponse();
        response.setRestaurantId(restaurantId);
        AnalyticsOverviewResponse.Period period = new AnalyticsOverviewResponse.Period();
        period.setDateFrom(finalDateFrom);
        period.setDateTo(finalDateTo);
        response.setPeriod(period);
        response.setBookings(bookingsInfo);
        response.setPreOrders(preOrdersInfo);
        response.setClients(clientsInfo);
        response.setPopularItems(popularItems);
        response.setPopularTables(popularTables);
        
        return response;
    }
    
    public String exportData(Long restaurantId, String type, String format, LocalDate dateFrom, LocalDate dateTo) {
        // Проверка существования ресторана
        restaurantRepository.findByIdAndIsActiveTrue(restaurantId)
                .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
        
        // Валидация дат
        final LocalDate finalDateFrom = dateFrom != null ? dateFrom : LocalDate.now().minusDays(30);
        final LocalDate finalDateTo = dateTo != null ? dateTo : LocalDate.now();
        if (finalDateFrom.isAfter(finalDateTo)) {
            throw new RuntimeException("INVALID_DATE_RANGE");
        }
        
        try {
            if ("json".equalsIgnoreCase(format)) {
                // Экспорт в JSON
                Object data = getExportData(restaurantId, type, finalDateFrom, finalDateTo);
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
            } else if ("csv".equalsIgnoreCase(format)) {
                // Упрощенный экспорт в CSV
                return exportToCsv(restaurantId, type, finalDateFrom, finalDateTo);
            } else {
                throw new RuntimeException("UNSUPPORTED_EXPORT_FORMAT");
            }
        } catch (Exception e) {
            throw new RuntimeException("EXPORT_ERROR");
        }
    }
    
    private Object getExportData(Long restaurantId, String type, LocalDate dateFrom, LocalDate dateTo) {
        if ("booking".equalsIgnoreCase(type)) {
            List<Booking> bookings = bookingRepository.findByRestaurantId(restaurantId).stream()
                    .filter(b -> !b.getDate().isBefore(dateFrom) && !b.getDate().isAfter(dateTo))
                    .collect(Collectors.toList());
            return bookings;
        } else if ("pre-order".equalsIgnoreCase(type) || "preorder".equalsIgnoreCase(type)) {
            List<Booking> bookings = bookingRepository.findByRestaurantId(restaurantId).stream()
                    .filter(b -> !b.getDate().isBefore(dateFrom) && !b.getDate().isAfter(dateTo))
                    .collect(Collectors.toList());
            return bookings.stream()
                    .flatMap(b -> bookingPreOrderRepository.findByBookingId(b.getId()).stream())
                    .collect(Collectors.toList());
        } else if ("client".equalsIgnoreCase(type)) {
            List<Booking> bookings = bookingRepository.findByRestaurantId(restaurantId).stream()
                    .filter(b -> b.getClient() != null)
                    .filter(b -> !b.getDate().isBefore(dateFrom) && !b.getDate().isAfter(dateTo))
                    .collect(Collectors.toList());
            Set<Long> clientIds = bookings.stream()
                    .map(b -> b.getClient().getId())
                    .collect(Collectors.toSet());
            return clientRepository.findAllById(clientIds);
        } else {
            throw new RuntimeException("INVALID_EXPORT_TYPE");
        }
    }
    
    private String exportToCsv(Long restaurantId, String type, LocalDate dateFrom, LocalDate dateTo) {
        // Упрощенная реализация CSV экспорта
        StringBuilder csv = new StringBuilder();
        
        if ("booking".equalsIgnoreCase(type)) {
            csv.append("ID,Date,Time,Table Number,Person Count,Status,Client Name\n");
            List<Booking> bookings = bookingRepository.findByRestaurantId(restaurantId).stream()
                    .filter(b -> !b.getDate().isBefore(dateFrom) && !b.getDate().isAfter(dateTo))
                    .collect(Collectors.toList());
            for (Booking booking : bookings) {
                csv.append(String.format("%d,%s,%s,%s,%d,%s,%s\n",
                        booking.getId(),
                        booking.getDate(),
                        booking.getTime(),
                        booking.getTable().getTableNumber(),
                        booking.getPersonCount(),
                        booking.getBookingStatus().getCode(),
                        booking.getClientName() != null ? booking.getClientName() : ""));
            }
        } else if ("pre-order".equalsIgnoreCase(type) || "preorder".equalsIgnoreCase(type)) {
            csv.append("ID,Booking ID,MenuItem,Quantity,Price,Total Price\n");
            List<Booking> bookings = bookingRepository.findByRestaurantId(restaurantId).stream()
                    .filter(b -> !b.getDate().isBefore(dateFrom) && !b.getDate().isAfter(dateTo))
                    .collect(Collectors.toList());
            for (Booking booking : bookings) {
                List<BookingPreOrder> preOrders = bookingPreOrderRepository.findByBookingId(booking.getId());
                for (BookingPreOrder preOrder : preOrders) {
                    csv.append(String.format("%d,%d,%s,%d,%s,%s\n",
                            preOrder.getId(),
                            booking.getId(),
                            preOrder.getMenuItem().getName(),
                            preOrder.getQuantity(),
                            preOrder.getPrice(),
                            preOrder.getTotalPrice()));
                }
            }
        } else if ("client".equalsIgnoreCase(type)) {
            csv.append("ID,Phone,First Name,Total Bookings,Total Pre Orders\n");
            List<Booking> bookings = bookingRepository.findByRestaurantId(restaurantId).stream()
                    .filter(b -> b.getClient() != null)
                    .filter(b -> !b.getDate().isBefore(dateFrom) && !b.getDate().isAfter(dateTo))
                    .collect(Collectors.toList());
            Set<Long> clientIds = bookings.stream()
                    .map(b -> b.getClient().getId())
                    .collect(Collectors.toSet());
            List<Client> clients = clientRepository.findAllById(clientIds);
            for (Client client : clients) {
                csv.append(String.format("%d,%s,%s,%d,%d\n",
                        client.getId(),
                        client.getPhone(),
                        client.getFirstName() != null ? client.getFirstName() : "",
                        client.getTotalBookings(),
                        client.getTotalPreOrders()));
            }
        }
        
        return csv.toString();
    }
    
    private List<BookingAnalyticsResponse.ChartItem> groupBookingsByPeriod(
            List<Booking> bookings, String groupBy) {
        if (groupBy == null || "day".equals(groupBy)) {
            return bookings.stream()
                    .collect(Collectors.groupingBy(Booking::getDate))
                    .entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> {
                        BookingAnalyticsResponse.ChartItem item = new BookingAnalyticsResponse.ChartItem();
                        item.setPeriod(entry.getKey().toString());
                        item.setCount((long) entry.getValue().size());
                        Map<String, Long> byStatus = entry.getValue().stream()
                                .collect(Collectors.groupingBy(
                                        b -> b.getBookingStatus().getCode(),
                                        Collectors.counting()
                                ));
                        item.setByStatus(byStatus);
                        return item;
                    })
                    .collect(Collectors.toList());
        }
        // TODO: Реализация для week и month
        return new ArrayList<>();
    }
    
    private List<PreOrderAnalyticsResponse.ChartItem> groupPreOrdersByPeriod(
            List<Booking> bookings, Map<Long, List<BookingPreOrder>> preOrdersByBooking, String groupBy) {
        if (groupBy == null || "day".equals(groupBy)) {
            return bookings.stream()
                    .filter(b -> preOrdersByBooking.containsKey(b.getId()))
                    .collect(Collectors.groupingBy(Booking::getDate))
                    .entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> {
                        PreOrderAnalyticsResponse.ChartItem item = new PreOrderAnalyticsResponse.ChartItem();
                        item.setPeriod(entry.getKey().toString());
                        long count = entry.getValue().stream()
                                .filter(b -> preOrdersByBooking.containsKey(b.getId()))
                                .count();
                        item.setCount(count);
                        
                        BigDecimal revenue = entry.getValue().stream()
                                .filter(b -> preOrdersByBooking.containsKey(b.getId()))
                                .map(b -> preOrdersByBooking.get(b.getId()).stream()
                                        .map(BookingPreOrder::getTotalPrice)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        item.setRevenue(revenue);
                        return item;
                    })
                    .collect(Collectors.toList());
        }
        // TODO: Реализация для week и month
        return new ArrayList<>();
    }
    
    private List<BookingAnalyticsResponse.PopularTable> getPopularTables(
            List<Booking> bookings, Long restaurantId) {
        Map<Long, Long> tableCounts = bookings.stream()
                .collect(Collectors.groupingBy(b -> b.getTable().getId(), Collectors.counting()));
        
        long total = bookings.size();
        
        return tableCounts.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    RestaurantTable table = tableRepository.findById(entry.getKey()).orElse(null);
                    if (table == null) return null;
                    
                    BookingAnalyticsResponse.PopularTable popularTable = new BookingAnalyticsResponse.PopularTable();
                    popularTable.setTableId(table.getId());
                    popularTable.setTableNumber(table.getTableNumber());
                    popularTable.setCount(entry.getValue());
                    popularTable.setPercentage(total > 0 ? (entry.getValue().doubleValue() / total) * 100 : 0.0);
                    return popularTable;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    private List<PreOrderAnalyticsResponse.PopularItem> getPopularItems(
            List<BookingPreOrder> preOrders, Long restaurantId) {
        Map<Long, ItemStats> itemStats = preOrders.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getMenuItem().getId(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                items -> {
                                    ItemStats stats = new ItemStats();
                                    stats.quantity = items.stream()
                                            .mapToInt(BookingPreOrder::getQuantity)
                                            .sum();
                                    stats.revenue = items.stream()
                                            .map(BookingPreOrder::getTotalPrice)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                                    stats.name = items.get(0).getMenuItem().getName();
                                    return stats;
                                }
                        )
                ));
        
        BigDecimal totalRevenue = itemStats.values().stream()
                .map(s -> s.revenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return itemStats.entrySet().stream()
                .sorted(Map.Entry.<Long, ItemStats>comparingByValue(
                        Comparator.comparing(s -> s.quantity)).reversed())
                .limit(10)
                .map(entry -> {
                    PreOrderAnalyticsResponse.PopularItem item = new PreOrderAnalyticsResponse.PopularItem();
                    item.setMenuItemId(entry.getKey());
                    item.setMenuItemName(entry.getValue().name);
                    item.setQuantity((long) entry.getValue().quantity);
                    item.setRevenue(entry.getValue().revenue);
                    item.setPercentage(totalRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                            entry.getValue().revenue.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100)).doubleValue() : 0.0);
                    return item;
                })
                .collect(Collectors.toList());
    }
    
    private List<ClientAnalyticsResponse.TopClient> getTopClients(
            List<Client> clients, Long restaurantId) {
        List<Booking> bookings = bookingRepository.findByRestaurantId(restaurantId);
        
        return clients.stream()
                .map(client -> {
                    List<Booking> clientBookings = bookings.stream()
                            .filter(b -> b.getClient() != null && b.getClient().getId().equals(client.getId()))
                            .collect(Collectors.toList());
                    
                    List<BookingPreOrder> clientPreOrders = clientBookings.stream()
                            .flatMap(b -> bookingPreOrderRepository.findByBookingId(b.getId()).stream())
                            .collect(Collectors.toList());
                    
                    BigDecimal totalSpent = clientPreOrders.stream()
                            .map(BookingPreOrder::getTotalPrice)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    ClientAnalyticsResponse.TopClient topClient = new ClientAnalyticsResponse.TopClient();
                    topClient.setClientId(client.getId());
                    topClient.setClientPhone(client.getPhone());
                    topClient.setTotalBookings(clientBookings.size());
                    topClient.setTotalPreOrders(clientPreOrders.size());
                    topClient.setTotalSpent(totalSpent);
                    return topClient;
                })
                .sorted((c1, c2) -> {
                    int bookingsCompare = Integer.compare(c2.getTotalBookings(), c1.getTotalBookings());
                    if (bookingsCompare != 0) return bookingsCompare;
                    return Integer.compare(c2.getTotalPreOrders(), c1.getTotalPreOrders());
                })
                .limit(10)
                .collect(Collectors.toList());
    }
    
    private List<AnalyticsOverviewResponse.PopularItem> getPopularItemsForOverview(
            List<BookingPreOrder> preOrders) {
        Map<Long, ItemStats> itemStats = preOrders.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getMenuItem().getId(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                items -> {
                                    ItemStats stats = new ItemStats();
                                    stats.quantity = items.stream()
                                            .mapToInt(BookingPreOrder::getQuantity)
                                            .sum();
                                    stats.name = items.get(0).getMenuItem().getName();
                                    return stats;
                                }
                        )
                ));
        
        return itemStats.entrySet().stream()
                .sorted(Map.Entry.<Long, ItemStats>comparingByValue(
                        Comparator.comparing(s -> s.quantity)).reversed())
                .limit(5)
                .map(entry -> {
                    AnalyticsOverviewResponse.PopularItem item = new AnalyticsOverviewResponse.PopularItem();
                    item.setMenuItemId(entry.getKey());
                    item.setMenuItemName(entry.getValue().name);
                    item.setQuantity((long) entry.getValue().quantity);
                    return item;
                })
                .collect(Collectors.toList());
    }
    
    private List<AnalyticsOverviewResponse.PopularTable> getPopularTablesForOverview(
            List<Booking> bookings) {
        Map<Long, Long> tableCounts = bookings.stream()
                .collect(Collectors.groupingBy(b -> b.getTable().getId(), Collectors.counting()));
        
        return tableCounts.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    RestaurantTable table = tableRepository.findById(entry.getKey()).orElse(null);
                    if (table == null) return null;
                    
                    AnalyticsOverviewResponse.PopularTable popularTable = new AnalyticsOverviewResponse.PopularTable();
                    popularTable.setTableId(table.getId());
                    popularTable.setTableNumber(table.getTableNumber());
                    popularTable.setCount(entry.getValue());
                    return popularTable;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    private static class ItemStats {
        int quantity;
        BigDecimal revenue;
        String name;
    }
}
