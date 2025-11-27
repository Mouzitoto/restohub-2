package com.restohub.clientapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {
    private List<RestaurantListResponse> restaurants;
    private Long total;
    private Integer limit;
    private Integer offset;
}

