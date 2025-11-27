package com.restohub.clientapi.service;

import com.restohub.clientapi.dto.ConfirmBookingResponse;
import com.restohub.clientapi.dto.BookingStatusResponse;
import com.restohub.clientapi.entity.*;
import com.restohub.clientapi.repository.*;
import com.restohub.clientapi.validation.PhoneValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class BookingConfirmationService {
    
    private final BookingRepository bookingRepository;
    private final BookingStatusRepository bookingStatusRepository;
    private final BookingHistoryRepository bookingHistoryRepository;
    private final ClientRepository clientRepository;
    
    @Autowired
    public BookingConfirmationService(
            BookingRepository bookingRepository,
            BookingStatusRepository bookingStatusRepository,
            BookingHistoryRepository bookingHistoryRepository,
            ClientRepository clientRepository) {
        this.bookingRepository = bookingRepository;
        this.bookingStatusRepository = bookingStatusRepository;
        this.bookingHistoryRepository = bookingHistoryRepository;
        this.clientRepository = clientRepository;
    }
    
    @Transactional
    public ConfirmBookingResponse confirmBooking(Long bookingId, String phone, String clientFirstName, String whatsappMessageId) {
        // Нормализуем телефон
        String normalizedPhone = PhoneValidator.normalizePhone(phone);
        
        // Находим бронирование в статусе DRAFT
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        if (booking.getBookingStatus() == null || !booking.getBookingStatus().getCode().equals("DRAFT")) {
            throw new RuntimeException("Booking is not in DRAFT status");
        }
        
        // Работа с клиентом
        Client client = clientRepository.findByPhone(normalizedPhone).orElse(null);
        
        if (client == null) {
            // Создаем нового клиента
            client = new Client();
            client.setPhone(normalizedPhone);
            client.setFirstName(clientFirstName);
            client.setFirstBookingDate(LocalDateTime.now());
            client.setLastBookingDate(LocalDateTime.now());
            client.setTotalBookings(1);
            client.setTotalPreOrders(0);
            client.setCreatedAt(LocalDateTime.now());
            client.setUpdatedAt(LocalDateTime.now());
            client = clientRepository.save(client);
        } else {
            // Обновляем существующего клиента
            if (clientFirstName != null && !clientFirstName.trim().isEmpty()) {
                client.setFirstName(clientFirstName);
            }
            client.setLastBookingDate(LocalDateTime.now());
            client.setTotalBookings(client.getTotalBookings() + 1);
            client.setUpdatedAt(LocalDateTime.now());
            client = clientRepository.save(client);
        }
        
        // Обновляем бронирование
        BookingStatus pendingStatus = bookingStatusRepository.findByCodeAndIsActiveTrue("PENDING")
                .orElseThrow(() -> new RuntimeException("PENDING status not found"));
        
        booking.setClient(client);
        booking.setBookingStatus(pendingStatus);
        booking.setWhatsappMessageId(whatsappMessageId);
        booking.setUpdatedAt(LocalDateTime.now());
        booking = bookingRepository.save(booking);
        
        // Создаем запись в истории
        BookingHistory history = new BookingHistory();
        history.setBooking(booking);
        history.setBookingStatus(pendingStatus);
        history.setChangedAt(LocalDateTime.now());
        history.setChangedBy(null);
        history.setComment(null);
        history.setCreatedAt(LocalDateTime.now());
        bookingHistoryRepository.save(history);
        
        // TODO: Отправка уведомления менеджерам через WhatsApp бот
        // Получаем менеджеров ресторана
        // Отправляем уведомление каждому менеджеру
        
        return ConfirmBookingResponse.builder()
                .id(booking.getId())
                .restaurantId(booking.getRestaurant().getId())
                .tableId(booking.getTable().getId())
                .date(booking.getDate())
                .time(booking.getTime())
                .personCount(booking.getPersonCount())
                .clientId(booking.getClient().getId())
                .clientName(booking.getClientName())
                .specialRequests(booking.getSpecialRequests())
                .status(BookingStatusResponse.builder()
                        .code(booking.getBookingStatus().getCode())
                        .name(booking.getBookingStatus().getName())
                        .build())
                .whatsappNotificationSent(true) // TODO: установить реальное значение после отправки
                .createdAt(booking.getCreatedAt())
                .build();
    }
}

