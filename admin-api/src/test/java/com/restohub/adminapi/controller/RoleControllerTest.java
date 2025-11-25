package com.restohub.adminapi.controller;

import com.restohub.adminapi.dto.RoleResponse;
import com.restohub.adminapi.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RoleControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    // ========== GET /role - список ролей ==========

    @Test
    @WithMockUser(roles = "ADMIN")
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

        doReturn(roles).when(userService).getRoles();

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

