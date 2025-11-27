package com.restohub.adminapi.service;

import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.entity.Image;
import com.restohub.adminapi.entity.Restaurant;
import com.restohub.adminapi.entity.User;
import com.restohub.adminapi.entity.UserRestaurant;
import com.restohub.adminapi.repository.ImageRepository;
import com.restohub.adminapi.repository.RestaurantRepository;
import com.restohub.adminapi.repository.RestaurantSubscriptionRepository;
import com.restohub.adminapi.repository.UserRepository;
import com.restohub.adminapi.repository.UserRestaurantRepository;
import com.restohub.adminapi.validation.InstagramValidator;
import com.restohub.adminapi.validation.PhoneValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RestaurantService {
    
    private static final Logger logger = LoggerFactory.getLogger(RestaurantService.class);
    
    private final RestaurantRepository restaurantRepository;
    private final ImageRepository imageRepository;
    private final UserRestaurantRepository userRestaurantRepository;
    private final RestaurantSubscriptionRepository restaurantSubscriptionRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final SubscriptionService subscriptionService;
    
    @Autowired
    public RestaurantService(
            RestaurantRepository restaurantRepository,
            ImageRepository imageRepository,
            UserRestaurantRepository userRestaurantRepository,
            RestaurantSubscriptionRepository restaurantSubscriptionRepository,
            UserRepository userRepository,
            ImageService imageService,
            SubscriptionService subscriptionService) {
        this.restaurantRepository = restaurantRepository;
        this.imageRepository = imageRepository;
        this.userRestaurantRepository = userRestaurantRepository;
        this.restaurantSubscriptionRepository = restaurantSubscriptionRepository;
        this.userRepository = userRepository;
        this.imageService = imageService;
        this.subscriptionService = subscriptionService;
    }
    
    @Transactional
    public RestaurantResponse createRestaurant(CreateRestaurantRequest request) {
        // Нормализация телефонов
        String phone = PhoneValidator.normalize(request.getPhone());
        String whatsapp = request.getWhatsapp() != null ? PhoneValidator.normalize(request.getWhatsapp()) : null;
        String instagram = request.getInstagram() != null ? InstagramValidator.normalize(request.getInstagram()) : null;
        
        // Проверка изображений
        Image logoImage = null;
        if (request.getLogoImageId() != null) {
            logoImage = imageRepository.findByIdAndIsActiveTrue(request.getLogoImageId())
                    .orElseThrow(() -> new RuntimeException("IMAGE_NOT_FOUND"));
        }
        
        Image bgImage = null;
        if (request.getBgImageId() != null) {
            bgImage = imageRepository.findByIdAndIsActiveTrue(request.getBgImageId())
                    .orElseThrow(() -> new RuntimeException("IMAGE_NOT_FOUND"));
        }
        
        Restaurant restaurant = new Restaurant();
        restaurant.setName(request.getName());
        restaurant.setAddress(request.getAddress());
        restaurant.setPhone(phone);
        restaurant.setWhatsapp(whatsapp);
        restaurant.setInstagram(instagram);
        restaurant.setDescription(request.getDescription());
        restaurant.setLatitude(request.getLatitude());
        restaurant.setLongitude(request.getLongitude());
        restaurant.setWorkingHours(request.getWorkingHours());
        
        // Устанавливаем код языка менеджера (по умолчанию "ru")
        String managerLanguageCode = request.getManagerLanguageCode();
        if (managerLanguageCode == null || managerLanguageCode.trim().isEmpty()) {
            managerLanguageCode = "ru";
        } else {
            // Валидация формата: только если поле не пустое
            if (!managerLanguageCode.matches("^[a-z]{2}$")) {
                throw new RuntimeException("INVALID_MANAGER_LANGUAGE_CODE");
            }
        }
        restaurant.setManagerLanguageCode(managerLanguageCode);
        
        restaurant.setLogoImage(logoImage);
        restaurant.setBgImage(bgImage);
        restaurant.setIsActive(false);
        
        restaurant = restaurantRepository.save(restaurant);
        
        // Привязка ресторана к пользователю
        linkRestaurantToUser(restaurant, request.getUserId());
        
        return toResponse(restaurant);
    }
    
    private void linkRestaurantToUser(Restaurant restaurant, Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return;
        }
        
        // Получаем роль пользователя
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        User userToLink;
        
        if (isAdmin) {
            // ADMIN может указать userId для привязки ресторана к менеджеру
            if (userId != null) {
                userToLink = userRepository.findByIdAndIsActiveTrue(userId)
                        .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));
            } else {
                // Если userId не указан, не привязываем (как было раньше)
                return;
            }
        } else {
            // MANAGER автоматически привязывает ресторан к себе
            // Игнорируем userId, если он указан
            String email = authentication.getName();
            userToLink = userRepository.findByEmailAndIsActiveTrue(email)
                    .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));
        }
        
        // Проверяем, не существует ли уже такая связь
        boolean linkExists = userRestaurantRepository.findByRestaurantId(restaurant.getId()).stream()
                .anyMatch(ur -> ur.getUser().getId().equals(userToLink.getId()));
        
        if (!linkExists) {
            UserRestaurant userRestaurant = new UserRestaurant();
            userRestaurant.setUser(userToLink);
            userRestaurant.setRestaurant(restaurant);
            userRestaurant.setCreatedAt(LocalDateTime.now());
            userRestaurantRepository.save(userRestaurant);
        }
    }
    
    public PaginationResponse<List<RestaurantListItemResponse>> getRestaurants(
            Integer limit, Integer offset, String search, Boolean isActive, String sortBy, String sortOrder) {
        
        // Определяем роль пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        // Построение спецификации
        Specification<Restaurant> spec = Specification.where(null);
        
        // Для MANAGER фильтруем только его рестораны
        if (!isAdmin) {
            // Получаем список ID ресторанов менеджера
            List<Long> restaurantIds = userRestaurantRepository.findByUserEmail(email).stream()
                    .map(ur -> ur.getRestaurant().getId())
                    .collect(Collectors.toList());
            
            if (restaurantIds.isEmpty()) {
                return createEmptyPaginationResponse(limit, offset);
            }
            
            spec = spec.and((root, query, cb) -> root.get("id").in(restaurantIds));
        }
        
        // Фильтр по активности
        if (isActive != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isActive"), isActive));
        }
        // Если isActive не указан, показываем все рестораны (и активные, и деактивированные)
        
        // Поиск по названию или адресу
        if (search != null && !search.trim().isEmpty()) {
            String searchPattern = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> 
                cb.or(
                    cb.like(cb.lower(root.get("name")), searchPattern),
                    cb.like(cb.lower(root.get("address")), searchPattern)
                )
            );
        }
        
        // Сортировка
        Sort sort = buildSort(sortBy, sortOrder);
        Pageable pageable = PageRequest.of(offset / limit, limit, sort);
        
        Page<Restaurant> page = restaurantRepository.findAll(spec, pageable);
        
        List<RestaurantListItemResponse> items = page.getContent().stream()
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
    
    public RestaurantResponse getRestaurant(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
        
        return toResponse(restaurant);
    }
    
    @Transactional
    public RestaurantResponse updateRestaurant(Long id, UpdateRestaurantRequest request) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
        
        // Определяем роль пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        // Обновление полей (PATCH-логика)
        if (request.getName() != null) {
            restaurant.setName(request.getName());
        }
        if (request.getAddress() != null) {
            restaurant.setAddress(request.getAddress());
        }
        if (request.getPhone() != null) {
            restaurant.setPhone(PhoneValidator.normalize(request.getPhone()));
        }
        if (request.getWhatsapp() != null) {
            restaurant.setWhatsapp(PhoneValidator.normalize(request.getWhatsapp()));
        }
        if (request.getInstagram() != null) {
            restaurant.setInstagram(InstagramValidator.normalize(request.getInstagram()));
        }
        if (request.getDescription() != null) {
            restaurant.setDescription(request.getDescription());
        }
        if (request.getLatitude() != null) {
            restaurant.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            restaurant.setLongitude(request.getLongitude());
        }
        if (request.getWorkingHours() != null) {
            restaurant.setWorkingHours(request.getWorkingHours());
        }
        if (request.getManagerLanguageCode() != null) {
            restaurant.setManagerLanguageCode(request.getManagerLanguageCode());
        }
        
        // Обновление изображений
        // Обрабатываем logoImageId: если передан 0, удаляем изображение
        // Если передан валидный ID, устанавливаем изображение
        logger.debug("Updating restaurant {}: logoImageId={}, bgImageId={}", 
                id, request.getLogoImageId(), request.getBgImageId());
        
        if (request.getLogoImageId() != null) {
            if (request.getLogoImageId() == 0) {
                logger.debug("Removing logo image from restaurant {}", id);
                restaurant.setLogoImage(null);
            } else {
                logger.debug("Setting logo image {} for restaurant {}", request.getLogoImageId(), id);
                Image logoImage = imageRepository.findByIdAndIsActiveTrue(request.getLogoImageId())
                        .orElseThrow(() -> new RuntimeException("IMAGE_NOT_FOUND"));
                restaurant.setLogoImage(logoImage);
            }
        } else {
            logger.debug("logoImageId is null, skipping logo image update");
        }
        
        // Аналогично для bgImageId
        if (request.getBgImageId() != null) {
            if (request.getBgImageId() == 0) {
                logger.debug("Removing background image from restaurant {}", id);
                restaurant.setBgImage(null);
            } else {
                logger.debug("Setting background image {} for restaurant {}", request.getBgImageId(), id);
                Image bgImage = imageRepository.findByIdAndIsActiveTrue(request.getBgImageId())
                        .orElseThrow(() -> new RuntimeException("IMAGE_NOT_FOUND"));
                restaurant.setBgImage(bgImage);
            }
        } else {
            logger.debug("bgImageId is null, skipping background image update");
        }
        
        // isActive может изменять только ADMIN
        if (isAdmin && request.getIsActive() != null) {
            restaurant.setIsActive(request.getIsActive());
        }
        
        restaurant = restaurantRepository.save(restaurant);
        
        return toResponse(restaurant);
    }
    
    @Transactional
    public void deleteRestaurant(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
        
        // Проверка использования ресторана
        boolean hasActiveSubscriptions = restaurantSubscriptionRepository
                .findByRestaurantIdAndIsActiveTrue(id).size() > 0;
        
        // TODO: Проверка активных бронирований и предзаказов (когда будут реализованы)
        
        if (hasActiveSubscriptions) {
            throw new RuntimeException("RESTAURANT_IN_USE");
        }
        
        // Мягкое удаление
        restaurant.setIsActive(false);
        restaurant.setDeletedAt(LocalDateTime.now());
        restaurantRepository.save(restaurant);
        
        // Каскадное мягкое удаление связанных сущностей
        // (будет выполнено через каскадные операции в Entity или отдельными запросами)
    }
    
    @Transactional
    public RestaurantResponse activateRestaurant(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
        
        // Проверка наличия активной подписки
        SubscriptionResponse subscription = subscriptionService.getRestaurantSubscription(id);
        if (!subscription.getIsActive()) {
            throw new RuntimeException("NO_ACTIVE_SUBSCRIPTION");
        }
        
        restaurant.setIsActive(true);
        restaurant = restaurantRepository.save(restaurant);
        
        return toResponse(restaurant);
    }
    
    @Transactional
    public RestaurantResponse deactivateRestaurant(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
        
        restaurant.setIsActive(false);
        restaurant = restaurantRepository.save(restaurant);
        
        return toResponse(restaurant);
    }
    
    @Transactional
    public RestaurantResponse uploadRestaurantImage(Long restaurantId, MultipartFile file, String imageType) throws IOException {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
        
        // Валидация типа изображения
        if (!"logo".equals(imageType) && !"background".equals(imageType)) {
            throw new RuntimeException("INVALID_IMAGE_TYPE");
        }
        
        // Загружаем изображение
        ImageResponse imageResponse = imageService.uploadImage(file);
        Image image = imageRepository.findByIdAndIsActiveTrue(imageResponse.getId())
                .orElseThrow(() -> new RuntimeException("IMAGE_NOT_FOUND"));
        
        // Удаляем старое изображение, если оно было
        Image oldImage = null;
        if ("logo".equals(imageType)) {
            oldImage = restaurant.getLogoImage();
            restaurant.setLogoImage(image);
        } else if ("background".equals(imageType)) {
            oldImage = restaurant.getBgImage();
            restaurant.setBgImage(image);
        }
        
        restaurant = restaurantRepository.save(restaurant);
        
        // Мягко удаляем старое изображение, если оно было и больше не используется
        if (oldImage != null) {
            try {
                imageService.deleteImage(oldImage.getId());
            } catch (RuntimeException e) {
                // Если изображение используется где-то еще, просто логируем
                logger.debug("Could not delete old image {}: {}", oldImage.getId(), e.getMessage());
            }
        }
        
        return toResponse(restaurant);
    }
    
    @Transactional
    public RestaurantResponse deleteRestaurantImage(Long restaurantId, String imageType) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
        
        // Валидация типа изображения
        if (!"logo".equals(imageType) && !"background".equals(imageType)) {
            throw new RuntimeException("INVALID_IMAGE_TYPE");
        }
        
        Image oldImage = null;
        if ("logo".equals(imageType)) {
            oldImage = restaurant.getLogoImage();
            restaurant.setLogoImage(null);
        } else if ("background".equals(imageType)) {
            oldImage = restaurant.getBgImage();
            restaurant.setBgImage(null);
        }
        
        restaurant = restaurantRepository.save(restaurant);
        
        // Мягко удаляем изображение, если оно было
        if (oldImage != null) {
            try {
                imageService.deleteImage(oldImage.getId());
            } catch (RuntimeException e) {
                // Если изображение используется где-то еще, просто логируем
                logger.debug("Could not delete image {}: {}", oldImage.getId(), e.getMessage());
            }
        }
        
        return toResponse(restaurant);
    }
    
    private RestaurantResponse toResponse(Restaurant restaurant) {
        RestaurantResponse response = new RestaurantResponse();
        response.setId(restaurant.getId());
        response.setName(restaurant.getName());
        response.setAddress(restaurant.getAddress());
        response.setPhone(restaurant.getPhone());
        response.setWhatsapp(restaurant.getWhatsapp());
        response.setInstagram(restaurant.getInstagram());
        response.setDescription(restaurant.getDescription());
        response.setLatitude(restaurant.getLatitude());
        response.setLongitude(restaurant.getLongitude());
        response.setWorkingHours(restaurant.getWorkingHours());
        response.setManagerLanguageCode(restaurant.getManagerLanguageCode());
        response.setLogoImageId(restaurant.getLogoImage() != null ? restaurant.getLogoImage().getId() : null);
        response.setBgImageId(restaurant.getBgImage() != null ? restaurant.getBgImage().getId() : null);
        response.setIsActive(restaurant.getIsActive());
        response.setCreatedAt(restaurant.getCreatedAt() != null ? restaurant.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        response.setUpdatedAt(restaurant.getUpdatedAt() != null ? restaurant.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        response.setDeletedAt(restaurant.getDeletedAt() != null ? restaurant.getDeletedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        return response;
    }
    
    private RestaurantListItemResponse toListItemResponse(Restaurant restaurant) {
        RestaurantListItemResponse response = new RestaurantListItemResponse();
        response.setId(restaurant.getId());
        response.setName(restaurant.getName());
        response.setAddress(restaurant.getAddress());
        response.setPhone(restaurant.getPhone());
        response.setLogoImageId(restaurant.getLogoImage() != null ? restaurant.getLogoImage().getId() : null);
        response.setIsActive(restaurant.getIsActive());
        response.setCreatedAt(restaurant.getCreatedAt() != null ? restaurant.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        return response;
    }
    
    private Sort buildSort(String sortBy, String sortOrder) {
        String field = sortBy != null ? sortBy : "createdAt";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        // Маппинг полей
        switch (field) {
            case "name":
                return Sort.by(direction, "name");
            case "createdAt":
                return Sort.by(direction, "createdAt");
            case "updatedAt":
                return Sort.by(direction, "updatedAt");
            default:
                return Sort.by(direction, "createdAt");
        }
    }
    
    private PaginationResponse<List<RestaurantListItemResponse>> createEmptyPaginationResponse(Integer limit, Integer offset) {
        PaginationResponse.PaginationInfo pagination = new PaginationResponse.PaginationInfo(
                0L, limit, offset, false
        );
        return new PaginationResponse<>(List.of(), pagination);
    }
}

