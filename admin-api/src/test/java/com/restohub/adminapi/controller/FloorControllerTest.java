package com.restohub.adminapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.service.FloorService;
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
class FloorControllerTest {

    @Mock
    private FloorService floorService;

    @InjectMocks
    private FloorController floorController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(floorController)
                .setControllerAdvice(new TestExceptionHandler())
                .setValidator(null)
                .build();
        objectMapper = new ObjectMapper();
    }

    // ========== POST /r/{id}/floor - создание этажа ==========

    @Test
    void testCreateFloor_Success() throws Exception {
        // Arrange
        CreateFloorRequest request = new CreateFloorRequest();
        request.setFloorNumber("1");

        FloorResponse response = new FloorResponse();
        response.setId(1L);
        response.setRestaurantId(1L);
        response.setFloorNumber("1");
        response.setIsActive(true);
        response.setCreatedAt(Instant.now());
        response.setUpdatedAt(Instant.now());

        when(floorService.createFloor(eq(1L), any(CreateFloorRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/r/1/floor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.floorNumber").value(1))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(floorService, times(1)).createFloor(eq(1L), any(CreateFloorRequest.class));
    }

    // ========== GET /r/{id}/floor - список этажей ==========

    @Test
    void testGetFloors_Success() throws Exception {
        // Arrange
        FloorListItemResponse item1 = new FloorListItemResponse();
        item1.setId(1L);
        item1.setRestaurantId(1L);
        item1.setFloorNumber("1");
        item1.setIsActive(true);
        item1.setCreatedAt(Instant.now());

        List<FloorListItemResponse> items = Arrays.asList(item1);
        PaginationResponse.PaginationInfo pagination = new PaginationResponse.PaginationInfo(1L, 100, 0, false);
        PaginationResponse<List<FloorListItemResponse>> response = new PaginationResponse<>(items, pagination);

        when(floorService.getFloors(eq(1L), eq(100), eq(0), eq("floorNumber"), eq("asc")))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/r/1/floor")
                        .param("limit", "100")
                        .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].floorNumber").value("1"))
                .andExpect(jsonPath("$.pagination.total").value(1L));

        verify(floorService, times(1)).getFloors(eq(1L), eq(100), eq(0), eq("floorNumber"), eq("asc"));
    }

    // ========== GET /r/{id}/floor/{floorId} - детали этажа ==========

    @Test
    void testGetFloor_Success() throws Exception {
        // Arrange
        FloorResponse response = new FloorResponse();
        response.setId(1L);
        response.setRestaurantId(1L);
        response.setFloorNumber("1");
        response.setIsActive(true);
        response.setCreatedAt(Instant.now());
        response.setUpdatedAt(Instant.now());

        when(floorService.getFloor(1L, 1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/r/1/floor/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.floorNumber").value("1"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(floorService, times(1)).getFloor(1L, 1L);
    }

    @Test
    void testGetFloor_NotFound() throws Exception {
        // Arrange
        when(floorService.getFloor(1L, 999L))
                .thenThrow(new RuntimeException("FLOOR_NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(get("/r/1/floor/999"))
                .andExpect(status().isNotFound());

        verify(floorService, times(1)).getFloor(1L, 999L);
    }

    // ========== PUT /r/{id}/floor/{floorId} - обновление этажа ==========

    @Test
    void testUpdateFloor_Success() throws Exception {
        // Arrange
        UpdateFloorRequest request = new UpdateFloorRequest();
        request.setFloorNumber("2");

        FloorResponse response = new FloorResponse();
        response.setId(1L);
        response.setRestaurantId(1L);
        response.setFloorNumber("2");
        response.setIsActive(true);
        response.setUpdatedAt(Instant.now());

        when(floorService.updateFloor(eq(1L), eq(1L), any(UpdateFloorRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/r/1/floor/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.floorNumber").value("2"));

        verify(floorService, times(1)).updateFloor(eq(1L), eq(1L), any(UpdateFloorRequest.class));
    }

    @Test
    void testUpdateFloor_NotFound() throws Exception {
        // Arrange
        UpdateFloorRequest request = new UpdateFloorRequest();
        request.setFloorNumber("2");

        when(floorService.updateFloor(eq(1L), eq(999L), any(UpdateFloorRequest.class)))
                .thenThrow(new RuntimeException("FLOOR_NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(put("/r/1/floor/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(floorService, times(1)).updateFloor(eq(1L), eq(999L), any(UpdateFloorRequest.class));
    }

    // ========== DELETE /r/{id}/floor/{floorId} - удаление этажа ==========

    @Test
    void testDeleteFloor_Success() throws Exception {
        // Arrange
        doNothing().when(floorService).deleteFloor(1L, 1L);

        // Act & Assert
        mockMvc.perform(delete("/r/1/floor/1"))
                .andExpect(status().isNoContent());

        verify(floorService, times(1)).deleteFloor(1L, 1L);
    }

    @Test
    void testDeleteFloor_NotFound() throws Exception {
        // Arrange
        doThrow(new RuntimeException("FLOOR_NOT_FOUND"))
                .when(floorService).deleteFloor(1L, 999L);

        // Act & Assert
        mockMvc.perform(delete("/r/1/floor/999"))
                .andExpect(status().isNotFound());

        verify(floorService, times(1)).deleteFloor(1L, 999L);
    }
}

