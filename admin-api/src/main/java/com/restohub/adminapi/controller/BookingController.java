package com.restohub.adminapi.controller;

import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/r/{id}/booking")
public class BookingController {
    
    private final BookingService bookingService;
    
    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }
    
    @GetMapping
    public ResponseEntity<PaginationResponse<List<BookingListItemResponse>>> getBookings(
            @PathVariable("id") Long restaurantId,
            @RequestParam(value = "limit", defaultValue = "50") Integer limit,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset,
            @RequestParam(value = "statusCode", required = false) String statusCode,
            @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(value = "tableId", required = false) Long tableId,
            @RequestParam(value = "clientPhone", required = false) String clientPhone,
            @RequestParam(value = "sortBy", defaultValue = "bookingDate") String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "desc") String sortOrder) {
        
        PaginationResponse<List<BookingListItemResponse>> response = bookingService.getBookings(
                restaurantId, limit, offset, statusCode, dateFrom, dateTo, tableId, clientPhone, sortBy, sortOrder);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getBooking(
            @PathVariable("id") Long restaurantId,
            @PathVariable Long bookingId) {
        
        BookingResponse response = bookingService.getBooking(restaurantId, bookingId);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable("id") Long restaurantId,
            @PathVariable Long bookingId) {
        
        BookingResponse response = bookingService.cancelBooking(restaurantId, bookingId);
        return ResponseEntity.ok(response);
    }
}

