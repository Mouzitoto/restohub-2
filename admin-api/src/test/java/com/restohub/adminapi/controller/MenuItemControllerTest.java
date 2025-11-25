package com.restohub.adminapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.service.MenuItemService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MenuItemControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MenuItemService menuItemService;

    @Autowired
    private ObjectMapper objectMapper;

    // ========== POST /r/{id}/menu-item - создание блюда ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testCreateMenuItem_Success() throws Exception {
        // Arrange
        CreateMenuItemRequest request = new CreateMenuItemRequest();
        request.setMenuCategoryId(1L);
        request.setName("Пицца Маргарита");
        request.setPrice(new BigDecimal("1000.00"));
        request.setDescription("Классическая пицца");

        MenuItemResponse response = new MenuItemResponse();
        response.setId(1L);
        response.setRestaurantId(1L);
        response.setMenuCategoryId(1L);
        response.setName("Пицца Маргарита");
        response.setPrice(new BigDecimal("1000.00"));
        response.setDescription("Классическая пицца");
        response.setIsActive(true);
        response.setCreatedAt(Instant.now());
        response.setUpdatedAt(Instant.now());

        doReturn(response).when(menuItemService).createMenuItem(eq(1L), any(CreateMenuItemRequest.class));

        // Act & Assert
        mockMvc.perform(post("/r/1/menu-item")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Пицца Маргарита"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(menuItemService, times(1)).createMenuItem(eq(1L), any(CreateMenuItemRequest.class));
    }

    // ========== GET /r/{id}/menu-item - список блюд ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetMenuItems_Success() throws Exception {
        // Arrange
        MenuItemListItemResponse item1 = new MenuItemListItemResponse();
        item1.setId(1L);
        item1.setRestaurantId(1L);
        item1.setMenuCategoryId(1L);
        item1.setName("Пицца Маргарита");
        item1.setPrice(new BigDecimal("1000.00"));
        item1.setIsActive(true);
        item1.setCreatedAt(Instant.now());

        List<MenuItemListItemResponse> items = Arrays.asList(item1);
        PaginationResponse.PaginationInfo pagination = new PaginationResponse.PaginationInfo(1L, 50, 0, false);
        PaginationResponse<List<MenuItemListItemResponse>> response = new PaginationResponse<>(items, pagination);

        when(menuItemService.getMenuItems(eq(1L), eq(50), eq(0), isNull(), isNull(), isNull(), eq("displayOrder"), eq("asc")))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/r/1/menu-item")
                        .param("limit", "50")
                        .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].name").value("Пицца Маргарита"))
                .andExpect(jsonPath("$.pagination.total").value(1L));

        verify(menuItemService, times(1)).getMenuItems(eq(1L), eq(50), eq(0), isNull(), isNull(), isNull(), eq("displayOrder"), eq("asc"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetMenuItems_WithFilters() throws Exception {
        // Arrange
        List<MenuItemListItemResponse> items = Arrays.asList();
        PaginationResponse.PaginationInfo pagination = new PaginationResponse.PaginationInfo(0L, 50, 0, false);
        PaginationResponse<List<MenuItemListItemResponse>> response = new PaginationResponse<>(items, pagination);

        when(menuItemService.getMenuItems(eq(1L), eq(50), eq(0), eq(1L), eq(true), eq("пицца"), eq("displayOrder"), eq("asc")))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/r/1/menu-item")
                        .param("limit", "50")
                        .param("offset", "0")
                        .param("menuCategoryId", "1")
                        .param("isActive", "true")
                        .param("search", "пицца"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.pagination.total").value(0L));

        verify(menuItemService, times(1)).getMenuItems(eq(1L), eq(50), eq(0), eq(1L), eq(true), eq("пицца"), eq("displayOrder"), eq("asc"));
    }

    // ========== GET /r/{id}/menu-item/{itemId} - детали блюда ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetMenuItem_Success() throws Exception {
        // Arrange
        MenuItemResponse response = new MenuItemResponse();
        response.setId(1L);
        response.setRestaurantId(1L);
        response.setMenuCategoryId(1L);
        response.setName("Пицца Маргарита");
        response.setPrice(new BigDecimal("1000.00"));
        response.setDescription("Классическая пицца");
        response.setIsActive(true);
        response.setCreatedAt(Instant.now());
        response.setUpdatedAt(Instant.now());

        doReturn(response).when(menuItemService).getMenuItem(1L, 1L);

        // Act & Assert
        mockMvc.perform(get("/r/1/menu-item/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Пицца Маргарита"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(menuItemService, times(1)).getMenuItem(1L, 1L);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetMenuItem_NotFound() throws Exception {
        // Arrange
        when(menuItemService.getMenuItem(1L, 999L))
                .thenThrow(new RuntimeException("MENU_ITEM_NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(get("/r/1/menu-item/999"))
                .andExpect(status().isNotFound());

        verify(menuItemService, times(1)).getMenuItem(1L, 999L);
    }

    // ========== PUT /r/{id}/menu-item/{itemId} - обновление блюда ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testUpdateMenuItem_Success() throws Exception {
        // Arrange
        UpdateMenuItemRequest request = new UpdateMenuItemRequest();
        request.setName("Обновленная пицца");
        request.setPrice(new BigDecimal("1200.00"));

        MenuItemResponse response = new MenuItemResponse();
        response.setId(1L);
        response.setRestaurantId(1L);
        response.setName("Обновленная пицца");
        response.setPrice(new BigDecimal("1200.00"));
        response.setIsActive(true);
        response.setUpdatedAt(Instant.now());

        when(menuItemService.updateMenuItem(eq(1L), eq(1L), any(UpdateMenuItemRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/r/1/menu-item/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Обновленная пицца"));

        verify(menuItemService, times(1)).updateMenuItem(eq(1L), eq(1L), any(UpdateMenuItemRequest.class));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testUpdateMenuItem_NotFound() throws Exception {
        // Arrange
        UpdateMenuItemRequest request = new UpdateMenuItemRequest();
        request.setName("Обновленная пицца");

        when(menuItemService.updateMenuItem(eq(1L), eq(999L), any(UpdateMenuItemRequest.class)))
                .thenThrow(new RuntimeException("MENU_ITEM_NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(put("/r/1/menu-item/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(menuItemService, times(1)).updateMenuItem(eq(1L), eq(999L), any(UpdateMenuItemRequest.class));
    }

    // ========== DELETE /r/{id}/menu-item/{itemId} - удаление блюда ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testDeleteMenuItem_Success() throws Exception {
        // Arrange
        doNothing().when(menuItemService).deleteMenuItem(1L, 1L);

        // Act & Assert
        mockMvc.perform(delete("/r/1/menu-item/1"))
                .andExpect(status().isNoContent());

        verify(menuItemService, times(1)).deleteMenuItem(1L, 1L);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testDeleteMenuItem_NotFound() throws Exception {
        // Arrange
        doThrow(new RuntimeException("MENU_ITEM_NOT_FOUND"))
                .when(menuItemService).deleteMenuItem(1L, 999L);

        // Act & Assert
        mockMvc.perform(delete("/r/1/menu-item/999"))
                .andExpect(status().isNotFound());

        verify(menuItemService, times(1)).deleteMenuItem(1L, 999L);
    }

    // ========== PUT /r/{id}/menu-item/reorder - изменение порядка блюд ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testReorderMenuItems_Success() throws Exception {
        // Arrange
        ReorderMenuItemsRequest request = new ReorderMenuItemsRequest();
        ReorderMenuItemsRequest.MenuItemOrder order1 = new ReorderMenuItemsRequest.MenuItemOrder();
        order1.setId(2L);
        order1.setDisplayOrder(1);
        ReorderMenuItemsRequest.MenuItemOrder order2 = new ReorderMenuItemsRequest.MenuItemOrder();
        order2.setId(1L);
        order2.setDisplayOrder(2);
        ReorderMenuItemsRequest.MenuItemOrder order3 = new ReorderMenuItemsRequest.MenuItemOrder();
        order3.setId(3L);
        order3.setDisplayOrder(3);
        request.setItems(Arrays.asList(order1, order2, order3));

        doNothing().when(menuItemService).reorderMenuItems(eq(1L), any(ReorderMenuItemsRequest.class));

        // Act & Assert
        mockMvc.perform(put("/r/1/menu-item/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Порядок блюд успешно обновлен"));

        verify(menuItemService, times(1)).reorderMenuItems(eq(1L), any(ReorderMenuItemsRequest.class));
    }

    // ========== POST /r/{id}/menu-item/{itemId}/image - загрузка изображения блюда ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testUploadMenuItemImage_Success() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "dish.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        MenuItemResponse response = new MenuItemResponse();
        response.setId(1L);
        response.setRestaurantId(1L);
        response.setMenuCategoryId(1L);
        response.setName("Пицца Маргарита");
        response.setImageId(123L);
        response.setIsActive(true);

        doReturn(response).when(menuItemService).uploadMenuItemImage(eq(1L), eq(1L), any(MultipartFile.class));

        // Act & Assert
        mockMvc.perform(multipart("/r/1/menu-item/1/image")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.imageId").value(123L));

        verify(menuItemService, times(1)).uploadMenuItemImage(eq(1L), eq(1L), any(MultipartFile.class));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testUploadMenuItemImage_NotFound() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "dish.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        doThrow(new RuntimeException("MENU_ITEM_NOT_FOUND"))
                .when(menuItemService).uploadMenuItemImage(eq(1L), eq(999L), any(MultipartFile.class));

        // Act & Assert
        mockMvc.perform(multipart("/r/1/menu-item/999/image")
                        .file(file))
                .andExpect(status().isNotFound());

        verify(menuItemService, times(1)).uploadMenuItemImage(eq(1L), eq(999L), any(MultipartFile.class));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testUploadMenuItemImage_IOException() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "dish.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        // Контроллер ловит IOException и бросает RuntimeException("IMAGE_UPLOAD_ERROR")
        // GlobalExceptionHandler обрабатывает "IMAGE_UPLOAD_ERROR" как BAD_REQUEST (400)
        doThrow(new IOException("IO_ERROR"))
                .when(menuItemService).uploadMenuItemImage(eq(1L), eq(1L), any(MultipartFile.class));

        // Act & Assert
        mockMvc.perform(multipart("/r/1/menu-item/1/image")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exceptionName").value("IMAGE_UPLOAD_ERROR"));

        verify(menuItemService, times(1)).uploadMenuItemImage(eq(1L), eq(1L), any(MultipartFile.class));
    }

    // ========== DELETE /r/{id}/menu-item/{itemId}/image - удаление изображения блюда ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testDeleteMenuItemImage_Success() throws Exception {
        // Arrange
        MenuItemResponse response = new MenuItemResponse();
        response.setId(1L);
        response.setRestaurantId(1L);
        response.setMenuCategoryId(1L);
        response.setName("Пицца Маргарита");
        response.setImageId(null);
        response.setIsActive(true);

        doReturn(response).when(menuItemService).deleteMenuItemImage(1L, 1L);

        // Act & Assert
        mockMvc.perform(delete("/r/1/menu-item/1/image"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.imageId").isEmpty());

        verify(menuItemService, times(1)).deleteMenuItemImage(1L, 1L);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testDeleteMenuItemImage_NotFound() throws Exception {
        // Arrange
        when(menuItemService.deleteMenuItemImage(1L, 999L))
                .thenThrow(new RuntimeException("MENU_ITEM_NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(delete("/r/1/menu-item/999/image"))
                .andExpect(status().isNotFound());

        verify(menuItemService, times(1)).deleteMenuItemImage(1L, 999L);
    }
}

