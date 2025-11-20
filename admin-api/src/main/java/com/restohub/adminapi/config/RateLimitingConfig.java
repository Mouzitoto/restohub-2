package com.restohub.adminapi.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitingConfig {
    
    // Лимит для /auth/login: 5 запросов в минуту с одного IP
    @Bean(name = "loginRateLimiter")
    public Map<String, Bucket> loginRateLimiter() {
        return new ConcurrentHashMap<>();
    }
    
    // Лимит для /auth/forgot-password: 3 запроса в час с одного IP
    @Bean(name = "forgotPasswordRateLimiter")
    public Map<String, Bucket> forgotPasswordRateLimiter() {
        return new ConcurrentHashMap<>();
    }
    
    public static Bucket createLoginBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(5)
                        .refillIntervally(5, Duration.ofMinutes(1))
                        .build())
                .build();
    }
    
    public static Bucket createForgotPasswordBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(3)
                        .refillIntervally(3, Duration.ofHours(1))
                        .build())
                .build();
    }
}

