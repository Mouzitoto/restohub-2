package com.restohub.adminapi.controller;

import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/r/{id}/client")
public class ClientController {
    
    private final ClientService clientService;
    
    @Autowired
    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }
    
    @GetMapping
    public ResponseEntity<PaginationResponse<List<ClientListItemResponse>>> getClients(
            @PathVariable("id") Long restaurantId,
            @RequestParam(value = "limit", defaultValue = "50") Integer limit,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "sortBy", defaultValue = "lastBookingDate") String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "desc") String sortOrder) {
        
        PaginationResponse<List<ClientListItemResponse>> response = clientService.getClients(
                restaurantId, limit, offset, search, sortBy, sortOrder);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{clientId}")
    public ResponseEntity<ClientResponse> getClient(
            @PathVariable("id") Long restaurantId,
            @PathVariable Long clientId) {
        ClientResponse response = clientService.getClient(restaurantId, clientId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{clientId}/booking")
    public ResponseEntity<PaginationResponse<List<BookingListItemResponse>>> getClientBookings(
            @PathVariable("id") Long restaurantId,
            @PathVariable Long clientId,
            @RequestParam(value = "limit", defaultValue = "50") Integer limit,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset) {
        
        PaginationResponse<List<BookingListItemResponse>> response = clientService.getClientBookings(
                restaurantId, clientId, limit, offset);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{clientId}/pre-order")
    public ResponseEntity<PaginationResponse<List<PreOrderListItemResponse>>> getClientPreOrders(
            @PathVariable("id") Long restaurantId,
            @PathVariable Long clientId,
            @RequestParam(value = "limit", defaultValue = "50") Integer limit,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset) {
        
        PaginationResponse<List<PreOrderListItemResponse>> response = clientService.getClientPreOrders(
                restaurantId, clientId, limit, offset);
        return ResponseEntity.ok(response);
    }
}

