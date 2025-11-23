package com.restohub.clientapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restohub.clientapi.service.SubscriptionCheckService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RestaurantSubscriptionInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(RestaurantSubscriptionInterceptor.class);
    
    private final SubscriptionCheckService subscriptionCheckService;
    private final ObjectMapper objectMapper;
    
    // Паттерны для извлечения restaurantId из пути
    private static final Pattern RESTAURANTS_PATTERN = Pattern.compile("^/client-api/restaurants/(\\d+)(?:/.*)?$");
    private static final Pattern R_PATTERN = Pattern.compile("^/client-api/r/(\\d+)(?:/.*)?$");
    
    @Autowired
    public RestaurantSubscriptionInterceptor(SubscriptionCheckService subscriptionCheckService) {
        this.subscriptionCheckService = subscriptionCheckService;
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        
        // Пропускаем список ресторанов (фильтрация будет в контроллере)
        if (path.equals("/client-api/restaurants") || path.equals("/client-api/r")) {
            return true;
        }
        
        // Проверяем, относится ли путь к ресторанам
        Long restaurantId = extractRestaurantId(path);
        
        if (restaurantId == null) {
            // Путь не относится к ресторанам, пропускаем
            return true;
        }
        
        // Проверяем активную подписку
        boolean hasActiveSubscription = subscriptionCheckService.hasActiveSubscription(restaurantId);
        
        if (!hasActiveSubscription) {
            logger.debug("Restaurant {} does not have active subscription, returning 404", restaurantId);
            
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("exceptionName", "RESTAURANT_NOT_FOUND");
            errorResponse.put("message", "Ресторан не найден или неактивен");
            
            String jsonResponse = objectMapper.writeValueAsString(errorResponse);
            response.getWriter().write(jsonResponse);
            
            return false;
        }
        
        return true;
    }
    
    /**
     * Извлекает restaurantId из пути запроса.
     * Поддерживает паттерны:
     * - /client-api/restaurants/{id}
     * - /client-api/restaurants/{id}/*
     * - /client-api/r/{id}
     * - /client-api/r/{id}/*
     * 
     * @param path путь запроса
     * @return restaurantId или null, если путь не соответствует паттернам
     */
    private Long extractRestaurantId(String path) {
        // Проверяем паттерн /client-api/restaurants/{id}
        Matcher restaurantsMatcher = RESTAURANTS_PATTERN.matcher(path);
        if (restaurantsMatcher.matches()) {
            try {
                return Long.parseLong(restaurantsMatcher.group(1));
            } catch (NumberFormatException e) {
                logger.warn("Failed to parse restaurantId from path: {}", path);
                return null;
            }
        }
        
        // Проверяем паттерн /client-api/r/{id}
        Matcher rMatcher = R_PATTERN.matcher(path);
        if (rMatcher.matches()) {
            try {
                return Long.parseLong(rMatcher.group(1));
            } catch (NumberFormatException e) {
                logger.warn("Failed to parse restaurantId from path: {}", path);
                return null;
            }
        }
        
        return null;
    }
}

