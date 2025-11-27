package com.restohub.clientapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantDetailResponse {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String email;
    private String description;
    private Long logoId;
    private Long backgroundId;
    private String primaryColor;
    private String cuisineType;
    private String establishmentType;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String instagram;
    private String whatsapp;
    private String website;
}

