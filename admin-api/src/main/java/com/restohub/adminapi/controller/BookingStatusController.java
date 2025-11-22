package com.restohub.adminapi.controller;

import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.service.BookingStatusService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/booking-status")
public class BookingStatusController {
    
    private final BookingStatusService bookingStatusService;
    
    @Autowired
    public BookingStatusController(BookingStatusService bookingStatusService) {
        this.bookingStatusService = bookingStatusService;
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingStatusResponse> createBookingStatus(@Valid @RequestBody CreateBookingStatusRequest request) {
        BookingStatusResponse response = bookingStatusService.createBookingStatus(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    public ResponseEntity<PaginationResponse<List<BookingStatusListItemResponse>>> getBookingStatuses(
            @RequestParam(value = "limit", defaultValue = "100") Integer limit,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset,
            @RequestParam(value = "sortBy", defaultValue = "displayOrder") String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "asc") String sortOrder) {
        
        PaginationResponse<List<BookingStatusListItemResponse>> response = bookingStatusService.getBookingStatuses(
                limit, offset, sortBy, sortOrder);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{statusId}")
    public ResponseEntity<BookingStatusResponse> getBookingStatus(@PathVariable Long statusId) {
        BookingStatusResponse response = bookingStatusService.getBookingStatus(statusId);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{statusId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingStatusResponse> updateBookingStatus(
            @PathVariable Long statusId,
            @Valid @RequestBody UpdateBookingStatusDetailsRequest request) {
        BookingStatusResponse response = bookingStatusService.updateBookingStatus(statusId, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{statusId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBookingStatus(@PathVariable Long statusId) {
        bookingStatusService.deleteBookingStatus(statusId);
        return ResponseEntity.noContent().build();
    }
}

