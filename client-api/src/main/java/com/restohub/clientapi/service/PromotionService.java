package com.restohub.clientapi.service;

import com.restohub.clientapi.dto.PromotionResponse;
import com.restohub.clientapi.dto.PromotionTypeResponse;
import com.restohub.clientapi.entity.Promotion;
import com.restohub.clientapi.repository.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PromotionService {
    
    private final PromotionRepository promotionRepository;
    
    @Autowired
    public PromotionService(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }
    
    public List<PromotionResponse> getPromotions(
            Long restaurantId,
            Long promotionTypeId,
            Boolean isCurrent,
            Integer limit,
            Integer offset) {
        
        Pageable pageable = PageRequest.of(
                offset != null ? offset / (limit != null ? limit : 50) : 0,
                limit != null ? limit : 50
        );
        
        List<Promotion> promotions = promotionRepository.findActivePromotionsByRestaurant(
                restaurantId, promotionTypeId, isCurrent, pageable);
        
        LocalDate today = LocalDate.now();
        DayOfWeek currentDayOfWeek = today.getDayOfWeek();
        int currentDayOfWeekNumber = currentDayOfWeek.getValue(); // 1 = Monday, 7 = Sunday
        
        return promotions.stream()
                .filter(p -> isPromotionActive(p, today, currentDayOfWeekNumber))
                .map(this::toPromotionResponse)
                .collect(Collectors.toList());
    }
    
    private boolean isPromotionActive(Promotion promotion, LocalDate today, int currentDayOfWeekNumber) {
        if (!promotion.getIsActive()) {
            return false;
        }
        
        if (promotion.getStartDate().isAfter(today)) {
            return false;
        }
        
        if (promotion.getEndDate() != null && promotion.getEndDate().isBefore(today)) {
            return false;
        }
        
        // Проверка для повторяющихся событий
        if (promotion.getIsRecurring() && "WEEKLY".equals(promotion.getRecurrenceType())) {
            List<Integer> recurrenceDays = promotion.getRecurrenceDaysOfWeek();
            if (recurrenceDays == null || recurrenceDays.isEmpty()) {
                return false;
            }
            return recurrenceDays.contains(currentDayOfWeekNumber);
        }
        
        return true;
    }
    
    private PromotionResponse toPromotionResponse(Promotion promotion) {
        return PromotionResponse.builder()
                .id(promotion.getId())
                .title(promotion.getTitle())
                .description(promotion.getDescription())
                .promotionType(PromotionTypeResponse.builder()
                        .id(promotion.getPromotionType().getId())
                        .code(promotion.getPromotionType().getCode())
                        .name(promotion.getPromotionType().getName())
                        .build())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .imageId(promotion.getImage() != null ? promotion.getImage().getId() : null)
                .isRecurring(promotion.getIsRecurring())
                .recurrenceType(promotion.getRecurrenceType())
                .recurrenceDayOfWeek(promotion.getRecurrenceDaysOfWeek())
                .build();
    }
}

