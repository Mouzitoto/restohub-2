package com.restohub.adminapi.controller;

import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.service.PromotionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin-api/r/{id}/promotion")
public class PromotionController {
    
    private final PromotionService promotionService;
    
    @Autowired
    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }
    
    @PostMapping
    public ResponseEntity<PromotionResponse> createPromotion(
            @PathVariable("id") Long restaurantId,
            @Valid @RequestBody CreatePromotionRequest request) {
        PromotionResponse response = promotionService.createPromotion(restaurantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    public ResponseEntity<PaginationResponse<List<PromotionListItemResponse>>> getPromotions(
            @PathVariable("id") Long restaurantId,
            @RequestParam(value = "limit", defaultValue = "50") Integer limit,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset,
            @RequestParam(value = "promotionTypeId", required = false) Long promotionTypeId,
            @RequestParam(value = "isActive", required = false) Boolean isActive,
            @RequestParam(value = "startDateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateFrom,
            @RequestParam(value = "startDateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateTo,
            @RequestParam(value = "endDateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDateFrom,
            @RequestParam(value = "endDateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDateTo,
            @RequestParam(value = "isCurrent", required = false) Boolean isCurrent,
            @RequestParam(value = "sortBy", defaultValue = "startDate") String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "desc") String sortOrder) {
        
        PaginationResponse<List<PromotionListItemResponse>> response = promotionService.getPromotions(
                restaurantId, limit, offset, promotionTypeId, isActive,
                startDateFrom, startDateTo, endDateFrom, endDateTo, isCurrent, sortBy, sortOrder);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{promotionId}")
    public ResponseEntity<PromotionResponse> getPromotion(
            @PathVariable("id") Long restaurantId,
            @PathVariable Long promotionId) {
        PromotionResponse response = promotionService.getPromotion(restaurantId, promotionId);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{promotionId}")
    public ResponseEntity<PromotionResponse> updatePromotion(
            @PathVariable("id") Long restaurantId,
            @PathVariable Long promotionId,
            @Valid @RequestBody UpdatePromotionRequest request) {
        PromotionResponse response = promotionService.updatePromotion(restaurantId, promotionId, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{promotionId}")
    public ResponseEntity<Void> deletePromotion(
            @PathVariable("id") Long restaurantId,
            @PathVariable Long promotionId) {
        promotionService.deletePromotion(restaurantId, promotionId);
        return ResponseEntity.noContent().build();
    }
}

