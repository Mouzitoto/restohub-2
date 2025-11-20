package com.restohub.adminapi.config;

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
    
    private final Map<String, Bucket> loginRateLimiter;
    private final Map<String, Bucket> forgotPasswordRateLimiter;
    
    public RateLimitingInterceptor(
            @Qualifier("loginRateLimiter") Map<String, Bucket> loginRateLimiter,
            @Qualifier("forgotPasswordRateLimiter") Map<String, Bucket> forgotPasswordRateLimiter) {
        this.loginRateLimiter = loginRateLimiter;
        this.forgotPasswordRateLimiter = forgotPasswordRateLimiter;
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        String clientIp = getClientIp(request);
        
        if (path.equals("/auth/login")) {
            Bucket bucket = loginRateLimiter.computeIfAbsent(clientIp, k -> RateLimitingConfig.createLoginBucket());
            if (!bucket.tryConsume(1)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"exceptionName\":\"TOO_MANY_REQUESTS\",\"message\":\"Слишком много запросов. Попробуйте позже.\"}");
                return false;
            }
        } else if (path.equals("/auth/forgot-password")) {
            Bucket bucket = forgotPasswordRateLimiter.computeIfAbsent(clientIp, k -> RateLimitingConfig.createForgotPasswordBucket());
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

