package com.restohub.adminapi.dto.whatsapp;

import lombok.Data;

import java.util.List;

@Data
public class WhatsAppMessageRequest {
    
    private String chatId; // номер телефона получателя
    private String message; // текст сообщения
    private List<Button> buttons; // интерактивные кнопки (если поддерживается)
    
    @Data
    public static class Button {
        private String id; // уникальный ID кнопки для callback
        private String text; // текст кнопки
    }
}

