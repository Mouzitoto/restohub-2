package com.restohub.adminapi.service;

import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.entity.Floor;
import com.restohub.adminapi.entity.Image;
import com.restohub.adminapi.entity.Restaurant;
import com.restohub.adminapi.entity.Room;
import com.restohub.adminapi.repository.FloorRepository;
import com.restohub.adminapi.repository.ImageRepository;
import com.restohub.adminapi.repository.RestaurantRepository;
import com.restohub.adminapi.repository.RoomRepository;
import com.restohub.adminapi.repository.TableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomService {
    
    private final RoomRepository roomRepository;
    private final RestaurantRepository restaurantRepository;
    private final FloorRepository floorRepository;
    private final ImageRepository imageRepository;
    private final TableRepository tableRepository;
    
    @Autowired
    public RoomService(
            RoomRepository roomRepository,
            RestaurantRepository restaurantRepository,
            FloorRepository floorRepository,
            ImageRepository imageRepository,
            TableRepository tableRepository) {
        this.roomRepository = roomRepository;
        this.restaurantRepository = restaurantRepository;
        this.floorRepository = floorRepository;
        this.imageRepository = imageRepository;
        this.tableRepository = tableRepository;
    }
    
    @Transactional
    public RoomResponse createRoom(Long restaurantId, CreateRoomRequest request) {
        // Проверка существования ресторана
        Restaurant restaurant = restaurantRepository.findByIdAndIsActiveTrue(restaurantId)
                .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
        
        // Проверка существования и принадлежности этажа к ресторану
        Floor floor = floorRepository.findByIdAndRestaurantIdAndIsActiveTrue(request.getFloorId(), restaurantId)
                .orElseThrow(() -> new RuntimeException("FLOOR_NOT_FOUND"));
        
        // Проверка изображения
        Image image = null;
        if (request.getImageId() != null) {
            image = imageRepository.findByIdAndIsActiveTrue(request.getImageId())
                    .orElseThrow(() -> new RuntimeException("IMAGE_NOT_FOUND"));
        }
        
        Room room = new Room();
        room.setFloor(floor);
        room.setName(request.getName().trim());
        room.setDescription(request.getDescription());
        room.setIsSmoking(request.getIsSmoking() != null ? request.getIsSmoking() : false);
        room.setIsOutdoor(request.getIsOutdoor() != null ? request.getIsOutdoor() : false);
        room.setImage(image);
        room.setIsActive(true);
        
        room = roomRepository.save(room);
        
        return toResponse(room);
    }
    
    public PaginationResponse<List<RoomListItemResponse>> getRooms(
            Long restaurantId, Integer limit, Integer offset, Long floorId, String sortBy, String sortOrder) {
        
        // Проверка существования ресторана
        restaurantRepository.findByIdAndIsActiveTrue(restaurantId)
                .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
        
        // Построение спецификации
        Specification<Room> spec = Specification.where(
                (root, query, cb) -> cb.equal(root.get("floor").get("restaurant").get("id"), restaurantId)
        );
        
        spec = spec.and((root, query, cb) -> cb.equal(root.get("isActive"), true));
        
        // Фильтр по этажу
        if (floorId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("floor").get("id"), floorId));
        }
        
        // Сортировка
        Sort sort = buildSort(sortBy, sortOrder);
        Pageable pageable = PageRequest.of(offset / limit, limit, sort);
        
        Page<Room> page = roomRepository.findAll(spec, pageable);
        
        List<RoomListItemResponse> items = page.getContent().stream()
                .map(room -> {
                    RoomListItemResponse response = toListItemResponse(room);
                    // Подсчет количества активных столов
                    int tableCount = tableRepository.findByRoomIdAndIsActiveTrue(room.getId()).size();
                    response.setTableCount(tableCount);
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
    
    public RoomResponse getRoom(Long restaurantId, Long roomId) {
        Room room = roomRepository.findByIdAndRestaurantIdAndIsActiveTrue(roomId, restaurantId)
                .orElseThrow(() -> new RuntimeException("ROOM_NOT_FOUND"));
        
        return toResponse(room);
    }
    
    @Transactional
    public RoomResponse updateRoom(Long restaurantId, Long roomId, UpdateRoomRequest request) {
        Room room = roomRepository.findByIdAndRestaurantIdAndIsActiveTrue(roomId, restaurantId)
                .orElseThrow(() -> new RuntimeException("ROOM_NOT_FOUND"));
        
        // Обновление полей (PATCH-логика)
        if (request.getName() != null) {
            room.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            room.setDescription(request.getDescription());
        }
        if (request.getFloorId() != null) {
            Floor floor = floorRepository.findByIdAndRestaurantIdAndIsActiveTrue(request.getFloorId(), restaurantId)
                    .orElseThrow(() -> new RuntimeException("FLOOR_NOT_FOUND"));
            room.setFloor(floor);
        }
        if (request.getIsSmoking() != null) {
            room.setIsSmoking(request.getIsSmoking());
        }
        if (request.getIsOutdoor() != null) {
            room.setIsOutdoor(request.getIsOutdoor());
        }
        if (request.getImageId() != null) {
            if (request.getImageId() == 0) {
                room.setImage(null);
            } else {
                Image image = imageRepository.findByIdAndIsActiveTrue(request.getImageId())
                        .orElseThrow(() -> new RuntimeException("IMAGE_NOT_FOUND"));
                room.setImage(image);
            }
        }
        
        room = roomRepository.save(room);
        
        return toResponse(room);
    }
    
    @Transactional
    public void deleteRoom(Long restaurantId, Long roomId) {
        Room room = roomRepository.findByIdAndRestaurantIdAndIsActiveTrue(roomId, restaurantId)
                .orElseThrow(() -> new RuntimeException("ROOM_NOT_FOUND"));
        
        // Проверка использования помещения
        int activeTablesCount = tableRepository.findByRoomIdAndIsActiveTrue(roomId).size();
        
        if (activeTablesCount > 0) {
            throw new RuntimeException("ROOM_IN_USE");
        }
        
        // Мягкое удаление
        room.setIsActive(false);
        room.setDeletedAt(LocalDateTime.now());
        roomRepository.save(room);
    }
    
    private RoomResponse toResponse(Room room) {
        RoomResponse response = new RoomResponse();
        response.setId(room.getId());
        response.setRestaurantId(room.getFloor().getRestaurant().getId());
        response.setFloorId(room.getFloor().getId());
        response.setName(room.getName());
        response.setDescription(room.getDescription());
        response.setIsSmoking(room.getIsSmoking());
        response.setIsOutdoor(room.getIsOutdoor());
        response.setImageId(room.getImage() != null ? room.getImage().getId() : null);
        response.setIsActive(room.getIsActive());
        response.setCreatedAt(room.getCreatedAt() != null ? room.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        response.setUpdatedAt(room.getUpdatedAt() != null ? room.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        response.setDeletedAt(room.getDeletedAt() != null ? room.getDeletedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        return response;
    }
    
    private RoomListItemResponse toListItemResponse(Room room) {
        RoomListItemResponse response = new RoomListItemResponse();
        response.setId(room.getId());
        response.setRestaurantId(room.getFloor().getRestaurant().getId());
        response.setFloorId(room.getFloor().getId());
        response.setName(room.getName());
        response.setDescription(room.getDescription());
        response.setIsSmoking(room.getIsSmoking());
        response.setIsOutdoor(room.getIsOutdoor());
        response.setImageId(room.getImage() != null ? room.getImage().getId() : null);
        response.setIsActive(room.getIsActive());
        response.setCreatedAt(room.getCreatedAt() != null ? room.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        return response;
    }
    
    private Sort buildSort(String sortBy, String sortOrder) {
        String field = sortBy != null ? sortBy : "name";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        // Маппинг полей
        switch (field) {
            case "name":
                return Sort.by(direction, "name");
            case "createdAt":
                return Sort.by(direction, "createdAt");
            default:
                return Sort.by(Sort.Direction.ASC, "name");
        }
    }
}

