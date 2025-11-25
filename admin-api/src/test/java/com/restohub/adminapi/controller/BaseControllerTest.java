package com.restohub.adminapi.controller;

import com.restohub.adminapi.config.ControllerTestConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Базовый класс для всех тестов контроллеров.
 * 
 * Импортирует общую тестовую конфигурацию (ControllerTestConfiguration),
 * которая содержит общие моки (например, JwtAuthenticationFilter).
 * 
 * Это помогает Spring кэшировать контекст между тестами,
 * так как общие моки определены в одном месте.
 * 
 * Использование: наследуйтесь от этого класса в тестах контроллеров.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(ControllerTestConfiguration.class)
public abstract class BaseControllerTest {
    // Базовый класс для всех тестов контроллеров
}

