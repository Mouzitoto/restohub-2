package com.restohub.adminapi.controller;

import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.service.PreOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/r/{id}/pre-order")
public class PreOrderController {
    
    private final PreOrderService preOrderService;
    
    @Autowired
    public PreOrderController(PreOrderService preOrderService) {
        this.preOrderService = preOrderService;
    }
    
    @GetMapping
    public ResponseEntity<PaginationResponse<List<PreOrderListItemResponse>>> getPreOrders(
            @PathVariable("id") Long restaurantId,
            @RequestParam(value = "limit", defaultValue = "50") Integer limit,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset,
            @RequestParam(value = "statusCode", required = false) String statusCode,
            @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(value = "bookingId", required = false) Long bookingId,
            @RequestParam(value = "clientPhone", required = false) String clientPhone,
            @RequestParam(value = "sortBy", defaultValue = "date") String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "desc") String sortOrder) {
        
        PaginationResponse<List<PreOrderListItemResponse>> response = preOrderService.getPreOrders(
                restaurantId, limit, offset, statusCode, dateFrom, dateTo, bookingId, clientPhone, sortBy, sortOrder);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{preOrderId}")
    public ResponseEntity<PreOrderResponse> getPreOrder(
            @PathVariable("id") Long restaurantId,
            @PathVariable Long preOrderId) {
        
        PreOrderResponse response = preOrderService.getPreOrder(restaurantId, preOrderId);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{preOrderId}/cancel")
    public ResponseEntity<PreOrderResponse> cancelPreOrder(
            @PathVariable("id") Long restaurantId,
            @PathVariable Long preOrderId) {
        
        PreOrderResponse response = preOrderService.cancelPreOrder(restaurantId, preOrderId);
        return ResponseEntity.ok(response);
    }
}

