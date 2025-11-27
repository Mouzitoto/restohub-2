package com.restohub.clientapi.controller;

import com.restohub.clientapi.dto.CreatePreOrderRequest;
import com.restohub.clientapi.dto.CreatePreOrderResponse;
import com.restohub.clientapi.service.PreOrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin-api/pre-order")
public class PreOrderController {
    
    private final PreOrderService preOrderService;
    
    @Autowired
    public PreOrderController(PreOrderService preOrderService) {
        this.preOrderService = preOrderService;
    }
    
    @PostMapping
    public ResponseEntity<CreatePreOrderResponse> createPreOrder(
            @Valid @RequestBody CreatePreOrderRequest request) {
        
        CreatePreOrderResponse response = preOrderService.createPreOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

