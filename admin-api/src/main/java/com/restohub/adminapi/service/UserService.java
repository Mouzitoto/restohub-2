package com.restohub.adminapi.service;

import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.entity.*;
import com.restohub.adminapi.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRestaurantRepository userRestaurantRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            RestaurantRepository restaurantRepository,
            UserRestaurantRepository userRestaurantRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.restaurantRepository = restaurantRepository;
        this.userRestaurantRepository = userRestaurantRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        // Проверка уникальности email
        if (userRepository.findByEmail(request.getEmail().trim().toLowerCase()).isPresent()) {
            throw new RuntimeException("EMAIL_ALREADY_EXISTS");
        }
        
        // Проверка существования роли
        Role role = roleRepository.findByIdAndIsActiveTrue(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("ROLE_NOT_FOUND"));
        
        // Проверка, что MANAGER имеет рестораны
        if ("MANAGER".equals(role.getCode()) && 
            (request.getRestaurantIds() == null || request.getRestaurantIds().isEmpty())) {
            throw new RuntimeException("MANAGER_MUST_HAVE_RESTAURANTS");
        }
        
        // Проверка существования ресторанов (для MANAGER)
        if (request.getRestaurantIds() != null && !request.getRestaurantIds().isEmpty()) {
            for (Long restaurantId : request.getRestaurantIds()) {
                restaurantRepository.findById(restaurantId)
                        .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
            }
        }
        
        // Создание пользователя
        User user = new User();
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setIsActive(true);
        
        user = userRepository.save(user);
        
        // Создание связей с ресторанами (для MANAGER)
        if ("MANAGER".equals(role.getCode()) && request.getRestaurantIds() != null) {
            for (Long restaurantId : request.getRestaurantIds()) {
                Restaurant restaurant = restaurantRepository.findById(restaurantId).orElse(null);
                if (restaurant != null) {
                    UserRestaurant userRestaurant = new UserRestaurant();
                    userRestaurant.setUser(user);
                    userRestaurant.setRestaurant(restaurant);
                    userRestaurant.setCreatedAt(LocalDateTime.now());
                    userRestaurantRepository.save(userRestaurant);
                }
            }
        }
        
        return toResponse(user);
    }
    
    public PaginationResponse<List<UserListItemResponse>> getUsers(
            Integer limit, Integer offset, String search, Long roleId, Boolean isActive,
            String sortBy, String sortOrder) {
        
        // Построение спецификации
        Specification<User> spec = Specification.where(null);
        
        // Фильтр по активности
        if (isActive != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isActive"), isActive));
        } else {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isActive"), true));
        }
        
        // Фильтр по роли
        if (roleId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("role").get("id"), roleId));
        }
        
        // Поиск по email
        if (search != null && !search.trim().isEmpty()) {
            String searchPattern = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> 
                cb.like(cb.lower(root.get("email")), searchPattern)
            );
        }
        
        // Сортировка
        Sort sort = buildSort(sortBy, sortOrder);
        Pageable pageable = PageRequest.of(offset / limit, limit, sort);
        
        Page<User> page = userRepository.findAll(spec, pageable);
        
        List<UserListItemResponse> items = page.getContent().stream()
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
    
    public UserResponse getUser(Long userId) {
        User user = userRepository.findByIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));
        
        return toResponse(user);
    }
    
    @Transactional
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findByIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));
        
        // Проверка, что пользователь не пытается изменить сам себя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        if (user.getEmail().equals(currentUserEmail)) {
            throw new RuntimeException("CANNOT_MODIFY_SELF");
        }
        
        // Обновление email
        if (request.getEmail() != null) {
            String newEmail = request.getEmail().trim().toLowerCase();
            if (!newEmail.equals(user.getEmail())) {
                if (userRepository.findByEmail(newEmail).isPresent()) {
                    throw new RuntimeException("EMAIL_ALREADY_EXISTS");
                }
                user.setEmail(newEmail);
            }
        }
        
        // Обновление пароля
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            // Удаление всех refresh tokens при смене пароля
            refreshTokenRepository.deleteByUser(user);
        }
        
        // Обновление роли
        if (request.getRoleId() != null) {
            Role role = roleRepository.findByIdAndIsActiveTrue(request.getRoleId())
                    .orElseThrow(() -> new RuntimeException("ROLE_NOT_FOUND"));
            user.setRole(role);
            
            // Удаление всех существующих связей с ресторанами
            userRestaurantRepository.findByUserId(userId).forEach(userRestaurantRepository::delete);
            
            // Создание новых связей (для MANAGER)
            if ("MANAGER".equals(role.getCode()) && request.getRestaurantIds() != null && !request.getRestaurantIds().isEmpty()) {
                for (Long restaurantId : request.getRestaurantIds()) {
                    Restaurant restaurant = restaurantRepository.findById(restaurantId)
                            .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
                    UserRestaurant userRestaurant = new UserRestaurant();
                    userRestaurant.setUser(user);
                    userRestaurant.setRestaurant(restaurant);
                    userRestaurant.setCreatedAt(LocalDateTime.now());
                    userRestaurantRepository.save(userRestaurant);
                }
            }
        } else if (request.getRestaurantIds() != null && "MANAGER".equals(user.getRole().getCode())) {
            // Обновление только связей с ресторанами (без изменения роли)
            userRestaurantRepository.findByUserId(userId).forEach(userRestaurantRepository::delete);
            
            for (Long restaurantId : request.getRestaurantIds()) {
                Restaurant restaurant = restaurantRepository.findById(restaurantId)
                        .orElseThrow(() -> new RuntimeException("RESTAURANT_NOT_FOUND"));
                UserRestaurant userRestaurant = new UserRestaurant();
                userRestaurant.setUser(user);
                userRestaurant.setRestaurant(restaurant);
                userRestaurant.setCreatedAt(LocalDateTime.now());
                userRestaurantRepository.save(userRestaurant);
            }
        }
        
        user = userRepository.save(user);
        
        return toResponse(user);
    }
    
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findByIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));
        
        // Проверка, что пользователь не пытается удалить сам себя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        if (user.getEmail().equals(currentUserEmail)) {
            throw new RuntimeException("CANNOT_DELETE_SELF");
        }
        
        // Мягкое удаление
        user.setIsActive(false);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
        
        // Удаление refresh tokens
        refreshTokenRepository.deleteByUser(user);
    }
    
    @Transactional
    public void resetPassword(Long userId, ResetPasswordRequest request) {
        User user = userRepository.findByIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));
        
        // Обновление пароля
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        // Удаление всех refresh tokens при сбросе пароля
        refreshTokenRepository.deleteByUser(user);
    }
    
    @Transactional
    public void activateUser(Long userId, Boolean isActive) {
        User user = userRepository.findByIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));
        
        // Проверка, что пользователь не пытается деактивировать сам себя
        if (!isActive) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserEmail = authentication.getName();
            if (user.getEmail().equals(currentUserEmail)) {
                throw new RuntimeException("CANNOT_DEACTIVATE_SELF");
            }
        }
        
        user.setIsActive(isActive);
        userRepository.save(user);
        
        // Удаление refresh tokens при деактивации
        if (!isActive) {
            refreshTokenRepository.deleteByUser(user);
        }
    }
    
    public List<RoleResponse> getRoles() {
        List<Role> roles = roleRepository.findByIsActiveTrue();
        return roles.stream()
                .map(this::toRoleResponse)
                .collect(Collectors.toList());
    }
    
    private UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setRoleId(user.getRole().getId());
        response.setRoleName(user.getRole().getName());
        response.setIsActive(user.getIsActive());
        
        // Загрузка связанных ресторанов (для MANAGER)
        if ("MANAGER".equals(user.getRole().getCode())) {
            List<UserRestaurant> userRestaurants = userRestaurantRepository.findByUserId(user.getId());
            List<UserResponse.RestaurantInfo> restaurants = userRestaurants.stream()
                    .map(ur -> {
                        UserResponse.RestaurantInfo info = new UserResponse.RestaurantInfo();
                        info.setId(ur.getRestaurant().getId());
                        info.setName(ur.getRestaurant().getName());
                        return info;
                    })
                    .collect(Collectors.toList());
            response.setRestaurants(restaurants);
        } else {
            response.setRestaurants(new ArrayList<>());
        }
        
        response.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        response.setUpdatedAt(user.getUpdatedAt() != null ? user.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        return response;
    }
    
    private UserListItemResponse toListItemResponse(User user) {
        UserListItemResponse response = new UserListItemResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setRoleId(user.getRole().getId());
        response.setRoleName(user.getRole().getName());
        response.setIsActive(user.getIsActive());
        
        // Загрузка связанных ресторанов (для MANAGER)
        if ("MANAGER".equals(user.getRole().getCode())) {
            List<UserRestaurant> userRestaurants = userRestaurantRepository.findByUserId(user.getId());
            List<UserListItemResponse.RestaurantInfo> restaurants = userRestaurants.stream()
                    .map(ur -> {
                        UserListItemResponse.RestaurantInfo info = new UserListItemResponse.RestaurantInfo();
                        info.setId(ur.getRestaurant().getId());
                        info.setName(ur.getRestaurant().getName());
                        return info;
                    })
                    .collect(Collectors.toList());
            response.setRestaurants(restaurants);
        } else {
            response.setRestaurants(new ArrayList<>());
        }
        
        response.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        return response;
    }
    
    private RoleResponse toRoleResponse(Role role) {
        RoleResponse response = new RoleResponse();
        response.setId(role.getId());
        response.setCode(role.getCode());
        response.setName(role.getName());
        response.setDescription(role.getDescription());
        response.setIsActive(role.getIsActive());
        response.setCreatedAt(role.getCreatedAt() != null ? role.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        response.setUpdatedAt(role.getUpdatedAt() != null ? role.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        return response;
    }
    
    private Sort buildSort(String sortBy, String sortOrder) {
        String field = sortBy != null ? sortBy : "createdAt";
        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        
        // Маппинг полей
        switch (field) {
            case "email":
                return Sort.by(direction, "email");
            case "createdAt":
                return Sort.by(direction, "createdAt");
            case "updatedAt":
                return Sort.by(direction, "updatedAt");
            default:
                return Sort.by(Sort.Direction.DESC, "createdAt");
        }
    }
}

