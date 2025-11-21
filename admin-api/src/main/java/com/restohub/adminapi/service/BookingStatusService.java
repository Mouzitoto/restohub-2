package com.restohub.adminapi.service;

import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.entity.BookingStatus;
import com.restohub.adminapi.repository.BookingRepository;
import com.restohub.adminapi.repository.BookingStatusRepository;
import com.restohub.adminapi.repository.BookingPreOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingStatusService {
    
    private final BookingStatusRepository bookingStatusRepository;
    private final BookingRepository bookingRepository;
    private final BookingPreOrderRepository bookingPreOrderRepository;
    
    @Autowired
    public BookingStatusService(
            BookingStatusRepository bookingStatusRepository,
            BookingRepository bookingRepository,
            BookingPreOrderRepository bookingPreOrderRepository) {
        this.bookingStatusRepository = bookingStatusRepository;
        this.bookingRepository = bookingRepository;
        this.bookingPreOrderRepository = bookingPreOrderRepository;
    }
    
    @Transactional
    public BookingStatusResponse createBookingStatus(CreateBookingStatusRequest request) {
        // Проверка уникальности кода
        String code = request.getCode().trim().toUpperCase();
        if (bookingStatusRepository.findByCodeAndIsActiveTrue(code).isPresent()) {
            throw new RuntimeException("BOOKING_STATUS_CODE_EXISTS");
        }
        
        BookingStatus status = new BookingStatus();
        status.setCode(code);
        status.setName(request.getName().trim());
        status.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        status.setIsActive(true);
        
        status = bookingStatusRepository.save(status);
        
        return toResponse(status);
    }
    
    public PaginationResponse<List<BookingStatusListItemResponse>> getBookingStatuses(
            Integer limit, Integer offset, String sortBy, String sortOrder) {
        
        // Сортировка
        Sort sort = buildSort(sortBy, sortOrder);
        Pageable pageable = PageRequest.of(offset / limit, limit, sort);
        
        Page<BookingStatus> page = bookingStatusRepository.findAll(
                (root, query, cb) -> cb.equal(root.get("isActive"), true),
                pageable
        );
        
        List<BookingStatusListItemResponse> items = page.getContent().stream()
                .map(this::toListItemResponse)
                .collect(Collectors.toList());
        
        PaginationResponse.PaginationInfo pagination = new PaginationResponse.PaginationInfo(
                page.getTotalElements(),
                limit,
                offset,
                (offset + limit) < page.getTotalElements()
        );
        
        return new PaginationResponse<>(items, pagination);
    }
    
    public BookingStatusResponse getBookingStatus(Long statusId) {
        BookingStatus status = bookingStatusRepository.findByIdAndIsActiveTrue(statusId)
                .orElseThrow(() -> new RuntimeException("BOOKING_STATUS_NOT_FOUND"));
        
        return toResponse(status);
    }
    
    @Transactional
    public BookingStatusResponse updateBookingStatus(Long statusId, UpdateBookingStatusDetailsRequest request) {
        BookingStatus status = bookingStatusRepository.findByIdAndIsActiveTrue(statusId)
                .orElseThrow(() -> new RuntimeException("BOOKING_STATUS_NOT_FOUND"));
        
        // Поле code нельзя изменять (неизменяемое после создания)
        
        // Обновление полей (PATCH-логика)
        if (request.getName() != null) {
            status.setName(request.getName().trim());
        }
        if (request.getDisplayOrder() != null) {
            status.setDisplayOrder(request.getDisplayOrder());
        }
        
        status = bookingStatusRepository.save(status);
        
        return toResponse(status);
    }
    
    @Transactional
    public void deleteBookingStatus(Long statusId) {
        BookingStatus status = bookingStatusRepository.findByIdAndIsActiveTrue(statusId)
                .orElseThrow(() -> new RuntimeException("BOOKING_STATUS_NOT_FOUND"));
        
        // Проверка использования статуса
        long bookingsCount = bookingRepository.findAll().stream()
                .filter(booking -> booking.getBookingStatus().getId().equals(statusId))
                .count();
        
        long preOrdersCount = bookingPreOrderRepository.findAll().stream()
                .filter(preOrder -> preOrder.getBooking().getBookingStatus().getId().equals(statusId))
                .count();
        
        if (bookingsCount > 0 || preOrdersCount > 0) {
            throw new RuntimeException("BOOKING_STATUS_IN_USE");
        }
        
        // Мягкое удаление
        status.setIsActive(false);
        status.setUpdatedAt(LocalDateTime.now());
        bookingStatusRepository.save(status);
    }
    
    private BookingStatusResponse toResponse(BookingStatus status) {
        BookingStatusResponse response = new BookingStatusResponse();
        response.setId(status.getId());
        response.setCode(status.getCode());
        response.setName(status.getName());
        response.setDisplayOrder(status.getDisplayOrder());
        response.setIsActive(status.getIsActive());
        response.setCreatedAt(status.getCreatedAt() != null ? status.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        response.setUpdatedAt(status.getUpdatedAt() != null ? status.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        return response;
    }
    
    private BookingStatusListItemResponse toListItemResponse(BookingStatus status) {
        BookingStatusListItemResponse response = new BookingStatusListItemResponse();
        response.setId(status.getId());
        response.setCode(status.getCode());
        response.setName(status.getName());
        response.setDisplayOrder(status.getDisplayOrder());
        response.setIsActive(status.getIsActive());
        response.setCreatedAt(status.getCreatedAt() != null ? status.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        return response;
    }
    
    private Sort buildSort(String sortBy, String sortOrder) {
        String field = sortBy != null ? sortBy : "displayOrder";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        // Маппинг полей
        switch (field) {
            case "code":
                return Sort.by(direction, "code");
            case "name":
                return Sort.by(direction, "name");
            case "displayOrder":
                return Sort.by(direction, "displayOrder");
            case "createdAt":
                return Sort.by(direction, "createdAt");
            default:
                return Sort.by(Sort.Direction.ASC, "displayOrder");
        }
    }
}

