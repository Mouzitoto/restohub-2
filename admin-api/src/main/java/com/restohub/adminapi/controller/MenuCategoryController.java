package com.restohub.adminapi.controller;

import com.restohub.adminapi.dto.CreateMenuCategoryRequest;
import com.restohub.adminapi.dto.MenuCategoryListItemResponse;
import com.restohub.adminapi.dto.MenuCategoryResponse;
import com.restohub.adminapi.dto.MessageResponse;
import com.restohub.adminapi.dto.PaginationResponse;
import com.restohub.adminapi.dto.ReorderMenuCategoriesRequest;
import com.restohub.adminapi.dto.UpdateMenuCategoryRequest;
import com.restohub.adminapi.service.MenuCategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin-api/menu-category")
public class MenuCategoryController {
    
    private final MenuCategoryService menuCategoryService;
    
    @Autowired
    public MenuCategoryController(MenuCategoryService menuCategoryService) {
        this.menuCategoryService = menuCategoryService;
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuCategoryResponse> createMenuCategory(@Valid @RequestBody CreateMenuCategoryRequest request) {
        MenuCategoryResponse response = menuCategoryService.createMenuCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    public ResponseEntity<PaginationResponse<List<MenuCategoryListItemResponse>>> getMenuCategories(
            @RequestParam(value = "limit", defaultValue = "100") Integer limit,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset,
            @RequestParam(value = "sortBy", defaultValue = "displayOrder") String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "asc") String sortOrder) {
        
        PaginationResponse<List<MenuCategoryListItemResponse>> response = menuCategoryService.getMenuCategories(
                limit, offset, sortBy, sortOrder);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{categoryId}")
    public ResponseEntity<MenuCategoryResponse> getMenuCategory(@PathVariable Long categoryId) {
        MenuCategoryResponse response = menuCategoryService.getMenuCategory(categoryId);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuCategoryResponse> updateMenuCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody UpdateMenuCategoryRequest request) {
        MenuCategoryResponse response = menuCategoryService.updateMenuCategory(categoryId, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMenuCategory(@PathVariable Long categoryId) {
        menuCategoryService.deleteMenuCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/reorder")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> reorderMenuCategories(@Valid @RequestBody ReorderMenuCategoriesRequest request) {
        menuCategoryService.reorderMenuCategories(request);
        return ResponseEntity.ok(new MessageResponse("Порядок категорий успешно обновлен"));
    }
}

