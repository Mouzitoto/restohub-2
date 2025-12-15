package com.restohub.adminapi.service;

import com.restohub.adminapi.dto.whatsapp.WhatsAppMessageRequest;
import com.restohub.adminapi.entity.*;
import com.restohub.adminapi.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class WhatsAppNotificationService {
    
    private final WhatsAppApiService whatsAppApiService;
    private final MessageTranslationService messageTranslationService;
    private final BookingRepository bookingRepository;
    private final BookingPreOrderRepository bookingPreOrderRepository;
    private final RestaurantRepository restaurantRepository;
    private final com.restohub.adminapi.config.WhatsAppConfig whatsAppConfig;
    
    @Autowired
    public WhatsAppNotificationService(
            WhatsAppApiService whatsAppApiService,
            MessageTranslationService messageTranslationService,
            BookingRepository bookingRepository,
            BookingPreOrderRepository bookingPreOrderRepository,
            RestaurantRepository restaurantRepository,
            com.restohub.adminapi.config.WhatsAppConfig whatsAppConfig) {
        this.whatsAppApiService = whatsAppApiService;
        this.messageTranslationService = messageTranslationService;
        this.bookingRepository = bookingRepository;
        this.bookingPreOrderRepository = bookingPreOrderRepository;
        this.restaurantRepository = restaurantRepository;
        this.whatsAppConfig = whatsAppConfig;
    }
    
    /**
     * Получить токен верификации webhook для Meta API
     */
    public String getWebhookVerifyToken() {
        return whatsAppConfig.getWebhookVerifyToken();
    }
    
    /**
     * Отправить уведомление менеджерам о новом бронировании
     * Отправляется на WhatsApp номер ресторана (restaurants.whatsapp)
     */
    @Transactional(readOnly = true)
    public void sendBookingNotificationToManagers(Long bookingId) {
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));
            
            Restaurant restaurant = booking.getRestaurant();
            String languageCode = restaurant.getManagerLanguageCode();
            
            // Получаем WhatsApp номер ресторана
            String restaurantWhatsApp = restaurant.getWhatsapp();
            if (restaurantWhatsApp == null || restaurantWhatsApp.trim().isEmpty()) {
                log.warn("Restaurant {} has no WhatsApp number, cannot send notification", restaurant.getId());
                return;
            }
            
            // Нормализуем номер (убираем все кроме цифр)
            String phone = restaurantWhatsApp.replaceAll("[^0-9]", "");
            if (phone.isEmpty()) {
                log.warn("Restaurant {} has invalid WhatsApp number: {}", restaurant.getId(), restaurantWhatsApp);
                return;
            }
            
            // Формируем сообщение
            String message = formatBookingMessageForManager(booking, languageCode);
            
            // Формируем кнопки
            List<WhatsAppMessageRequest.Button> buttons = createBookingButtons(bookingId);
            
            // Отправляем уведомление на WhatsApp номер ресторана
            String messageId = whatsAppApiService.sendMessageWithButtons(phone, message, buttons);
            if (messageId != null) {
                log.info("Sent booking notification to restaurant WhatsApp {} (booking: {})", phone, bookingId);
            } else {
                log.error("Failed to send booking notification to restaurant WhatsApp {} (booking: {})", phone, bookingId);
            }
        } catch (Exception e) {
            log.error("Error sending booking notification to managers: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Отправить уведомление клиенту о подтверждении бронирования
     */
    @Transactional(readOnly = true)
    public void sendBookingConfirmationToClient(Long bookingId) {
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));
            
            if (booking.getClient() == null || booking.getClient().getPhone() == null) {
                log.warn("Booking {} has no client or phone number", bookingId);
                return;
            }
            
            Restaurant restaurant = booking.getRestaurant();
            String languageCode = "ru"; // Дефолтный язык для клиентов, можно определить по номеру телефона
            
            String message = formatBookingConfirmationForClient(booking, restaurant, languageCode);
            String phone = booking.getClient().getPhone();
            
            String messageId = whatsAppApiService.sendTextMessage(phone, message);
            if (messageId != null) {
                log.info("Sent booking confirmation to client {} (booking: {})", phone, bookingId);
            } else {
                log.error("Failed to send booking confirmation to client {} (booking: {})", phone, bookingId);
            }
        } catch (Exception e) {
            log.error("Error sending booking confirmation to client: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Отправить уведомление клиенту об отклонении бронирования
     */
    @Transactional(readOnly = true)
    public void sendBookingRejectionToClient(Long bookingId, String reason) {
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));
            
            if (booking.getClient() == null || booking.getClient().getPhone() == null) {
                log.warn("Booking {} has no client or phone number", bookingId);
                return;
            }
            
            Restaurant restaurant = booking.getRestaurant();
            String languageCode = "ru"; // Дефолтный язык для клиентов
            
            String message = formatBookingRejectionForClient(booking, restaurant, languageCode, reason);
            String phone = booking.getClient().getPhone();
            
            String messageId = whatsAppApiService.sendTextMessage(phone, message);
            if (messageId != null) {
                log.info("Sent booking rejection to client {} (booking: {})", phone, bookingId);
            } else {
                log.error("Failed to send booking rejection to client {} (booking: {})", phone, bookingId);
            }
        } catch (Exception e) {
            log.error("Error sending booking rejection to client: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Отправить подтверждение менеджеру об изменении статуса бронирования
     * Отправляется на WhatsApp номер ресторана (restaurants.whatsapp)
     */
    @Transactional(readOnly = true)
    public void sendBookingStatusUpdateToManager(Long bookingId, boolean approved) {
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));
            
            Restaurant restaurant = booking.getRestaurant();
            String languageCode = restaurant.getManagerLanguageCode();
            
            String messageKey = approved ? "manager.booking.approved" : "manager.booking.rejected";
            String message = messageTranslationService.getMessage(messageKey, languageCode, bookingId.toString());
            
            // Получаем WhatsApp номер ресторана
            String restaurantWhatsApp = restaurant.getWhatsapp();
            if (restaurantWhatsApp == null || restaurantWhatsApp.trim().isEmpty()) {
                log.warn("Restaurant {} has no WhatsApp number, cannot send status update", restaurant.getId());
                return;
            }
            
            // Нормализуем номер
            String phone = restaurantWhatsApp.replaceAll("[^0-9]", "");
            if (phone.isEmpty()) {
                log.warn("Restaurant {} has invalid WhatsApp number: {}", restaurant.getId(), restaurantWhatsApp);
                return;
            }
            
            whatsAppApiService.sendTextMessage(phone, message);
            log.info("Sent booking status update to restaurant WhatsApp {} (booking: {})", phone, bookingId);
        } catch (Exception e) {
            log.error("Error sending booking status update to manager: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Отправить ссылку для связи с клиентом менеджеру
     * Отправляется на WhatsApp номер ресторана (restaurants.whatsapp)
     */
    @Transactional(readOnly = true)
    public void sendClientContactLinkToManager(Long bookingId) {
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));
            
            if (booking.getClient() == null || booking.getClient().getPhone() == null) {
                log.warn("Booking {} has no client or phone number", bookingId);
                return;
            }
            
            Restaurant restaurant = booking.getRestaurant();
            String languageCode = restaurant.getManagerLanguageCode();
            
            String clientPhone = booking.getClient().getPhone();
            String link = "https://wa.me/" + clientPhone.replaceAll("[^0-9]", "");
            
            String message = messageTranslationService.getMessage("manager.contact.client.link", languageCode, link);
            
            // Получаем WhatsApp номер ресторана
            String restaurantWhatsApp = restaurant.getWhatsapp();
            if (restaurantWhatsApp == null || restaurantWhatsApp.trim().isEmpty()) {
                log.warn("Restaurant {} has no WhatsApp number, cannot send contact link", restaurant.getId());
                return;
            }
            
            // Нормализуем номер
            String phone = restaurantWhatsApp.replaceAll("[^0-9]", "");
            if (phone.isEmpty()) {
                log.warn("Restaurant {} has invalid WhatsApp number: {}", restaurant.getId(), restaurantWhatsApp);
                return;
            }
            
            whatsAppApiService.sendTextMessage(phone, message);
            log.info("Sent client contact link to restaurant WhatsApp {} (booking: {})", phone, bookingId);
        } catch (Exception e) {
            log.error("Error sending client contact link to manager: {}", e.getMessage(), e);
        }
    }
    
    // Вспомогательные методы
    
    /**
     * Получить ресторан по WhatsApp номеру телефона
     * Используется для определения, от какого ресторана пришло сообщение
     */
    @Transactional(readOnly = true)
    public Restaurant getRestaurantByWhatsAppPhone(String phone) {
        // Нормализуем номер
        String normalizedPhone = phone.replaceAll("[^0-9]", "");
        
        // Ищем ресторан по WhatsApp номеру
        return restaurantRepository.findAll().stream()
                .filter(r -> r.getWhatsapp() != null)
                .filter(r -> r.getWhatsapp().replaceAll("[^0-9]", "").equals(normalizedPhone))
                .findFirst()
                .orElse(null);
    }
    
    private String formatBookingMessageForManager(Booking booking, String languageCode) {
        StringBuilder message = new StringBuilder();
        
        message.append(messageTranslationService.getMessage("manager.booking.new", languageCode, booking.getId().toString()));
        message.append("\n\n");
        
        Restaurant restaurant = booking.getRestaurant();
        message.append(messageTranslationService.getMessage("manager.booking.restaurant", languageCode, restaurant.getName()));
        message.append("\n");
        
        message.append(messageTranslationService.getMessage("manager.booking.table", languageCode, 
                booking.getTable().getTableNumber(), booking.getTable().getCapacity().toString()));
        message.append("\n");
        
        message.append(messageTranslationService.getMessage("manager.booking.date", languageCode, booking.getDate().toString()));
        message.append("\n");
        
        message.append(messageTranslationService.getMessage("manager.booking.time", languageCode, booking.getTime().toString()));
        message.append("\n");
        
        message.append(messageTranslationService.getMessage("manager.booking.persons", languageCode, booking.getPersonCount().toString()));
        message.append("\n");
        
        if (booking.getClient() != null) {
            String clientName = booking.getClientName() != null ? booking.getClientName() : 
                    (booking.getClient().getFirstName() != null ? booking.getClient().getFirstName() : "");
            message.append(messageTranslationService.getMessage("manager.booking.client", languageCode, 
                    clientName, booking.getClient().getPhone()));
            message.append("\n");
        }
        
        if (booking.getSpecialRequests() != null && !booking.getSpecialRequests().trim().isEmpty()) {
            message.append(messageTranslationService.getMessage("manager.booking.special_requests", languageCode, booking.getSpecialRequests()));
            message.append("\n");
        }
        
        // Предзаказ
        List<BookingPreOrder> preOrders = bookingPreOrderRepository.findByBookingId(booking.getId());
        if (!preOrders.isEmpty()) {
            message.append("\n");
            message.append(messageTranslationService.getMessage("manager.booking.preorder", languageCode));
            message.append("\n");
            
            BigDecimal total = BigDecimal.ZERO;
            for (BookingPreOrder preOrder : preOrders) {
                String itemMessage = messageTranslationService.getMessage("manager.booking.preorder.item", languageCode,
                        preOrder.getQuantity().toString(),
                        preOrder.getMenuItem().getName(),
                        preOrder.getTotalPrice().toString());
                message.append(itemMessage);
                message.append("\n");
                total = total.add(preOrder.getTotalPrice());
            }
            
            message.append(messageTranslationService.getMessage("manager.booking.preorder.total", languageCode, total.toString()));
            message.append("\n");
        }
        
        return message.toString();
    }
    
    private List<WhatsAppMessageRequest.Button> createBookingButtons(Long bookingId) {
        List<WhatsAppMessageRequest.Button> buttons = new ArrayList<>();
        
        // Кнопка "Подтвердить"
        WhatsAppMessageRequest.Button approveButton = new WhatsAppMessageRequest.Button();
        approveButton.setId("APPROVE_BOOKING:" + bookingId);
        approveButton.setText(messageTranslationService.getMessage("button.approve", "ru", (Object[]) null));
        buttons.add(approveButton);
        
        // Кнопка "Отказать"
        WhatsAppMessageRequest.Button rejectButton = new WhatsAppMessageRequest.Button();
        rejectButton.setId("REJECT_BOOKING:" + bookingId);
        rejectButton.setText(messageTranslationService.getMessage("button.reject", "ru", (Object[]) null));
        buttons.add(rejectButton);
        
        // Кнопка "Связаться с клиентом"
        WhatsAppMessageRequest.Button contactButton = new WhatsAppMessageRequest.Button();
        contactButton.setId("CONTACT_CLIENT:" + bookingId);
        contactButton.setText(messageTranslationService.getMessage("button.contact_client", "ru", (Object[]) null));
        buttons.add(contactButton);
        
        return buttons;
    }
    
    private String formatBookingConfirmationForClient(Booking booking, Restaurant restaurant, String languageCode) {
        StringBuilder message = new StringBuilder();
        
        message.append(messageTranslationService.getMessage("client.booking.confirmed", languageCode, (Object[]) null));
        message.append("\n\n");
        
        message.append(messageTranslationService.getMessage("client.booking.restaurant", languageCode, restaurant.getName()));
        message.append("\n");
        
        if (restaurant.getAddress() != null) {
            message.append(messageTranslationService.getMessage("client.booking.address", languageCode, restaurant.getAddress()));
            message.append("\n");
        }
        
        message.append(messageTranslationService.getMessage("client.booking.table", languageCode, booking.getTable().getTableNumber()));
        message.append("\n");
        
        message.append(messageTranslationService.getMessage("client.booking.date", languageCode, booking.getDate().toString()));
        message.append("\n");
        
        message.append(messageTranslationService.getMessage("client.booking.time", languageCode, booking.getTime().toString()));
        message.append("\n");
        
        message.append(messageTranslationService.getMessage("client.booking.persons", languageCode, booking.getPersonCount().toString()));
        message.append("\n");
        
        // Предзаказ
        List<BookingPreOrder> preOrders = bookingPreOrderRepository.findByBookingId(booking.getId());
        if (!preOrders.isEmpty()) {
            message.append("\n");
            message.append(messageTranslationService.getMessage("client.booking.preorder", languageCode, (Object[]) null));
            message.append("\n");
            
            BigDecimal total = BigDecimal.ZERO;
            for (BookingPreOrder preOrder : preOrders) {
                String itemMessage = messageTranslationService.getMessage("client.booking.preorder.item", languageCode,
                        preOrder.getQuantity().toString(),
                        preOrder.getMenuItem().getName(),
                        preOrder.getTotalPrice().toString());
                message.append(itemMessage);
                message.append("\n");
                total = total.add(preOrder.getTotalPrice());
            }
            
            message.append(messageTranslationService.getMessage("client.booking.preorder.total", languageCode, total.toString()));
            message.append("\n");
        }
        
        message.append("\n");
        message.append(messageTranslationService.getMessage("client.booking.waiting", languageCode, (Object[]) null));
        
        return message.toString();
    }
    
    private String formatBookingRejectionForClient(Booking booking, Restaurant restaurant, String languageCode, String reason) {
        StringBuilder message = new StringBuilder();
        
        message.append(messageTranslationService.getMessage("client.booking.rejected", languageCode, (Object[]) null));
        message.append("\n\n");
        
        message.append(messageTranslationService.getMessage("client.booking.restaurant", languageCode, restaurant.getName()));
        message.append("\n");
        
        message.append(messageTranslationService.getMessage("client.booking.date", languageCode, booking.getDate().toString()));
        message.append("\n");
        
        message.append(messageTranslationService.getMessage("client.booking.time", languageCode, booking.getTime().toString()));
        message.append("\n");
        
        if (reason != null && !reason.trim().isEmpty()) {
            message.append("\n");
            message.append(messageTranslationService.getMessage("client.booking.rejected.reason", languageCode, reason));
            message.append("\n");
        }
        
        message.append("\n");
        message.append(messageTranslationService.getMessage("client.booking.rejected.new_booking", languageCode, (Object[]) null));
        
        return message.toString();
    }
}

