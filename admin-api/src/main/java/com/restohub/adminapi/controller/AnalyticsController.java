package com.restohub.adminapi.controller;

import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/r/{id}/analytics")
public class AnalyticsController {
    
    private final AnalyticsService analyticsService;
    
    @Autowired
    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }
    
    @GetMapping("/booking")
    public ResponseEntity<BookingAnalyticsResponse> getBookingAnalytics(
            @PathVariable("id") Long restaurantId,
            @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(value = "groupBy", defaultValue = "day") String groupBy) {
        
        BookingAnalyticsResponse response = analyticsService.getBookingAnalytics(restaurantId, dateFrom, dateTo, groupBy);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/pre-order")
    public ResponseEntity<PreOrderAnalyticsResponse> getPreOrderAnalytics(
            @PathVariable("id") Long restaurantId,
            @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(value = "groupBy", defaultValue = "day") String groupBy) {
        
        PreOrderAnalyticsResponse response = analyticsService.getPreOrderAnalytics(restaurantId, dateFrom, dateTo, groupBy);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/client")
    public ResponseEntity<ClientAnalyticsResponse> getClientAnalytics(
            @PathVariable("id") Long restaurantId,
            @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        
        ClientAnalyticsResponse response = analyticsService.getClientAnalytics(restaurantId, dateFrom, dateTo);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/overview")
    public ResponseEntity<AnalyticsOverviewResponse> getOverview(
            @PathVariable("id") Long restaurantId,
            @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        
        AnalyticsOverviewResponse response = analyticsService.getOverview(restaurantId, dateFrom, dateTo);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/export")
    public ResponseEntity<String> exportData(
            @PathVariable("id") Long restaurantId,
            @RequestParam(value = "type", required = true) String type,
            @RequestParam(value = "format", defaultValue = "json") String format,
            @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        
        // Упрощенная реализация экспорта - возвращаем JSON
        // Полная реализация CSV/XLSX требует дополнительных библиотек
        String data = analyticsService.exportData(restaurantId, type, format, dateFrom, dateTo);
        
        HttpHeaders headers = new HttpHeaders();
        switch (format.toLowerCase()) {
            case "csv":
                headers.setContentType(MediaType.parseMediaType("text/csv"));
                headers.setContentDispositionFormData("attachment", "export.csv");
                break;
            case "xlsx":
                headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
                headers.setContentDispositionFormData("attachment", "export.xlsx");
                break;
            default:
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setContentDispositionFormData("attachment", "export.json");
        }
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }
}

