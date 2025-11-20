package com.restohub.adminapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class UserInfoResponse {
    private Long id;
    private String email;
    private String role;
    private List<RestaurantInfo> restaurants;
    
    @Getter
    @Setter
    @AllArgsConstructor
    public static class RestaurantInfo {
        private Long id;
        private String name;
        private SubscriptionInfo subscription;
    }
    
    @Getter
    @Setter
    @AllArgsConstructor
    public static class SubscriptionInfo {
        private Long restaurantId;
        private Boolean isActive;
        private String endDate;
        private Integer daysRemaining;
        private Boolean isExpiringSoon;
    }
}

