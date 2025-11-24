package com.restohub.adminapi.controller;

import com.restohub.adminapi.dto.CreateRoomRequest;
import com.restohub.adminapi.dto.RoomListItemResponse;
import com.restohub.adminapi.dto.RoomResponse;
import com.restohub.adminapi.dto.PaginationResponse;
import com.restohub.adminapi.dto.UpdateRoomRequest;
import com.restohub.adminapi.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/r/{id}/room")
public class RoomController {
    
    private final RoomService roomService;
    
    @Autowired
    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }
    
    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(
            @PathVariable("id") Long restaurantId,
            @Valid @RequestBody CreateRoomRequest request) {
        RoomResponse response = roomService.createRoom(restaurantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    public ResponseEntity<PaginationResponse<List<RoomListItemResponse>>> getRooms(
            @PathVariable("id") Long restaurantId,
            @RequestParam(value = "limit", defaultValue = "100") Integer limit,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset,
            @RequestParam(value = "floorId", required = false) Long floorId,
            @RequestParam(value = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "asc") String sortOrder) {
        
        PaginationResponse<List<RoomListItemResponse>> response = roomService.getRooms(
                restaurantId, limit, offset, floorId, sortBy, sortOrder);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponse> getRoom(
            @PathVariable("id") Long restaurantId,
            @PathVariable("roomId") Long roomId) {
        RoomResponse response = roomService.getRoom(restaurantId, roomId);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{roomId}")
    public ResponseEntity<RoomResponse> updateRoom(
            @PathVariable("id") Long restaurantId,
            @PathVariable("roomId") Long roomId,
            @Valid @RequestBody UpdateRoomRequest request) {
        RoomResponse response = roomService.updateRoom(restaurantId, roomId, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(
            @PathVariable("id") Long restaurantId,
            @PathVariable("roomId") Long roomId) {
        roomService.deleteRoom(restaurantId, roomId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping(value = "/{roomId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RoomResponse> uploadRoomImage(
            @PathVariable("id") Long restaurantId,
            @PathVariable("roomId") Long roomId,
            @RequestParam("file") MultipartFile file) {
        try {
            RoomResponse response = roomService.uploadRoomImage(restaurantId, roomId, file);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            throw new RuntimeException("IMAGE_UPLOAD_ERROR");
        }
    }
    
    @DeleteMapping("/{roomId}/image")
    public ResponseEntity<RoomResponse> deleteRoomImage(
            @PathVariable("id") Long restaurantId,
            @PathVariable("roomId") Long roomId) {
        RoomResponse response = roomService.deleteRoomImage(restaurantId, roomId);
        return ResponseEntity.ok(response);
    }
}

