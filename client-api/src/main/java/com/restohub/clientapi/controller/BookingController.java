package com.restohub.clientapi.controller;

import com.restohub.clientapi.dto.*;
import com.restohub.clientapi.service.BookingService;
import com.restohub.clientapi.service.BookingConfirmationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class BookingController {
    
    private final BookingService bookingService;
    private final BookingConfirmationService bookingConfirmationService;
    
    @Autowired
    public BookingController(
            BookingService bookingService,
            BookingConfirmationService bookingConfirmationService) {
        this.bookingService = bookingService;
        this.bookingConfirmationService = bookingConfirmationService;
    }
    
    @PostMapping("/client-api/r/{id}/booking")
    public ResponseEntity<CreateBookingResponse> createBooking(
            @PathVariable Long id,
            @Valid @RequestBody CreateBookingRequest request) {
        
        // Устанавливаем restaurantId для валидации стола
        // Валидация будет выполнена в сервисе
        
        CreateBookingResponse response = bookingService.createBooking(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/admin-api/booking/{id}/confirm")
    public ResponseEntity<ConfirmBookingResponse> confirmBooking(
            @PathVariable Long id,
            @Valid @RequestBody ConfirmBookingRequest request) {
        
        ConfirmBookingResponse response = bookingConfirmationService.confirmBooking(
                id, request.getPhone(), request.getClientFirstName(), request.getWhatsappMessageId());
        return ResponseEntity.ok(response);
    }
}

