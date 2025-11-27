package com.restohub.clientapi.controller;

import com.restohub.clientapi.dto.SearchResponse;
import com.restohub.clientapi.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/client-api/r/search")
public class SearchController {
    
    private final SearchService searchService;
    
    @Autowired
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }
    
    @GetMapping
    public ResponseEntity<SearchResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String menu_item,
            @RequestParam(required = false) String promotion,
            @RequestParam(required = false) Long promotion_type,
            @RequestParam(required = false) String cuisineType,
            @RequestParam(required = false) Boolean isOutdoor,
            @RequestParam(required = false) Boolean isSmoking,
            @RequestParam(required = false) BigDecimal lat,
            @RequestParam(required = false) BigDecimal lng,
            @RequestParam(required = false) BigDecimal radius,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset) {
        
        SearchResponse response = searchService.search(
                q, menu_item, promotion, promotion_type, cuisineType,
                isOutdoor, isSmoking, lat, lng, radius, limit, offset);
        
        return ResponseEntity.ok(response);
    }
}

