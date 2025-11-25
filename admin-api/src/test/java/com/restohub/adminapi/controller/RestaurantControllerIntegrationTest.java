package com.restohub.adminapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restohub.adminapi.dto.CreateRestaurantRequest;
import com.restohub.adminapi.dto.RestaurantResponse;
import com.restohub.adminapi.service.RestaurantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RestaurantControllerIntegrationTest extends BaseControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private RestaurantService restaurantService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @WithMockUser(roles = "MANAGER")
    void testCreateRestaurant_AsManager_Returns201() throws Exception {
        // Arrange
        CreateRestaurantRequest request = new CreateRestaurantRequest();
        request.setName("Test Restaurant");
        request.setAddress("Test Address");
        request.setPhone("+79991234567");
        
        RestaurantResponse response = new RestaurantResponse();
        response.setId(1L);
        response.setName("Test Restaurant");
        response.setAddress("Test Address");
        response.setPhone("+79991234567");
        response.setIsActive(true);
        
        doReturn(response).when(restaurantService).createRestaurant(any(CreateRestaurantRequest.class));
        
        // Act & Assert
        mockMvc.perform(post("/r")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Restaurant"));

        verify(restaurantService, times(1)).createRestaurant(any(CreateRestaurantRequest.class));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateRestaurant_AsAdmin_WithUserId_Returns201() throws Exception {
        // Arrange
        CreateRestaurantRequest request = new CreateRestaurantRequest();
        request.setName("Test Restaurant");
        request.setAddress("Test Address");
        request.setPhone("+79991234567");
        request.setUserId(10L); // ID менеджера
        
        RestaurantResponse response = new RestaurantResponse();
        response.setId(1L);
        response.setName("Test Restaurant");
        response.setIsActive(true);
        
        doReturn(response).when(restaurantService).createRestaurant(any(CreateRestaurantRequest.class));
        
        // Act & Assert
        mockMvc.perform(post("/r")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));

        verify(restaurantService, times(1)).createRestaurant(any(CreateRestaurantRequest.class));
    }
    
    @Test
    void testCreateRestaurant_WithoutAuthentication_Returns403() throws Exception {
        // Arrange
        CreateRestaurantRequest request = new CreateRestaurantRequest();
        request.setName("Test Restaurant");
        request.setAddress("Test Address");
        request.setPhone("+79991234567");
        
        // Act & Assert
        // Spring Security с @PreAuthorize возвращает 403 для неаутентифицированных запросов
        mockMvc.perform(post("/r")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
        
        verify(restaurantService, never()).createRestaurant(any());
    }
    
    @Test
    @WithMockUser(roles = "MANAGER")
    void testCreateRestaurant_AsManager_WithInvalidUserId_IgnoresUserId() throws Exception {
        // Arrange
        CreateRestaurantRequest request = new CreateRestaurantRequest();
        request.setName("Test Restaurant");
        request.setAddress("Test Address");
        request.setPhone("+79991234567");
        request.setUserId(999L); // Несуществующий userId, должен быть проигнорирован
        
        RestaurantResponse response = new RestaurantResponse();
        response.setId(1L);
        response.setName("Test Restaurant");
        response.setIsActive(true);
        
        doReturn(response).when(restaurantService).createRestaurant(any(CreateRestaurantRequest.class));
        
        // Act & Assert
        mockMvc.perform(post("/r")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        
        verify(restaurantService, times(1)).createRestaurant(any(CreateRestaurantRequest.class));
    }
}

