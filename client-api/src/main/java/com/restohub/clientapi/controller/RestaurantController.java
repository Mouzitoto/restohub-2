package com.restohub.clientapi.controller;

import com.restohub.clientapi.dto.*;
import com.restohub.clientapi.entity.Floor;
import com.restohub.clientapi.entity.Room;
import com.restohub.clientapi.entity.RestaurantTable;
import com.restohub.clientapi.repository.FloorRepository;
import com.restohub.clientapi.repository.RoomRepository;
import com.restohub.clientapi.repository.TableRepository;
import com.restohub.clientapi.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/client-api/r")
public class RestaurantController {
    
    private final RestaurantService restaurantService;
    private final MenuService menuService;
    private final TableMapService tableMapService;
    private final PromotionService promotionService;
    private final FloorRepository floorRepository;
    private final RoomRepository roomRepository;
    private final TableRepository tableRepository;
    
    @Autowired
    public RestaurantController(
            RestaurantService restaurantService,
            MenuService menuService,
            TableMapService tableMapService,
            PromotionService promotionService,
            FloorRepository floorRepository,
            RoomRepository roomRepository,
            TableRepository tableRepository) {
        this.restaurantService = restaurantService;
        this.menuService = menuService;
        this.tableMapService = tableMapService;
        this.promotionService = promotionService;
        this.floorRepository = floorRepository;
        this.roomRepository = roomRepository;
        this.tableRepository = tableRepository;
    }
    
    @GetMapping
    public ResponseEntity<List<RestaurantListResponse>> getRestaurants(
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset) {
        List<RestaurantListResponse> restaurants = restaurantService.getRestaurants(limit, offset);
        return ResponseEntity.ok(restaurants);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<RestaurantDetailResponse> getRestaurantById(@PathVariable Long id) {
        RestaurantDetailResponse restaurant = restaurantService.getRestaurantById(id);
        return ResponseEntity.ok(restaurant);
    }
    
    @GetMapping("/{id}/menu")
    public ResponseEntity<MenuResponse> getMenu(@PathVariable Long id) {
        MenuResponse menu = menuService.getMenuByRestaurantId(id);
        return ResponseEntity.ok(menu);
    }
    
    @GetMapping("/{id}/floor")
    public ResponseEntity<List<FloorResponse>> getFloors(@PathVariable Long id) {
        List<Floor> floors = floorRepository.findByRestaurantIdAndIsActiveTrue(id);
        List<FloorResponse> response = floors.stream()
                .map(floor -> FloorResponse.builder()
                        .id(floor.getId())
                        .floorNumber(floor.getFloorNumber())
                        .build())
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/room")
    public ResponseEntity<List<RoomResponse>> getRooms(
            @PathVariable Long id,
            @RequestParam(required = false) Long floorId) {
        List<Room> rooms = roomRepository.findByRestaurantIdAndFloorIdOptional(id, floorId);
        List<RoomResponse> response = rooms.stream()
                .map(room -> RoomResponse.builder()
                        .id(room.getId())
                        .name(room.getName())
                        .floorId(room.getFloor().getId())
                        .isSmoking(room.getIsSmoking())
                        .isOutdoor(room.getIsOutdoor())
                        .build())
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/room/{roomId}")
    public ResponseEntity<RoomDetailResponse> getRoom(
            @PathVariable Long id,
            @PathVariable Long roomId) {
        Room room = roomRepository.findByIdAndIsActiveTrue(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        RoomDetailResponse response = RoomDetailResponse.builder()
                .id(room.getId())
                .name(room.getName())
                .description(room.getDescription())
                .floorId(room.getFloor().getId())
                .isSmoking(room.getIsSmoking())
                .isOutdoor(room.getIsOutdoor())
                .imageId(room.getImage() != null ? room.getImage().getId() : null)
                .build();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/table")
    public ResponseEntity<List<TableResponse>> getTables(
            @PathVariable Long id,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Long floorId) {
        List<RestaurantTable> tables = tableRepository.findByRestaurantIdAndRoomIdAndFloorIdOptional(id, roomId, floorId);
        List<TableResponse> response = tables.stream()
                .map(table -> TableResponse.builder()
                        .id(table.getId())
                        .tableNumber(table.getTableNumber())
                        .roomId(table.getRoom().getId())
                        .capacity(table.getCapacity())
                        .description(table.getDescription())
                        .imageId(table.getImage() != null ? table.getImage().getId() : null)
                        .depositAmount(table.getDepositAmount())
                        .depositNote(table.getDepositNote())
                        .positionX1(table.getPositionX1())
                        .positionY1(table.getPositionY1())
                        .positionX2(table.getPositionX2())
                        .positionY2(table.getPositionY2())
                        .build())
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/table/map")
    public ResponseEntity<TableMapResponse> getTableMap(
            @PathVariable Long id,
            @RequestParam(required = false) Long floorId,
            @RequestParam(required = false) Long roomId) {
        TableMapResponse map = tableMapService.getTableMap(id, floorId, roomId);
        return ResponseEntity.ok(map);
    }
    
    @GetMapping("/{id}/promotion")
    public ResponseEntity<List<PromotionResponse>> getPromotions(
            @PathVariable Long id,
            @RequestParam(required = false) Long promotionTypeId,
            @RequestParam(required = false) Boolean isCurrent,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset) {
        List<PromotionResponse> promotions = promotionService.getPromotions(
                id, promotionTypeId, isCurrent, limit, offset);
        return ResponseEntity.ok(promotions);
    }
}

