package com.restohub.adminapi.config;

import com.restohub.adminapi.util.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    private final JwtTokenProvider jwtTokenProvider;
    
    @Value("${cors.allowed-origin:http://localhost:3001}")
    private String allowedOrigin;
    
    @Value("${cors.client-web-url:http://localhost:3000}")
    private String clientWebUrl;
    
    @Autowired
    private TraceIdConfig traceIdConfig;
    
    @Autowired(required = false)
    private ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;
    
    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login", "/auth/logout", "/auth/refresh", "/auth/forgot-password", "/auth/reset-password").permitAll()
                        .requestMatchers("/auth/register", "/auth/verify-email", "/auth/resend-verification-code", "/auth/terms").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/whatsapp/webhook").permitAll() // Публичный endpoint для WhatsApp webhook
                        .requestMatchers("/booking/{id}/status").permitAll() // Публичный endpoint для изменения статуса (вызывается WhatsApp ботом)
                        .requestMatchers("/subscriptions/activate").hasAnyRole("1C", "ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(traceIdConfig, UsernamePasswordAuthenticationFilter.class);
        
        // Добавляем ApiKeyAuthenticationFilter только если он существует (не в тестах)
        if (apiKeyAuthenticationFilter != null) {
            http.addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        }
        
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider);
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
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
        
        // Добавляем clientWebUrl если он указан
        if (clientWebUrl != null && !clientWebUrl.isEmpty() && !allowedOriginPatterns.contains(clientWebUrl)) {
            if (clientWebUrl.contains("localhost") || clientWebUrl.contains("127.0.0.1")) {
                String baseUrl = clientWebUrl.replaceAll(":\\d+$", "");
                allowedOriginPatterns.add(baseUrl + ":*");
            } else {
                allowedOriginPatterns.add(clientWebUrl);
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

