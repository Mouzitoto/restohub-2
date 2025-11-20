package com.restohub.adminapi.config;

import com.restohub.adminapi.entity.User;
import com.restohub.adminapi.entity.UserRestaurant;
import com.restohub.adminapi.repository.UserRepository;
import com.restohub.adminapi.repository.UserRestaurantRepository;
import com.restohub.adminapi.util.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;
import java.util.Optional;

@Component
public class RestaurantAccessInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(RestaurantAccessInterceptor.class);
    
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final UserRestaurantRepository userRestaurantRepository;
    
    public RestaurantAccessInterceptor(
            JwtTokenProvider jwtTokenProvider,
            UserRepository userRepository,
            UserRestaurantRepository userRestaurantRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.userRestaurantRepository = userRestaurantRepository;
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        
        // Проверяем, что это путь /r/* (короткий путь для ресторанов)
        if (!path.startsWith("/r/")) {
            return true;
        }
        
        // Извлекаем restaurantId из пути
        String[] pathParts = path.split("/");
        if (pathParts.length < 3) {
            return true;
        }
        
        try {
            Long restaurantId = Long.parseLong(pathParts[2]);
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"exceptionName\":\"UNAUTHORIZED\",\"message\":\"Требуется аутентификация\"}");
                return false;
            }
            
            String email = authentication.getName();
            String role = null;
            
            // Получаем роль из authorities
            if (authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
                String authority = authentication.getAuthorities().iterator().next().getAuthority();
                role = authority.replace("ROLE_", "");
            }
            
            // Если роль не найдена в authorities, пытаемся получить из токена
            if (role == null && authentication instanceof org.springframework.security.authentication.UsernamePasswordAuthenticationToken) {
                Object credentials = ((org.springframework.security.authentication.UsernamePasswordAuthenticationToken) authentication).getCredentials();
                if (credentials instanceof String) {
                    String token = (String) credentials;
                    try {
                        role = jwtTokenProvider.getRoleFromToken(token);
                    } catch (Exception e) {
                        logger.error("Failed to get role from token", e);
                    }
                }
            }
            
            // ADMIN имеет доступ ко всем ресторанам
            if ("ADMIN".equals(role)) {
                return true;
            }
            
            // MANAGER проверяем доступ через таблицу users_2_restaurants
            if ("MANAGER".equals(role)) {
                Optional<User> userOpt = userRepository.findByEmailAndIsActiveTrue(email);
                if (userOpt.isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"exceptionName\":\"ACCESS_DENIED\",\"message\":\"Доступ запрещен\"}");
                    return false;
                }
                
                User user = userOpt.get();
                List<UserRestaurant> userRestaurants = userRestaurantRepository.findByUserId(user.getId());
                
                boolean hasAccess = userRestaurants.stream()
                        .anyMatch(ur -> ur.getRestaurant().getId().equals(restaurantId));
                
                if (!hasAccess) {
                    logger.warn("Manager {} attempted to access restaurant {} without permission", email, restaurantId);
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"exceptionName\":\"ACCESS_DENIED\",\"message\":\"Доступ к ресторану запрещен\"}");
                    return false;
                }
            }
            
            return true;
        } catch (NumberFormatException e) {
            // Если не удалось распарсить restaurantId, пропускаем запрос
            return true;
        }
    }
}

