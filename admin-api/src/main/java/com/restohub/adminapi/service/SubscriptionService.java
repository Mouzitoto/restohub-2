package com.restohub.adminapi.service;

import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.entity.Restaurant;
import com.restohub.adminapi.entity.RestaurantSubscription;
import com.restohub.adminapi.entity.SubscriptionType;
import com.restohub.adminapi.repository.RestaurantRepository;
import com.restohub.adminapi.repository.RestaurantSubscriptionRepository;
import com.restohub.adminapi.repository.SubscriptionTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubscriptionService {
    
    private final RestaurantSubscriptionRepository subscriptionRepository;
    private final RestaurantRepository restaurantRepository;
    private final SubscriptionTypeRepository subscriptionTypeRepository;
    
    @Autowired
    public SubscriptionService(
            RestaurantSubscriptionRepository subscriptionRepository,
            RestaurantRepository restaurantRepository,
            SubscriptionTypeRepository subscriptionTypeRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.restaurantRepository = restaurantRepository;
        this.subscriptionTypeRepository = subscriptionTypeRepository;
    }
    
    public SubscriptionResponse getRestaurantSubscription(Long restaurantId) {
        // Проверка существования ресторана
        Restaurant restaurant = restaurantRepository.findByIdAndIsActiveTrue(restaurantId)
                .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
        
        // Поиск активной подписки
        List<RestaurantSubscription> activeSubscriptions = subscriptionRepository
                .findByRestaurantIdAndIsActiveTrue(restaurantId);
        
        RestaurantSubscription subscription = activeSubscriptions.stream()
                .sorted((s1, s2) -> s2.getEndDate().compareTo(s1.getEndDate()))
                .findFirst()
                .orElse(null);
        
        if (subscription == null) {
            // Возвращаем ответ с isActive = false
            SubscriptionResponse response = new SubscriptionResponse();
            response.setRestaurantId(restaurantId);
            response.setIsActive(false);
            response.setDaysRemaining(0);
            response.setIsExpiringSoon(false);
            return response;
        }
        
        return toResponse(subscription);
    }
    
    @Transactional
    public SubscriptionResponse updateRestaurantSubscription(Long restaurantId, UpdateSubscriptionRequest request) {
        // Проверка существования ресторана
        Restaurant restaurant = restaurantRepository.findByIdAndIsActiveTrue(restaurantId)
                .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
        
        // Поиск текущей подписки (активной или последней)
        List<RestaurantSubscription> subscriptions = subscriptionRepository.findByRestaurantId(restaurantId);
        RestaurantSubscription subscription = subscriptions.stream()
                .sorted((s1, s2) -> s2.getEndDate().compareTo(s1.getEndDate()))
                .findFirst()
                .orElse(null);
        
        // Валидация дат
        if (request.getStartDate() != null && request.getEndDate() != null) {
            if (request.getEndDate().isBefore(request.getStartDate())) {
                throw new RuntimeException("INVALID_DATE_RANGE");
            }
        }
        
        if (subscription == null) {
            // Создание новой подписки
            subscription = new RestaurantSubscription();
            subscription.setRestaurant(restaurant);
        }
        
        // Обновление полей
        if (request.getSubscriptionTypeId() != null) {
            SubscriptionType subscriptionType = subscriptionTypeRepository
                    .findByIdAndIsActiveTrue(request.getSubscriptionTypeId())
                    .orElseThrow(() -> new RuntimeException("SUBSCRIPTION_TYPE_NOT_FOUND"));
            subscription.setSubscriptionType(subscriptionType);
        }
        
        if (request.getStartDate() != null) {
            subscription.setStartDate(request.getStartDate());
        }
        
        if (request.getEndDate() != null) {
            subscription.setEndDate(request.getEndDate());
        }
        
        if (request.getIsActive() != null) {
            subscription.setIsActive(request.getIsActive());
        }
        
        if (request.getDescription() != null) {
            subscription.setDescription(request.getDescription());
        }
        
        subscription = subscriptionRepository.save(subscription);
        
        return toResponse(subscription);
    }
    
    public PaginationResponse<List<SubscriptionListItemResponse>> getAllSubscriptions(
            Integer limit, Integer offset, Boolean isActive, Long restaurantId,
            Long subscriptionTypeId, Boolean expiringSoon, String sortBy, String sortOrder) {
        
        // Построение спецификации
        Specification<RestaurantSubscription> spec = Specification.where(null);
        
        // Фильтр по активности
        if (isActive != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isActive"), isActive));
        }
        
        // Фильтр по ресторану
        if (restaurantId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("restaurant").get("id"), restaurantId));
        }
        
        // Фильтр по типу подписки
        if (subscriptionTypeId != null) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("subscriptionType").get("id"), subscriptionTypeId)
            );
        }
        
        // Фильтр по истекающим подпискам
        if (expiringSoon != null && expiringSoon) {
            LocalDate sevenDaysFromNow = LocalDate.now().plusDays(7);
            spec = spec.and((root, query, cb) -> 
                cb.and(
                    cb.lessThanOrEqualTo(root.get("endDate"), sevenDaysFromNow),
                    cb.equal(root.get("isActive"), true)
                )
            );
        }
        
        // Сортировка
        Sort sort = buildSort(sortBy, sortOrder);
        Pageable pageable = PageRequest.of(offset / limit, limit, sort);
        
        Page<RestaurantSubscription> page = subscriptionRepository.findAll(spec, pageable);
        
        List<SubscriptionListItemResponse> items = page.getContent().stream()
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
    
    private SubscriptionResponse toResponse(RestaurantSubscription subscription) {
        SubscriptionResponse response = new SubscriptionResponse();
        response.setId(subscription.getId());
        response.setRestaurantId(subscription.getRestaurant().getId());
        
        if (subscription.getSubscriptionType() != null) {
            SubscriptionResponse.SubscriptionTypeInfo typeInfo = new SubscriptionResponse.SubscriptionTypeInfo();
            typeInfo.setId(subscription.getSubscriptionType().getId());
            typeInfo.setCode(subscription.getSubscriptionType().getCode());
            typeInfo.setName(subscription.getSubscriptionType().getName());
            typeInfo.setDescription(subscription.getSubscriptionType().getDescription());
            typeInfo.setPrice(subscription.getSubscriptionType().getPrice());
            response.setSubscriptionType(typeInfo);
        }
        
        response.setStartDate(subscription.getStartDate());
        response.setEndDate(subscription.getEndDate());
        response.setIsActive(subscription.getIsActive());
        response.setDescription(subscription.getDescription());
        
        // Расчет daysRemaining и isExpiringSoon
        if (subscription.getEndDate() != null && subscription.getIsActive()) {
            LocalDate today = LocalDate.now();
            long daysRemaining = ChronoUnit.DAYS.between(today, subscription.getEndDate());
            response.setDaysRemaining((int) Math.max(0, daysRemaining));
            response.setIsExpiringSoon(daysRemaining <= 7);
        } else {
            response.setDaysRemaining(0);
            response.setIsExpiringSoon(false);
        }
        
        response.setCreatedAt(subscription.getCreatedAt() != null ? subscription.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        response.setUpdatedAt(subscription.getUpdatedAt() != null ? subscription.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        
        return response;
    }
    
    private SubscriptionListItemResponse toListItemResponse(RestaurantSubscription subscription) {
        SubscriptionListItemResponse response = new SubscriptionListItemResponse();
        response.setId(subscription.getId());
        response.setRestaurantId(subscription.getRestaurant().getId());
        response.setRestaurantName(subscription.getRestaurant().getName());
        
        if (subscription.getSubscriptionType() != null) {
            SubscriptionListItemResponse.SubscriptionTypeInfo typeInfo = new SubscriptionListItemResponse.SubscriptionTypeInfo();
            typeInfo.setId(subscription.getSubscriptionType().getId());
            typeInfo.setCode(subscription.getSubscriptionType().getCode());
            typeInfo.setName(subscription.getSubscriptionType().getName());
            response.setSubscriptionType(typeInfo);
        }
        
        response.setStartDate(subscription.getStartDate());
        response.setEndDate(subscription.getEndDate());
        response.setIsActive(subscription.getIsActive());
        
        // Расчет daysRemaining и isExpiringSoon
        if (subscription.getEndDate() != null && subscription.getIsActive()) {
            LocalDate today = LocalDate.now();
            long daysRemaining = ChronoUnit.DAYS.between(today, subscription.getEndDate());
            response.setDaysRemaining((int) Math.max(0, daysRemaining));
            response.setIsExpiringSoon(daysRemaining <= 7);
        } else {
            response.setDaysRemaining(0);
            response.setIsExpiringSoon(false);
        }
        
        return response;
    }
    
    private Sort buildSort(String sortBy, String sortOrder) {
        String field = sortBy != null ? sortBy : "endDate";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        // Маппинг полей
        switch (field) {
            case "endDate":
                return Sort.by(direction, "endDate");
            case "startDate":
                return Sort.by(direction, "startDate");
            case "createdAt":
                return Sort.by(direction, "createdAt");
            default:
                return Sort.by(Sort.Direction.ASC, "endDate");
        }
    }
}

