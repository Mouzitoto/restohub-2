package com.restohub.clientapi.controller;

import com.restohub.clientapi.entity.Image;
import com.restohub.clientapi.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/client-api/images")
public class ImageController {
    
    private final ImageRepository imageRepository;
    
    @Autowired
    public ImageController(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }
    
    @GetMapping("/{imageId}")
    public ResponseEntity<byte[]> getImage(
            @PathVariable Long imageId,
            @RequestParam(value = "preview", defaultValue = "false") boolean preview) {
        Image image = imageRepository.findByIdAndIsActiveTrue(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));
        
        byte[] imageData = preview ? image.getPreviewData() : image.getImageData();
        long contentLength = preview ? image.getPreviewData().length : image.getFileSize();
        
        HttpHeaders headers = new HttpHeaders();
        String mimeType = image.getMimeType();
        if (mimeType != null) {
            headers.setContentType(MediaType.parseMediaType(mimeType));
        }
        headers.setContentLength(contentLength);
        
        return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
    }
}

