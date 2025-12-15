package com.restohub.adminapi.controller;

import com.restohub.adminapi.dto.BookingResponse;
import com.restohub.adminapi.dto.ChangeBookingStatusRequest;
import com.restohub.adminapi.service.BookingService;
import com.restohub.adminapi.service.WhatsAppNotificationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/booking")
@Slf4j
public class WhatsAppBookingController {
    
    private final BookingService bookingService;
    private final WhatsAppNotificationService whatsAppNotificationService;
    
    @Autowired
    public WhatsAppBookingController(
            BookingService bookingService,
            WhatsAppNotificationService whatsAppNotificationService) {
        this.bookingService = bookingService;
        this.whatsAppNotificationService = whatsAppNotificationService;
    }
    
    /**
     * Изменение статуса бронирования (публичный endpoint для WhatsApp бота)
     */
    @PostMapping("/{id}/status")
    public ResponseEntity<BookingResponse> changeBookingStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChangeBookingStatusRequest request) {
        
        log.info("Changing booking status: bookingId={}, status={}, managerId={}", 
                id, request.getStatus(), request.getManagerId());
        
        BookingResponse response = bookingService.changeStatus(id, request.getStatus(), request.getManagerId());
        
        // Отправляем уведомления
        if ("APPROVED".equals(request.getStatus())) {
            whatsAppNotificationService.sendBookingStatusUpdateToManager(id, true);
            whatsAppNotificationService.sendBookingConfirmationToClient(id);
        } else if ("REJECTED".equals(request.getStatus())) {
            whatsAppNotificationService.sendBookingStatusUpdateToManager(id, false);
            whatsAppNotificationService.sendBookingRejectionToClient(id, null);
        }
        
        return ResponseEntity.ok(response);
    }
}

