package com.restohub.adminapi.service;

import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.entity.Image;
import com.restohub.adminapi.entity.Promotion;
import com.restohub.adminapi.entity.PromotionType;
import com.restohub.adminapi.entity.Restaurant;
import com.restohub.adminapi.repository.ImageRepository;
import com.restohub.adminapi.repository.PromotionRepository;
import com.restohub.adminapi.repository.PromotionTypeRepository;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PromotionService {
    
    private static final Logger logger = LoggerFactory.getLogger(PromotionService.class);
    
    private final PromotionRepository promotionRepository;
    private final RestaurantRepository restaurantRepository;
    private final PromotionTypeRepository promotionTypeRepository;
    private final ImageRepository imageRepository;
    private final ImageService imageService;
    
    @Autowired
    public PromotionService(
            PromotionRepository promotionRepository,
            RestaurantRepository restaurantRepository,
            PromotionTypeRepository promotionTypeRepository,
            ImageRepository imageRepository,
            ImageService imageService) {
        this.promotionRepository = promotionRepository;
        this.restaurantRepository = restaurantRepository;
        this.promotionTypeRepository = promotionTypeRepository;
        this.imageRepository = imageRepository;
        this.imageService = imageService;
    }
    
    @Transactional
    public PromotionResponse createPromotion(Long restaurantId, CreatePromotionRequest request) {
        // Проверка существования ресторана
        Restaurant restaurant = restaurantRepository.findByIdAndIsActiveTrue(restaurantId)
                .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
        
        // Проверка существования типа промо-события
        PromotionType promotionType = promotionTypeRepository.findByIdAndIsActiveTrue(request.getPromotionTypeId())
                .orElseThrow(() -> new RuntimeException("PROMOTION_TYPE_NOT_FOUND"));
        
        // Валидация дат
        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("INVALID_DATE_RANGE");
        }
        
        // Валидация повторяющихся событий
        boolean isRecurring = request.getIsRecurring() != null && request.getIsRecurring();
        if (isRecurring) {
            if (request.getRecurrenceType() == null || request.getRecurrenceType().trim().isEmpty()) {
                throw new RuntimeException("RECURRENCE_TYPE_REQUIRED");
            }
            String recurrenceType = request.getRecurrenceType().trim().toUpperCase();
            if (!"WEEKLY".equals(recurrenceType) && !"MONTHLY".equals(recurrenceType) && !"DAILY".equals(recurrenceType)) {
                throw new RuntimeException("INVALID_RECURRENCE_TYPE");
            }
            if ("WEEKLY".equals(recurrenceType) && (request.getRecurrenceDayOfWeek() == null || 
                    request.getRecurrenceDayOfWeek() < 1 || request.getRecurrenceDayOfWeek() > 7)) {
                throw new RuntimeException("RECURRENCE_DAY_OF_WEEK_REQUIRED");
            }
        } else {
            if (request.getRecurrenceType() != null || request.getRecurrenceDayOfWeek() != null) {
                throw new RuntimeException("RECURRENCE_FIELDS_NOT_ALLOWED");
            }
        }
        
        // Проверка изображения
        Image image = null;
        if (request.getImageId() != null) {
            image = imageRepository.findByIdAndIsActiveTrue(request.getImageId())
                    .orElseThrow(() -> new RuntimeException("IMAGE_NOT_FOUND"));
        }
        
        Promotion promotion = new Promotion();
        promotion.setRestaurant(restaurant);
        promotion.setPromotionType(promotionType);
        promotion.setTitle(request.getTitle().trim());
        promotion.setDescription(request.getDescription());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setImage(image);
        promotion.setIsRecurring(isRecurring);
        promotion.setRecurrenceType(isRecurring ? request.getRecurrenceType().trim().toUpperCase() : null);
        promotion.setRecurrenceDayOfWeek(isRecurring && "WEEKLY".equals(promotion.getRecurrenceType()) ? request.getRecurrenceDayOfWeek() : null);
        promotion.setIsActive(true);
        
        promotion = promotionRepository.save(promotion);
        
        return toResponse(promotion);
    }
    
    public PaginationResponse<List<PromotionListItemResponse>> getPromotions(
            Long restaurantId, Integer limit, Integer offset, Long promotionTypeId, Boolean isActive,
            LocalDate startDateFrom, LocalDate startDateTo, LocalDate endDateFrom, LocalDate endDateTo,
            Boolean isCurrent, String sortBy, String sortOrder) {
        
        // Проверка существования ресторана
        restaurantRepository.findByIdAndIsActiveTrue(restaurantId)
                .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
        
        // Построение спецификации
        Specification<Promotion> spec = Specification.where(
                (root, query, cb) -> cb.equal(root.get("restaurant").get("id"), restaurantId)
        );
        
        // Фильтр по активности
        if (isActive != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isActive"), isActive));
        } else {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isActive"), true));
        }
        
        // Фильтр по типу
        if (promotionTypeId != null) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("promotionType").get("id"), promotionTypeId)
            );
        }
        
        // Фильтры по датам
        if (startDateFrom != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startDate"), startDateFrom));
        }
        if (startDateTo != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("startDate"), startDateTo));
        }
        if (endDateFrom != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("endDate"), endDateFrom));
        }
        if (endDateTo != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("endDate"), endDateTo));
        }
        
        // Фильтр по текущим акциям
        if (isCurrent != null && isCurrent) {
            LocalDate today = LocalDate.now();
            spec = spec.and((root, query, cb) -> 
                cb.and(
                    cb.lessThanOrEqualTo(root.get("startDate"), today),
                    cb.or(
                        cb.isNull(root.get("endDate")),
                        cb.greaterThanOrEqualTo(root.get("endDate"), today)
                    )
                )
            );
        }
        
        // Сортировка
        Sort sort = buildSort(sortBy, sortOrder);
        Pageable pageable = PageRequest.of(offset / limit, limit, sort);
        
        Page<Promotion> page = promotionRepository.findAll(spec, pageable);
        
        List<PromotionListItemResponse> items = page.getContent().stream()
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
    
    public PromotionResponse getPromotion(Long restaurantId, Long promotionId) {
        Promotion promotion = promotionRepository.findByIdAndRestaurantIdAndIsActiveTrue(promotionId, restaurantId)
                .orElseThrow(() -> new RuntimeException("PROMOTION_NOT_FOUND"));
        
        return toResponse(promotion);
    }
    
    @Transactional
    public PromotionResponse updatePromotion(Long restaurantId, Long promotionId, UpdatePromotionRequest request) {
        Promotion promotion = promotionRepository.findByIdAndRestaurantIdAndIsActiveTrue(promotionId, restaurantId)
                .orElseThrow(() -> new RuntimeException("PROMOTION_NOT_FOUND"));
        
        // Обновление полей (PATCH-логика)
        if (request.getTitle() != null) {
            promotion.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            promotion.setDescription(request.getDescription());
        }
        if (request.getPromotionTypeId() != null) {
            PromotionType promotionType = promotionTypeRepository.findByIdAndIsActiveTrue(request.getPromotionTypeId())
                    .orElseThrow(() -> new RuntimeException("PROMOTION_TYPE_NOT_FOUND"));
            promotion.setPromotionType(promotionType);
        }
        if (request.getStartDate() != null) {
            promotion.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            promotion.setEndDate(request.getEndDate());
        }
        
        // Валидация дат после обновления
        if (promotion.getEndDate() != null && promotion.getEndDate().isBefore(promotion.getStartDate())) {
            throw new RuntimeException("INVALID_DATE_RANGE");
        }
        
        // Обновление повторяющихся событий
        if (request.getIsRecurring() != null) {
            boolean isRecurring = request.getIsRecurring();
            promotion.setIsRecurring(isRecurring);
            
            if (isRecurring) {
                if (request.getRecurrenceType() == null || request.getRecurrenceType().trim().isEmpty()) {
                    throw new RuntimeException("RECURRENCE_TYPE_REQUIRED");
                }
                String recurrenceType = request.getRecurrenceType().trim().toUpperCase();
                if (!"WEEKLY".equals(recurrenceType) && !"MONTHLY".equals(recurrenceType) && !"DAILY".equals(recurrenceType)) {
                    throw new RuntimeException("INVALID_RECURRENCE_TYPE");
                }
                promotion.setRecurrenceType(recurrenceType);
                
                if ("WEEKLY".equals(recurrenceType)) {
                    if (request.getRecurrenceDayOfWeek() == null || 
                            request.getRecurrenceDayOfWeek() < 1 || request.getRecurrenceDayOfWeek() > 7) {
                        throw new RuntimeException("RECURRENCE_DAY_OF_WEEK_REQUIRED");
                    }
                    promotion.setRecurrenceDayOfWeek(request.getRecurrenceDayOfWeek());
                } else {
                    promotion.setRecurrenceDayOfWeek(null);
                }
            } else {
                promotion.setRecurrenceType(null);
                promotion.setRecurrenceDayOfWeek(null);
            }
        } else if (promotion.getIsRecurring() && request.getRecurrenceType() != null) {
            // Обновление типа повторения для существующего повторяющегося события
            String recurrenceType = request.getRecurrenceType().trim().toUpperCase();
            if (!"WEEKLY".equals(recurrenceType) && !"MONTHLY".equals(recurrenceType) && !"DAILY".equals(recurrenceType)) {
                throw new RuntimeException("INVALID_RECURRENCE_TYPE");
            }
            promotion.setRecurrenceType(recurrenceType);
            if ("WEEKLY".equals(recurrenceType)) {
                if (request.getRecurrenceDayOfWeek() != null) {
                    if (request.getRecurrenceDayOfWeek() < 1 || request.getRecurrenceDayOfWeek() > 7) {
                        throw new RuntimeException("INVALID_RECURRENCE_DAY_OF_WEEK");
                    }
                    promotion.setRecurrenceDayOfWeek(request.getRecurrenceDayOfWeek());
                }
            } else {
                promotion.setRecurrenceDayOfWeek(null);
            }
        }
        
        if (request.getImageId() != null) {
            if (request.getImageId() == 0) {
                promotion.setImage(null);
            } else {
                Image image = imageRepository.findByIdAndIsActiveTrue(request.getImageId())
                        .orElseThrow(() -> new RuntimeException("IMAGE_NOT_FOUND"));
                promotion.setImage(image);
            }
        }
        
        promotion = promotionRepository.save(promotion);
        
        return toResponse(promotion);
    }
    
    @Transactional
    public void deletePromotion(Long restaurantId, Long promotionId) {
        Promotion promotion = promotionRepository.findByIdAndRestaurantIdAndIsActiveTrue(promotionId, restaurantId)
                .orElseThrow(() -> new RuntimeException("PROMOTION_NOT_FOUND"));
        
        // Мягкое удаление
        promotion.setIsActive(false);
        promotion.setDeletedAt(LocalDateTime.now());
        promotionRepository.save(promotion);
    }
    
    private PromotionResponse toResponse(Promotion promotion) {
        PromotionResponse response = new PromotionResponse();
        response.setId(promotion.getId());
        response.setRestaurantId(promotion.getRestaurant().getId());
        
        if (promotion.getPromotionType() != null) {
            PromotionResponse.PromotionTypeInfo typeInfo = new PromotionResponse.PromotionTypeInfo();
            typeInfo.setId(promotion.getPromotionType().getId());
            typeInfo.setCode(promotion.getPromotionType().getCode());
            typeInfo.setName(promotion.getPromotionType().getName());
            response.setPromotionType(typeInfo);
        }
        
        response.setTitle(promotion.getTitle());
        response.setDescription(promotion.getDescription());
        response.setStartDate(promotion.getStartDate());
        response.setEndDate(promotion.getEndDate());
        response.setImageId(promotion.getImage() != null ? promotion.getImage().getId() : null);
        response.setIsRecurring(promotion.getIsRecurring());
        response.setRecurrenceType(promotion.getRecurrenceType());
        response.setRecurrenceDayOfWeek(promotion.getRecurrenceDayOfWeek());
        response.setIsActive(promotion.getIsActive());
        response.setCreatedAt(promotion.getCreatedAt() != null ? promotion.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        response.setUpdatedAt(promotion.getUpdatedAt() != null ? promotion.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        response.setDeletedAt(promotion.getDeletedAt() != null ? promotion.getDeletedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        return response;
    }
    
    private PromotionListItemResponse toListItemResponse(Promotion promotion) {
        PromotionListItemResponse response = new PromotionListItemResponse();
        response.setId(promotion.getId());
        response.setRestaurantId(promotion.getRestaurant().getId());
        
        if (promotion.getPromotionType() != null) {
            PromotionListItemResponse.PromotionTypeInfo typeInfo = new PromotionListItemResponse.PromotionTypeInfo();
            typeInfo.setId(promotion.getPromotionType().getId());
            typeInfo.setCode(promotion.getPromotionType().getCode());
            typeInfo.setName(promotion.getPromotionType().getName());
            response.setPromotionType(typeInfo);
        }
        
        response.setTitle(promotion.getTitle());
        response.setDescription(promotion.getDescription());
        response.setStartDate(promotion.getStartDate());
        response.setEndDate(promotion.getEndDate());
        response.setImageId(promotion.getImage() != null ? promotion.getImage().getId() : null);
        response.setIsRecurring(promotion.getIsRecurring());
        response.setRecurrenceType(promotion.getRecurrenceType());
        response.setRecurrenceDayOfWeek(promotion.getRecurrenceDayOfWeek());
        response.setIsActive(promotion.getIsActive());
        response.setCreatedAt(promotion.getCreatedAt() != null ? promotion.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        return response;
    }
    
    @Transactional
    public PromotionResponse uploadPromotionImage(Long restaurantId, Long promotionId, MultipartFile file) throws IOException {
        Promotion promotion = promotionRepository.findByIdAndRestaurantIdAndIsActiveTrue(promotionId, restaurantId)
                .orElseThrow(() -> new RuntimeException("PROMOTION_NOT_FOUND"));
        
        // Загружаем изображение
        ImageResponse imageResponse = imageService.uploadImage(file);
        Image image = imageRepository.findByIdAndIsActiveTrue(imageResponse.getId())
                .orElseThrow(() -> new RuntimeException("IMAGE_NOT_FOUND"));
        
        // Удаляем старое изображение, если оно было
        Image oldImage = promotion.getImage();
        promotion.setImage(image);
        
        promotion = promotionRepository.save(promotion);
        
        // Мягко удаляем старое изображение, если оно было и больше не используется
        if (oldImage != null) {
            try {
                imageService.deleteImage(oldImage.getId());
            } catch (RuntimeException e) {
                // Если изображение используется где-то еще, просто логируем
                logger.debug("Could not delete old image {}: {}", oldImage.getId(), e.getMessage());
            }
        }
        
        return toResponse(promotion);
    }
    
    @Transactional
    public PromotionResponse deletePromotionImage(Long restaurantId, Long promotionId) {
        Promotion promotion = promotionRepository.findByIdAndRestaurantIdAndIsActiveTrue(promotionId, restaurantId)
                .orElseThrow(() -> new RuntimeException("PROMOTION_NOT_FOUND"));
        
        Image oldImage = promotion.getImage();
        promotion.setImage(null);
        
        promotion = promotionRepository.save(promotion);
        
        // Мягко удаляем изображение, если оно было
        if (oldImage != null) {
            try {
                imageService.deleteImage(oldImage.getId());
            } catch (RuntimeException e) {
                // Если изображение используется где-то еще, просто логируем
                logger.debug("Could not delete image {}: {}", oldImage.getId(), e.getMessage());
            }
        }
        
        return toResponse(promotion);
    }
    
    private Sort buildSort(String sortBy, String sortOrder) {
        String field = sortBy != null ? sortBy : "startDate";
        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        
        // Маппинг полей
        switch (field) {
            case "startDate":
                return Sort.by(direction, "startDate");
            case "endDate":
                return Sort.by(direction, "endDate");
            case "title":
                return Sort.by(direction, "title");
            case "createdAt":
                return Sort.by(direction, "createdAt");
            default:
                return Sort.by(Sort.Direction.DESC, "startDate");
        }
    }
}

