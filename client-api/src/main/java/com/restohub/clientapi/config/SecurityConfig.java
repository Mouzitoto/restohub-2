package com.restohub.clientapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Value("${cors.allowed-origin:http://localhost:3000}")
    private String allowedOrigin;
    
    @Autowired
    private TraceIdConfig traceIdConfig;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .addFilterBefore(traceIdConfig, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
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
        
        configuration.setAllowedOriginPatterns(allowedOriginPatterns);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

