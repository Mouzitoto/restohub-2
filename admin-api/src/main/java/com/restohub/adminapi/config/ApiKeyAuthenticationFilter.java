package com.restohub.adminapi.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@ConditionalOnProperty(name = "api.1c.enabled", havingValue = "true", matchIfMissing = true)
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {
    
    @Value("${api.1c.key:change-me-in-production}")
    private String apiKey;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        
        // Применяем фильтр только к /subscriptions/activate
        if (requestPath.contains("/subscriptions/activate")) {
            String providedApiKey = request.getHeader("X-API-Key");
            
            if (providedApiKey == null || !providedApiKey.equals(apiKey)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Invalid or missing API key\"}");
                return;
            }
            
            // Устанавливаем аутентификацию для успешного запроса
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    "1c-system",
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_1C"))
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        
        filterChain.doFilter(request, response);
    }
}

