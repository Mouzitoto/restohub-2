package com.restohub.adminapi.dto;

import lombok.Data;

import java.util.List;

@Data
public class RoomLayoutResponse {
    private RoomResponse room;
    private List<TableResponse> tables;
    private String imageUrl;
}

