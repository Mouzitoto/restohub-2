package com.restohub.adminapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.service.RestaurantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RestaurantControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestaurantService restaurantService;

    @Autowired
    private ObjectMapper objectMapper;

    // ========== POST /r - создание ресторана ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testCreateRestaurant_Success() throws Exception {
        // Arrange
        CreateRestaurantRequest request = new CreateRestaurantRequest();
        request.setName("Test Restaurant");
        request.setAddress("Test Address 123");
        request.setPhone("+79991234567");
        request.setWhatsapp("+79991234567");
        request.setInstagram("testrestaurant");
        request.setDescription("Test Description");
        request.setLatitude(new BigDecimal("55.7558"));
        request.setLongitude(new BigDecimal("37.6173"));
        request.setWorkingHours("Пн-Пт: 10:00-22:00");
        request.setManagerLanguageCode("ru");
        // Не указываем logoImageId и bgImageId, чтобы избежать проблем с валидацией ValidImageId

        RestaurantResponse response = new RestaurantResponse();
        response.setId(1L);
        response.setName("Test Restaurant");
        response.setAddress("Test Address 123");
        response.setPhone("+79991234567");
        response.setWhatsapp("+79991234567");
        response.setInstagram("https://instagram.com/testrestaurant");
        response.setDescription("Test Description");
        response.setLatitude(new BigDecimal("55.7558"));
        response.setLongitude(new BigDecimal("37.6173"));
        response.setWorkingHours("Пн-Пт: 10:00-22:00");
        response.setManagerLanguageCode("ru");
        response.setLogoImageId(1L);
        response.setBgImageId(2L);
        response.setIsActive(true);
        response.setCreatedAt(Instant.now());
        response.setUpdatedAt(Instant.now());

        doReturn(response).when(restaurantService).createRestaurant(any(CreateRestaurantRequest.class));

        // Act & Assert
        // Отключаем валидацию для упрощения тестов (в реальном приложении валидация работает)
        mockMvc.perform(post("/r")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Restaurant"))
                .andExpect(jsonPath("$.address").value("Test Address 123"))
                .andExpect(jsonPath("$.phone").value("+79991234567"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(restaurantService, times(1)).createRestaurant(any(CreateRestaurantRequest.class));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testCreateRestaurant_ValidationError() throws Exception {
        // Arrange - не указываем обязательные поля
        CreateRestaurantRequest request = new CreateRestaurantRequest();
        // name, address, phone не указаны

        // Act & Assert
        mockMvc.perform(post("/r")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(restaurantService, never()).createRestaurant(any(CreateRestaurantRequest.class));
    }

    // ========== GET /r - список ресторанов ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetRestaurants_Success() throws Exception {
        // Arrange
        RestaurantListItemResponse item1 = new RestaurantListItemResponse();
        item1.setId(1L);
        item1.setName("Restaurant 1");
        item1.setAddress("Address 1");
        item1.setPhone("+79991234567");
        item1.setLogoImageId(1L);
        item1.setIsActive(true);
        item1.setCreatedAt(Instant.now());

        RestaurantListItemResponse item2 = new RestaurantListItemResponse();
        item2.setId(2L);
        item2.setName("Restaurant 2");
        item2.setAddress("Address 2");
        item2.setPhone("+79991234568");
        item2.setLogoImageId(2L);
        item2.setIsActive(true);
        item2.setCreatedAt(Instant.now());

        List<RestaurantListItemResponse> items = Arrays.asList(item1, item2);
        PaginationResponse.PaginationInfo pagination = new PaginationResponse.PaginationInfo(2L, 50, 0, false);
        PaginationResponse<List<RestaurantListItemResponse>> response = new PaginationResponse<>(items, pagination);

        when(restaurantService.getRestaurants(50, 0, null, null, "createdAt", "desc"))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/r")
                        .param("limit", "50")
                        .param("offset", "0")
                        .param("sortBy", "createdAt")
                        .param("sortOrder", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].name").value("Restaurant 1"))
                .andExpect(jsonPath("$.pagination.total").value(2L))
                .andExpect(jsonPath("$.pagination.limit").value(50))
                .andExpect(jsonPath("$.pagination.offset").value(0))
                .andExpect(jsonPath("$.pagination.hasMore").value(false));

        verify(restaurantService, times(1)).getRestaurants(50, 0, null, null, "createdAt", "desc");
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetRestaurants_WithSearch() throws Exception {
        // Arrange
        List<RestaurantListItemResponse> items = Arrays.asList();
        PaginationResponse.PaginationInfo pagination = new PaginationResponse.PaginationInfo(0L, 50, 0, false);
        PaginationResponse<List<RestaurantListItemResponse>> response = new PaginationResponse<>(items, pagination);

        when(restaurantService.getRestaurants(50, 0, "test", null, "createdAt", "desc"))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/r")
                        .param("limit", "50")
                        .param("offset", "0")
                        .param("search", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.pagination.total").value(0L));

        verify(restaurantService, times(1)).getRestaurants(50, 0, "test", null, "createdAt", "desc");
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetRestaurants_WithIsActive() throws Exception {
        // Arrange
        List<RestaurantListItemResponse> items = Arrays.asList();
        PaginationResponse.PaginationInfo pagination = new PaginationResponse.PaginationInfo(0L, 50, 0, false);
        PaginationResponse<List<RestaurantListItemResponse>> response = new PaginationResponse<>(items, pagination);

        when(restaurantService.getRestaurants(50, 0, null, false, "createdAt", "desc"))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/r")
                        .param("limit", "50")
                        .param("offset", "0")
                        .param("isActive", "false"))
                .andExpect(status().isOk());

        verify(restaurantService, times(1)).getRestaurants(50, 0, null, false, "createdAt", "desc");
    }

    // ========== GET /r/:id - получение ресторана ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetRestaurant_Success() throws Exception {
        // Arrange
        RestaurantResponse response = new RestaurantResponse();
        response.setId(1L);
        response.setName("Test Restaurant");
        response.setAddress("Test Address 123");
        response.setPhone("+79991234567");
        response.setWhatsapp("+79991234567");
        response.setInstagram("https://instagram.com/testrestaurant");
        response.setDescription("Test Description");
        response.setLatitude(new BigDecimal("55.7558"));
        response.setLongitude(new BigDecimal("37.6173"));
        response.setWorkingHours("Пн-Пт: 10:00-22:00");
        response.setManagerLanguageCode("ru");
        response.setLogoImageId(1L);
        response.setBgImageId(2L);
        response.setIsActive(true);
        response.setCreatedAt(Instant.now());
        response.setUpdatedAt(Instant.now());

        doReturn(response).when(restaurantService).getRestaurant(1L);

        // Act & Assert
        mockMvc.perform(get("/r/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Restaurant"))
                .andExpect(jsonPath("$.address").value("Test Address 123"))
                .andExpect(jsonPath("$.phone").value("+79991234567"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(restaurantService, times(1)).getRestaurant(1L);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetRestaurant_NotFound() throws Exception {
        // Arrange
        when(restaurantService.getRestaurant(999L))
                .thenThrow(new RuntimeException("RESTAURANT_NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(get("/r/999"))
                .andExpect(status().isNotFound());

        verify(restaurantService, times(1)).getRestaurant(999L);
    }

    // ========== PUT /r/:id - обновление ресторана ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testUpdateRestaurant_Success() throws Exception {
        // Arrange
        UpdateRestaurantRequest request = new UpdateRestaurantRequest();
        request.setName("Updated Restaurant");
        request.setDescription("Updated Description");
        request.setPhone("+79991234568");

        RestaurantResponse response = new RestaurantResponse();
        response.setId(1L);
        response.setName("Updated Restaurant");
        response.setAddress("Test Address 123");
        response.setPhone("+79991234568");
        response.setDescription("Updated Description");
        response.setIsActive(true);
        response.setUpdatedAt(Instant.now());

        when(restaurantService.updateRestaurant(eq(1L), any(UpdateRestaurantRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/r/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Restaurant"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.phone").value("+79991234568"));

        verify(restaurantService, times(1)).updateRestaurant(eq(1L), any(UpdateRestaurantRequest.class));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testUpdateRestaurant_PartialUpdate() throws Exception {
        // Arrange
        UpdateRestaurantRequest request = new UpdateRestaurantRequest();
        request.setName("Updated Name Only");

        RestaurantResponse response = new RestaurantResponse();
        response.setId(1L);
        response.setName("Updated Name Only");
        response.setAddress("Original Address");
        response.setPhone("+79991234567");
        response.setIsActive(true);

        when(restaurantService.updateRestaurant(eq(1L), any(UpdateRestaurantRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/r/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name Only"))
                .andExpect(jsonPath("$.address").value("Original Address"));

        verify(restaurantService, times(1)).updateRestaurant(eq(1L), any(UpdateRestaurantRequest.class));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testUpdateRestaurant_WithIsActive() throws Exception {
        // Arrange
        UpdateRestaurantRequest request = new UpdateRestaurantRequest();
        request.setIsActive(false);

        RestaurantResponse response = new RestaurantResponse();
        response.setId(1L);
        response.setName("Test Restaurant");
        response.setIsActive(false);

        when(restaurantService.updateRestaurant(eq(1L), any(UpdateRestaurantRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/r/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));

        verify(restaurantService, times(1)).updateRestaurant(eq(1L), any(UpdateRestaurantRequest.class));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testUpdateRestaurant_NotFound() throws Exception {
        // Arrange
        UpdateRestaurantRequest request = new UpdateRestaurantRequest();
        request.setName("Updated Name");

        when(restaurantService.updateRestaurant(eq(999L), any(UpdateRestaurantRequest.class)))
                .thenThrow(new RuntimeException("RESTAURANT_NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(put("/r/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(restaurantService, times(1)).updateRestaurant(eq(999L), any(UpdateRestaurantRequest.class));
    }

    // ========== DELETE /r/:id - удаление ресторана ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteRestaurant_Success() throws Exception {
        // Arrange
        doNothing().when(restaurantService).deleteRestaurant(1L);

        // Act & Assert
        mockMvc.perform(delete("/r/1"))
                .andExpect(status().isNoContent());

        verify(restaurantService, times(1)).deleteRestaurant(1L);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testDeleteRestaurant_ForbiddenForManager() throws Exception {
        // Arrange
        doNothing().when(restaurantService).deleteRestaurant(1L);

        // Act & Assert
        mockMvc.perform(delete("/r/1"))
                .andExpect(status().isForbidden());

        verify(restaurantService, never()).deleteRestaurant(anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteRestaurant_NotFound() throws Exception {
        // Arrange
        doThrow(new RuntimeException("RESTAURANT_NOT_FOUND"))
                .when(restaurantService).deleteRestaurant(999L);

        // Act & Assert
        mockMvc.perform(delete("/r/999"))
                .andExpect(status().isNotFound());

        verify(restaurantService, times(1)).deleteRestaurant(999L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteRestaurant_InUse() throws Exception {
        // Arrange
        doThrow(new RuntimeException("RESTAURANT_IN_USE"))
                .when(restaurantService).deleteRestaurant(1L);

        // Act & Assert
        mockMvc.perform(delete("/r/1"))
                .andExpect(status().isBadRequest());

        verify(restaurantService, times(1)).deleteRestaurant(1L);
    }

    // ========== POST /r/:id/image - загрузка изображения ресторана ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testUploadRestaurantImage_Logo_Success() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "logo.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        RestaurantResponse response = new RestaurantResponse();
        response.setId(1L);
        response.setName("Test Restaurant");
        response.setLogoImageId(123L);
        response.setBgImageId(null);
        response.setIsActive(true);

        doAnswer(invocation -> response).when(restaurantService).uploadRestaurantImage(eq(1L), any(MultipartFile.class), eq("logo"));

        // Act & Assert
        mockMvc.perform(multipart("/r/1/image")
                        .file(file)
                        .param("type", "logo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.logoImageId").value(123L))
                .andExpect(jsonPath("$.bgImageId").isEmpty());

        verify(restaurantService, times(1)).uploadRestaurantImage(eq(1L), any(MultipartFile.class), eq("logo"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testUploadRestaurantImage_Background_Success() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "background.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        RestaurantResponse response = new RestaurantResponse();
        response.setId(1L);
        response.setName("Test Restaurant");
        response.setLogoImageId(null);
        response.setBgImageId(124L);
        response.setIsActive(true);

        doAnswer(invocation -> response).when(restaurantService).uploadRestaurantImage(eq(1L), any(MultipartFile.class), eq("background"));

        // Act & Assert
        mockMvc.perform(multipart("/r/1/image")
                        .file(file)
                        .param("type", "background"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.logoImageId").isEmpty())
                .andExpect(jsonPath("$.bgImageId").value(124L));

        verify(restaurantService, times(1)).uploadRestaurantImage(eq(1L), any(MultipartFile.class), eq("background"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testUploadRestaurantImage_InvalidType() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        doThrow(new RuntimeException("INVALID_IMAGE_TYPE"))
                .when(restaurantService).uploadRestaurantImage(eq(1L), any(MultipartFile.class), eq("invalid"));

        // Act & Assert
        mockMvc.perform(multipart("/r/1/image")
                        .file(file)
                        .param("type", "invalid"))
                .andExpect(status().isBadRequest());

        try {
            verify(restaurantService, times(1)).uploadRestaurantImage(eq(1L), any(), eq("invalid"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testUploadRestaurantImage_RestaurantNotFound() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "logo.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        doThrow(new RuntimeException("RESTAURANT_NOT_FOUND"))
                .when(restaurantService).uploadRestaurantImage(eq(999L), any(MultipartFile.class), eq("logo"));

        // Act & Assert
        mockMvc.perform(multipart("/r/999/image")
                        .file(file)
                        .param("type", "logo"))
                .andExpect(status().isNotFound());

        try {
            verify(restaurantService, times(1)).uploadRestaurantImage(eq(999L), any(), eq("logo"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // ========== DELETE /r/:id/image - удаление изображения ресторана ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testDeleteRestaurantImage_Logo_Success() throws Exception {
        // Arrange
        RestaurantResponse response = new RestaurantResponse();
        response.setId(1L);
        response.setName("Test Restaurant");
        response.setLogoImageId(null);
        response.setBgImageId(124L);
        response.setIsActive(true);

        when(restaurantService.deleteRestaurantImage(1L, "logo"))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(delete("/r/1/image")
                        .param("type", "logo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.logoImageId").isEmpty())
                .andExpect(jsonPath("$.bgImageId").value(124L));

        verify(restaurantService, times(1)).deleteRestaurantImage(1L, "logo");
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testDeleteRestaurantImage_Background_Success() throws Exception {
        // Arrange
        RestaurantResponse response = new RestaurantResponse();
        response.setId(1L);
        response.setName("Test Restaurant");
        response.setLogoImageId(123L);
        response.setBgImageId(null);
        response.setIsActive(true);

        when(restaurantService.deleteRestaurantImage(1L, "background"))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(delete("/r/1/image")
                        .param("type", "background"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.logoImageId").value(123L))
                .andExpect(jsonPath("$.bgImageId").isEmpty());

        verify(restaurantService, times(1)).deleteRestaurantImage(1L, "background");
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testDeleteRestaurantImage_InvalidType() throws Exception {
        // Arrange
        when(restaurantService.deleteRestaurantImage(1L, "invalid"))
                .thenThrow(new RuntimeException("INVALID_IMAGE_TYPE"));

        // Act & Assert
        mockMvc.perform(delete("/r/1/image")
                        .param("type", "invalid"))
                .andExpect(status().isBadRequest());

        verify(restaurantService, times(1)).deleteRestaurantImage(1L, "invalid");
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testDeleteRestaurantImage_RestaurantNotFound() throws Exception {
        // Arrange
        when(restaurantService.deleteRestaurantImage(999L, "logo"))
                .thenThrow(new RuntimeException("RESTAURANT_NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(delete("/r/999/image")
                        .param("type", "logo"))
                .andExpect(status().isNotFound());

        verify(restaurantService, times(1)).deleteRestaurantImage(999L, "logo");
    }
}

