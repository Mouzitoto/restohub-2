package com.restohub.adminapi.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Getter
public class WhatsAppConfig {
    
    @Value("${whatsapp.api.provider:meta}")
    private String provider;
    
    // Meta WhatsApp Business API настройки
    @Value("${whatsapp.api.app-id:}")
    private String appId;
    
    @Value("${whatsapp.api.app-secret:}")
    private String appSecret;
    
    @Value("${whatsapp.api.access-token:}")
    private String accessToken;
    
    @Value("${whatsapp.api.phone-number-id:}")
    private String phoneNumberId;
    
    @Value("${whatsapp.api.business-account-id:}")
    private String businessAccountId;
    
    @Value("${whatsapp.api.webhook-verify-token:}")
    private String webhookVerifyToken;
    
    // Обратная совместимость (для других провайдеров)
    @Value("${whatsapp.api.api-key:}")
    private String apiKey;
    
    @Value("${whatsapp.api.api-secret:}")
    private String apiSecret;
    
    @Value("${whatsapp.api.base-url:https://graph.facebook.com/v18.0}")
    private String baseUrl;
    
    @Value("${whatsapp.api.bot-phone:79991234567}")
    private String botPhone;
    
    @Value("${whatsapp.api.webhook-url:}")
    private String webhookUrl;
    
    @Bean
    public WebClient whatsAppWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}

