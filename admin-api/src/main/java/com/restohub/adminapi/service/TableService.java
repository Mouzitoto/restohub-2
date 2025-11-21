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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TableService {
    
    private final TableRepository tableRepository;
    private final RestaurantRepository restaurantRepository;
    private final RoomRepository roomRepository;
    private final FloorRepository floorRepository;
    private final ImageRepository imageRepository;
    private final BookingRepository bookingRepository;
    private final BookingStatusRepository bookingStatusRepository;
    
    @Autowired
    public TableService(
            TableRepository tableRepository,
            RestaurantRepository restaurantRepository,
            RoomRepository roomRepository,
            FloorRepository floorRepository,
            ImageRepository imageRepository,
            BookingRepository bookingRepository,
            BookingStatusRepository bookingStatusRepository) {
        this.tableRepository = tableRepository;
        this.restaurantRepository = restaurantRepository;
        this.roomRepository = roomRepository;
        this.floorRepository = floorRepository;
        this.imageRepository = imageRepository;
        this.bookingRepository = bookingRepository;
        this.bookingStatusRepository = bookingStatusRepository;
    }
    
    @Transactional
    public TableResponse createTable(Long restaurantId, CreateTableRequest request) {
        // Проверка существования ресторана
        Restaurant restaurant = restaurantRepository.findByIdAndIsActiveTrue(restaurantId)
                .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
        
        // Проверка существования и принадлежности помещения к ресторану
        Room room = roomRepository.findByIdAndRestaurantIdAndIsActiveTrue(request.getRoomId(), restaurantId)
                .orElseThrow(() -> new RuntimeException("ROOM_NOT_FOUND"));
        
        // Проверка уникальности номера стола в рамках помещения
        if (tableRepository.findByRoomIdAndTableNumberAndIsActiveTrue(request.getRoomId(), request.getTableNumber().trim()).isPresent()) {
            throw new RuntimeException("TABLE_NUMBER_EXISTS");
        }
        
        // Проверка изображения
        Image image = null;
        if (request.getImageId() != null) {
            image = imageRepository.findByIdAndIsActiveTrue(request.getImageId())
                    .orElseThrow(() -> new RuntimeException("IMAGE_NOT_FOUND"));
        }
        
        RestaurantTable table = new RestaurantTable();
        table.setRoom(room);
        table.setTableNumber(request.getTableNumber().trim());
        table.setCapacity(request.getCapacity());
        table.setDescription(request.getDescription());
        table.setImage(image);
        table.setDepositAmount(request.getDepositAmount());
        table.setDepositNote(request.getDepositNote());
        table.setIsActive(true);
        
        table = tableRepository.save(table);
        
        return toResponse(table);
    }
    
    public PaginationResponse<List<TableListItemResponse>> getTables(
            Long restaurantId, Integer limit, Integer offset, Long roomId, Long floorId,
            Integer minCapacity, Integer maxCapacity, String sortBy, String sortOrder) {
        
        // Проверка существования ресторана
        restaurantRepository.findByIdAndIsActiveTrue(restaurantId)
                .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
        
        // Построение спецификации
        Specification<RestaurantTable> spec = Specification.where(
                (root, query, cb) -> cb.equal(root.get("room").get("floor").get("restaurant").get("id"), restaurantId)
        );
        
        spec = spec.and((root, query, cb) -> cb.equal(root.get("isActive"), true));
        
        // Фильтр по помещению
        if (roomId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("room").get("id"), roomId));
        }
        
        // Фильтр по этажу
        if (floorId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("room").get("floor").get("id"), floorId));
        }
        
        // Фильтр по количеству мест
        if (minCapacity != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("capacity"), minCapacity));
        }
        if (maxCapacity != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("capacity"), maxCapacity));
        }
        
        // Сортировка
        Sort sort = buildSort(sortBy, sortOrder);
        Pageable pageable = PageRequest.of(offset / limit, limit, sort);
        
        Page<RestaurantTable> page = tableRepository.findAll(spec, pageable);
        
        List<TableListItemResponse> items = page.getContent().stream()
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
    
    public TableResponse getTable(Long restaurantId, Long tableId) {
        RestaurantTable table = tableRepository.findByIdAndRestaurantIdAndIsActiveTrue(tableId, restaurantId)
                .orElseThrow(() -> new RuntimeException("TABLE_NOT_FOUND"));
        
        return toResponse(table);
    }
    
    @Transactional
    public TableResponse updateTable(Long restaurantId, Long tableId, UpdateTableRequest request) {
        RestaurantTable table = tableRepository.findByIdAndRestaurantIdAndIsActiveTrue(tableId, restaurantId)
                .orElseThrow(() -> new RuntimeException("TABLE_NOT_FOUND"));
        
        // Обновление полей (PATCH-логика)
        if (request.getTableNumber() != null) {
            String trimmedTableNumber = request.getTableNumber().trim();
            Long roomIdToCheck = request.getRoomId() != null ? request.getRoomId() : table.getRoom().getId();
            
            // Проверка уникальности (исключая текущий стол)
            tableRepository.findByRoomIdAndTableNumberAndIsActiveTrue(roomIdToCheck, trimmedTableNumber)
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(tableId)) {
                            throw new RuntimeException("TABLE_NUMBER_EXISTS");
                        }
                    });
            table.setTableNumber(trimmedTableNumber);
        }
        
        if (request.getRoomId() != null) {
            Room room = roomRepository.findByIdAndRestaurantIdAndIsActiveTrue(request.getRoomId(), restaurantId)
                    .orElseThrow(() -> new RuntimeException("ROOM_NOT_FOUND"));
            table.setRoom(room);
        }
        
        if (request.getCapacity() != null) {
            table.setCapacity(request.getCapacity());
        }
        if (request.getDescription() != null) {
            table.setDescription(request.getDescription());
        }
        if (request.getImageId() != null) {
            if (request.getImageId() == 0) {
                table.setImage(null);
            } else {
                Image image = imageRepository.findByIdAndIsActiveTrue(request.getImageId())
                        .orElseThrow(() -> new RuntimeException("IMAGE_NOT_FOUND"));
                table.setImage(image);
            }
        }
        if (request.getDepositAmount() != null) {
            table.setDepositAmount(request.getDepositAmount());
        }
        if (request.getDepositNote() != null) {
            table.setDepositNote(request.getDepositNote());
        }
        
        table = tableRepository.save(table);
        
        return toResponse(table);
    }
    
    @Transactional
    public void deleteTable(Long restaurantId, Long tableId) {
        RestaurantTable table = tableRepository.findByIdAndRestaurantIdAndIsActiveTrue(tableId, restaurantId)
                .orElseThrow(() -> new RuntimeException("TABLE_NOT_FOUND"));
        
        // Проверка активных бронирований
        BookingStatus approvedStatus = bookingStatusRepository.findByCodeAndIsActiveTrue("APPROVED")
                .orElse(null);
        BookingStatus pendingStatus = bookingStatusRepository.findByCodeAndIsActiveTrue("PENDING")
                .orElse(null);
        
        if (approvedStatus != null || pendingStatus != null) {
            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();
            
            long activeBookingsCount = bookingRepository.findAll().stream()
                    .filter(booking -> booking.getTable().getId().equals(tableId))
                    .filter(booking -> {
                        if (approvedStatus != null && booking.getBookingStatus().getId().equals(approvedStatus.getId())) {
                            return true;
                        }
                        if (pendingStatus != null && booking.getBookingStatus().getId().equals(pendingStatus.getId())) {
                            return true;
                        }
                        return false;
                    })
                    .filter(booking -> {
                        LocalDate bookingDate = booking.getDate();
                        LocalTime bookingTime = booking.getTime();
                        return bookingDate.isAfter(today) || 
                               (bookingDate.equals(today) && bookingTime != null && bookingTime.isAfter(now));
                    })
                    .count();
            
            if (activeBookingsCount > 0) {
                throw new RuntimeException("TABLE_HAS_ACTIVE_BOOKINGS");
            }
        }
        
        // Мягкое удаление
        table.setIsActive(false);
        table.setDeletedAt(LocalDateTime.now());
        tableRepository.save(table);
    }
    
    public TableMapResponse getTableMap(Long restaurantId, Long floorId, Long roomId) {
        // Проверка существования ресторана
        restaurantRepository.findByIdAndIsActiveTrue(restaurantId)
                .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
        
        // Получаем все этажи ресторана
        List<Floor> floors = floorRepository.findByRestaurantIdAndIsActiveTrue(restaurantId);
        
        // Фильтр по этажу, если указан
        if (floorId != null) {
            floors = floors.stream()
                    .filter(f -> f.getId().equals(floorId))
                    .collect(Collectors.toList());
        }
        
        List<TableMapResponse.FloorMapItem> floorItems = new ArrayList<>();
        
        for (Floor floor : floors) {
            // Получаем все помещения этажа
            List<Room> rooms = roomRepository.findByFloorIdAndIsActiveTrue(floor.getId());
            
            // Фильтр по помещению, если указано
            if (roomId != null) {
                rooms = rooms.stream()
                        .filter(r -> r.getId().equals(roomId))
                        .collect(Collectors.toList());
            }
            
            List<TableMapResponse.RoomMapItem> roomItems = new ArrayList<>();
            
            for (Room room : rooms) {
                // Получаем все столы помещения
                List<RestaurantTable> tables = tableRepository.findByRoomIdAndIsActiveTrue(room.getId());
                
                List<TableMapResponse.TableMapItem> tableItems = tables.stream()
                        .map(this::toTableMapItem)
                        .collect(Collectors.toList());
                
                TableMapResponse.RoomMapItem roomItem = new TableMapResponse.RoomMapItem();
                roomItem.setId(room.getId());
                roomItem.setName(room.getName());
                roomItem.setImageId(room.getImage() != null ? room.getImage().getId() : null);
                roomItem.setTables(tableItems);
                
                roomItems.add(roomItem);
            }
            
            TableMapResponse.FloorMapItem floorItem = new TableMapResponse.FloorMapItem();
            floorItem.setId(floor.getId());
            floorItem.setFloorNumber(floor.getFloorNumber());
            floorItem.setRooms(roomItems);
            
            floorItems.add(floorItem);
        }
        
        TableMapResponse response = new TableMapResponse();
        response.setFloors(floorItems);
        
        return response;
    }
    
    private TableResponse toResponse(RestaurantTable table) {
        TableResponse response = new TableResponse();
        response.setId(table.getId());
        response.setRestaurantId(table.getRoom().getFloor().getRestaurant().getId());
        response.setRoomId(table.getRoom().getId());
        response.setTableNumber(table.getTableNumber());
        response.setCapacity(table.getCapacity());
        response.setDescription(table.getDescription());
        response.setImageId(table.getImage() != null ? table.getImage().getId() : null);
        response.setDepositAmount(table.getDepositAmount());
        response.setDepositNote(table.getDepositNote());
        response.setIsActive(table.getIsActive());
        response.setCreatedAt(table.getCreatedAt() != null ? table.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        response.setUpdatedAt(table.getUpdatedAt() != null ? table.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        response.setDeletedAt(table.getDeletedAt() != null ? table.getDeletedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        return response;
    }
    
    private TableListItemResponse toListItemResponse(RestaurantTable table) {
        TableListItemResponse response = new TableListItemResponse();
        response.setId(table.getId());
        response.setRestaurantId(table.getRoom().getFloor().getRestaurant().getId());
        response.setRoomId(table.getRoom().getId());
        response.setTableNumber(table.getTableNumber());
        response.setCapacity(table.getCapacity());
        response.setDescription(table.getDescription());
        response.setImageId(table.getImage() != null ? table.getImage().getId() : null);
        response.setDepositAmount(table.getDepositAmount());
        response.setDepositNote(table.getDepositNote());
        response.setIsActive(table.getIsActive());
        response.setCreatedAt(table.getCreatedAt() != null ? table.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        return response;
    }
    
    private TableMapResponse.TableMapItem toTableMapItem(RestaurantTable table) {
        TableMapResponse.TableMapItem item = new TableMapResponse.TableMapItem();
        item.setId(table.getId());
        item.setTableNumber(table.getTableNumber());
        item.setCapacity(table.getCapacity());
        item.setDescription(table.getDescription());
        item.setImageId(table.getImage() != null ? table.getImage().getId() : null);
        item.setDepositAmount(table.getDepositAmount());
        item.setDepositNote(table.getDepositNote());
        return item;
    }
    
    private Sort buildSort(String sortBy, String sortOrder) {
        String field = sortBy != null ? sortBy : "tableNumber";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        // Маппинг полей
        switch (field) {
            case "tableNumber":
                return Sort.by(direction, "tableNumber");
            case "capacity":
                return Sort.by(direction, "capacity");
            case "createdAt":
                return Sort.by(direction, "createdAt");
            default:
                return Sort.by(Sort.Direction.ASC, "tableNumber");
        }
    }
}

