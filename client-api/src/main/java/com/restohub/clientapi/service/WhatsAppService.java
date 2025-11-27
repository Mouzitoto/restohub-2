package com.restohub.clientapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class WhatsAppService {
    
    @Value("${whatsapp.bot.phone:79991234567}")
    private String whatsappBotPhone;
    
    public String generateBookingUrl(Long bookingId) {
        String message = "BOOKING:" + bookingId;
        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
        return "https://wa.me/" + whatsappBotPhone + "?text=" + encodedMessage;
    }
    
    public String getBookingMessage(Long bookingId) {
        return "BOOKING:" + bookingId;
    }
}

