package com.restohub.clientapi.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CircuitBreakerConfig {
    
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        var config = io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                .failureRateThreshold(50) // Порог ошибок 50%
                .waitDurationInOpenState(Duration.ofSeconds(30)) // Время ожидания перед попыткой восстановления
                .slidingWindowSize(10) // Размер окна для подсчета ошибок
                .minimumNumberOfCalls(5) // Минимальное количество вызовов перед открытием circuit breaker
                .build();
        
        return CircuitBreakerRegistry.of(config);
    }
    
    @Bean
    public CircuitBreaker databaseCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("database");
    }
}

