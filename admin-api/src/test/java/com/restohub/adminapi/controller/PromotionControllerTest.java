package com.restohub.adminapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.entity.PromotionType;
import com.restohub.adminapi.repository.PromotionTypeRepository;
import com.restohub.adminapi.service.PromotionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PromotionControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PromotionService promotionService;

    @MockBean
    private PromotionTypeRepository promotionTypeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // ========== POST /r/{id}/promotion - создание акции ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testCreatePromotion_Success() throws Exception {
        // Arrange
        CreatePromotionRequest request = new CreatePromotionRequest();
        request.setPromotionTypeId(1L);
        request.setTitle("Скидка 20%");
        request.setDescription("Скидка на все блюда");
        request.setStartDate(LocalDate.of(2024, 1, 1));
        request.setEndDate(LocalDate.of(2024, 1, 31));

        PromotionResponse response = new PromotionResponse();
        response.setId(1L);
        response.setRestaurantId(1L);
        PromotionResponse.PromotionTypeInfo promotionTypeInfo = new PromotionResponse.PromotionTypeInfo();
        promotionTypeInfo.setId(1L);
        promotionTypeInfo.setCode("DISCOUNT");
        promotionTypeInfo.setName("Скидка");
        response.setPromotionType(promotionTypeInfo);
        response.setTitle("Скидка 20%");
        response.setDescription("Скидка на все блюда");
        response.setStartDate(LocalDate.of(2024, 1, 1));
        response.setEndDate(LocalDate.of(2024, 1, 31));
        response.setIsActive(true);
        response.setCreatedAt(Instant.now());
        response.setUpdatedAt(Instant.now());

        // Настраиваем мок репозитория для валидатора ValidPromotionTypeId
        PromotionType promotionType = new PromotionType();
        promotionType.setId(1L);
        promotionType.setCode("DISCOUNT");
        promotionType.setName("Скидка");
        promotionType.setIsActive(true);
        doReturn(Optional.of(promotionType)).when(promotionTypeRepository).findByIdAndIsActiveTrue(1L);

        doReturn(response).when(promotionService).createPromotion(eq(1L), any(CreatePromotionRequest.class));

        // Act & Assert
        mockMvc.perform(post("/r/1/promotion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Скидка 20%"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(promotionService, times(1)).createPromotion(eq(1L), any(CreatePromotionRequest.class));
    }

    // ========== GET /r/{id}/promotion - список акций ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetPromotions_Success() throws Exception {
        // Arrange
        PromotionListItemResponse item1 = new PromotionListItemResponse();
        item1.setId(1L);
        item1.setRestaurantId(1L);
        PromotionListItemResponse.PromotionTypeInfo promotionTypeInfo = new PromotionListItemResponse.PromotionTypeInfo();
        promotionTypeInfo.setId(1L);
        promotionTypeInfo.setCode("DISCOUNT");
        promotionTypeInfo.setName("Скидка");
        item1.setPromotionType(promotionTypeInfo);
        item1.setTitle("Скидка 20%");
        item1.setStartDate(LocalDate.of(2024, 1, 1));
        item1.setEndDate(LocalDate.of(2024, 1, 31));
        item1.setIsActive(true);
        item1.setCreatedAt(Instant.now());

        List<PromotionListItemResponse> items = Arrays.asList(item1);
        PaginationResponse.PaginationInfo pagination = new PaginationResponse.PaginationInfo(1L, 50, 0, false);
        PaginationResponse<List<PromotionListItemResponse>> response = new PaginationResponse<>(items, pagination);

        when(promotionService.getPromotions(eq(1L), eq(50), eq(0), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq("startDate"), eq("desc")))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/r/1/promotion")
                        .param("limit", "50")
                        .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].title").value("Скидка 20%"))
                .andExpect(jsonPath("$.pagination.total").value(1L));

        verify(promotionService, times(1)).getPromotions(eq(1L), eq(50), eq(0), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq("startDate"), eq("desc"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetPromotions_WithFilters() throws Exception {
        // Arrange
        List<PromotionListItemResponse> items = Arrays.asList();
        PaginationResponse.PaginationInfo pagination = new PaginationResponse.PaginationInfo(0L, 50, 0, false);
        PaginationResponse<List<PromotionListItemResponse>> response = new PaginationResponse<>(items, pagination);

        LocalDate startDateFrom = LocalDate.of(2024, 1, 1);
        LocalDate startDateTo = LocalDate.of(2024, 1, 31);

        when(promotionService.getPromotions(eq(1L), eq(50), eq(0), eq(1L), eq(true), 
                eq(startDateFrom), eq(startDateTo), isNull(), isNull(), isNull(), eq("startDate"), eq("desc")))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/r/1/promotion")
                        .param("limit", "50")
                        .param("offset", "0")
                        .param("promotionTypeId", "1")
                        .param("isActive", "true")
                        .param("startDateFrom", "2024-01-01")
                        .param("startDateTo", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.pagination.total").value(0L));

        verify(promotionService, times(1)).getPromotions(eq(1L), eq(50), eq(0), eq(1L), eq(true), 
                eq(startDateFrom), eq(startDateTo), isNull(), isNull(), isNull(), eq("startDate"), eq("desc"));
    }

    // ========== GET /r/{id}/promotion/{promotionId} - детали акции ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetPromotion_Success() throws Exception {
        // Arrange
        PromotionResponse response = new PromotionResponse();
        response.setId(1L);
        response.setRestaurantId(1L);
        PromotionResponse.PromotionTypeInfo promotionTypeInfo = new PromotionResponse.PromotionTypeInfo();
        promotionTypeInfo.setId(1L);
        promotionTypeInfo.setCode("DISCOUNT");
        promotionTypeInfo.setName("Скидка");
        response.setPromotionType(promotionTypeInfo);
        response.setTitle("Скидка 20%");
        response.setDescription("Скидка на все блюда");
        response.setStartDate(LocalDate.of(2024, 1, 1));
        response.setEndDate(LocalDate.of(2024, 1, 31));
        response.setIsActive(true);
        response.setCreatedAt(Instant.now());
        response.setUpdatedAt(Instant.now());

        doReturn(response).when(promotionService).getPromotion(1L, 1L);

        // Act & Assert
        mockMvc.perform(get("/r/1/promotion/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Скидка 20%"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(promotionService, times(1)).getPromotion(1L, 1L);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetPromotion_NotFound() throws Exception {
        // Arrange
        when(promotionService.getPromotion(1L, 999L))
                .thenThrow(new RuntimeException("PROMOTION_NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(get("/r/1/promotion/999"))
                .andExpect(status().isNotFound());

        verify(promotionService, times(1)).getPromotion(1L, 999L);
    }

    // ========== PUT /r/{id}/promotion/{promotionId} - обновление акции ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testUpdatePromotion_Success() throws Exception {
        // Arrange
        UpdatePromotionRequest request = new UpdatePromotionRequest();
        request.setTitle("Обновленная акция");
        request.setDescription("Новое описание");

        PromotionResponse response = new PromotionResponse();
        response.setId(1L);
        response.setRestaurantId(1L);
        response.setTitle("Обновленная акция");
        response.setDescription("Новое описание");
        response.setIsActive(true);
        response.setUpdatedAt(Instant.now());

        when(promotionService.updatePromotion(eq(1L), eq(1L), any(UpdatePromotionRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/r/1/promotion/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Обновленная акция"));

        verify(promotionService, times(1)).updatePromotion(eq(1L), eq(1L), any(UpdatePromotionRequest.class));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testUpdatePromotion_NotFound() throws Exception {
        // Arrange
        UpdatePromotionRequest request = new UpdatePromotionRequest();
        request.setTitle("Обновленная акция");

        when(promotionService.updatePromotion(eq(1L), eq(999L), any(UpdatePromotionRequest.class)))
                .thenThrow(new RuntimeException("PROMOTION_NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(put("/r/1/promotion/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(promotionService, times(1)).updatePromotion(eq(1L), eq(999L), any(UpdatePromotionRequest.class));
    }

    // ========== DELETE /r/{id}/promotion/{promotionId} - удаление акции ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testDeletePromotion_Success() throws Exception {
        // Arrange
        doNothing().when(promotionService).deletePromotion(1L, 1L);

        // Act & Assert
        mockMvc.perform(delete("/r/1/promotion/1"))
                .andExpect(status().isNoContent());

        verify(promotionService, times(1)).deletePromotion(1L, 1L);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testDeletePromotion_NotFound() throws Exception {
        // Arrange
        doThrow(new RuntimeException("PROMOTION_NOT_FOUND"))
                .when(promotionService).deletePromotion(1L, 999L);

        // Act & Assert
        mockMvc.perform(delete("/r/1/promotion/999"))
                .andExpect(status().isNotFound());

        verify(promotionService, times(1)).deletePromotion(1L, 999L);
    }

    // ========== POST /r/{id}/promotion/{promotionId}/image - загрузка изображения акции ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testUploadPromotionImage_Success() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "promotion.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        PromotionResponse response = new PromotionResponse();
        response.setId(1L);
        response.setRestaurantId(1L);
        PromotionResponse.PromotionTypeInfo promotionTypeInfo = new PromotionResponse.PromotionTypeInfo();
        promotionTypeInfo.setId(1L);
        promotionTypeInfo.setCode("DISCOUNT");
        promotionTypeInfo.setName("Скидка");
        response.setPromotionType(promotionTypeInfo);
        response.setTitle("Скидка 20%");
        response.setImageId(123L);
        response.setIsActive(true);

        doReturn(response).when(promotionService).uploadPromotionImage(eq(1L), eq(1L), any(MultipartFile.class));

        // Act & Assert
        mockMvc.perform(multipart("/r/1/promotion/1/image")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.imageId").value(123L));

        verify(promotionService, times(1)).uploadPromotionImage(eq(1L), eq(1L), any(MultipartFile.class));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testUploadPromotionImage_NotFound() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "promotion.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        doThrow(new RuntimeException("PROMOTION_NOT_FOUND"))
                .when(promotionService).uploadPromotionImage(eq(1L), eq(999L), any(MultipartFile.class));

        // Act & Assert
        mockMvc.perform(multipart("/r/1/promotion/999/image")
                        .file(file))
                .andExpect(status().isNotFound());

        verify(promotionService, times(1)).uploadPromotionImage(eq(1L), eq(999L), any(MultipartFile.class));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testUploadPromotionImage_IOException() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "promotion.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        // Контроллер ловит IOException и бросает RuntimeException("IMAGE_UPLOAD_ERROR")
        // GlobalExceptionHandler обрабатывает "IMAGE_UPLOAD_ERROR" как BAD_REQUEST (400)
        doThrow(new IOException("IO_ERROR"))
                .when(promotionService).uploadPromotionImage(eq(1L), eq(1L), any(MultipartFile.class));

        // Act & Assert
        mockMvc.perform(multipart("/r/1/promotion/1/image")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exceptionName").value("IMAGE_UPLOAD_ERROR"));

        verify(promotionService, times(1)).uploadPromotionImage(eq(1L), eq(1L), any(MultipartFile.class));
    }

    // ========== DELETE /r/{id}/promotion/{promotionId}/image - удаление изображения акции ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testDeletePromotionImage_Success() throws Exception {
        // Arrange
        PromotionResponse response = new PromotionResponse();
        response.setId(1L);
        response.setRestaurantId(1L);
        PromotionResponse.PromotionTypeInfo promotionTypeInfo = new PromotionResponse.PromotionTypeInfo();
        promotionTypeInfo.setId(1L);
        promotionTypeInfo.setCode("DISCOUNT");
        promotionTypeInfo.setName("Скидка");
        response.setPromotionType(promotionTypeInfo);
        response.setTitle("Скидка 20%");
        response.setImageId(null);
        response.setIsActive(true);

        doReturn(response).when(promotionService).deletePromotionImage(1L, 1L);

        // Act & Assert
        mockMvc.perform(delete("/r/1/promotion/1/image"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.imageId").isEmpty());

        verify(promotionService, times(1)).deletePromotionImage(1L, 1L);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testDeletePromotionImage_NotFound() throws Exception {
        // Arrange
        when(promotionService.deletePromotionImage(1L, 999L))
                .thenThrow(new RuntimeException("PROMOTION_NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(delete("/r/1/promotion/999/image"))
                .andExpect(status().isNotFound());

        verify(promotionService, times(1)).deletePromotionImage(1L, 999L);
    }
}

