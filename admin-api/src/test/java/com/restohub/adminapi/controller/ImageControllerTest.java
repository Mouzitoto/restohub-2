package com.restohub.adminapi.controller;

import com.restohub.adminapi.dto.ImageResponse;
import com.restohub.adminapi.service.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ImageControllerTest {

    @Mock
    private ImageService imageService;

    @InjectMocks
    private ImageController imageController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(imageController)
                .setControllerAdvice(new TestExceptionHandler())
                .build();
    }

    // ========== POST /admin-api/image - загрузка изображения ==========

    @Test
    void testUploadImage_Success() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        ImageResponse response = new ImageResponse();
        response.setId(1L);
        response.setMimeType("image/jpeg");
        response.setFileSize(1024L);

        when(imageService.uploadImage(any())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(multipart("/admin-api/image")
                        .file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.mimeType").value("image/jpeg"))
                .andExpect(jsonPath("$.fileSize").value(1024L));

        verify(imageService, times(1)).uploadImage(any());
    }

    @Test
    void testUploadImage_EmptyFile() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[0]
        );

        when(imageService.uploadImage(any()))
                .thenThrow(new RuntimeException("FILE_REQUIRED"));

        // Act & Assert
        mockMvc.perform(multipart("/admin-api/image")
                        .file(file))
                .andExpect(status().isBadRequest());

        verify(imageService, times(1)).uploadImage(any());
    }

    @Test
    void testUploadImage_FileTooLarge() throws Exception {
        // Arrange
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                largeContent
        );

        when(imageService.uploadImage(any()))
                .thenThrow(new RuntimeException("FILE_TOO_LARGE"));

        // Act & Assert
        mockMvc.perform(multipart("/admin-api/image")
                        .file(file))
                .andExpect(status().isBadRequest());

        verify(imageService, times(1)).uploadImage(any());
    }

    @Test
    void testUploadImage_InvalidFormat() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "not an image".getBytes()
        );

        when(imageService.uploadImage(any()))
                .thenThrow(new RuntimeException("INVALID_FILE_FORMAT"));

        // Act & Assert
        mockMvc.perform(multipart("/admin-api/image")
                        .file(file))
                .andExpect(status().isBadRequest());

        verify(imageService, times(1)).uploadImage(any());
    }

    // ========== GET /admin-api/image - получение изображения ==========

    @Test
    void testGetImage_Original() throws Exception {
        // Arrange
        byte[] imageData = "image content".getBytes();
        String mimeType = "image/jpeg";

        when(imageService.getImage(1L, false)).thenReturn(imageData);
        when(imageService.getImageMimeType(1L)).thenReturn(mimeType);

        // Act & Assert
        mockMvc.perform(get("/admin-api/image")
                        .param("id", "1")
                        .param("isPreview", "false"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes(imageData));

        verify(imageService, times(1)).getImage(1L, false);
        verify(imageService, times(1)).getImageMimeType(1L);
    }

    @Test
    void testGetImage_Preview() throws Exception {
        // Arrange
        byte[] previewData = "preview content".getBytes();
        String mimeType = "image/jpeg";

        when(imageService.getImage(1L, true)).thenReturn(previewData);
        when(imageService.getImageMimeType(1L)).thenReturn(mimeType);

        // Act & Assert
        mockMvc.perform(get("/admin-api/image")
                        .param("id", "1")
                        .param("isPreview", "true"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes(previewData));

        verify(imageService, times(1)).getImage(1L, true);
        verify(imageService, times(1)).getImageMimeType(1L);
    }

    @Test
    void testGetImage_DefaultPreviewFalse() throws Exception {
        // Arrange
        byte[] imageData = "image content".getBytes();
        String mimeType = "image/png";

        when(imageService.getImage(1L, false)).thenReturn(imageData);
        when(imageService.getImageMimeType(1L)).thenReturn(mimeType);

        // Act & Assert
        mockMvc.perform(get("/admin-api/image")
                        .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(imageData));

        verify(imageService, times(1)).getImage(1L, false);
        verify(imageService, times(1)).getImageMimeType(1L);
    }

    @Test
    void testGetImage_NotFound() throws Exception {
        // Arrange
        when(imageService.getImage(999L, false))
                .thenThrow(new RuntimeException("IMAGE_NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(get("/admin-api/image")
                        .param("id", "999"))
                .andExpect(status().isNotFound());

        verify(imageService, times(1)).getImage(999L, false);
    }

    @Test
    void testGetImage_WithoutId() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/admin-api/image"))
                .andExpect(status().isBadRequest());

        verify(imageService, never()).getImage(anyLong(), anyBoolean());
    }

    // ========== DELETE /admin-api/image/:id - удаление изображения ==========

    @Test
    void testDeleteImage_Success() throws Exception {
        // Arrange
        doNothing().when(imageService).deleteImage(1L);

        // Act & Assert
        mockMvc.perform(delete("/admin-api/image/1"))
                .andExpect(status().isNoContent());

        verify(imageService, times(1)).deleteImage(1L);
    }

    @Test
    void testDeleteImage_NotFound() throws Exception {
        // Arrange
        doThrow(new RuntimeException("IMAGE_NOT_FOUND"))
                .when(imageService).deleteImage(999L);

        // Act & Assert
        mockMvc.perform(delete("/admin-api/image/999"))
                .andExpect(status().isNotFound());

        verify(imageService, times(1)).deleteImage(999L);
    }

    @Test
    void testDeleteImage_InUse() throws Exception {
        // Arrange
        doThrow(new RuntimeException("IMAGE_IN_USE"))
                .when(imageService).deleteImage(1L);

        // Act & Assert
        mockMvc.perform(delete("/admin-api/image/1"))
                .andExpect(status().isBadRequest());

        verify(imageService, times(1)).deleteImage(1L);
    }
}

