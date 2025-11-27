package com.restohub.clientapi.service;

import com.restohub.clientapi.dto.RestaurantDetailResponse;
import com.restohub.clientapi.dto.RestaurantListResponse;
import com.restohub.clientapi.entity.Restaurant;
import com.restohub.clientapi.repository.RestaurantRepository;
import com.restohub.clientapi.service.SubscriptionCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RestaurantService {
    
    private final RestaurantRepository restaurantRepository;
    private final SubscriptionCheckService subscriptionCheckService;
    
    @Autowired
    public RestaurantService(
            RestaurantRepository restaurantRepository,
            SubscriptionCheckService subscriptionCheckService) {
        this.restaurantRepository = restaurantRepository;
        this.subscriptionCheckService = subscriptionCheckService;
    }
    
    public List<RestaurantListResponse> getRestaurants(Integer limit, Integer offset) {
        List<Restaurant> allRestaurants = restaurantRepository.findByIsActiveTrue();
        
        // Фильтруем по активной подписке
        List<Restaurant> restaurantsWithSubscription = allRestaurants.stream()
                .filter(r -> subscriptionCheckService.hasActiveSubscription(r.getId()))
                .collect(Collectors.toList());
        
        // Применяем пагинацию
        int start = offset != null ? offset : 0;
        int end = start + (limit != null ? limit : 50);
        if (end > restaurantsWithSubscription.size()) {
            end = restaurantsWithSubscription.size();
        }
        if (start > restaurantsWithSubscription.size()) {
            start = restaurantsWithSubscription.size();
        }
        
        return restaurantsWithSubscription.subList(start, end).stream()
                .map(this::toListResponse)
                .collect(Collectors.toList());
    }
    
    public RestaurantDetailResponse getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
        
        return toDetailResponse(restaurant);
    }
    
    private RestaurantListResponse toListResponse(Restaurant restaurant) {
        return RestaurantListResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .address(restaurant.getAddress())
                .phone(restaurant.getPhone())
                .latitude(restaurant.getLatitude())
                .longitude(restaurant.getLongitude())
                .logoId(restaurant.getLogoImage() != null ? restaurant.getLogoImage().getId() : null)
                .description(restaurant.getDescription())
                .build();
    }
    
    private RestaurantDetailResponse toDetailResponse(Restaurant restaurant) {
        return RestaurantDetailResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .address(restaurant.getAddress())
                .phone(restaurant.getPhone())
                .email(null) // TODO: добавить поле в entity если нужно
                .description(restaurant.getDescription())
                .logoId(restaurant.getLogoImage() != null ? restaurant.getLogoImage().getId() : null)
                .backgroundId(restaurant.getBgImage() != null ? restaurant.getBgImage().getId() : null)
                .primaryColor(null) // TODO: добавить поле в entity если нужно
                .cuisineType(null) // TODO: добавить поле в entity если нужно
                .establishmentType(null) // TODO: добавить поле в entity если нужно
                .latitude(restaurant.getLatitude())
                .longitude(restaurant.getLongitude())
                .instagram(restaurant.getInstagram())
                .whatsapp(restaurant.getWhatsapp())
                .website(null) // TODO: добавить поле в entity если нужно
                .build();
    }
}

