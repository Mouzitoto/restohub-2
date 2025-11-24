package com.restohub.adminapi.service;

import com.restohub.adminapi.dto.ImageResponse;
import com.restohub.adminapi.entity.Image;
import com.restohub.adminapi.repository.ImageRepository;
import com.restohub.adminapi.repository.RestaurantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ImageService {
    
    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);
    private static final int PREVIEW_WIDTH = 300;
    private static final int PREVIEW_HEIGHT = 300;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    
    private final ImageRepository imageRepository;
    private final RestaurantRepository restaurantRepository;
    
    @Autowired
    public ImageService(ImageRepository imageRepository, RestaurantRepository restaurantRepository) {
        this.imageRepository = imageRepository;
        this.restaurantRepository = restaurantRepository;
    }
    
    @Transactional
    public ImageResponse uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("FILE_REQUIRED");
        }
        
        // Проверка размера файла
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("FILE_TOO_LARGE");
        }
        
        // Проверка типа файла
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("INVALID_FILE_TYPE");
        }
        
        // Читаем оригинальное изображение
        byte[] originalData = file.getBytes();
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        
        if (originalImage == null) {
            logger.warn("Failed to read image from file: contentType={}, size={}", contentType, file.getSize());
            throw new RuntimeException("INVALID_IMAGE");
        }
        
        // Генерируем превью
        BufferedImage previewImage = generatePreview(originalImage);
        ByteArrayOutputStream previewOutputStream = new ByteArrayOutputStream();
        String format = getImageFormat(contentType);
        ImageIO.write(previewImage, format, previewOutputStream);
        byte[] previewData = previewOutputStream.toByteArray();
        
        // Сохраняем в БД
        Image image = new Image();
        image.setImageData(originalData);
        image.setPreviewData(previewData);
        image.setMimeType(contentType);
        image.setFileSize(file.getSize());
        image.setIsActive(true);
        
        image = imageRepository.save(image);
        logger.debug("Image uploaded successfully: id={}, size={}, type={}", image.getId(), file.getSize(), contentType);
        
        ImageResponse response = new ImageResponse();
        response.setId(image.getId());
        response.setMimeType(image.getMimeType());
        response.setFileSize(image.getFileSize());
        
        return response;
    }
    
    public byte[] getImage(Long id, boolean isPreview) {
        Optional<Image> imageOpt = imageRepository.findByIdAndIsActiveTrue(id);
        if (imageOpt.isEmpty()) {
            throw new RuntimeException("IMAGE_NOT_FOUND");
        }
        
        Image image = imageOpt.get();
        return isPreview ? image.getPreviewData() : image.getImageData();
    }
    
    public String getImageMimeType(Long id) {
        Optional<Image> imageOpt = imageRepository.findByIdAndIsActiveTrue(id);
        if (imageOpt.isEmpty()) {
            throw new RuntimeException("IMAGE_NOT_FOUND");
        }
        
        return imageOpt.get().getMimeType();
    }
    
    @Transactional
    public void deleteImage(Long id) {
        Optional<Image> imageOpt = imageRepository.findByIdAndIsActiveTrue(id);
        if (imageOpt.isEmpty()) {
            throw new RuntimeException("IMAGE_NOT_FOUND");
        }
        
        Image image = imageOpt.get();
        
        // Проверка использования изображения в ресторанах
        boolean isUsed = restaurantRepository.findAll().stream()
                .anyMatch(r -> (r.getLogoImage() != null && r.getLogoImage().getId().equals(id)) ||
                              (r.getBgImage() != null && r.getBgImage().getId().equals(id)));
        
        if (isUsed) {
            throw new RuntimeException("IMAGE_IN_USE");
        }
        
        // Мягкое удаление
        image.setIsActive(false);
        image.setDeletedAt(LocalDateTime.now());
        imageRepository.save(image);
        logger.debug("Image soft deleted: id={}", id);
    }
    
    private BufferedImage generatePreview(BufferedImage original) {
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();
        
        // Вычисляем размеры превью с сохранением пропорций
        double scale = Math.min((double) PREVIEW_WIDTH / originalWidth, (double) PREVIEW_HEIGHT / originalHeight);
        int previewWidth = (int) (originalWidth * scale);
        int previewHeight = (int) (originalHeight * scale);
        
        // Создаем превью
        BufferedImage preview = new BufferedImage(previewWidth, previewHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = preview.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, previewWidth, previewHeight, null);
        g.dispose();
        
        return preview;
    }
    
    private String getImageFormat(String contentType) {
        if (contentType == null) {
            return "jpg";
        }
        
        return switch (contentType.toLowerCase()) {
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }
}

