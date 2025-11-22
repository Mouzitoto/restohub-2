package com.restohub.adminapi.controller;

import com.restohub.adminapi.dto.ImageResponse;
import com.restohub.adminapi.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/image")
public class ImageController {
    
    private final ImageService imageService;
    
    @Autowired
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }
    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponse> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            ImageResponse response = imageService.uploadImage(file);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IOException e) {
            throw new RuntimeException("IMAGE_UPLOAD_ERROR");
        }
    }
    
    @GetMapping
    public ResponseEntity<byte[]> getImage(
            @RequestParam("id") Long id,
            @RequestParam(value = "isPreview", defaultValue = "false") boolean isPreview) {
        
        byte[] imageData = imageService.getImage(id, isPreview);
        String mimeType = imageService.getImageMimeType(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(mimeType));
        headers.setContentLength(imageData.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(imageData);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long id) {
        imageService.deleteImage(id);
        return ResponseEntity.noContent().build();
    }
}

