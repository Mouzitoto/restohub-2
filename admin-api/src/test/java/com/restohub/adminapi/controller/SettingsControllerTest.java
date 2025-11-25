package com.restohub.adminapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restohub.adminapi.dto.SettingsResponse;
import com.restohub.adminapi.dto.UpdateSettingsRequest;
import com.restohub.adminapi.service.SettingsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SettingsControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SettingsService settingsService;

    @Autowired
    private ObjectMapper objectMapper;

    // ========== GET /settings - получение настроек ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetSettings_Success() throws Exception {
        // Arrange
        SettingsResponse response = new SettingsResponse();
        response.setSystemName("RestoHub");
        response.setWhatsappNumber("+79991234567");
        response.setWhatsappToken("test-token");

        doReturn(response).when(settingsService).getSettings();

        // Act & Assert
        mockMvc.perform(get("/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.systemName").value("RestoHub"))
                .andExpect(jsonPath("$.whatsappNumber").value("+79991234567"))
                .andExpect(jsonPath("$.whatsappToken").value("test-token"));

        verify(settingsService, times(1)).getSettings();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetSettings_WithDefaultValues() throws Exception {
        // Arrange
        SettingsResponse response = new SettingsResponse();
        response.setSystemName("RestoHub");
        response.setWhatsappNumber("");
        response.setWhatsappToken("");

        doReturn(response).when(settingsService).getSettings();

        // Act & Assert
        mockMvc.perform(get("/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.systemName").value("RestoHub"))
                .andExpect(jsonPath("$.whatsappNumber").value(""))
                .andExpect(jsonPath("$.whatsappToken").value(""));

        verify(settingsService, times(1)).getSettings();
    }

    // ========== PUT /settings - обновление настроек ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateSettings_Success() throws Exception {
        // Arrange
        UpdateSettingsRequest request = new UpdateSettingsRequest();
        request.setSystemName("Updated RestoHub");
        request.setWhatsappNumber("+79991234568");
        request.setWhatsappToken("updated-token");

        SettingsResponse response = new SettingsResponse();
        response.setSystemName("Updated RestoHub");
        response.setWhatsappNumber("+79991234568");
        response.setWhatsappToken("updated-token");

        doReturn(response).when(settingsService).updateSettings(any(UpdateSettingsRequest.class));

        // Act & Assert
        mockMvc.perform(put("/settings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.systemName").value("Updated RestoHub"))
                .andExpect(jsonPath("$.whatsappNumber").value("+79991234568"))
                .andExpect(jsonPath("$.whatsappToken").value("updated-token"));

        verify(settingsService, times(1)).updateSettings(any(UpdateSettingsRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateSettings_PartialUpdate() throws Exception {
        // Arrange
        UpdateSettingsRequest request = new UpdateSettingsRequest();
        request.setSystemName("Partially Updated");

        SettingsResponse response = new SettingsResponse();
        response.setSystemName("Partially Updated");
        response.setWhatsappNumber("");
        response.setWhatsappToken("");

        doReturn(response).when(settingsService).updateSettings(any(UpdateSettingsRequest.class));

        // Act & Assert
        mockMvc.perform(put("/settings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.systemName").value("Partially Updated"));

        verify(settingsService, times(1)).updateSettings(any(UpdateSettingsRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateSettings_OnlyWhatsappNumber() throws Exception {
        // Arrange
        UpdateSettingsRequest request = new UpdateSettingsRequest();
        request.setWhatsappNumber("+79991234569");

        SettingsResponse response = new SettingsResponse();
        response.setSystemName("RestoHub");
        response.setWhatsappNumber("+79991234569");
        response.setWhatsappToken("");

        doReturn(response).when(settingsService).updateSettings(any(UpdateSettingsRequest.class));

        // Act & Assert
        mockMvc.perform(put("/settings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.whatsappNumber").value("+79991234569"))
                .andExpect(jsonPath("$.systemName").value("RestoHub"));

        verify(settingsService, times(1)).updateSettings(any(UpdateSettingsRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateSettings_EmptyRequest() throws Exception {
        // Arrange
        UpdateSettingsRequest request = new UpdateSettingsRequest();

        SettingsResponse response = new SettingsResponse();
        response.setSystemName("RestoHub");
        response.setWhatsappNumber("");
        response.setWhatsappToken("");

        doReturn(response).when(settingsService).updateSettings(any(UpdateSettingsRequest.class));

        // Act & Assert
        mockMvc.perform(put("/settings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(settingsService, times(1)).updateSettings(any(UpdateSettingsRequest.class));
    }
}

