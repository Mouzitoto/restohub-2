package com.restohub.adminapi.dto;

import lombok.Data;

@Data
public class ImageResponse {
    private Long id;
    private String mimeType;
    private Long fileSize;
}

