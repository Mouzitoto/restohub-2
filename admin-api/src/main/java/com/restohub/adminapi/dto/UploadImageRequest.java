package com.restohub.adminapi.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UploadImageRequest {
    private MultipartFile file;
    private String type; // LOGO или BACKGROUND (опционально)
}

