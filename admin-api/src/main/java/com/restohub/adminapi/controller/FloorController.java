package com.restohub.adminapi.controller;

import com.restohub.adminapi.dto.CreateFloorRequest;
import com.restohub.adminapi.dto.FloorListItemResponse;
import com.restohub.adminapi.dto.FloorResponse;
import com.restohub.adminapi.dto.PaginationResponse;
import com.restohub.adminapi.dto.UpdateFloorRequest;
import com.restohub.adminapi.service.FloorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin-api/r/{id}/floor")
public class FloorController {
    
    private final FloorService floorService;
    
    @Autowired
    public FloorController(FloorService floorService) {
        this.floorService = floorService;
    }
    
    @PostMapping
    public ResponseEntity<FloorResponse> createFloor(
            @PathVariable("id") Long restaurantId,
            @Valid @RequestBody CreateFloorRequest request) {
        FloorResponse response = floorService.createFloor(restaurantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    public ResponseEntity<PaginationResponse<List<FloorListItemResponse>>> getFloors(
            @PathVariable("id") Long restaurantId,
            @RequestParam(value = "limit", defaultValue = "100") Integer limit,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset,
            @RequestParam(value = "sortBy", defaultValue = "floorNumber") String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "asc") String sortOrder) {
        
        PaginationResponse<List<FloorListItemResponse>> response = floorService.getFloors(
                restaurantId, limit, offset, sortBy, sortOrder);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{floorId}")
    public ResponseEntity<FloorResponse> getFloor(
            @PathVariable("id") Long restaurantId,
            @PathVariable Long floorId) {
        FloorResponse response = floorService.getFloor(restaurantId, floorId);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{floorId}")
    public ResponseEntity<FloorResponse> updateFloor(
            @PathVariable("id") Long restaurantId,
            @PathVariable Long floorId,
            @Valid @RequestBody UpdateFloorRequest request) {
        FloorResponse response = floorService.updateFloor(restaurantId, floorId, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{floorId}")
    public ResponseEntity<Void> deleteFloor(
            @PathVariable("id") Long restaurantId,
            @PathVariable Long floorId) {
        floorService.deleteFloor(restaurantId, floorId);
        return ResponseEntity.noContent().build();
    }
}

