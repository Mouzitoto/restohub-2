package com.restohub.clientapi.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitingConfig {
    
    // Общий лимит: 100 запросов в минуту с одного IP
    @Bean(name = "generalRateLimiter")
    public Map<String, Bucket> generalRateLimiter() {
        return new ConcurrentHashMap<>();
    }
    
    // Лимит для /api/bookings: 10 запросов в минуту с одного IP
    @Bean(name = "bookingsRateLimiter")
    public Map<String, Bucket> bookingsRateLimiter() {
        return new ConcurrentHashMap<>();
    }
    
    public static Bucket createGeneralBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(100)
                        .refillIntervally(100, Duration.ofMinutes(1))
                        .build())
                .build();
    }
    
    public static Bucket createBookingsBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(10)
                        .refillIntervally(10, Duration.ofMinutes(1))
                        .build())
                .build();
    }
}

