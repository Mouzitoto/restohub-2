package com.restohub.adminapi.controller;

import com.lowagie.text.DocumentException;
import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.entity.RestaurantSubscription;
import com.restohub.adminapi.entity.SubscriptionPayment;
import com.restohub.adminapi.repository.RestaurantSubscriptionRepository;
import com.restohub.adminapi.repository.SubscriptionPaymentRepository;
import com.restohub.adminapi.service.InvoicePdfService;
import com.restohub.adminapi.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("")
public class SubscriptionController {
    
    private final SubscriptionService subscriptionService;
    private final InvoicePdfService invoicePdfService;
    private final RestaurantSubscriptionRepository subscriptionRepository;
    private final SubscriptionPaymentRepository paymentRepository;
    
    @Autowired
    public SubscriptionController(
            SubscriptionService subscriptionService,
            InvoicePdfService invoicePdfService,
            RestaurantSubscriptionRepository subscriptionRepository,
            SubscriptionPaymentRepository paymentRepository) {
        this.subscriptionService = subscriptionService;
        this.invoicePdfService = invoicePdfService;
        this.subscriptionRepository = subscriptionRepository;
        this.paymentRepository = paymentRepository;
    }
    
    @GetMapping("/r/{id}/subscription")
    public ResponseEntity<SubscriptionResponse> getRestaurantSubscription(@PathVariable("id") Long restaurantId) {
        SubscriptionResponse response = subscriptionService.getRestaurantSubscription(restaurantId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/r/{id}/subscriptions")
    public ResponseEntity<List<SubscriptionListItemResponse>> getRestaurantSubscriptions(@PathVariable("id") Long restaurantId) {
        List<SubscriptionListItemResponse> response = subscriptionService.getRestaurantSubscriptions(restaurantId);
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
    
    @PostMapping("/r/{id}/subscription")
    public ResponseEntity<SubscriptionResponse> createSubscription(
            @PathVariable("id") Long restaurantId,
            @Valid @RequestBody CreateSubscriptionRequest request) {
        SubscriptionResponse response = subscriptionService.createSubscription(restaurantId, request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/subscriptions/activate")
    public ResponseEntity<SubscriptionResponse> activateSubscription(
            @Valid @RequestBody ActivateSubscriptionRequest request) {
        SubscriptionResponse response = subscriptionService.activateSubscription(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/r/{id}/subscriptions/{subscriptionId}/invoice")
    public ResponseEntity<byte[]> getInvoice(
            @PathVariable("id") Long restaurantId,
            @PathVariable("subscriptionId") Long subscriptionId) throws DocumentException {
        RestaurantSubscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("SUBSCRIPTION_NOT_FOUND"));
        
        if (!subscription.getRestaurant().getId().equals(restaurantId)) {
            throw new RuntimeException("SUBSCRIPTION_NOT_FOUND");
        }
        
        byte[] pdf = invoicePdfService.generateInvoice(subscription);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String filename = "invoice-" + (subscription.getPaymentReference() != null ? subscription.getPaymentReference() : subscriptionId) + ".pdf";
        headers.setContentDispositionFormData("attachment", filename);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }
    
    @GetMapping("/r/{id}/subscriptions/{subscriptionId}/paid-invoice")
    public ResponseEntity<byte[]> getPaidInvoice(
            @PathVariable("id") Long restaurantId,
            @PathVariable("subscriptionId") Long subscriptionId) throws DocumentException {
        RestaurantSubscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("SUBSCRIPTION_NOT_FOUND"));
        
        if (!subscription.getRestaurant().getId().equals(restaurantId)) {
            throw new RuntimeException("SUBSCRIPTION_NOT_FOUND");
        }
        
        if (subscription.getStatus() != com.restohub.adminapi.entity.SubscriptionStatus.ACTIVATED) {
            throw new RuntimeException("SUBSCRIPTION_NOT_ACTIVATED");
        }
        
        List<SubscriptionPayment> payments = paymentRepository.findBySubscriptionId(subscriptionId);
        SubscriptionPayment payment = payments.stream()
                .filter(p -> p.getStatus() == com.restohub.adminapi.entity.PaymentStatus.SUCCESS)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("PAYMENT_NOT_FOUND"));
        
        byte[] pdf = invoicePdfService.generatePaidInvoice(subscription, payment);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String filename = "paid-invoice-" + (subscription.getPaymentReference() != null ? subscription.getPaymentReference() : subscriptionId) + ".pdf";
        headers.setContentDispositionFormData("attachment", filename);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
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

