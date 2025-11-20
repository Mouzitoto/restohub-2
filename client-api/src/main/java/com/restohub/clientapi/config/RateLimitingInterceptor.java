package com.restohub.clientapi.config;

import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
public class RateLimitingInterceptor implements HandlerInterceptor {
    
    private final Map<String, Bucket> generalRateLimiter;
    private final Map<String, Bucket> bookingsRateLimiter;
    
    public RateLimitingInterceptor(
            @Qualifier("generalRateLimiter") Map<String, Bucket> generalRateLimiter,
            @Qualifier("bookingsRateLimiter") Map<String, Bucket> bookingsRateLimiter) {
        this.generalRateLimiter = generalRateLimiter;
        this.bookingsRateLimiter = bookingsRateLimiter;
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        String clientIp = getClientIp(request);
        
        // Проверяем лимит для /api/bookings
        if (path.startsWith("/api/bookings")) {
            Bucket bucket = bookingsRateLimiter.computeIfAbsent(clientIp, k -> RateLimitingConfig.createBookingsBucket());
            if (!bucket.tryConsume(1)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"exceptionName\":\"TOO_MANY_REQUESTS\",\"message\":\"Слишком много запросов. Попробуйте позже.\"}");
                return false;
            }
        } else {
            // Общий лимит для всех остальных endpoints
            Bucket bucket = generalRateLimiter.computeIfAbsent(clientIp, k -> RateLimitingConfig.createGeneralBucket());
            if (!bucket.tryConsume(1)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"exceptionName\":\"TOO_MANY_REQUESTS\",\"message\":\"Слишком много запросов. Попробуйте позже.\"}");
                return false;
            }
        }
        
        return true;
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}

