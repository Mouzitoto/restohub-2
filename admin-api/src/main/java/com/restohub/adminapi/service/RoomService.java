package com.restohub.adminapi.service;

import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.entity.Floor;
import com.restohub.adminapi.entity.Image;
import com.restohub.adminapi.entity.Room;
import com.restohub.adminapi.entity.RestaurantTable;
import com.restohub.adminapi.repository.FloorRepository;
import com.restohub.adminapi.repository.ImageRepository;
import com.restohub.adminapi.repository.RestaurantRepository;
import com.restohub.adminapi.repository.RoomRepository;
import com.restohub.adminapi.repository.TableRepository;
import com.restohub.adminapi.util.TablePositionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomService {
    
    private static final Logger logger = LoggerFactory.getLogger(RoomService.class);
    
    private final RoomRepository roomRepository;
    private final RestaurantRepository restaurantRepository;
    private final FloorRepository floorRepository;
    private final ImageRepository imageRepository;
    private final TableRepository tableRepository;
    private final ImageService imageService;
    private final TableService tableService;
    private final TablePositionUtils tablePositionUtils;
    
    @Autowired
    public RoomService(
            RoomRepository roomRepository,
            RestaurantRepository restaurantRepository,
            FloorRepository floorRepository,
            ImageRepository imageRepository,
            TableRepository tableRepository,
            ImageService imageService,
            TableService tableService,
            TablePositionUtils tablePositionUtils) {
        this.roomRepository = roomRepository;
        this.restaurantRepository = restaurantRepository;
        this.floorRepository = floorRepository;
        this.imageRepository = imageRepository;
        this.tableRepository = tableRepository;
        this.imageService = imageService;
        this.tableService = tableService;
        this.tablePositionUtils = tablePositionUtils;
    }
    
    @Transactional
    public RoomResponse createRoom(Long restaurantId, CreateRoomRequest request) {
        // Проверка существования ресторана
        restaurantRepository.findByIdAndIsActiveTrue(restaurantId)
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
            Image oldImage = room.getImage();
            if (request.getImageId() == 0) {
                room.setImage(null);
            } else {
                Image image = imageRepository.findByIdAndIsActiveTrue(request.getImageId())
                        .orElseThrow(() -> new RuntimeException("IMAGE_NOT_FOUND"));
                room.setImage(image);
            }
            // Если изображение изменилось, очищаем координаты столов
            Long oldImageId = oldImage != null ? oldImage.getId() : null;
            Long newImageId = room.getImage() != null ? room.getImage().getId() : null;
            if ((oldImageId == null && newImageId != null) || (oldImageId != null && !oldImageId.equals(newImageId))) {
                tableService.clearTablePositionsForRoom(roomId);
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
    
    @Transactional
    public RoomResponse uploadRoomImage(Long restaurantId, Long roomId, MultipartFile file) throws IOException {
        Room room = roomRepository.findByIdAndRestaurantIdAndIsActiveTrue(roomId, restaurantId)
                .orElseThrow(() -> new RuntimeException("ROOM_NOT_FOUND"));
        
        // Загружаем изображение
        ImageResponse imageResponse = imageService.uploadImage(file);
        Image image = imageRepository.findByIdAndIsActiveTrue(imageResponse.getId())
                .orElseThrow(() -> new RuntimeException("IMAGE_NOT_FOUND"));
        
        // Удаляем старое изображение, если оно было
        Image oldImage = room.getImage();
        room.setImage(image);
        
        room = roomRepository.save(room);
        
        // Очищаем координаты столов при замене изображения
        tableService.clearTablePositionsForRoom(roomId);
        
        // Мягко удаляем старое изображение, если оно было и больше не используется
        if (oldImage != null) {
            try {
                imageService.deleteImage(oldImage.getId());
            } catch (RuntimeException e) {
                // Если изображение используется где-то еще, просто логируем
                logger.debug("Could not delete old image {}: {}", oldImage.getId(), e.getMessage());
            }
        }
        
        return toResponse(room);
    }
    
    @Transactional
    public RoomResponse deleteRoomImage(Long restaurantId, Long roomId) {
        Room room = roomRepository.findByIdAndRestaurantIdAndIsActiveTrue(roomId, restaurantId)
                .orElseThrow(() -> new RuntimeException("ROOM_NOT_FOUND"));
        
        Image oldImage = room.getImage();
        room.setImage(null);
        
        room = roomRepository.save(room);
        
        // Очищаем координаты столов при удалении изображения
        tableService.clearTablePositionsForRoom(roomId);
        
        // Мягко удаляем изображение, если оно было
        if (oldImage != null) {
            try {
                imageService.deleteImage(oldImage.getId());
            } catch (RuntimeException e) {
                // Если изображение используется где-то еще, просто логируем
                logger.debug("Could not delete image {}: {}", oldImage.getId(), e.getMessage());
            }
        }
        
        return toResponse(room);
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
    
    public RoomLayoutResponse getRoomLayout(Long restaurantId, Long roomId) {
        Room room = roomRepository.findByIdAndRestaurantIdAndIsActiveTrue(roomId, restaurantId)
                .orElseThrow(() -> new RuntimeException("ROOM_NOT_FOUND"));
        
        RoomResponse roomResponse = toResponse(room);
        
        // Получаем все столы зала
        List<RestaurantTable> tables = tableRepository.findByRoomIdAndIsActiveTrue(roomId);
        List<TableResponse> tableResponses = tables.stream()
                .map(table -> tableService.getTable(restaurantId, table.getId()))
                .collect(Collectors.toList());
        
        // Формируем URL изображения
        String imageUrl = null;
        if (room.getImage() != null) {
            imageUrl = "/admin-api/image?id=" + room.getImage().getId() + "&isPreview=false";
        }
        
        RoomLayoutResponse response = new RoomLayoutResponse();
        response.setRoom(roomResponse);
        response.setTables(tableResponses);
        response.setImageUrl(imageUrl);
        
        return response;
    }
    
    @Transactional
    public List<TableResponse> updateTablePositions(Long restaurantId, Long roomId, List<UpdateTablePositionRequest> requests) {
        // Проверка принадлежности зала к ресторану
        roomRepository.findByIdAndRestaurantIdAndIsActiveTrue(roomId, restaurantId)
                .orElseThrow(() -> new RuntimeException("ROOM_NOT_FOUND"));
        
        // Валидация координат и проверка пересечений
        tablePositionUtils.checkRectangleIntersections(requests);
        
        // Обновляем позиции столов
        List<TableResponse> updatedTables = new java.util.ArrayList<>();
        for (UpdateTablePositionRequest request : requests) {
            RestaurantTable table = tableRepository.findByIdAndRestaurantIdAndIsActiveTrue(
                    request.getTableId(), restaurantId)
                    .orElseThrow(() -> new RuntimeException("TABLE_NOT_FOUND"));
            
            // Проверяем, что стол принадлежит указанному залу
            if (!table.getRoom().getId().equals(roomId)) {
                throw new RuntimeException("TABLE_NOT_IN_ROOM");
            }
            
            table.setPositionX1(request.getPositionX1());
            table.setPositionY1(request.getPositionY1());
            table.setPositionX2(request.getPositionX2());
            table.setPositionY2(request.getPositionY2());
            
            table = tableRepository.save(table);
            updatedTables.add(tableService.getTable(restaurantId, table.getId()));
        }
        
        return updatedTables;
    }
}

