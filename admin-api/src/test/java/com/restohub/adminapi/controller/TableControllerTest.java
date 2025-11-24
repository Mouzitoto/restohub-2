package com.restohub.adminapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.service.TableService;
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
class TableControllerTest {

    @Mock
    private TableService tableService;

    @InjectMocks
    private TableController tableController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(tableController)
                .setControllerAdvice(new TestExceptionHandler())
                .setValidator(null)
                .build();
        objectMapper = new ObjectMapper();
    }

    // ========== POST /r/{id}/table - создание стола ==========

    @Test
    void testCreateTable_Success() throws Exception {
        // Arrange
        CreateTableRequest request = new CreateTableRequest();
        request.setRoomId(1L);
        request.setTableNumber("1");
        request.setCapacity(4);

        TableResponse response = new TableResponse();
        response.setId(1L);
        response.setRestaurantId(1L);
        response.setRoomId(1L);
        response.setTableNumber("1");
        response.setCapacity(4);
        response.setIsActive(true);
        response.setCreatedAt(Instant.now());
        response.setUpdatedAt(Instant.now());

        when(tableService.createTable(eq(1L), any(CreateTableRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/r/1/table")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.tableNumber").value("1"))
                .andExpect(jsonPath("$.capacity").value(4))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(tableService, times(1)).createTable(eq(1L), any(CreateTableRequest.class));
    }

    // ========== GET /r/{id}/table - список столов ==========

    @Test
    void testGetTables_Success() throws Exception {
        // Arrange
        TableListItemResponse item1 = new TableListItemResponse();
        item1.setId(1L);
        item1.setRestaurantId(1L);
        item1.setRoomId(1L);
        item1.setTableNumber("1");
        item1.setCapacity(4);
        item1.setIsActive(true);
        item1.setCreatedAt(Instant.now());

        List<TableListItemResponse> items = Arrays.asList(item1);
        PaginationResponse.PaginationInfo pagination = new PaginationResponse.PaginationInfo(1L, 50, 0, false);
        PaginationResponse<List<TableListItemResponse>> response = new PaginationResponse<>(items, pagination);

        when(tableService.getTables(eq(1L), eq(50), eq(0), isNull(), isNull(), isNull(), isNull(), eq("tableNumber"), eq("asc")))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/r/1/table")
                        .param("limit", "50")
                        .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].tableNumber").value("1"))
                .andExpect(jsonPath("$.pagination.total").value(1L));

        verify(tableService, times(1)).getTables(eq(1L), eq(50), eq(0), isNull(), isNull(), isNull(), isNull(), eq("tableNumber"), eq("asc"));
    }

    @Test
    void testGetTables_WithFilters() throws Exception {
        // Arrange
        List<TableListItemResponse> items = Arrays.asList();
        PaginationResponse.PaginationInfo pagination = new PaginationResponse.PaginationInfo(0L, 50, 0, false);
        PaginationResponse<List<TableListItemResponse>> response = new PaginationResponse<>(items, pagination);

        when(tableService.getTables(eq(1L), eq(50), eq(0), eq(1L), eq(1L), eq(2), eq(6), eq("tableNumber"), eq("asc")))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/r/1/table")
                        .param("limit", "50")
                        .param("offset", "0")
                        .param("roomId", "1")
                        .param("floorId", "1")
                        .param("minCapacity", "2")
                        .param("maxCapacity", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.pagination.total").value(0L));

        verify(tableService, times(1)).getTables(eq(1L), eq(50), eq(0), eq(1L), eq(1L), eq(2), eq(6), eq("tableNumber"), eq("asc"));
    }

    // ========== GET /r/{id}/table/{tableId} - детали стола ==========

    @Test
    void testGetTable_Success() throws Exception {
        // Arrange
        TableResponse response = new TableResponse();
        response.setId(1L);
        response.setRestaurantId(1L);
        response.setRoomId(1L);
        response.setTableNumber("1");
        response.setCapacity(4);
        response.setIsActive(true);
        response.setCreatedAt(Instant.now());
        response.setUpdatedAt(Instant.now());

        when(tableService.getTable(1L, 1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/r/1/table/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.tableNumber").value("1"))
                .andExpect(jsonPath("$.capacity").value(4))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(tableService, times(1)).getTable(1L, 1L);
    }

    @Test
    void testGetTable_NotFound() throws Exception {
        // Arrange
        when(tableService.getTable(1L, 999L))
                .thenThrow(new RuntimeException("TABLE_NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(get("/r/1/table/999"))
                .andExpect(status().isNotFound());

        verify(tableService, times(1)).getTable(1L, 999L);
    }

    // ========== PUT /r/{id}/table/{tableId} - обновление стола ==========

    @Test
    void testUpdateTable_Success() throws Exception {
        // Arrange
        UpdateTableRequest request = new UpdateTableRequest();
        request.setTableNumber("2");
        request.setCapacity(6);

        TableResponse response = new TableResponse();
        response.setId(1L);
        response.setRestaurantId(1L);
        response.setRoomId(1L);
        response.setTableNumber("2");
        response.setCapacity(6);
        response.setIsActive(true);
        response.setUpdatedAt(Instant.now());

        when(tableService.updateTable(eq(1L), eq(1L), any(UpdateTableRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/r/1/table/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.tableNumber").value("2"))
                .andExpect(jsonPath("$.capacity").value(6));

        verify(tableService, times(1)).updateTable(eq(1L), eq(1L), any(UpdateTableRequest.class));
    }

    @Test
    void testUpdateTable_NotFound() throws Exception {
        // Arrange
        UpdateTableRequest request = new UpdateTableRequest();
        request.setTableNumber("2");

        when(tableService.updateTable(eq(1L), eq(999L), any(UpdateTableRequest.class)))
                .thenThrow(new RuntimeException("TABLE_NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(put("/r/1/table/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(tableService, times(1)).updateTable(eq(1L), eq(999L), any(UpdateTableRequest.class));
    }

    // ========== DELETE /r/{id}/table/{tableId} - удаление стола ==========

    @Test
    void testDeleteTable_Success() throws Exception {
        // Arrange
        doNothing().when(tableService).deleteTable(1L, 1L);

        // Act & Assert
        mockMvc.perform(delete("/r/1/table/1"))
                .andExpect(status().isNoContent());

        verify(tableService, times(1)).deleteTable(1L, 1L);
    }

    @Test
    void testDeleteTable_NotFound() throws Exception {
        // Arrange
        doThrow(new RuntimeException("TABLE_NOT_FOUND"))
                .when(tableService).deleteTable(1L, 999L);

        // Act & Assert
        mockMvc.perform(delete("/r/1/table/999"))
                .andExpect(status().isNotFound());

        verify(tableService, times(1)).deleteTable(1L, 999L);
    }

    // ========== GET /r/{id}/table/map - карта столов ==========

    @Test
    void testGetTableMap_Success() throws Exception {
        // Arrange
        TableMapResponse response = new TableMapResponse();
        response.setFloors(java.util.Collections.emptyList());

        when(tableService.getTableMap(eq(1L), isNull(), isNull())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/r/1/table/map"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.floors").isArray());

        verify(tableService, times(1)).getTableMap(eq(1L), isNull(), isNull());
    }

    @Test
    void testGetTableMap_WithFilters() throws Exception {
        // Arrange
        TableMapResponse response = new TableMapResponse();
        response.setFloors(java.util.Collections.emptyList());

        when(tableService.getTableMap(eq(1L), eq(1L), eq(1L))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/r/1/table/map")
                        .param("floorId", "1")
                        .param("roomId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.floors").isArray());

        verify(tableService, times(1)).getTableMap(eq(1L), eq(1L), eq(1L));
    }
}

