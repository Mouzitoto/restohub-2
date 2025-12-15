package com.restohub.adminapi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@Slf4j
public class MessageTranslationService {
    
    private final MessageSource messageSource;
    
    public MessageTranslationService() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        this.messageSource = messageSource;
    }
    
    /**
     * Получить переведенное сообщение
     * @param key ключ сообщения
     * @param languageCode код языка (ru, kk, en)
     * @param args аргументы для подстановки в сообщение
     * @return переведенное сообщение
     */
    public String getMessage(String key, String languageCode, Object... args) {
        try {
            Locale locale = getLocale(languageCode);
            return messageSource.getMessage(key, args, locale);
        } catch (Exception e) {
            log.warn("Failed to get message for key: {} with language: {}, using default", key, languageCode, e);
            // Пытаемся получить на русском как fallback
            try {
                return messageSource.getMessage(key, args, Locale.forLanguageTag("ru"));
            } catch (Exception ex) {
                log.error("Failed to get message even with fallback: {}", key, ex);
                return key; // Возвращаем ключ, если не удалось найти перевод
            }
        }
    }
    
    /**
     * Получить Locale по коду языка
     */
    private Locale getLocale(String languageCode) {
        if (languageCode == null || languageCode.trim().isEmpty()) {
            return Locale.forLanguageTag("ru");
        }
        
        return switch (languageCode.toLowerCase()) {
            case "kk", "kz" -> Locale.forLanguageTag("kk");
            case "en" -> Locale.forLanguageTag("en");
            default -> Locale.forLanguageTag("ru");
        };
    }
}

