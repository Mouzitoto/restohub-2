package com.restohub.adminapi.service;

import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.entity.Image;
import com.restohub.adminapi.entity.MenuCategory;
import com.restohub.adminapi.entity.MenuItem;
import com.restohub.adminapi.entity.Restaurant;
import com.restohub.adminapi.repository.ImageRepository;
import com.restohub.adminapi.repository.MenuCategoryRepository;
import com.restohub.adminapi.repository.MenuItemRepository;
import com.restohub.adminapi.repository.RestaurantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuItemService {
    
    private static final Logger logger = LoggerFactory.getLogger(MenuItemService.class);
    
    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final ImageRepository imageRepository;
    private final ImageService imageService;
    
    @Autowired
    public MenuItemService(
            MenuItemRepository menuItemRepository,
            RestaurantRepository restaurantRepository,
            MenuCategoryRepository menuCategoryRepository,
            ImageRepository imageRepository,
            ImageService imageService) {
        this.menuItemRepository = menuItemRepository;
        this.restaurantRepository = restaurantRepository;
        this.menuCategoryRepository = menuCategoryRepository;
        this.imageRepository = imageRepository;
        this.imageService = imageService;
    }
    
    @Transactional
    public MenuItemResponse createMenuItem(Long restaurantId, CreateMenuItemRequest request) {
        // Проверка существования ресторана
        Restaurant restaurant = restaurantRepository.findByIdAndIsActiveTrue(restaurantId)
                .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
        
        // Проверка существования категории
        MenuCategory category = menuCategoryRepository.findByIdAndIsActiveTrue(request.getMenuCategoryId())
                .orElseThrow(() -> new RuntimeException("CATEGORY_NOT_FOUND"));
        
        // Проверка изображения
        Image image = null;
        if (request.getImageId() != null) {
            image = imageRepository.findByIdAndIsActiveTrue(request.getImageId())
                    .orElseThrow(() -> new RuntimeException("IMAGE_NOT_FOUND"));
        }
        
        MenuItem menuItem = new MenuItem();
        menuItem.setRestaurant(restaurant);
        menuItem.setMenuCategory(category);
        menuItem.setName(request.getName().trim());
        menuItem.setDescription(request.getDescription());
        menuItem.setIngredients(request.getIngredients());
        menuItem.setPrice(request.getPrice());
        menuItem.setDiscountPercent(request.getDiscountPercent() != null ? request.getDiscountPercent() : 0);
        menuItem.setSpicinessLevel(request.getSpicinessLevel() != null ? request.getSpicinessLevel() : 0);
        menuItem.setHasSugar(request.getHasSugar() != null ? request.getHasSugar() : false);
        menuItem.setImage(image);
        menuItem.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        menuItem.setIsActive(true);
        
        menuItem = menuItemRepository.save(menuItem);
        
        return toResponse(menuItem);
    }
    
    public PaginationResponse<List<MenuItemListItemResponse>> getMenuItems(
            Long restaurantId,
            Integer limit, Integer offset, Long menuCategoryId, Boolean isActive,
            String search, String sortBy, String sortOrder) {
        
        // Проверка существования ресторана
        restaurantRepository.findByIdAndIsActiveTrue(restaurantId)
                .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
        
        // Построение спецификации
        Specification<MenuItem> spec = Specification.where(
                (root, query, cb) -> cb.equal(root.get("restaurant").get("id"), restaurantId)
        );
        
        // Фильтр по активности
        if (isActive != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isActive"), isActive));
        } else {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isActive"), true));
        }
        
        // Фильтр по категории
        if (menuCategoryId != null) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("menuCategory").get("id"), menuCategoryId)
            );
        }
        
        // Поиск по названию
        if (search != null && !search.trim().isEmpty()) {
            String searchPattern = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> 
                cb.like(cb.lower(root.get("name")), searchPattern)
            );
        }
        
        // Сортировка
        Sort sort = buildSort(sortBy, sortOrder);
        Pageable pageable = PageRequest.of(offset / limit, limit, sort);
        
        Page<MenuItem> page = menuItemRepository.findAll(spec, pageable);
        
        List<MenuItemListItemResponse> items = page.getContent().stream()
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
    
    public MenuItemResponse getMenuItem(Long restaurantId, Long itemId) {
        MenuItem menuItem = menuItemRepository.findByIdAndIsActiveTrue(itemId)
                .orElseThrow(() -> new RuntimeException("MENU_ITEM_NOT_FOUND"));
        
        // Проверка принадлежности к ресторану
        if (!menuItem.getRestaurant().getId().equals(restaurantId)) {
            throw new RuntimeException("MENU_ITEM_NOT_FOUND");
        }
        
        return toResponse(menuItem);
    }
    
    @Transactional
    public MenuItemResponse updateMenuItem(Long restaurantId, Long itemId, UpdateMenuItemRequest request) {
        MenuItem menuItem = menuItemRepository.findByIdAndIsActiveTrue(itemId)
                .orElseThrow(() -> new RuntimeException("MENU_ITEM_NOT_FOUND"));
        
        // Проверка принадлежности к ресторану
        if (!menuItem.getRestaurant().getId().equals(restaurantId)) {
            throw new RuntimeException("MENU_ITEM_NOT_FOUND");
        }
        
        // Обновление полей (PATCH-логика)
        if (request.getName() != null) {
            menuItem.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            menuItem.setDescription(request.getDescription());
        }
        if (request.getIngredients() != null) {
            menuItem.setIngredients(request.getIngredients());
        }
        if (request.getPrice() != null) {
            menuItem.setPrice(request.getPrice());
        }
        if (request.getMenuCategoryId() != null) {
            MenuCategory category = menuCategoryRepository.findByIdAndIsActiveTrue(request.getMenuCategoryId())
                    .orElseThrow(() -> new RuntimeException("CATEGORY_NOT_FOUND"));
            menuItem.setMenuCategory(category);
        }
        if (request.getDiscountPercent() != null) {
            menuItem.setDiscountPercent(request.getDiscountPercent());
        }
        if (request.getSpicinessLevel() != null) {
            menuItem.setSpicinessLevel(request.getSpicinessLevel());
        }
        if (request.getHasSugar() != null) {
            menuItem.setHasSugar(request.getHasSugar());
        }
        if (request.getImageId() != null) {
            if (request.getImageId() == 0) {
                menuItem.setImage(null);
            } else {
                Image image = imageRepository.findByIdAndIsActiveTrue(request.getImageId())
                        .orElseThrow(() -> new RuntimeException("IMAGE_NOT_FOUND"));
                menuItem.setImage(image);
            }
        }
        if (request.getDisplayOrder() != null) {
            menuItem.setDisplayOrder(request.getDisplayOrder());
        }
        
        menuItem = menuItemRepository.save(menuItem);
        
        return toResponse(menuItem);
    }
    
    @Transactional
    public void deleteMenuItem(Long restaurantId, Long itemId) {
        MenuItem menuItem = menuItemRepository.findByIdAndIsActiveTrue(itemId)
                .orElseThrow(() -> new RuntimeException("MENU_ITEM_NOT_FOUND"));
        
        // Проверка принадлежности к ресторану
        if (!menuItem.getRestaurant().getId().equals(restaurantId)) {
            throw new RuntimeException("MENU_ITEM_NOT_FOUND");
        }
        
        // TODO: Проверка использования в активных предзаказах (когда будут реализованы)
        
        // Мягкое удаление
        menuItem.setIsActive(false);
        menuItem.setDeletedAt(LocalDateTime.now());
        menuItemRepository.save(menuItem);
    }
    
    @Transactional
    public void reorderMenuItems(Long restaurantId, ReorderMenuItemsRequest request) {
        // Проверка существования ресторана
        restaurantRepository.findByIdAndIsActiveTrue(restaurantId)
                .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
        
        List<Long> itemIds = request.getItems().stream()
                .map(ReorderMenuItemsRequest.MenuItemOrder::getId)
                .collect(Collectors.toList());
        
        // Проверка существования всех блюд
        List<MenuItem> menuItems = menuItemRepository.findAllById(itemIds);
        if (menuItems.size() != itemIds.size()) {
            throw new RuntimeException("MENU_ITEM_NOT_FOUND");
        }
        
        // Проверка принадлежности к ресторану и активности
        boolean allValid = menuItems.stream()
                .allMatch(item -> item.getRestaurant().getId().equals(restaurantId) && item.getIsActive());
        if (!allValid) {
            throw new RuntimeException("MENU_ITEM_NOT_FOUND");
        }
        
        // Обновление displayOrder
        for (ReorderMenuItemsRequest.MenuItemOrder order : request.getItems()) {
            MenuItem menuItem = menuItems.stream()
                    .filter(item -> item.getId().equals(order.getId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("MENU_ITEM_NOT_FOUND"));
            menuItem.setDisplayOrder(order.getDisplayOrder());
        }
        
        menuItemRepository.saveAll(menuItems);
    }
    
    private MenuItemResponse toResponse(MenuItem menuItem) {
        MenuItemResponse response = new MenuItemResponse();
        response.setId(menuItem.getId());
        response.setRestaurantId(menuItem.getRestaurant().getId());
        response.setMenuCategoryId(menuItem.getMenuCategory().getId());
        response.setName(menuItem.getName());
        response.setDescription(menuItem.getDescription());
        response.setIngredients(menuItem.getIngredients());
        response.setPrice(menuItem.getPrice());
        response.setDiscountPercent(menuItem.getDiscountPercent());
        response.setFinalPrice(calculateFinalPrice(menuItem.getPrice(), menuItem.getDiscountPercent()));
        response.setSpicinessLevel(menuItem.getSpicinessLevel());
        response.setHasSugar(menuItem.getHasSugar());
        response.setImageId(menuItem.getImage() != null ? menuItem.getImage().getId() : null);
        response.setDisplayOrder(menuItem.getDisplayOrder());
        response.setIsActive(menuItem.getIsActive());
        response.setCreatedAt(menuItem.getCreatedAt() != null ? menuItem.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        response.setUpdatedAt(menuItem.getUpdatedAt() != null ? menuItem.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        response.setDeletedAt(menuItem.getDeletedAt() != null ? menuItem.getDeletedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        return response;
    }
    
    private MenuItemListItemResponse toListItemResponse(MenuItem menuItem) {
        MenuItemListItemResponse response = new MenuItemListItemResponse();
        response.setId(menuItem.getId());
        response.setRestaurantId(menuItem.getRestaurant().getId());
        response.setMenuCategoryId(menuItem.getMenuCategory().getId());
        response.setName(menuItem.getName());
        response.setDescription(menuItem.getDescription());
        response.setIngredients(menuItem.getIngredients());
        response.setPrice(menuItem.getPrice());
        response.setDiscountPercent(menuItem.getDiscountPercent());
        response.setFinalPrice(calculateFinalPrice(menuItem.getPrice(), menuItem.getDiscountPercent()));
        response.setSpicinessLevel(menuItem.getSpicinessLevel());
        response.setHasSugar(menuItem.getHasSugar());
        response.setImageId(menuItem.getImage() != null ? menuItem.getImage().getId() : null);
        response.setDisplayOrder(menuItem.getDisplayOrder());
        response.setIsActive(menuItem.getIsActive());
        response.setCreatedAt(menuItem.getCreatedAt() != null ? menuItem.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        return response;
    }
    
    private BigDecimal calculateFinalPrice(BigDecimal price, Integer discountPercent) {
        if (price == null) {
            return null;
        }
        
        int discount = discountPercent != null ? discountPercent : 0;
        BigDecimal multiplier = BigDecimal.ONE.subtract(BigDecimal.valueOf(discount).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        return price.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }
    
    @Transactional
    public MenuItemResponse uploadMenuItemImage(Long restaurantId, Long itemId, MultipartFile file) throws IOException {
        MenuItem menuItem = menuItemRepository.findByIdAndIsActiveTrue(itemId)
                .orElseThrow(() -> new RuntimeException("MENU_ITEM_NOT_FOUND"));
        
        // Проверка принадлежности к ресторану
        if (!menuItem.getRestaurant().getId().equals(restaurantId)) {
            throw new RuntimeException("MENU_ITEM_NOT_FOUND");
        }
        
        // Загружаем изображение
        ImageResponse imageResponse = imageService.uploadImage(file);
        Image image = imageRepository.findByIdAndIsActiveTrue(imageResponse.getId())
                .orElseThrow(() -> new RuntimeException("IMAGE_NOT_FOUND"));
        
        // Удаляем старое изображение, если оно было
        Image oldImage = menuItem.getImage();
        menuItem.setImage(image);
        
        menuItem = menuItemRepository.save(menuItem);
        
        // Мягко удаляем старое изображение, если оно было и больше не используется
        if (oldImage != null) {
            try {
                imageService.deleteImage(oldImage.getId());
            } catch (RuntimeException e) {
                // Если изображение используется где-то еще, просто логируем
                logger.debug("Could not delete old image {}: {}", oldImage.getId(), e.getMessage());
            }
        }
        
        return toResponse(menuItem);
    }
    
    @Transactional
    public MenuItemResponse deleteMenuItemImage(Long restaurantId, Long itemId) {
        MenuItem menuItem = menuItemRepository.findByIdAndIsActiveTrue(itemId)
                .orElseThrow(() -> new RuntimeException("MENU_ITEM_NOT_FOUND"));
        
        // Проверка принадлежности к ресторану
        if (!menuItem.getRestaurant().getId().equals(restaurantId)) {
            throw new RuntimeException("MENU_ITEM_NOT_FOUND");
        }
        
        Image oldImage = menuItem.getImage();
        menuItem.setImage(null);
        
        menuItem = menuItemRepository.save(menuItem);
        
        // Мягко удаляем изображение, если оно было
        if (oldImage != null) {
            try {
                imageService.deleteImage(oldImage.getId());
            } catch (RuntimeException e) {
                // Если изображение используется где-то еще, просто логируем
                logger.debug("Could not delete image {}: {}", oldImage.getId(), e.getMessage());
            }
        }
        
        return toResponse(menuItem);
    }
    
    private Sort buildSort(String sortBy, String sortOrder) {
        String field = sortBy != null ? sortBy : "displayOrder";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        // Маппинг полей
        switch (field) {
            case "name":
                return Sort.by(direction, "name");
            case "price":
                return Sort.by(direction, "price");
            case "displayOrder":
                return Sort.by(direction, "displayOrder");
            case "createdAt":
                return Sort.by(direction, "createdAt");
            default:
                return Sort.by(Sort.Direction.ASC, "displayOrder");
        }
    }
}

