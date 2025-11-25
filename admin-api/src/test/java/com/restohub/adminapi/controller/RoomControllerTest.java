package com.restohub.adminapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.service.RoomService;
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
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RoomControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomService roomService;

    @Autowired
    private ObjectMapper objectMapper;

    // ========== POST /r/{id}/room - создание помещения ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testCreateRoom_Success() throws Exception {
        // Arrange
        CreateRoomRequest request = new CreateRoomRequest();
        request.setFloorId(1L);
        request.setName("Зал 1");
        request.setDescription("Основной зал");

        RoomResponse response = new RoomResponse();
        response.setId(1L);
        response.setRestaurantId(1L);
        response.setFloorId(1L);
        response.setName("Зал 1");
        response.setDescription("Основной зал");
        response.setIsActive(true);
        response.setCreatedAt(Instant.now());
        response.setUpdatedAt(Instant.now());

        doReturn(response).when(roomService).createRoom(eq(1L), any(CreateRoomRequest.class));

        // Act & Assert
        mockMvc.perform(post("/r/1/room")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Зал 1"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(roomService, times(1)).createRoom(eq(1L), any(CreateRoomRequest.class));
    }

    // ========== GET /r/{id}/room - список помещений ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetRooms_Success() throws Exception {
        // Arrange
        RoomListItemResponse item1 = new RoomListItemResponse();
        item1.setId(1L);
        item1.setRestaurantId(1L);
        item1.setFloorId(1L);
        item1.setName("Зал 1");
        item1.setIsActive(true);
        item1.setCreatedAt(Instant.now());

        List<RoomListItemResponse> items = Arrays.asList(item1);
        PaginationResponse.PaginationInfo pagination = new PaginationResponse.PaginationInfo(1L, 100, 0, false);
        PaginationResponse<List<RoomListItemResponse>> response = new PaginationResponse<>(items, pagination);

        doReturn(response).when(roomService).getRooms(eq(1L), eq(100), eq(0), isNull(), eq("name"), eq("asc"));

        // Act & Assert
        mockMvc.perform(get("/r/1/room")
                        .param("limit", "100")
                        .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].name").value("Зал 1"))
                .andExpect(jsonPath("$.pagination.total").value(1L));

        verify(roomService, times(1)).getRooms(eq(1L), eq(100), eq(0), isNull(), eq("name"), eq("asc"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetRooms_WithFloorFilter() throws Exception {
        // Arrange
        List<RoomListItemResponse> items = Arrays.asList();
        PaginationResponse.PaginationInfo pagination = new PaginationResponse.PaginationInfo(0L, 100, 0, false);
        PaginationResponse<List<RoomListItemResponse>> response = new PaginationResponse<>(items, pagination);

        doReturn(response).when(roomService).getRooms(eq(1L), eq(100), eq(0), eq(1L), eq("name"), eq("asc"));

        // Act & Assert
        mockMvc.perform(get("/r/1/room")
                        .param("limit", "100")
                        .param("offset", "0")
                        .param("floorId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.pagination.total").value(0L));

        verify(roomService, times(1)).getRooms(eq(1L), eq(100), eq(0), eq(1L), eq("name"), eq("asc"));
    }

    // ========== GET /r/{id}/room/{roomId} - детали помещения ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetRoom_Success() throws Exception {
        // Arrange
        RoomResponse response = new RoomResponse();
        response.setId(1L);
        response.setRestaurantId(1L);
        response.setFloorId(1L);
        response.setName("Зал 1");
        response.setDescription("Основной зал");
        response.setIsActive(true);
        response.setCreatedAt(Instant.now());
        response.setUpdatedAt(Instant.now());

        doReturn(response).when(roomService).getRoom(1L, 1L);

        // Act & Assert
        mockMvc.perform(get("/r/1/room/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Зал 1"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(roomService, times(1)).getRoom(1L, 1L);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetRoom_NotFound() throws Exception {
        // Arrange
        when(roomService.getRoom(1L, 999L))
                .thenThrow(new RuntimeException("ROOM_NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(get("/r/1/room/999"))
                .andExpect(status().isNotFound());

        verify(roomService, times(1)).getRoom(1L, 999L);
    }

    // ========== PUT /r/{id}/room/{roomId} - обновление помещения ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testUpdateRoom_Success() throws Exception {
        // Arrange
        UpdateRoomRequest request = new UpdateRoomRequest();
        request.setName("Обновленный зал");
        request.setDescription("Новое описание");

        RoomResponse response = new RoomResponse();
        response.setId(1L);
        response.setRestaurantId(1L);
        response.setFloorId(1L);
        response.setName("Обновленный зал");
        response.setDescription("Новое описание");
        response.setIsActive(true);
        response.setUpdatedAt(Instant.now());

        doReturn(response).when(roomService).updateRoom(eq(1L), eq(1L), any(UpdateRoomRequest.class));

        // Act & Assert
        mockMvc.perform(put("/r/1/room/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Обновленный зал"));

        verify(roomService, times(1)).updateRoom(eq(1L), eq(1L), any(UpdateRoomRequest.class));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testUpdateRoom_NotFound() throws Exception {
        // Arrange
        UpdateRoomRequest request = new UpdateRoomRequest();
        request.setName("Обновленный зал");

        doThrow(new RuntimeException("ROOM_NOT_FOUND")).when(roomService).updateRoom(eq(1L), eq(999L), any(UpdateRoomRequest.class));

        // Act & Assert
        mockMvc.perform(put("/r/1/room/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(roomService, times(1)).updateRoom(eq(1L), eq(999L), any(UpdateRoomRequest.class));
    }

    // ========== DELETE /r/{id}/room/{roomId} - удаление помещения ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testDeleteRoom_Success() throws Exception {
        // Arrange
        doNothing().when(roomService).deleteRoom(1L, 1L);

        // Act & Assert
        mockMvc.perform(delete("/r/1/room/1"))
                .andExpect(status().isNoContent());

        verify(roomService, times(1)).deleteRoom(1L, 1L);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testDeleteRoom_NotFound() throws Exception {
        // Arrange
        doThrow(new RuntimeException("ROOM_NOT_FOUND"))
                .when(roomService).deleteRoom(1L, 999L);

        // Act & Assert
        mockMvc.perform(delete("/r/1/room/999"))
                .andExpect(status().isNotFound());

        verify(roomService, times(1)).deleteRoom(1L, 999L);
    }

    // ========== POST /r/{id}/room/{roomId}/image - загрузка изображения помещения ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testUploadRoomImage_Success() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "room.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        RoomResponse response = new RoomResponse();
        response.setId(1L);
        response.setRestaurantId(1L);
        response.setFloorId(1L);
        response.setName("Зал 1");
        response.setImageId(123L);
        response.setIsActive(true);

        doReturn(response).when(roomService).uploadRoomImage(eq(1L), eq(1L), any(MultipartFile.class));

        // Act & Assert
        mockMvc.perform(multipart("/r/1/room/1/image")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.imageId").value(123L));

        verify(roomService, times(1)).uploadRoomImage(eq(1L), eq(1L), any(MultipartFile.class));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testUploadRoomImage_NotFound() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "room.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        doThrow(new RuntimeException("ROOM_NOT_FOUND"))
                .when(roomService).uploadRoomImage(eq(1L), eq(999L), any(MultipartFile.class));

        // Act & Assert
        mockMvc.perform(multipart("/r/1/room/999/image")
                        .file(file))
                .andExpect(status().isNotFound());

        verify(roomService, times(1)).uploadRoomImage(eq(1L), eq(999L), any(MultipartFile.class));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testUploadRoomImage_IOException() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "room.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        // Контроллер ловит IOException и бросает RuntimeException("IMAGE_UPLOAD_ERROR")
        // GlobalExceptionHandler обрабатывает "IMAGE_UPLOAD_ERROR" как BAD_REQUEST (400)
        doThrow(new IOException("IO_ERROR"))
                .when(roomService).uploadRoomImage(eq(1L), eq(1L), any(MultipartFile.class));

        // Act & Assert
        mockMvc.perform(multipart("/r/1/room/1/image")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exceptionName").value("IMAGE_UPLOAD_ERROR"));

        verify(roomService, times(1)).uploadRoomImage(eq(1L), eq(1L), any(MultipartFile.class));
    }

    // ========== DELETE /r/{id}/room/{roomId}/image - удаление изображения помещения ==========

    @Test
    @WithMockUser(roles = "MANAGER")
    void testDeleteRoomImage_Success() throws Exception {
        // Arrange
        RoomResponse response = new RoomResponse();
        response.setId(1L);
        response.setRestaurantId(1L);
        response.setFloorId(1L);
        response.setName("Зал 1");
        response.setImageId(null);
        response.setIsActive(true);

        doReturn(response).when(roomService).deleteRoomImage(1L, 1L);

        // Act & Assert
        mockMvc.perform(delete("/r/1/room/1/image"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.imageId").isEmpty());

        verify(roomService, times(1)).deleteRoomImage(1L, 1L);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testDeleteRoomImage_NotFound() throws Exception {
        // Arrange
        doThrow(new RuntimeException("ROOM_NOT_FOUND")).when(roomService).deleteRoomImage(1L, 999L);

        // Act & Assert
        mockMvc.perform(delete("/r/1/room/999/image"))
                .andExpect(status().isNotFound());

        verify(roomService, times(1)).deleteRoomImage(1L, 999L);
    }
}

