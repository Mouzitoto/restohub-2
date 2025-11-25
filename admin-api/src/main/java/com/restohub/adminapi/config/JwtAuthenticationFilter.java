package com.restohub.adminapi.config;

import com.restohub.adminapi.util.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    
    // Публичные эндпоинты, для которых не нужно проверять токен
    private static final String[] PUBLIC_ENDPOINTS = {
        "/auth/login",
        "/auth/refresh",
        "/auth/forgot-password",
        "/auth/reset-password",
        "/auth/register",
        "/auth/verify-email",
        "/auth/resend-verification-code",
        "/auth/terms",
        "/actuator/health",
        "/actuator/info"
    };
    
    private final JwtTokenProvider jwtTokenProvider;
    
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    private boolean isPublicEndpoint(String requestURI) {
        for (String endpoint : PUBLIC_ENDPOINTS) {
            if (requestURI.startsWith(endpoint)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        
        // Пропускаем публичные эндпоинты без проверки токена
        if (isPublicEndpoint(requestURI)) {
            logger.debug("Skipping JWT authentication for public endpoint: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        
        // Если аутентификация уже установлена (например, через @WithMockUser в тестах), пропускаем
        if (SecurityContextHolder.getContext().getAuthentication() != null 
                && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            logger.debug("Authentication already exists, skipping JWT filter for: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        
        // Если токена нет, но аутентификация уже установлена (например, через @WithMockUser), пропускаем
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            // Проверяем, есть ли уже аутентификация (например, установленная через @WithMockUser)
            if (SecurityContextHolder.getContext().getAuthentication() != null 
                    && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
                logger.debug("No JWT token but authentication exists, skipping JWT filter for: {}", requestURI);
                filterChain.doFilter(request, response);
                return;
            }
            // Если токена нет и аутентификации нет, просто продолжаем цепочку фильтров
            filterChain.doFilter(request, response);
            return;
        }
        
        // Обрабатываем JWT токен
        String token = authHeader.substring(BEARER_PREFIX.length());
        
        try {
            // Проверяем, истек ли токен
            if (jwtTokenProvider.isTokenExpired(token)) {
                logger.debug("Token expired for request: {}", request.getRequestURI());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                String errorJson = "{\"exceptionName\":\"TOKEN_EXPIRED\",\"message\":\"Токен истек\",\"timestamp\":\"" + 
                        java.time.Instant.now().toString() + "\",\"traceId\":\"" + 
                        org.slf4j.MDC.get("traceId") + "\"}";
                response.getWriter().write(errorJson);
                return;
            }
            
            if (jwtTokenProvider.validateToken(token)) {
                String email = jwtTokenProvider.getEmailFromToken(token);
                String role = jwtTokenProvider.getRoleFromToken(token);
                
                if (role == null || role.isEmpty()) {
                    logger.warn("Role is null or empty for user: {}", email);
                    // Не очищаем контекст, если аутентификация уже установлена
                    if (SecurityContextHolder.getContext().getAuthentication() == null) {
                        SecurityContextHolder.clearContext();
                    }
                    filterChain.doFilter(request, response);
                    return;
                }
                
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        email,
                        token,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("Authenticated user: {} with role: {}", email, role);
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            logger.debug("Token expired: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            String errorJson = "{\"exceptionName\":\"TOKEN_EXPIRED\",\"message\":\"Токен истек\",\"timestamp\":\"" + 
                    java.time.Instant.now().toString() + "\",\"traceId\":\"" + 
                    org.slf4j.MDC.get("traceId") + "\"}";
            response.getWriter().write(errorJson);
            return;
        } catch (Exception e) {
            logger.error("JWT authentication failed", e);
            // Не очищаем контекст, если аутентификация уже установлена (например, в тестах)
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                SecurityContextHolder.clearContext();
            }
        }
        
        filterChain.doFilter(request, response);
    }
}

