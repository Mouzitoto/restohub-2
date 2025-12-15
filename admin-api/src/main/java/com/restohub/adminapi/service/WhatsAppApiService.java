package com.restohub.adminapi.service;

import com.restohub.adminapi.config.WhatsAppConfig;
import com.restohub.adminapi.dto.whatsapp.WhatsAppMessageRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class WhatsAppApiService {
    
    private final WebClient whatsAppWebClient;
    private final WhatsAppConfig whatsAppConfig;
    
    @Autowired
    public WhatsAppApiService(WebClient whatsAppWebClient, WhatsAppConfig whatsAppConfig) {
        this.whatsAppWebClient = whatsAppWebClient;
        this.whatsAppConfig = whatsAppConfig;
    }
    
    /**
     * Отправка текстового сообщения через WhatsApp API
     * @param phoneNumber номер телефона получателя (формат: 79991234567)
     * @param message текст сообщения
     * @return ID отправленного сообщения или null в случае ошибки
     */
    public String sendTextMessage(String phoneNumber, String message) {
        try {
            log.info("Sending WhatsApp message to {}: {}", phoneNumber, message);
            
            if ("meta".equals(whatsAppConfig.getProvider())) {
                return sendViaMeta(phoneNumber, message);
            } else if ("green-api".equals(whatsAppConfig.getProvider())) {
                return sendViaGreenApi(phoneNumber, message);
            } else if ("twilio".equals(whatsAppConfig.getProvider())) {
                return sendViaTwilio(phoneNumber, message);
            } else {
                log.error("Unsupported WhatsApp provider: {}", whatsAppConfig.getProvider());
                return null;
            }
        } catch (Exception e) {
            log.error("Error sending WhatsApp message to {}: {}", phoneNumber, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Отправка сообщения с кнопками через WhatsApp API
     * @param phoneNumber номер телефона получателя
     * @param message текст сообщения
     * @param buttons список кнопок
     * @return ID отправленного сообщения или null в случае ошибки
     */
    public String sendMessageWithButtons(String phoneNumber, String message, java.util.List<WhatsAppMessageRequest.Button> buttons) {
        try {
            log.info("Sending WhatsApp message with buttons to {}: {}", phoneNumber, message);
            
            if ("meta".equals(whatsAppConfig.getProvider())) {
                return sendViaMetaWithButtons(phoneNumber, message, buttons);
            } else if ("green-api".equals(whatsAppConfig.getProvider())) {
                return sendViaGreenApiWithButtons(phoneNumber, message, buttons);
            } else if ("twilio".equals(whatsAppConfig.getProvider())) {
                return sendViaTwilioWithButtons(phoneNumber, message, buttons);
            } else {
                log.error("Unsupported WhatsApp provider: {}", whatsAppConfig.getProvider());
                return null;
            }
        } catch (Exception e) {
            log.error("Error sending WhatsApp message with buttons to {}: {}", phoneNumber, e.getMessage(), e);
            return null;
        }
    }
    
    private String sendViaGreenApi(String phoneNumber, String message) {
        // Green API формат: POST /waInstance{idInstance}/sendMessage/{apiTokenInstance}
        // Или через универсальный endpoint
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("chatId", phoneNumber + "@c.us");
        requestBody.put("message", message);
        
        try {
            String url = String.format("/waInstance%s/sendMessage/%s", 
                    whatsAppConfig.getApiKey(), whatsAppConfig.getApiSecret());
            
            Map<String, Object> response = whatsAppWebClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            if (response != null && response.containsKey("idMessage")) {
                return (String) response.get("idMessage");
            }
            return null;
        } catch (WebClientResponseException e) {
            log.error("Green API error: {}", e.getResponseBodyAsString(), e);
            return null;
        }
    }
    
    private String sendViaGreenApiWithButtons(String phoneNumber, String message, 
                                               java.util.List<WhatsAppMessageRequest.Button> buttons) {
        // Green API поддерживает кнопки через специальный формат
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("chatId", phoneNumber + "@c.us");
        requestBody.put("message", message);
        
        // Формируем кнопки для Green API
        if (buttons != null && !buttons.isEmpty()) {
            java.util.List<Map<String, String>> buttonsList = buttons.stream()
                    .map(btn -> {
                        Map<String, String> btnMap = new HashMap<>();
                        btnMap.put("id", btn.getId());
                        btnMap.put("text", btn.getText());
                        return btnMap;
                    })
                    .toList();
            requestBody.put("buttons", buttonsList);
        }
        
        try {
            String url = String.format("/waInstance%s/sendMessage/%s", 
                    whatsAppConfig.getApiKey(), whatsAppConfig.getApiSecret());
            
            Map<String, Object> response = whatsAppWebClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            if (response != null && response.containsKey("idMessage")) {
                return (String) response.get("idMessage");
            }
            return null;
        } catch (WebClientResponseException e) {
            log.error("Green API error with buttons: {}", e.getResponseBodyAsString(), e);
            return null;
        }
    }
    
    private String sendViaTwilio(String phoneNumber, String message) {
        // Twilio формат
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("To", "whatsapp:" + phoneNumber);
        requestBody.put("From", "whatsapp:" + whatsAppConfig.getBotPhone());
        requestBody.put("Body", message);
        
        try {
            Map<String, Object> response = whatsAppWebClient.post()
                    .uri("/2010-04-01/Accounts/" + whatsAppConfig.getApiKey() + "/Messages.json")
                    .headers(headers -> headers.setBasicAuth(whatsAppConfig.getApiKey(), whatsAppConfig.getApiSecret()))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            if (response != null && response.containsKey("sid")) {
                return (String) response.get("sid");
            }
            return null;
        } catch (WebClientResponseException e) {
            log.error("Twilio error: {}", e.getResponseBodyAsString(), e);
            return null;
        }
    }
    
    private String sendViaTwilioWithButtons(String phoneNumber, String message, 
                                             java.util.List<WhatsAppMessageRequest.Button> buttons) {
        // Twilio поддерживает кнопки через специальный формат
        // Реализация зависит от версии Twilio API
        return sendViaTwilio(phoneNumber, message);
    }
    
    /**
     * Отправка текстового сообщения через WhatsApp Business API (Meta)
     */
    private String sendViaMeta(String phoneNumber, String message) {
        // Meta WhatsApp Business API использует Graph API
        // Формат: POST /{phone-number-id}/messages
        
        // Нормализуем номер телефона (добавляем код страны, убираем +)
        String normalizedPhone = normalizePhoneForMeta(phoneNumber);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("messaging_product", "whatsapp");
        requestBody.put("to", normalizedPhone);
        requestBody.put("type", "text");
        
        Map<String, String> textContent = new HashMap<>();
        textContent.put("body", message);
        requestBody.put("text", textContent);
        
        try {
            String url = String.format("/%s/messages", whatsAppConfig.getPhoneNumberId());
            
            Map<String, Object> response = whatsAppWebClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + whatsAppConfig.getAccessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            if (response != null && response.containsKey("messages")) {
                @SuppressWarnings("unchecked")
                java.util.List<Map<String, Object>> messages = (java.util.List<Map<String, Object>>) response.get("messages");
                if (!messages.isEmpty() && messages.get(0).containsKey("id")) {
                    return (String) messages.get(0).get("id");
                }
            }
            return null;
        } catch (WebClientResponseException e) {
            log.error("Meta WhatsApp API error: {}", e.getResponseBodyAsString(), e);
            return null;
        }
    }
    
    /**
     * Отправка сообщения с кнопками через WhatsApp Business API (Meta)
     */
    private String sendViaMetaWithButtons(String phoneNumber, String message, 
                                         java.util.List<WhatsAppMessageRequest.Button> buttons) {
        // Meta WhatsApp Business API поддерживает интерактивные кнопки
        // Формат: POST /{phone-number-id}/messages с типом "interactive"
        
        String normalizedPhone = normalizePhoneForMeta(phoneNumber);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("messaging_product", "whatsapp");
        requestBody.put("to", normalizedPhone);
        requestBody.put("type", "interactive");
        
        // Формируем интерактивное сообщение
        Map<String, Object> interactive = new HashMap<>();
        interactive.put("type", "button");
        
        // Тело сообщения
        Map<String, Object> body = new HashMap<>();
        body.put("text", message);
        interactive.put("body", body);
        
        // Кнопки (максимум 3 кнопки)
        if (buttons != null && !buttons.isEmpty()) {
            java.util.List<Map<String, Object>> buttonsList = buttons.stream()
                    .limit(3) // Meta поддерживает максимум 3 кнопки
                    .map(btn -> {
                        Map<String, Object> btnMap = new HashMap<>();
                        btnMap.put("type", "reply");
                        Map<String, String> reply = new HashMap<>();
                        reply.put("id", btn.getId());
                        reply.put("title", btn.getText());
                        btnMap.put("reply", reply);
                        return btnMap;
                    })
                    .toList();
            
            Map<String, Object> action = new HashMap<>();
            action.put("buttons", buttonsList);
            interactive.put("action", action);
        }
        
        requestBody.put("interactive", interactive);
        
        try {
            String url = String.format("/%s/messages", whatsAppConfig.getPhoneNumberId());
            
            Map<String, Object> response = whatsAppWebClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + whatsAppConfig.getAccessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            if (response != null && response.containsKey("messages")) {
                @SuppressWarnings("unchecked")
                java.util.List<Map<String, Object>> messages = (java.util.List<Map<String, Object>>) response.get("messages");
                if (!messages.isEmpty() && messages.get(0).containsKey("id")) {
                    return (String) messages.get(0).get("id");
                }
            }
            return null;
        } catch (WebClientResponseException e) {
            log.error("Meta WhatsApp API error with buttons: {}", e.getResponseBodyAsString(), e);
            return null;
        }
    }
    
    /**
     * Нормализация номера телефона для Meta API
     * Формат: код страны + номер (например: 79991234567 -> 79991234567)
     */
    private String normalizePhoneForMeta(String phoneNumber) {
        // Убираем все кроме цифр
        String normalized = phoneNumber.replaceAll("[^0-9]", "");
        
        // Если номер начинается с 8, заменяем на 7 (для России/Казахстана)
        if (normalized.startsWith("8") && normalized.length() == 11) {
            normalized = "7" + normalized.substring(1);
        }
        
        return normalized;
    }
}

