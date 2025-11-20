package com.restohub.adminapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Autowired
    private RestaurantAccessInterceptor restaurantAccessInterceptor;
    
    @Autowired
    private RateLimitingInterceptor rateLimitingInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitingInterceptor)
                .addPathPatterns("/auth/login", "/auth/forgot-password");
        registry.addInterceptor(restaurantAccessInterceptor)
                .addPathPatterns("/r/**");
    }
}

