package com.restohub.adminapi.controller;

import com.restohub.adminapi.dto.CreateMenuItemRequest;
import com.restohub.adminapi.dto.MenuItemListItemResponse;
import com.restohub.adminapi.dto.MenuItemResponse;
import com.restohub.adminapi.dto.MessageResponse;
import com.restohub.adminapi.dto.PaginationResponse;
import com.restohub.adminapi.dto.ReorderMenuItemsRequest;
import com.restohub.adminapi.dto.UpdateMenuItemRequest;
import com.restohub.adminapi.service.MenuItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin-api/r/{id}/menu-item")
public class MenuItemController {
    
    private final MenuItemService menuItemService;
    
    @Autowired
    public MenuItemController(MenuItemService menuItemService) {
        this.menuItemService = menuItemService;
    }
    
    @PostMapping
    public ResponseEntity<MenuItemResponse> createMenuItem(
            @PathVariable("id") Long restaurantId,
            @Valid @RequestBody CreateMenuItemRequest request) {
        MenuItemResponse response = menuItemService.createMenuItem(restaurantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    public ResponseEntity<PaginationResponse<List<MenuItemListItemResponse>>> getMenuItems(
            @PathVariable("id") Long restaurantId,
            @RequestParam(value = "limit", defaultValue = "50") Integer limit,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset,
            @RequestParam(value = "menuCategoryId", required = false) Long menuCategoryId,
            @RequestParam(value = "isActive", required = false) Boolean isActive,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "sortBy", defaultValue = "displayOrder") String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "asc") String sortOrder) {
        
        PaginationResponse<List<MenuItemListItemResponse>> response = menuItemService.getMenuItems(
                restaurantId, limit, offset, menuCategoryId, isActive, search, sortBy, sortOrder);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{itemId}")
    public ResponseEntity<MenuItemResponse> getMenuItem(
            @PathVariable("id") Long restaurantId,
            @PathVariable Long itemId) {
        MenuItemResponse response = menuItemService.getMenuItem(restaurantId, itemId);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{itemId}")
    public ResponseEntity<MenuItemResponse> updateMenuItem(
            @PathVariable("id") Long restaurantId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateMenuItemRequest request) {
        MenuItemResponse response = menuItemService.updateMenuItem(restaurantId, itemId, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteMenuItem(
            @PathVariable("id") Long restaurantId,
            @PathVariable Long itemId) {
        menuItemService.deleteMenuItem(restaurantId, itemId);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/reorder")
    public ResponseEntity<MessageResponse> reorderMenuItems(
            @PathVariable("id") Long restaurantId,
            @Valid @RequestBody ReorderMenuItemsRequest request) {
        menuItemService.reorderMenuItems(restaurantId, request);
        return ResponseEntity.ok(new MessageResponse("Порядок блюд успешно обновлен"));
    }
}

