package com.restohub.adminapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.service.MenuCategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MenuCategoryControllerTest {

    @Mock
    private MenuCategoryService menuCategoryService;

    @InjectMocks
    private MenuCategoryController menuCategoryController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Создаем фиктивный валидатор, который ничего не делает
        org.springframework.validation.Validator noOpValidator = new org.springframework.validation.Validator() {
            @Override
            public boolean supports(Class<?> clazz) {
                return false; // Не поддерживаем никакие классы, чтобы валидация не вызывалась
            }
            
            @Override
            public void validate(Object target, org.springframework.validation.Errors errors) {
                // Ничего не делаем - валидация отключена
            }
        };
        
        mockMvc = MockMvcBuilders.standaloneSetup(menuCategoryController)
                .setControllerAdvice(new TestExceptionHandler())
                .setValidator(noOpValidator)
                .build();
        objectMapper = new ObjectMapper();
    }

    // ========== POST /menu-category - создание категории ==========

    @Test
    void testCreateMenuCategory_Success() throws Exception {
        // Arrange
        CreateMenuCategoryRequest request = new CreateMenuCategoryRequest();
        request.setName("Пицца");
        request.setDescription("Категория пицц");
        request.setDisplayOrder(1);

        MenuCategoryResponse response = new MenuCategoryResponse();
        response.setId(1L);
        response.setName("Пицца");
        response.setDescription("Категория пицц");
        response.setDisplayOrder(1);
        response.setIsActive(true);
        response.setCreatedAt(Instant.now());
        response.setUpdatedAt(Instant.now());

        when(menuCategoryService.createMenuCategory(any(CreateMenuCategoryRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/menu-category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Пицца"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(menuCategoryService, times(1)).createMenuCategory(any(CreateMenuCategoryRequest.class));
    }

    // ========== GET /menu-category - список категорий ==========

    @Test
    void testGetMenuCategories_Success() throws Exception {
        // Arrange
        MenuCategoryListItemResponse item1 = new MenuCategoryListItemResponse();
        item1.setId(1L);
        item1.setName("Пицца");
        item1.setDescription("Категория пицц");
        item1.setDisplayOrder(1);
        item1.setIsActive(true);
        item1.setCreatedAt(Instant.now());

        List<MenuCategoryListItemResponse> items = Arrays.asList(item1);
        PaginationResponse.PaginationInfo pagination = new PaginationResponse.PaginationInfo(1L, 100, 0, false);
        PaginationResponse<List<MenuCategoryListItemResponse>> response = new PaginationResponse<>(items, pagination);

        when(menuCategoryService.getMenuCategories(eq(100), eq(0), eq("displayOrder"), eq("asc")))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/menu-category")
                        .param("limit", "100")
                        .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].name").value("Пицца"))
                .andExpect(jsonPath("$.pagination.total").value(1L));

        verify(menuCategoryService, times(1)).getMenuCategories(eq(100), eq(0), eq("displayOrder"), eq("asc"));
    }

    // ========== GET /menu-category/{categoryId} - детали категории ==========

    @Test
    void testGetMenuCategory_Success() throws Exception {
        // Arrange
        MenuCategoryResponse response = new MenuCategoryResponse();
        response.setId(1L);
        response.setName("Пицца");
        response.setDescription("Категория пицц");
        response.setDisplayOrder(1);
        response.setIsActive(true);
        response.setCreatedAt(Instant.now());
        response.setUpdatedAt(Instant.now());

        when(menuCategoryService.getMenuCategory(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/menu-category/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Пицца"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(menuCategoryService, times(1)).getMenuCategory(1L);
    }

    @Test
    void testGetMenuCategory_NotFound() throws Exception {
        // Arrange
        when(menuCategoryService.getMenuCategory(999L))
                .thenThrow(new RuntimeException("MENU_CATEGORY_NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(get("/menu-category/999"))
                .andExpect(status().isNotFound());

        verify(menuCategoryService, times(1)).getMenuCategory(999L);
    }

    // ========== PUT /menu-category/{categoryId} - обновление категории ==========

    @Test
    void testUpdateMenuCategory_Success() throws Exception {
        // Arrange
        UpdateMenuCategoryRequest request = new UpdateMenuCategoryRequest();
        request.setName("Обновленная категория");
        request.setDescription("Новое описание");

        MenuCategoryResponse response = new MenuCategoryResponse();
        response.setId(1L);
        response.setName("Обновленная категория");
        response.setDescription("Новое описание");
        response.setIsActive(true);
        response.setUpdatedAt(Instant.now());

        when(menuCategoryService.updateMenuCategory(eq(1L), any(UpdateMenuCategoryRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/menu-category/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Обновленная категория"));

        verify(menuCategoryService, times(1)).updateMenuCategory(eq(1L), any(UpdateMenuCategoryRequest.class));
    }

    @Test
    void testUpdateMenuCategory_NotFound() throws Exception {
        // Arrange
        UpdateMenuCategoryRequest request = new UpdateMenuCategoryRequest();
        request.setName("Обновленная категория");

        when(menuCategoryService.updateMenuCategory(eq(999L), any(UpdateMenuCategoryRequest.class)))
                .thenThrow(new RuntimeException("MENU_CATEGORY_NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(put("/menu-category/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(menuCategoryService, times(1)).updateMenuCategory(eq(999L), any(UpdateMenuCategoryRequest.class));
    }

    // ========== DELETE /menu-category/{categoryId} - удаление категории ==========

    @Test
    void testDeleteMenuCategory_Success() throws Exception {
        // Arrange
        doNothing().when(menuCategoryService).deleteMenuCategory(1L);

        // Act & Assert
        mockMvc.perform(delete("/menu-category/1"))
                .andExpect(status().isNoContent());

        verify(menuCategoryService, times(1)).deleteMenuCategory(1L);
    }

    @Test
    void testDeleteMenuCategory_NotFound() throws Exception {
        // Arrange
        doThrow(new RuntimeException("MENU_CATEGORY_NOT_FOUND"))
                .when(menuCategoryService).deleteMenuCategory(999L);

        // Act & Assert
        mockMvc.perform(delete("/menu-category/999"))
                .andExpect(status().isNotFound());

        verify(menuCategoryService, times(1)).deleteMenuCategory(999L);
    }

    // ========== PUT /menu-category/reorder - изменение порядка категорий ==========

    @Test
    void testReorderMenuCategories_Success() throws Exception {
        // Arrange
        ReorderMenuCategoriesRequest request = new ReorderMenuCategoriesRequest();
        request.setCategoryIds(Arrays.asList(2L, 1L, 3L));

        doNothing().when(menuCategoryService).reorderMenuCategories(any(ReorderMenuCategoriesRequest.class));

        // Act & Assert
        mockMvc.perform(put("/menu-category/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Порядок категорий успешно обновлен"));

        verify(menuCategoryService, times(1)).reorderMenuCategories(any(ReorderMenuCategoriesRequest.class));
    }
}

