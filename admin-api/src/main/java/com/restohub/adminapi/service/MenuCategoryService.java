package com.restohub.adminapi.service;

import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.entity.MenuCategory;
import com.restohub.adminapi.repository.MenuCategoryRepository;
import com.restohub.adminapi.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuCategoryService {
    
    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuItemRepository menuItemRepository;
    
    @Autowired
    public MenuCategoryService(MenuCategoryRepository menuCategoryRepository, MenuItemRepository menuItemRepository) {
        this.menuCategoryRepository = menuCategoryRepository;
        this.menuItemRepository = menuItemRepository;
    }
    
    @Transactional
    public MenuCategoryResponse createMenuCategory(CreateMenuCategoryRequest request) {
        // Проверка уникальности названия
        if (menuCategoryRepository.findByNameAndIsActiveTrue(request.getName().trim()).isPresent()) {
            throw new RuntimeException("CATEGORY_NAME_EXISTS");
        }
        
        // Определяем displayOrder (максимальное значение + 1)
        Integer maxDisplayOrder = menuCategoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
                .mapToInt(MenuCategory::getDisplayOrder)
                .max()
                .orElse(-1);
        
        MenuCategory category = new MenuCategory();
        category.setName(request.getName().trim());
        category.setDescription(request.getDescription());
        category.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : (maxDisplayOrder + 1));
        category.setIsActive(true);
        
        category = menuCategoryRepository.save(category);
        
        return toResponse(category);
    }
    
    public PaginationResponse<List<MenuCategoryListItemResponse>> getMenuCategories(
            Integer limit, Integer offset, String sortBy, String sortOrder) {
        
        // Сортировка
        Sort sort = buildSort(sortBy, sortOrder);
        Pageable pageable = PageRequest.of(offset / limit, limit, sort);
        
        Page<MenuCategory> page = menuCategoryRepository.findAll(
                (root, query, cb) -> cb.equal(root.get("isActive"), true),
                pageable
        );
        
        List<MenuCategoryListItemResponse> items = page.getContent().stream()
                .map(this::toListItemResponse)
                .collect(Collectors.toList());
        
        PaginationResponse.PaginationInfo pagination = new PaginationResponse.PaginationInfo(
                page.getTotalElements(),
                limit,
                offset,
                (offset + limit) < page.getTotalElements()
        );
        
        return new PaginationResponse<>(items, pagination);
    }
    
    public MenuCategoryResponse getMenuCategory(Long categoryId) {
        MenuCategory category = menuCategoryRepository.findByIdAndIsActiveTrue(categoryId)
                .orElseThrow(() -> new RuntimeException("CATEGORY_NOT_FOUND"));
        
        return toResponse(category);
    }
    
    @Transactional
    public MenuCategoryResponse updateMenuCategory(Long categoryId, UpdateMenuCategoryRequest request) {
        MenuCategory category = menuCategoryRepository.findByIdAndIsActiveTrue(categoryId)
                .orElseThrow(() -> new RuntimeException("CATEGORY_NOT_FOUND"));
        
        // Обновление полей (PATCH-логика)
        if (request.getName() != null) {
            String trimmedName = request.getName().trim();
            // Проверка уникальности (исключая текущую категорию)
            menuCategoryRepository.findByNameAndIsActiveTrue(trimmedName)
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(categoryId)) {
                            throw new RuntimeException("CATEGORY_NAME_EXISTS");
                        }
                    });
            category.setName(trimmedName);
        }
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }
        
        category = menuCategoryRepository.save(category);
        
        return toResponse(category);
    }
    
    @Transactional
    public void deleteMenuCategory(Long categoryId) {
        MenuCategory category = menuCategoryRepository.findByIdAndIsActiveTrue(categoryId)
                .orElseThrow(() -> new RuntimeException("CATEGORY_NOT_FOUND"));
        
        // Проверка использования категории
        long activeMenuItemsCount = menuItemRepository.findByMenuCategoryIdAndIsActiveTrue(categoryId).size();
        
        if (activeMenuItemsCount > 0) {
            throw new RuntimeException("CATEGORY_IN_USE");
        }
        
        // Мягкое удаление
        category.setIsActive(false);
        category.setDeletedAt(LocalDateTime.now());
        menuCategoryRepository.save(category);
    }
    
    @Transactional
    public void reorderMenuCategories(ReorderMenuCategoriesRequest request) {
        List<Long> categoryIds = request.getCategoryIds();
        
        // Проверка существования всех категорий
        List<MenuCategory> categories = menuCategoryRepository.findAllById(categoryIds);
        if (categories.size() != categoryIds.size()) {
            throw new RuntimeException("CATEGORY_NOT_FOUND");
        }
        
        // Проверка, что все категории активны
        boolean allActive = categories.stream().allMatch(MenuCategory::getIsActive);
        if (!allActive) {
            throw new RuntimeException("CATEGORY_NOT_FOUND");
        }
        
        // Обновление displayOrder согласно позиции в массиве
        for (int i = 0; i < categoryIds.size(); i++) {
            Long categoryId = categoryIds.get(i);
            MenuCategory category = categories.stream()
                    .filter(c -> c.getId().equals(categoryId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("CATEGORY_NOT_FOUND"));
            category.setDisplayOrder(i);
        }
        
        menuCategoryRepository.saveAll(categories);
    }
    
    private MenuCategoryResponse toResponse(MenuCategory category) {
        MenuCategoryResponse response = new MenuCategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setDisplayOrder(category.getDisplayOrder());
        response.setIsActive(category.getIsActive());
        response.setCreatedAt(category.getCreatedAt() != null ? category.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        response.setUpdatedAt(category.getUpdatedAt() != null ? category.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        response.setDeletedAt(category.getDeletedAt() != null ? category.getDeletedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        return response;
    }
    
    private MenuCategoryListItemResponse toListItemResponse(MenuCategory category) {
        MenuCategoryListItemResponse response = new MenuCategoryListItemResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setDisplayOrder(category.getDisplayOrder());
        response.setIsActive(category.getIsActive());
        response.setCreatedAt(category.getCreatedAt() != null ? category.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        return response;
    }
    
    private Sort buildSort(String sortBy, String sortOrder) {
        String field = sortBy != null ? sortBy : "displayOrder";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        // Маппинг полей
        switch (field) {
            case "name":
                return Sort.by(direction, "name");
            case "displayOrder":
                return Sort.by(direction, "displayOrder");
            case "createdAt":
                return Sort.by(direction, "createdAt");
            default:
                return Sort.by(Sort.Direction.ASC, "displayOrder");
        }
    }
}

