package com.restohub.clientapi.service;

import com.restohub.clientapi.dto.MenuCategoryResponse;
import com.restohub.clientapi.dto.MenuItemResponse;
import com.restohub.clientapi.dto.MenuResponse;
import com.restohub.clientapi.entity.MenuCategory;
import com.restohub.clientapi.entity.MenuItem;
import com.restohub.clientapi.repository.MenuCategoryRepository;
import com.restohub.clientapi.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MenuService {
    
    private final MenuItemRepository menuItemRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    
    @Autowired
    public MenuService(
            MenuItemRepository menuItemRepository,
            MenuCategoryRepository menuCategoryRepository) {
        this.menuItemRepository = menuItemRepository;
        this.menuCategoryRepository = menuCategoryRepository;
    }
    
    public MenuResponse getMenuByRestaurantId(Long restaurantId) {
        List<MenuItem> items = menuItemRepository.findByRestaurantIdAndIsActiveTrueAndIsAvailableTrue(restaurantId);
        
        // Группируем по категориям
        Map<MenuCategory, List<MenuItem>> itemsByCategory = items.stream()
                .collect(Collectors.groupingBy(MenuItem::getMenuCategory));
        
        // Получаем все категории и сортируем
        List<MenuCategoryResponse> categories = menuCategoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
                .filter(category -> itemsByCategory.containsKey(category))
                .sorted(Comparator.comparing(MenuCategory::getDisplayOrder))
                .map(category -> {
                    List<MenuItemResponse> categoryItems = itemsByCategory.get(category).stream()
                            .sorted(Comparator.comparing(MenuItem::getDisplayOrder))
                            .map(this::toMenuItemResponse)
                            .collect(Collectors.toList());
                    
                    return MenuCategoryResponse.builder()
                            .id(category.getId())
                            .name(category.getName())
                            .description(category.getDescription())
                            .displayOrder(category.getDisplayOrder())
                            .items(categoryItems)
                            .build();
                })
                .collect(Collectors.toList());
        
        return MenuResponse.builder()
                .categories(categories)
                .build();
    }
    
    private MenuItemResponse toMenuItemResponse(MenuItem item) {
        return MenuItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .ingredients(item.getIngredients())
                .price(item.getPrice())
                .discountPercent(item.getDiscountPercent())
                .spicinessLevel(item.getSpicinessLevel())
                .hasSugar(item.getHasSugar())
                .imageId(item.getImage() != null ? item.getImage().getId() : null)
                .displayOrder(item.getDisplayOrder())
                .build();
    }
}

