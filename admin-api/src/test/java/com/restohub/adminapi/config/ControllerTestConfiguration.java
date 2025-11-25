package com.restohub.adminapi.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

/**
 * Общая тестовая конфигурация для всех тестов контроллеров.
 * Содержит общие моки, которые используются во всех тестах.
 * 
 * Важно: Spring кэширует контекст только если все тесты имеют одинаковый набор бинов.
 * Вынося общие моки (например, JwtAuthenticationFilter) в эту конфигурацию,
 * мы помогаем Spring понять, что контекст можно переиспользовать.
 * 
 * Использование: добавьте @Import(ControllerTestConfiguration.class) в тестовые классы.
 */
@TestConfiguration
public class ControllerTestConfiguration {
    
    /**
     * Мок RestaurantAccessInterceptor, чтобы он не блокировал запросы в тестах.
     * Вынесен в общую конфигурацию для оптимизации кэширования контекста.
     * 
     * Примечание: JwtAuthenticationFilter не мокируется, так как он сам проверяет
     * наличие аутентификации через @WithMockUser и пропускает запросы.
     */
    @MockBean
    public RestaurantAccessInterceptor restaurantAccessInterceptor;
    
    @PostConstruct
    public void setupMocks() throws Exception {
        // Настраиваем мок так, чтобы он всегда пропускал запросы
        doReturn(true).when(restaurantAccessInterceptor).preHandle(any(), any(), any());
    }
    
    /**
     * Тестовая конфигурация WebMvcConfig, которая не регистрирует RestaurantAccessInterceptor.
     * Это позволяет тестам работать без проверки доступа к ресторанам.
     */
    @Bean
    @Primary
    public WebMvcConfigurer testWebMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                // Не регистрируем RestaurantAccessInterceptor в тестах
                // Только rateLimitingInterceptor для /auth/login и /auth/forgot-password
            }
        };
    }
}

