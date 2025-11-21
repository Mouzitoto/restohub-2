package com.restohub.adminapi.controller;

import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.service.TableService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin-api/r/{id}/table")
public class TableController {
    
    private final TableService tableService;
    
    @Autowired
    public TableController(TableService tableService) {
        this.tableService = tableService;
    }
    
    @PostMapping
    public ResponseEntity<TableResponse> createTable(
            @PathVariable("id") Long restaurantId,
            @Valid @RequestBody CreateTableRequest request) {
        TableResponse response = tableService.createTable(restaurantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    public ResponseEntity<PaginationResponse<List<TableListItemResponse>>> getTables(
            @PathVariable("id") Long restaurantId,
            @RequestParam(value = "limit", defaultValue = "50") Integer limit,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset,
            @RequestParam(value = "roomId", required = false) Long roomId,
            @RequestParam(value = "floorId", required = false) Long floorId,
            @RequestParam(value = "minCapacity", required = false) Integer minCapacity,
            @RequestParam(value = "maxCapacity", required = false) Integer maxCapacity,
            @RequestParam(value = "sortBy", defaultValue = "tableNumber") String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "asc") String sortOrder) {
        
        PaginationResponse<List<TableListItemResponse>> response = tableService.getTables(
                restaurantId, limit, offset, roomId, floorId, minCapacity, maxCapacity, sortBy, sortOrder);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{tableId}")
    public ResponseEntity<TableResponse> getTable(
            @PathVariable("id") Long restaurantId,
            @PathVariable Long tableId) {
        TableResponse response = tableService.getTable(restaurantId, tableId);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{tableId}")
    public ResponseEntity<TableResponse> updateTable(
            @PathVariable("id") Long restaurantId,
            @PathVariable Long tableId,
            @Valid @RequestBody UpdateTableRequest request) {
        TableResponse response = tableService.updateTable(restaurantId, tableId, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{tableId}")
    public ResponseEntity<Void> deleteTable(
            @PathVariable("id") Long restaurantId,
            @PathVariable Long tableId) {
        tableService.deleteTable(restaurantId, tableId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/map")
    public ResponseEntity<TableMapResponse> getTableMap(
            @PathVariable("id") Long restaurantId,
            @RequestParam(value = "floorId", required = false) Long floorId,
            @RequestParam(value = "roomId", required = false) Long roomId) {
        TableMapResponse response = tableService.getTableMap(restaurantId, floorId, roomId);
        return ResponseEntity.ok(response);
    }
}

