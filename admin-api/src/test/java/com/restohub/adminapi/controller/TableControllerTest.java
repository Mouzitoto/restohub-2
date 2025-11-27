package com.restohub.adminapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.service.TableService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TableControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TableService tableService;

    @Autowired
    private ObjectMapper objectMapper;

    // ========== POST /r/{id}/table - создание стола ==========

    @Test
    @WithMockUser(roles = "MANAGER")
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

        doReturn(response).when(tableService).createTable(eq(1L), any(CreateTableRequest.class));

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
    @WithMockUser(roles = "MANAGER")
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
    @WithMockUser(roles = "MANAGER")
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
    @WithMockUser(roles = "MANAGER")
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

        doReturn(response).when(tableService).getTable(1L, 1L);

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
    @WithMockUser(roles = "MANAGER")
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
    @WithMockUser(roles = "MANAGER")
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
    @WithMockUser(roles = "MANAGER")
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
    @WithMockUser(roles = "MANAGER")
    void testDeleteTable_Success() throws Exception {
        // Arrange
        doNothing().when(tableService).deleteTable(1L, 1L);

        // Act & Assert
        mockMvc.perform(delete("/r/1/table/1"))
                .andExpect(status().isNoContent());

        verify(tableService, times(1)).deleteTable(1L, 1L);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
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
    @WithMockUser(roles = "MANAGER")
    void testGetTableMap_Success() throws Exception {
        // Arrange
        TableMapResponse response = new TableMapResponse();
        response.setFloors(java.util.Collections.emptyList());

        doReturn(response).when(tableService).getTableMap(eq(1L), isNull(), isNull());

        // Act & Assert
        mockMvc.perform(get("/r/1/table/map"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.floors").isArray());

        verify(tableService, times(1)).getTableMap(eq(1L), isNull(), isNull());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetTableMap_WithFilters() throws Exception {
        // Arrange
        TableMapResponse response = new TableMapResponse();
        response.setFloors(java.util.Collections.emptyList());

        doReturn(response).when(tableService).getTableMap(eq(1L), eq(1L), eq(1L));

        // Act & Assert
        mockMvc.perform(get("/r/1/table/map")
                        .param("floorId", "1")
                        .param("roomId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.floors").isArray());

        verify(tableService, times(1)).getTableMap(eq(1L), eq(1L), eq(1L));
    }

    // ========== POST /r/{id}/table/{tableId}/image - загрузка изображения стола ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testUploadTableImage_Success() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "table.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        TableResponse response = new TableResponse();
        response.setId(1L);
        response.setRestaurantId(1L);
        response.setRoomId(1L);
        response.setTableNumber("1");
        response.setCapacity(4);
        response.setImageId(123L);
        response.setIsActive(true);

        doReturn(response).when(tableService).uploadTableImage(eq(1L), eq(1L), any(MultipartFile.class));

        // Act & Assert
        mockMvc.perform(multipart("/r/1/table/1/image")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.imageId").value(123L));

        verify(tableService, times(1)).uploadTableImage(eq(1L), eq(1L), any(MultipartFile.class));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testUploadTableImage_NotFound() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "table.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        doThrow(new RuntimeException("TABLE_NOT_FOUND"))
                .when(tableService).uploadTableImage(eq(1L), eq(999L), any(MultipartFile.class));

        // Act & Assert
        mockMvc.perform(multipart("/r/1/table/999/image")
                        .file(file))
                .andExpect(status().isNotFound());

        verify(tableService, times(1)).uploadTableImage(eq(1L), eq(999L), any(MultipartFile.class));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testUploadTableImage_IOException() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "table.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        // Контроллер ловит IOException и бросает RuntimeException("IMAGE_UPLOAD_ERROR")
        // GlobalExceptionHandler обрабатывает "IMAGE_UPLOAD_ERROR" как BAD_REQUEST (400)
        doThrow(new IOException("IO_ERROR"))
                .when(tableService).uploadTableImage(eq(1L), eq(1L), any(MultipartFile.class));

        // Act & Assert
        mockMvc.perform(multipart("/r/1/table/1/image")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exceptionName").value("IMAGE_UPLOAD_ERROR"));

        verify(tableService, times(1)).uploadTableImage(eq(1L), eq(1L), any(MultipartFile.class));
    }

    // ========== DELETE /r/{id}/table/{tableId}/image - удаление изображения стола ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testDeleteTableImage_Success() throws Exception {
        // Arrange
        TableResponse response = new TableResponse();
        response.setId(1L);
        response.setRestaurantId(1L);
        response.setRoomId(1L);
        response.setTableNumber("1");
        response.setCapacity(4);
        response.setImageId(null);
        response.setIsActive(true);

        doReturn(response).when(tableService).deleteTableImage(1L, 1L);

        // Act & Assert
        mockMvc.perform(delete("/r/1/table/1/image"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.imageId").isEmpty());

        verify(tableService, times(1)).deleteTableImage(1L, 1L);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testDeleteTableImage_NotFound() throws Exception {
        // Arrange
        when(tableService.deleteTableImage(1L, 999L))
                .thenThrow(new RuntimeException("TABLE_NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(delete("/r/1/table/999/image"))
                .andExpect(status().isNotFound());

        verify(tableService, times(1)).deleteTableImage(1L, 999L);
    }

    // ========== DELETE /r/{id}/table/{tableId}/position - удаление координат стола ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testDeleteTablePosition_Success() throws Exception {
        doNothing().when(tableService).clearTablePosition(1L, 1L);

        // Act & Assert
        mockMvc.perform(delete("/r/1/table/1/position"))
                .andExpect(status().isNoContent());

        verify(tableService, times(1)).clearTablePosition(1L, 1L);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testDeleteTablePosition_NotFound() throws Exception {
        doThrow(new RuntimeException("TABLE_NOT_FOUND"))
                .when(tableService).clearTablePosition(1L, 999L);

        // Act & Assert
        mockMvc.perform(delete("/r/1/table/999/position"))
                .andExpect(status().isNotFound());

        verify(tableService, times(1)).clearTablePosition(1L, 999L);
    }
}

