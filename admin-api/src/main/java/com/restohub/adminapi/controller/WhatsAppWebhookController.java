package com.restohub.adminapi.controller;

import com.restohub.adminapi.dto.ConfirmBookingRequest;
import com.restohub.adminapi.dto.whatsapp.WhatsAppWebhookRequest;
import com.restohub.adminapi.entity.Role;
import com.restohub.adminapi.entity.User;
import com.restohub.adminapi.entity.UserRestaurant;
import com.restohub.adminapi.repository.RoleRepository;
import com.restohub.adminapi.repository.UserRestaurantRepository;
import com.restohub.adminapi.service.WhatsAppNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/whatsapp")
@Slf4j
public class WhatsAppWebhookController {
    
    private static final Pattern BOOKING_PATTERN = Pattern.compile("BOOKING:(\\d+)");
    private static final Pattern PREORDER_PATTERN = Pattern.compile("PREORDER:(\\d+)");
    private static final Pattern APPROVE_BOOKING_PATTERN = Pattern.compile("APPROVE_BOOKING:(\\d+)");
    private static final Pattern REJECT_BOOKING_PATTERN = Pattern.compile("REJECT_BOOKING:(\\d+)");
    private static final Pattern CONTACT_CLIENT_PATTERN = Pattern.compile("CONTACT_CLIENT:(\\d+)");
    
    private final WhatsAppNotificationService whatsAppNotificationService;
    private final RestTemplate restTemplate;
    private final UserRestaurantRepository userRestaurantRepository;
    private final RoleRepository roleRepository;
    
    @Autowired
    public WhatsAppWebhookController(
            WhatsAppNotificationService whatsAppNotificationService,
            RestTemplate restTemplate,
            UserRestaurantRepository userRestaurantRepository,
            RoleRepository roleRepository) {
        this.whatsAppNotificationService = whatsAppNotificationService;
        this.restTemplate = restTemplate;
        this.userRestaurantRepository = userRestaurantRepository;
        this.roleRepository = roleRepository;
    }
    
