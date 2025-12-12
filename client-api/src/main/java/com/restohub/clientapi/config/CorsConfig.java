package com.restohub.clientapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origin:http://localhost:3000}")
    private String allowedOrigin;

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Используем setAllowedOriginPatterns вместо setAllowedOrigins при allowCredentials(true)
        // Это позволяет использовать wildcards для поддержки разных портов localhost
        List<String> allowedOriginPatterns = new ArrayList<>();
        
        // Добавляем localhost patterns для разработки
        allowedOriginPatterns.add("http://localhost:*");
        allowedOriginPatterns.add("http://127.0.0.1:*");
        
        // Добавляем restohub.local домены для локального развертывания
        allowedOriginPatterns.add("http://*.restohub.local");
        allowedOriginPatterns.add("https://*.restohub.local");
        
        // Добавляем явно указанный allowedOrigin из конфигурации
        if (allowedOrigin != null && !allowedOrigin.isEmpty()) {
            // Если это URL с портом, добавляем как паттерн
            if (allowedOrigin.contains("localhost") || allowedOrigin.contains("127.0.0.1")) {
                // Для localhost используем паттерн с портом
                String baseUrl = allowedOrigin.replaceAll(":\\d+$", "");
                allowedOriginPatterns.add(baseUrl + ":*");
            } else {
                // Для других доменов добавляем как есть
                allowedOriginPatterns.add(allowedOrigin);
            }
        }
        
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(allowedOriginPatterns);
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setMaxAge(3600L);
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}

