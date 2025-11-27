package com.restohub.adminapi.service;

import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.entity.Floor;
import com.restohub.adminapi.entity.Restaurant;
import com.restohub.adminapi.repository.FloorRepository;
import com.restohub.adminapi.repository.RestaurantRepository;
import com.restohub.adminapi.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FloorService {
    
    private final FloorRepository floorRepository;
    private final RestaurantRepository restaurantRepository;
    private final RoomRepository roomRepository;
    
    @Autowired
    public FloorService(
            FloorRepository floorRepository,
            RestaurantRepository restaurantRepository,
            RoomRepository roomRepository) {
        this.floorRepository = floorRepository;
        this.restaurantRepository = restaurantRepository;
        this.roomRepository = roomRepository;
    }
    
    @Transactional
    public FloorResponse createFloor(Long restaurantId, CreateFloorRequest request) {
        // Проверка существования ресторана
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
        
        // Проверка уникальности номера этажа
        if (floorRepository.findByRestaurantIdAndFloorNumberAndIsActiveTrue(restaurantId, request.getFloorNumber().trim()).isPresent()) {
            throw new RuntimeException("FLOOR_NUMBER_EXISTS");
        }
        
        Floor floor = new Floor();
        floor.setRestaurant(restaurant);
        floor.setFloorNumber(request.getFloorNumber().trim());
        floor.setIsActive(true);
        
        floor = floorRepository.save(floor);
        
        return toResponse(floor);
    }
    
    public PaginationResponse<List<FloorListItemResponse>> getFloors(
            Long restaurantId, Integer limit, Integer offset, String sortBy, String sortOrder) {
        
        // Проверка существования ресторана
        restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
        
        // Сортировка
        Sort sort = buildSort(sortBy, sortOrder);
        Pageable pageable = PageRequest.of(offset / limit, limit, sort);
        
        Page<Floor> page = floorRepository.findAll(
                (root, query, cb) -> cb.and(
                    cb.equal(root.get("restaurant").get("id"), restaurantId),
                    cb.equal(root.get("isActive"), true)
                ),
                pageable
        );
        
        List<FloorListItemResponse> items = page.getContent().stream()
                .map(floor -> {
                    FloorListItemResponse response = toListItemResponse(floor);
                    // Подсчет количества активных залов
                    int roomsCount = roomRepository.findByFloorIdAndIsActiveTrue(floor.getId()).size();
                    response.setRoomsCount(roomsCount);
                    return response;
                })
                .collect(Collectors.toList());
        
        PaginationResponse.PaginationInfo pagination = new PaginationResponse.PaginationInfo(
                page.getTotalElements(),
                limit,
                offset,
                (offset + limit) < page.getTotalElements()
        );
        
        return new PaginationResponse<>(items, pagination);
    }
    
    public FloorResponse getFloor(Long restaurantId, Long floorId) {
        Floor floor = floorRepository.findByIdAndRestaurantIdAndIsActiveTrue(floorId, restaurantId)
                .orElseThrow(() -> new RuntimeException("FLOOR_NOT_FOUND"));
        
        return toResponse(floor);
    }
    
    @Transactional
    public FloorResponse updateFloor(Long restaurantId, Long floorId, UpdateFloorRequest request) {
        Floor floor = floorRepository.findByIdAndRestaurantIdAndIsActiveTrue(floorId, restaurantId)
                .orElseThrow(() -> new RuntimeException("FLOOR_NOT_FOUND"));
        
        // Обновление полей (PATCH-логика)
        if (request.getFloorNumber() != null) {
            String trimmedFloorNumber = request.getFloorNumber().trim();
            // Проверка уникальности (исключая текущий этаж)
            floorRepository.findByRestaurantIdAndFloorNumberAndIsActiveTrue(restaurantId, trimmedFloorNumber)
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(floorId)) {
                            throw new RuntimeException("FLOOR_NUMBER_EXISTS");
                        }
                    });
            floor.setFloorNumber(trimmedFloorNumber);
        }
        
        floor = floorRepository.save(floor);
        
        return toResponse(floor);
    }
    
    @Transactional
    public void deleteFloor(Long restaurantId, Long floorId) {
        Floor floor = floorRepository.findByIdAndRestaurantIdAndIsActiveTrue(floorId, restaurantId)
                .orElseThrow(() -> new RuntimeException("FLOOR_NOT_FOUND"));
        
        // Проверка использования этажа
        int activeRoomsCount = roomRepository.findByFloorIdAndIsActiveTrue(floorId).size();
        
        if (activeRoomsCount > 0) {
            throw new RuntimeException("FLOOR_IN_USE");
        }
        
        // Мягкое удаление
        floor.setIsActive(false);
        floor.setDeletedAt(LocalDateTime.now());
        floorRepository.save(floor);
    }
    
    private FloorResponse toResponse(Floor floor) {
        FloorResponse response = new FloorResponse();
        response.setId(floor.getId());
        response.setRestaurantId(floor.getRestaurant().getId());
        response.setFloorNumber(floor.getFloorNumber());
        response.setIsActive(floor.getIsActive());
        response.setCreatedAt(floor.getCreatedAt() != null ? floor.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        response.setUpdatedAt(floor.getUpdatedAt() != null ? floor.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        response.setDeletedAt(floor.getDeletedAt() != null ? floor.getDeletedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        return response;
    }
    
    private FloorListItemResponse toListItemResponse(Floor floor) {
        FloorListItemResponse response = new FloorListItemResponse();
        response.setId(floor.getId());
        response.setRestaurantId(floor.getRestaurant().getId());
        response.setFloorNumber(floor.getFloorNumber());
        response.setIsActive(floor.getIsActive());
        response.setCreatedAt(floor.getCreatedAt() != null ? floor.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        return response;
    }
    
    private Sort buildSort(String sortBy, String sortOrder) {
        String field = sortBy != null ? sortBy : "floorNumber";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        // Маппинг полей
        switch (field) {
            case "floorNumber":
                return Sort.by(direction, "floorNumber");
            case "createdAt":
                return Sort.by(direction, "createdAt");
            default:
                return Sort.by(Sort.Direction.ASC, "floorNumber");
        }
    }
}

