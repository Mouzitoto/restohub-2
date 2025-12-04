package com.restohub.clientapi.service;

import com.restohub.clientapi.dto.RestaurantListResponse;
import com.restohub.clientapi.dto.SearchResponse;
import com.restohub.clientapi.entity.MenuItem;
import com.restohub.clientapi.entity.Promotion;
import com.restohub.clientapi.entity.Restaurant;
import com.restohub.clientapi.repository.MenuItemRepository;
import com.restohub.clientapi.repository.PromotionRepository;
import com.restohub.clientapi.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {
    
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final PromotionRepository promotionRepository;
    private final SubscriptionCheckService subscriptionCheckService;
    
    @Autowired
    public SearchService(
            RestaurantRepository restaurantRepository,
            MenuItemRepository menuItemRepository,
            PromotionRepository promotionRepository,
            SubscriptionCheckService subscriptionCheckService) {
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
        this.promotionRepository = promotionRepository;
        this.subscriptionCheckService = subscriptionCheckService;
    }
    
    public SearchResponse search(
            String q,
            String menuItem,
            String promotion,
            Long promotionType,
            String cuisineType,
            Boolean isOutdoor,
            Boolean isSmoking,
            BigDecimal lat,
            BigDecimal lng,
            BigDecimal radius,
            Integer limit,
            Integer offset) {
        
        Set<Restaurant> resultSet = new HashSet<>();
        
        // Поиск по названию ресторана
        if (q != null && !q.trim().isEmpty()) {
            List<Restaurant> restaurants = restaurantRepository.searchRestaurants(q);
            resultSet.addAll(restaurants);
        }
        
        // Поиск по блюдам из меню
        if (menuItem != null && !menuItem.trim().isEmpty()) {
            List<MenuItem> allItems = menuItemRepository.findAll();
            List<MenuItem> items = allItems.stream()
                    .filter(item -> item.getIsActive() && item.getIsAvailable() &&
                            (item.getName().toLowerCase().contains(menuItem.toLowerCase()) ||
                            (item.getDescription() != null && item.getDescription().toLowerCase().contains(menuItem.toLowerCase()))))
                    .collect(Collectors.toList());
            
            for (MenuItem item : items) {
                if (item.getRestaurant() != null && item.getRestaurant().getIsActive()) {
                    resultSet.add(item.getRestaurant());
                }
            }
        }
        
        // Поиск по промо-событиям
        if ((promotion != null && !promotion.trim().isEmpty()) || promotionType != null) {
            List<Promotion> allPromotions = promotionRepository.findAll();
            List<Promotion> promotions = allPromotions.stream()
                    .filter(p -> {
                        if (!p.getIsActive()) return false;
                        boolean matches = true;
                        if (promotion != null && !promotion.trim().isEmpty()) {
                            matches = matches && (p.getTitle().toLowerCase().contains(promotion.toLowerCase()) ||
                                    (p.getDescription() != null && p.getDescription().toLowerCase().contains(promotion.toLowerCase())));
                        }
                        if (promotionType != null) {
                            matches = matches && p.getPromotionType() != null && p.getPromotionType().getId().equals(promotionType);
                        }
                        return matches;
                    })
                    .collect(Collectors.toList());
            
            for (Promotion promo : promotions) {
                if (promo.getRestaurant() != null && promo.getRestaurant().getIsActive()) {
                    resultSet.add(promo.getRestaurant());
                }
            }
        }
        
        // Если нет параметров поиска, получаем все активные рестораны
        if (q == null && menuItem == null && promotion == null && promotionType == null) {
            resultSet.addAll(restaurantRepository.findByIsActiveTrue());
        }
        
        // Фильтр по типу кухни (если поле существует в entity)
        // TODO: добавить поле cuisineType в Restaurant entity если нужно
        // if (cuisineType != null && !cuisineType.trim().isEmpty()) {
        //     resultSet = resultSet.stream()
        //             .filter(r -> cuisineType.equals(r.getCuisineType()))
        //             .collect(Collectors.toSet());
        // }
        
        // Фильтр по характеристикам залов
        if (isOutdoor != null && isOutdoor) {
            resultSet = resultSet.stream()
                    .filter(r -> hasOutdoorRooms(r))
                    .collect(Collectors.toSet());
        }
        
        if (isSmoking != null && isSmoking) {
            resultSet = resultSet.stream()
                    .filter(r -> hasSmokingRooms(r))
                    .collect(Collectors.toSet());
        }
        
        // Фильтр по геолокации
        if (lat != null && lng != null && radius != null) {
            resultSet = resultSet.stream()
                    .filter(r -> {
                        if (r.getLatitude() == null || r.getLongitude() == null) {
                            return false;
                        }
                        double distance = calculateDistance(
                                lat.doubleValue(), lng.doubleValue(),
                                r.getLatitude().doubleValue(), r.getLongitude().doubleValue());
                        return distance <= radius.doubleValue();
                    })
                    .collect(Collectors.toSet());
        }
        
        // Фильтруем по активной подписке
        List<Restaurant> filtered = resultSet.stream()
                .filter(r -> subscriptionCheckService.hasActiveSubscription(r.getId()))
                .collect(Collectors.toList());
        
        // Сортировка по расстоянию (если указана геолокация)
        if (lat != null && lng != null) {
            filtered.sort((r1, r2) -> {
                if (r1.getLatitude() == null || r1.getLongitude() == null) return 1;
                if (r2.getLatitude() == null || r2.getLongitude() == null) return -1;
                
                double d1 = calculateDistance(lat.doubleValue(), lng.doubleValue(),
                        r1.getLatitude().doubleValue(), r1.getLongitude().doubleValue());
                double d2 = calculateDistance(lat.doubleValue(), lng.doubleValue(),
                        r2.getLatitude().doubleValue(), r2.getLongitude().doubleValue());
                return Double.compare(d1, d2);
            });
        }
        
        // Применяем пагинацию
        int start = offset != null ? offset : 0;
        int end = start + (limit != null ? limit : 50);
        if (end > filtered.size()) {
            end = filtered.size();
        }
        if (start > filtered.size()) {
            start = filtered.size();
        }
        
        List<Restaurant> paginated = start < filtered.size() ? filtered.subList(start, end) : new ArrayList<>();
        
        List<RestaurantListResponse> response = paginated.stream()
                .map(this::toRestaurantListResponse)
                .collect(Collectors.toList());
        
        return SearchResponse.builder()
                .restaurants(response)
                .total((long) filtered.size())
                .limit(limit != null ? limit : 50)
                .offset(offset != null ? offset : 0)
                .build();
    }
    
    private boolean hasOutdoorRooms(Restaurant restaurant) {
        return restaurant.getFloors() != null && restaurant.getFloors().stream()
                .anyMatch(floor -> floor.getRooms() != null && floor.getRooms().stream()
                        .anyMatch(room -> room.getIsActive() && room.getIsOutdoor()));
    }
    
    private boolean hasSmokingRooms(Restaurant restaurant) {
        return restaurant.getFloors() != null && restaurant.getFloors().stream()
                .anyMatch(floor -> floor.getRooms() != null && floor.getRooms().stream()
                        .anyMatch(room -> room.getIsActive() && room.getIsSmoking()));
    }
    
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
    
    private RestaurantListResponse toRestaurantListResponse(Restaurant restaurant) {
        return RestaurantListResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .address(restaurant.getAddress())
                .phone(restaurant.getPhone())
                .latitude(restaurant.getLatitude())
                .longitude(restaurant.getLongitude())
                .logoId(restaurant.getLogoImage() != null ? restaurant.getLogoImage().getId() : null)
                .backgroundId(restaurant.getBgImage() != null ? restaurant.getBgImage().getId() : null)
                .description(restaurant.getDescription())
                .build();
    }
}

