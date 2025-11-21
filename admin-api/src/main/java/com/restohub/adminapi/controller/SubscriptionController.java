package com.restohub.adminapi.controller;

import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin-api")
public class SubscriptionController {
    
    private final SubscriptionService subscriptionService;
    
    @Autowired
    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }
    
    @GetMapping("/r/{id}/subscription")
    public ResponseEntity<SubscriptionResponse> getRestaurantSubscription(@PathVariable("id") Long restaurantId) {
        SubscriptionResponse response = subscriptionService.getRestaurantSubscription(restaurantId);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/r/{id}/subscription")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubscriptionResponse> updateRestaurantSubscription(
            @PathVariable("id") Long restaurantId,
            @Valid @RequestBody UpdateSubscriptionRequest request) {
        SubscriptionResponse response = subscriptionService.updateRestaurantSubscription(restaurantId, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/subscription")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginationResponse<List<SubscriptionListItemResponse>>> getAllSubscriptions(
            @RequestParam(value = "limit", defaultValue = "50") Integer limit,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset,
            @RequestParam(value = "isActive", required = false) Boolean isActive,
            @RequestParam(value = "restaurantId", required = false) Long restaurantId,
            @RequestParam(value = "subscriptionTypeId", required = false) Long subscriptionTypeId,
            @RequestParam(value = "expiringSoon", required = false) Boolean expiringSoon,
            @RequestParam(value = "sortBy", defaultValue = "endDate") String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "asc") String sortOrder) {
        
        PaginationResponse<List<SubscriptionListItemResponse>> response = subscriptionService.getAllSubscriptions(
                limit, offset, isActive, restaurantId, subscriptionTypeId, expiringSoon, sortBy, sortOrder);
        return ResponseEntity.ok(response);
    }
}

