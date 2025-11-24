package com.restohub.adminapi.controller;

import com.restohub.adminapi.dto.RoleResponse;
import com.restohub.adminapi.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private RoleController roleController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(roleController)
                .setControllerAdvice(new TestExceptionHandler())
                .setValidator(null)
                .build();
    }

    // ========== GET /role - список ролей ==========

    @Test
    void testGetRoles_Success() throws Exception {
        // Arrange
        RoleResponse role1 = new RoleResponse();
        role1.setId(1L);
        role1.setCode("ADMIN");
        role1.setName("Администратор");

        RoleResponse role2 = new RoleResponse();
        role2.setId(2L);
        role2.setCode("MANAGER");
        role2.setName("Менеджер");

        List<RoleResponse> roles = Arrays.asList(role1, role2);

        when(userService.getRoles()).thenReturn(roles);

        // Act & Assert
        mockMvc.perform(get("/role"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].code").value("ADMIN"))
                .andExpect(jsonPath("$[0].name").value("Администратор"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].code").value("MANAGER"))
                .andExpect(jsonPath("$[1].name").value("Менеджер"));

        verify(userService, times(1)).getRoles();
    }
}

