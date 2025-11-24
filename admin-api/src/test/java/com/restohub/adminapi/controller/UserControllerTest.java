package com.restohub.adminapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.service.UserService;
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
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new TestExceptionHandler())
                .setValidator(null)
                .build();
        objectMapper = new ObjectMapper();
    }

    // ========== POST /user - создание пользователя ==========

    @Test
    void testCreateUser_Success() throws Exception {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setRoleId(2L);

        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setEmail("test@example.com");
        response.setRoleId(2L);
        response.setIsActive(true);
        response.setCreatedAt(Instant.now());
        response.setUpdatedAt(Instant.now());

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(userService, times(1)).createUser(any(CreateUserRequest.class));
    }

    // ========== GET /user - список пользователей ==========

    @Test
    void testGetUsers_Success() throws Exception {
        // Arrange
        UserListItemResponse item1 = new UserListItemResponse();
        item1.setId(1L);
        item1.setEmail("test@example.com");
        item1.setRoleId(2L);
        item1.setIsActive(true);
        item1.setCreatedAt(Instant.now());

        List<UserListItemResponse> items = Arrays.asList(item1);
        PaginationResponse.PaginationInfo pagination = new PaginationResponse.PaginationInfo(1L, 50, 0, false);
        PaginationResponse<List<UserListItemResponse>> response = new PaginationResponse<>(items, pagination);

        when(userService.getUsers(eq(50), eq(0), isNull(), isNull(), isNull(), eq("createdAt"), eq("desc")))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/user")
                        .param("limit", "50")
                        .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].email").value("test@example.com"))
                .andExpect(jsonPath("$.pagination.total").value(1L));

        verify(userService, times(1)).getUsers(eq(50), eq(0), isNull(), isNull(), isNull(), eq("createdAt"), eq("desc"));
    }

    @Test
    void testGetUsers_WithFilters() throws Exception {
        // Arrange
        List<UserListItemResponse> items = Arrays.asList();
        PaginationResponse.PaginationInfo pagination = new PaginationResponse.PaginationInfo(0L, 50, 0, false);
        PaginationResponse<List<UserListItemResponse>> response = new PaginationResponse<>(items, pagination);

        when(userService.getUsers(eq(50), eq(0), eq("test"), eq(2L), eq(true), eq("createdAt"), eq("desc")))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/user")
                        .param("limit", "50")
                        .param("offset", "0")
                        .param("search", "test")
                        .param("roleId", "2")
                        .param("isActive", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.pagination.total").value(0L));

        verify(userService, times(1)).getUsers(eq(50), eq(0), eq("test"), eq(2L), eq(true), eq("createdAt"), eq("desc"));
    }

    // ========== GET /user/{userId} - детали пользователя ==========

    @Test
    void testGetUser_Success() throws Exception {
        // Arrange
        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setEmail("test@example.com");
        response.setRoleId(2L);
        response.setIsActive(true);
        response.setCreatedAt(Instant.now());
        response.setUpdatedAt(Instant.now());

        when(userService.getUser(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(userService, times(1)).getUser(1L);
    }

    @Test
    void testGetUser_NotFound() throws Exception {
        // Arrange
        when(userService.getUser(999L))
                .thenThrow(new RuntimeException("USER_NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(get("/user/999"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUser(999L);
    }

    // ========== PUT /user/{userId} - обновление пользователя ==========

    @Test
    void testUpdateUser_Success() throws Exception {
        // Arrange
        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("updated@example.com");
        request.setRoleId(2L);

        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setEmail("updated@example.com");
        response.setRoleId(2L);
        response.setIsActive(true);
        response.setUpdatedAt(Instant.now());

        when(userService.updateUser(eq(1L), any(UpdateUserRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("updated@example.com"));

        verify(userService, times(1)).updateUser(eq(1L), any(UpdateUserRequest.class));
    }

    @Test
    void testUpdateUser_NotFound() throws Exception {
        // Arrange
        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("updated@example.com");

        when(userService.updateUser(eq(999L), any(UpdateUserRequest.class)))
                .thenThrow(new RuntimeException("USER_NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(put("/user/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).updateUser(eq(999L), any(UpdateUserRequest.class));
    }

    // ========== DELETE /user/{userId} - удаление пользователя ==========

    @Test
    void testDeleteUser_Success() throws Exception {
        // Arrange
        doNothing().when(userService).deleteUser(1L);

        // Act & Assert
        mockMvc.perform(delete("/user/1"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    void testDeleteUser_NotFound() throws Exception {
        // Arrange
        doThrow(new RuntimeException("USER_NOT_FOUND"))
                .when(userService).deleteUser(999L);

        // Act & Assert
        mockMvc.perform(delete("/user/999"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).deleteUser(999L);
    }

    // ========== PUT /user/{userId}/password - сброс пароля ==========

    @Test
    void testResetPassword_Success() throws Exception {
        // Arrange
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setNewPassword("newPassword123");

        doNothing().when(userService).resetPassword(eq(1L), any(ResetPasswordRequest.class));

        // Act & Assert
        mockMvc.perform(put("/user/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Пароль успешно изменен"));

        verify(userService, times(1)).resetPassword(eq(1L), any(ResetPasswordRequest.class));
    }

    @Test
    void testResetPassword_NotFound() throws Exception {
        // Arrange
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setNewPassword("newPassword123");

        doThrow(new RuntimeException("USER_NOT_FOUND"))
                .when(userService).resetPassword(eq(999L), any(ResetPasswordRequest.class));

        // Act & Assert
        mockMvc.perform(put("/user/999/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).resetPassword(eq(999L), any(ResetPasswordRequest.class));
    }

    // ========== PUT /user/{userId}/activate - активация/деактивация ==========

    @Test
    void testActivateUser_Success() throws Exception {
        // Arrange
        doNothing().when(userService).activateUser(1L, true);

        // Act & Assert
        mockMvc.perform(put("/user/1/activate")
                        .param("isActive", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Пользователь активирован"));

        verify(userService, times(1)).activateUser(1L, true);
    }

    @Test
    void testDeactivateUser_Success() throws Exception {
        // Arrange
        doNothing().when(userService).activateUser(1L, false);

        // Act & Assert
        mockMvc.perform(put("/user/1/activate")
                        .param("isActive", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Пользователь деактивирован"));

        verify(userService, times(1)).activateUser(1L, false);
    }

    @Test
    void testActivateUser_NotFound() throws Exception {
        // Arrange
        doThrow(new RuntimeException("USER_NOT_FOUND"))
                .when(userService).activateUser(999L, true);

        // Act & Assert
        mockMvc.perform(put("/user/999/activate")
                        .param("isActive", "true"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).activateUser(999L, true);
    }
}