    /**
     * Верификация webhook для Meta WhatsApp Business API
     * Meta отправляет GET запрос для верификации webhook
     */
    @GetMapping("/webhook")
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {
        
        log.info("Webhook verification request: mode={}, token={}", mode, token);
        
        // Проверяем токен верификации
        String expectedToken = whatsAppNotificationService.getWebhookVerifyToken();
        if ("subscribe".equals(mode) && expectedToken != null && expectedToken.equals(token)) {
            log.info("Webhook verified successfully");
            return ResponseEntity.ok(challenge);
        } else {
            log.warn("Webhook verification failed: invalid token");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
    
    /**
     * Webhook для получения входящих сообщений от WhatsApp API
     */
    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(@RequestBody WhatsAppWebhookRequest request) {
        try {
            log.info("Received WhatsApp webhook");
            
            // Определяем провайдера по структуре запроса
            if (request.getObject() != null && "whatsapp_business_account".equals(request.getObject())) {
                // Meta WhatsApp Business API формат
                handleMetaWebhook(request);
            } else if (request.getTypeWebhook() != null) {
                // Green API формат
                handleGreenApiWebhook(request);
            } else {
                log.warn("Unknown webhook format");
            }
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error handling WhatsApp webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Обработка webhook от Meta WhatsApp Business API
     */
    private void handleMetaWebhook(WhatsAppWebhookRequest request) {
        if (request.getEntry() == null || request.getEntry().isEmpty()) {
            return;
        }
        
        for (WhatsAppWebhookRequest.Entry entry : request.getEntry()) {
            if (entry.getChanges() == null) {
                continue;
            }
            
            for (WhatsAppWebhookRequest.Change change : entry.getChanges()) {
                WhatsAppWebhookRequest.Value value = change.getValue();
                if (value == null) {
                    continue;
                }
                
                // Обработка входящих сообщений
                if (value.getMessages() != null && !value.getMessages().isEmpty()) {
                    for (WhatsAppWebhookRequest.MetaMessage message : value.getMessages()) {
                        handleMetaIncomingMessage(message, value.getContacts());
                    }
                }
                
                // Обработка статусов сообщений
                if (value.getStatuses() != null && !value.getStatuses().isEmpty()) {
                    for (WhatsAppWebhookRequest.Status status : value.getStatuses()) {
                        handleMetaMessageStatus(status);
                    }
                }
            }
        }
    }
    
    /**
     * Обработка входящего сообщения от Meta
     */
    private void handleMetaIncomingMessage(WhatsAppWebhookRequest.MetaMessage message, 
                                          java.util.List<WhatsAppWebhookRequest.Contact> contacts) {
        String phoneNumber = message.getFrom();
        String messageId = message.getId();
        
        // Получаем текст сообщения
        String messageText = null;
        if (message.getText() != null) {
            messageText = message.getText().getBody();
        } else if (message.getInteractive() != null && message.getInteractive().getButtonReply() != null) {
            // Обработка callback от кнопки
            String buttonId = message.getInteractive().getButtonReply().getId();
            handleMetaButtonCallback(buttonId, phoneNumber);
            return;
        }
        
        if (phoneNumber == null || messageText == null) {
            log.warn("Cannot extract phone number or message text from Meta webhook");
            return;
        }
        
        log.info("Processing Meta incoming message from {}: {}", phoneNumber, messageText);
        
        // Проверяем формат BOOKING:<id>
        Matcher bookingMatcher = BOOKING_PATTERN.matcher(messageText);
        if (bookingMatcher.find()) {
            Long bookingId = Long.parseLong(bookingMatcher.group(1));
            handleBookingConfirmation(bookingId, phoneNumber, messageId);
            return;
        }
        
        // Проверяем формат PREORDER:<id>
        Matcher preorderMatcher = PREORDER_PATTERN.matcher(messageText);
        if (preorderMatcher.find()) {
            Long preOrderId = Long.parseLong(preorderMatcher.group(1));
            handlePreOrderConfirmation(preOrderId, phoneNumber, messageId);
            return;
        }
        
        log.info("Message does not match booking or preorder pattern: {}", messageText);
    }
    
    /**
     * Обработка callback от кнопки Meta
     */
    private void handleMetaButtonCallback(String buttonId, String phoneNumber) {
        log.info("Processing Meta button callback from {}: {}", phoneNumber, buttonId);
        
        // Обработка кнопки "Подтвердить"
        Matcher approveMatcher = APPROVE_BOOKING_PATTERN.matcher(buttonId);
        if (approveMatcher.find()) {
            Long bookingId = Long.parseLong(approveMatcher.group(1));
            handleBookingApprove(bookingId, phoneNumber);
            return;
        }
        
        // Обработка кнопки "Отказать"
        Matcher rejectMatcher = REJECT_BOOKING_PATTERN.matcher(buttonId);
        if (rejectMatcher.find()) {
            Long bookingId = Long.parseLong(rejectMatcher.group(1));
            handleBookingReject(bookingId, phoneNumber);
            return;
        }
        
        // Обработка кнопки "Связаться с клиентом"
        Matcher contactMatcher = CONTACT_CLIENT_PATTERN.matcher(buttonId);
        if (contactMatcher.find()) {
            Long bookingId = Long.parseLong(contactMatcher.group(1));
            handleContactClient(bookingId, phoneNumber);
            return;
        }
        
        log.info("Button callback does not match known patterns: {}", buttonId);
    }
    
    /**
     * Обработка статуса сообщения от Meta
     */
    private void handleMetaMessageStatus(WhatsAppWebhookRequest.Status status) {
        log.debug("Meta message status: id={}, status={}", status.getId(), status.getStatus());
        // Можно логировать статусы доставки сообщений
    }
    
    /**
     * Обработка webhook от Green API (обратная совместимость)
     */
    private void handleGreenApiWebhook(WhatsAppWebhookRequest request) {
        log.info("Received Green API webhook: type={}", request.getTypeWebhook());
        
        // Обработка входящего сообщения
        if ("incomingMessageReceived".equals(request.getTypeWebhook()) && request.getIncomingMessage() != null) {
            handleIncomingMessage(request.getIncomingMessage());
        }
        
        // Обработка callback от кнопок
        if ("incomingMessageReceived".equals(request.getTypeWebhook()) && 
            request.getIncomingMessage() != null &&
            request.getIncomingMessage().getButtonMessageData() != null) {
            handleButtonCallback(request.getIncomingMessage());
        }
        
        // Обработка статуса исходящего сообщения
        if ("outgoingMessageStatus".equals(request.getTypeWebhook()) && request.getOutgoingMessageStatus() != null) {
            handleOutgoingMessageStatus(request.getOutgoingMessageStatus());
        }
    }
    
    /**
     * Обработка входящего текстового сообщения
     */
    private void handleIncomingMessage(WhatsAppWebhookRequest.IncomingMessage message) {
        String phoneNumber = extractPhoneNumber(message.getChatId() != null ? message.getChatId() : message.getSenderId());
        String messageText = extractMessageText(message);
        
        if (phoneNumber == null || messageText == null) {
            log.warn("Cannot extract phone number or message text from incoming message");
            return;
        }
        
        log.info("Processing incoming message from {}: {}", phoneNumber, messageText);
        
        // Проверяем формат BOOKING:<id>
        Matcher bookingMatcher = BOOKING_PATTERN.matcher(messageText);
        if (bookingMatcher.find()) {
            Long bookingId = Long.parseLong(bookingMatcher.group(1));
            handleBookingConfirmation(bookingId, phoneNumber, message.getIdMessage());
            return;
        }
        
        // Проверяем формат PREORDER:<id>
        Matcher preorderMatcher = PREORDER_PATTERN.matcher(messageText);
        if (preorderMatcher.find()) {
            Long preOrderId = Long.parseLong(preorderMatcher.group(1));
            handlePreOrderConfirmation(preOrderId, phoneNumber, message.getIdMessage());
            return;
        }
        
        log.info("Message does not match booking or preorder pattern: {}", messageText);
    }
    
    /**
     * Обработка callback от кнопок
     */
    private void handleButtonCallback(WhatsAppWebhookRequest.IncomingMessage message) {
        String phoneNumber = extractPhoneNumber(message.getChatId() != null ? message.getChatId() : message.getSenderId());
        String buttonId = message.getButtonMessageData().getSelectedButtonId();
        
        if (buttonId == null) {
            log.warn("Button callback has no button ID");
            return;
        }
        
        log.info("Processing button callback from {}: {}", phoneNumber, buttonId);
        
        // Обработка кнопки "Подтвердить"
        Matcher approveMatcher = APPROVE_BOOKING_PATTERN.matcher(buttonId);
        if (approveMatcher.find()) {
            Long bookingId = Long.parseLong(approveMatcher.group(1));
            handleBookingApprove(bookingId, phoneNumber);
            return;
        }
        
        // Обработка кнопки "Отказать"
        Matcher rejectMatcher = REJECT_BOOKING_PATTERN.matcher(buttonId);
        if (rejectMatcher.find()) {
            Long bookingId = Long.parseLong(rejectMatcher.group(1));
            handleBookingReject(bookingId, phoneNumber);
            return;
        }
        
        // Обработка кнопки "Связаться с клиентом"
        Matcher contactMatcher = CONTACT_CLIENT_PATTERN.matcher(buttonId);
        if (contactMatcher.find()) {
            Long bookingId = Long.parseLong(contactMatcher.group(1));
            handleContactClient(bookingId, phoneNumber);
            return;
        }
        
        log.info("Button callback does not match known patterns: {}", buttonId);
    }
    
    /**
     * Обработка подтверждения бронирования клиентом
     */
    private void handleBookingConfirmation(Long bookingId, String phoneNumber, String whatsappMessageId) {
        try {
            log.info("Handling booking confirmation: bookingId={}, phone={}", bookingId, phoneNumber);
            
            // Вызываем API подтверждения бронирования в client-api
            String url = "http://client-api:8081/admin-api/booking/" + bookingId + "/confirm";
            
            // Создаем DTO для запроса подтверждения
            ConfirmBookingRequest requestBody = ConfirmBookingRequest.builder()
                    .phone(phoneNumber)
                    .whatsappMessageId(whatsappMessageId)
                    .clientFirstName(null) // Имя клиента не передается в сообщении, можно получить из БД если нужно
                    .build();
            
            try {
                restTemplate.postForEntity(url, requestBody, Object.class);
                log.info("Booking {} confirmed successfully", bookingId);
                
                // Отправляем уведомление менеджерам
                whatsAppNotificationService.sendBookingNotificationToManagers(bookingId);
            } catch (Exception e) {
                log.error("Error confirming booking {}: {}", bookingId, e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error("Error handling booking confirmation: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Обработка подтверждения предзаказа клиентом
     */
    private void handlePreOrderConfirmation(Long preOrderId, String phoneNumber, String whatsappMessageId) {
        // TODO: Реализовать подтверждение предзаказа
        log.info("Pre-order confirmation not yet implemented: preOrderId={}, phone={}", preOrderId, phoneNumber);
    }
    
    /**
     * Обработка подтверждения бронирования менеджером
     * managerPhone - это WhatsApp номер ресторана, откуда пришло сообщение
     */
    private void handleBookingApprove(Long bookingId, String restaurantPhone) {
        try {
            log.info("Handling booking approval: bookingId={}, restaurantPhone={}", bookingId, restaurantPhone);
            
            // Находим ресторан по WhatsApp номеру
            com.restohub.adminapi.entity.Restaurant restaurant = 
                    whatsAppNotificationService.getRestaurantByWhatsAppPhone(restaurantPhone);
            
            if (restaurant == null) {
                log.warn("Restaurant not found by WhatsApp phone: {}", restaurantPhone);
                return;
            }
            
            // Получаем первого активного менеджера ресторана
            // В будущем можно улучшить - определять конкретного менеджера, который ответил
            Long managerId = getFirstManagerIdForRestaurant(restaurant.getId());
            if (managerId == null) {
                log.warn("No active manager found for restaurant: {}", restaurant.getId());
                return;
            }
            
            // Вызываем API изменения статуса на APPROVED
            String url = "http://localhost:8082/admin-api/booking/" + bookingId + "/status";
            java.util.Map<String, Object> requestBody = new java.util.HashMap<>();
            requestBody.put("status", "APPROVED");
            requestBody.put("managerId", managerId);
            
            restTemplate.postForEntity(url, requestBody, Object.class);
            log.info("Booking {} approved successfully", bookingId);
        } catch (Exception e) {
            log.error("Error handling booking approval: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Обработка отклонения бронирования менеджером
     * managerPhone - это WhatsApp номер ресторана, откуда пришло сообщение
     */
    private void handleBookingReject(Long bookingId, String restaurantPhone) {
        try {
            log.info("Handling booking rejection: bookingId={}, restaurantPhone={}", bookingId, restaurantPhone);
            
            // Находим ресторан по WhatsApp номеру
            com.restohub.adminapi.entity.Restaurant restaurant = 
                    whatsAppNotificationService.getRestaurantByWhatsAppPhone(restaurantPhone);
            
            if (restaurant == null) {
                log.warn("Restaurant not found by WhatsApp phone: {}", restaurantPhone);
                return;
            }
            
            // Получаем первого активного менеджера ресторана
            Long managerId = getFirstManagerIdForRestaurant(restaurant.getId());
            if (managerId == null) {
                log.warn("No active manager found for restaurant: {}", restaurant.getId());
                return;
            }
            
            // Вызываем API изменения статуса на REJECTED
            String url = "http://localhost:8082/admin-api/booking/" + bookingId + "/status";
            java.util.Map<String, Object> requestBody = new java.util.HashMap<>();
            requestBody.put("status", "REJECTED");
            requestBody.put("managerId", managerId);
            
            restTemplate.postForEntity(url, requestBody, Object.class);
            log.info("Booking {} rejected successfully", bookingId);
        } catch (Exception e) {
            log.error("Error handling booking rejection: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Получить ID первого активного менеджера ресторана
     */
    private Long getFirstManagerIdForRestaurant(Long restaurantId) {
        Role managerRole = roleRepository.findByCodeAndIsActiveTrue("MANAGER")
                .orElse(null);
        
        if (managerRole == null) {
            return null;
        }
        
        return userRestaurantRepository.findByRestaurantId(restaurantId).stream()
                .map(UserRestaurant::getUser)
                .filter(user -> user.getIsActive() != null && user.getIsActive())
                .filter(user -> user.getRole() != null && user.getRole().getId().equals(managerRole.getId()))
                .map(User::getId)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Обработка запроса на связь с клиентом
     */
    private void handleContactClient(Long bookingId, String managerPhone) {
        try {
            log.info("Handling contact client request: bookingId={}, managerPhone={}", bookingId, managerPhone);
            
            // TODO: Получить managerId по номеру телефона
            Long managerId = getManagerIdByPhone(managerPhone);
            if (managerId == null) {
                log.warn("Manager not found by phone: {}", managerPhone);
                return;
            }
            
            whatsAppNotificationService.sendClientContactLinkToManager(bookingId);
        } catch (Exception e) {
            log.error("Error handling contact client request: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Получить managerId по номеру телефона
     * TODO: Реализовать после добавления поля phone в User
     */
    private Long getManagerIdByPhone(String phone) {
        // TODO: Реализовать поиск менеджера по номеру телефона
        // После добавления поля phone в User entity
        return null;
    }
    
    /**
     * Обработка статуса исходящего сообщения
     */
    private void handleOutgoingMessageStatus(WhatsAppWebhookRequest.OutgoingMessageStatus status) {
        log.debug("Outgoing message status: id={}, status={}", status.getIdMessage(), status.getStatus());
        // Можно логировать статусы доставки сообщений
    }
    
    /**
     * Извлечение номера телефона из chatId или senderId
     */
    private String extractPhoneNumber(String chatIdOrSenderId) {
        if (chatIdOrSenderId == null) {
            return null;
        }
        
        // Формат может быть: "79991234567@c.us" или просто "79991234567"
        String phone = chatIdOrSenderId.replace("@c.us", "").replaceAll("[^0-9]", "");
        return phone.isEmpty() ? null : phone;
    }
    
    /**
     * Извлечение текста сообщения
     */
    private String extractMessageText(WhatsAppWebhookRequest.IncomingMessage message) {
        if (message.getTextMessageData() != null) {
            return message.getTextMessageData().getTextMessage();
        }
        return null;
    }
}

