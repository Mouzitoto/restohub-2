package com.restohub.adminapi.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class UserListItemResponse {
    private Long id;
    private String email;
    private Long roleId;
    private String roleName;
    private Boolean isActive;
    private List<RestaurantInfo> restaurants;
    private Instant createdAt;
    
    @Data
    public static class RestaurantInfo {
        private Long id;
        private String name;
    }
}

