package com.restohub.clientapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Autowired
    private RateLimitingInterceptor rateLimitingInterceptor;
    
    @Autowired
    private RestaurantSubscriptionInterceptor restaurantSubscriptionInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Rate limiting должен выполняться первым
        registry.addInterceptor(rateLimitingInterceptor)
                .addPathPatterns("/**");
        
        // Проверка подписки выполняется после rate limiting, но перед обработкой контроллера
        registry.addInterceptor(restaurantSubscriptionInterceptor)
                .addPathPatterns("/client-api/restaurants/**", "/client-api/r/**");
    }
}

