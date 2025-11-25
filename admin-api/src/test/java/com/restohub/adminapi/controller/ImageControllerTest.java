package com.restohub.adminapi.controller;

import com.restohub.adminapi.service.ImageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ImageControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImageService imageService;

    // ========== GET /image - получение изображения ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetImage_Original() throws Exception {
        // Arrange
        byte[] imageData = "image content".getBytes();
        String mimeType = "image/jpeg";

        doReturn(imageData).when(imageService).getImage(1L, false);
        doReturn(mimeType).when(imageService).getImageMimeType(1L);

        // Act & Assert
        mockMvc.perform(get("/image")
                        .param("id", "1")
                        .param("isPreview", "false"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes(imageData));

        verify(imageService, times(1)).getImage(1L, false);
        verify(imageService, times(1)).getImageMimeType(1L);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetImage_Preview() throws Exception {
        // Arrange
        byte[] previewData = "preview content".getBytes();
        String mimeType = "image/jpeg";

        doReturn(previewData).when(imageService).getImage(1L, true);
        doReturn(mimeType).when(imageService).getImageMimeType(1L);

        // Act & Assert
        mockMvc.perform(get("/image")
                        .param("id", "1")
                        .param("isPreview", "true"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes(previewData));

        verify(imageService, times(1)).getImage(1L, true);
        verify(imageService, times(1)).getImageMimeType(1L);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetImage_DefaultPreviewFalse() throws Exception {
        // Arrange
        byte[] imageData = "image content".getBytes();
        String mimeType = "image/png";

        doReturn(imageData).when(imageService).getImage(1L, false);
        doReturn(mimeType).when(imageService).getImageMimeType(1L);

        // Act & Assert
        mockMvc.perform(get("/image")
                        .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(imageData));

        verify(imageService, times(1)).getImage(1L, false);
        verify(imageService, times(1)).getImageMimeType(1L);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetImage_NotFound() throws Exception {
        // Arrange
        doThrow(new RuntimeException("IMAGE_NOT_FOUND")).when(imageService).getImage(999L, false);

        // Act & Assert
        mockMvc.perform(get("/image")
                        .param("id", "999"))
                .andExpect(status().isNotFound());

        verify(imageService, times(1)).getImage(999L, false);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetImage_WithoutId() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/image"))
                .andExpect(status().isBadRequest());

        verify(imageService, never()).getImage(anyLong(), anyBoolean());
    }

    // ========== DELETE /image/:id - удаление изображения ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testDeleteImage_Success() throws Exception {
        // Arrange
        doNothing().when(imageService).deleteImage(1L);

        // Act & Assert
        mockMvc.perform(delete("/image/1"))
                .andExpect(status().isNoContent());

        verify(imageService, times(1)).deleteImage(1L);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testDeleteImage_NotFound() throws Exception {
        // Arrange
        doThrow(new RuntimeException("IMAGE_NOT_FOUND"))
                .when(imageService).deleteImage(999L);

        // Act & Assert
        mockMvc.perform(delete("/image/999"))
                .andExpect(status().isNotFound());

        verify(imageService, times(1)).deleteImage(999L);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testDeleteImage_InUse() throws Exception {
        // Arrange
        doThrow(new RuntimeException("IMAGE_IN_USE"))
                .when(imageService).deleteImage(1L);

        // Act & Assert
        mockMvc.perform(delete("/image/1"))
                .andExpect(status().isBadRequest());

        verify(imageService, times(1)).deleteImage(1L);
    }
}

