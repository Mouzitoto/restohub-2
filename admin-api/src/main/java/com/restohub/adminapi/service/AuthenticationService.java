package com.restohub.adminapi.service;

import com.restohub.adminapi.dto.LoginResponse;
import com.restohub.adminapi.dto.RefreshTokenResponse;
import com.restohub.adminapi.dto.UserInfoResponse;
import com.restohub.adminapi.entity.*;
import com.restohub.adminapi.repository.*;
import com.restohub.adminapi.util.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AuthenticationService {
    
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserRestaurantRepository userRestaurantRepository;
    private final RestaurantSubscriptionRepository restaurantSubscriptionRepository;
    private final RestaurantRepository restaurantRepository;
    
    @Value("${jwt.access-token-expiration:300}")
    private long accessTokenExpiration;
    
    public AuthenticationService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            JwtTokenProvider jwtTokenProvider,
            PasswordEncoder passwordEncoder,
            UserRestaurantRepository userRestaurantRepository,
            RestaurantSubscriptionRepository restaurantSubscriptionRepository,
            RestaurantRepository restaurantRepository) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.userRestaurantRepository = userRestaurantRepository;
        this.restaurantSubscriptionRepository = restaurantSubscriptionRepository;
        this.restaurantRepository = restaurantRepository;
    }
    
    @Transactional
    public LoginResponse login(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmailAndIsActiveTrue(email);
        
        if (userOpt.isEmpty() || !passwordEncoder.matches(password, userOpt.get().getPasswordHash())) {
            throw new RuntimeException("INVALID_CREDENTIALS");
        }
        
        User user = userOpt.get();
        String role = user.getRole().getCode();
        
        String accessToken = jwtTokenProvider.generateAccessToken(email, role);
        String refreshToken = jwtTokenProvider.generateRefreshToken(email, role);
        
        // Сохраняем refresh token в БД (хешированный)
        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setUser(user);
        refreshTokenEntity.setToken(passwordEncoder.encode(refreshToken));
        refreshTokenEntity.setExpiresAt(LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshTokenExpiration()));
        refreshTokenRepository.save(refreshTokenEntity);
        
        return new LoginResponse(accessToken, refreshToken, role, accessTokenExpiration);
    }
    
    @Transactional
    public void logout(String refreshToken) {
        // Находим и удаляем refresh token
        refreshTokenRepository.findAll().stream()
                .filter(rt -> passwordEncoder.matches(refreshToken, rt.getToken()))
                .findFirst()
                .ifPresent(refreshTokenRepository::delete);
    }
    
    @Transactional
    public RefreshTokenResponse refresh(String refreshToken) {
        // Ищем refresh token в БД
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findAll().stream()
                .filter(rt -> passwordEncoder.matches(refreshToken, rt.getToken()))
                .findFirst();
        
        if (tokenOpt.isEmpty()) {
            throw new RuntimeException("INVALID_REFRESH_TOKEN");
        }
        
        RefreshToken tokenEntity = tokenOpt.get();
        
        // Проверяем истечение JWT токена (JWT содержит время истечения в UTC)
        // Проверка expires_at в БД избыточна, так как JWT уже содержит эту информацию
        try {
            if (jwtTokenProvider.isTokenExpired(refreshToken)) {
                refreshTokenRepository.delete(tokenEntity);
                throw new RuntimeException("REFRESH_TOKEN_EXPIRED");
            }
        } catch (Exception e) {
            // Если не можем проверить истечение (токен невалиден), удаляем из БД
            refreshTokenRepository.delete(tokenEntity);
            throw new RuntimeException("INVALID_REFRESH_TOKEN");
        }
        
        // Валидируем токен через JWT (проверка подписи и структуры)
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            refreshTokenRepository.delete(tokenEntity);
            throw new RuntimeException("INVALID_REFRESH_TOKEN");
        }
        
        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        String role = jwtTokenProvider.getRoleFromToken(refreshToken);
        
        // Генерируем новые токены
        String newAccessToken = jwtTokenProvider.generateAccessToken(email, role);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(email, role);
        
        // Обновляем refresh token в БД
        tokenEntity.setToken(passwordEncoder.encode(newRefreshToken));
        tokenEntity.setExpiresAt(LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshTokenExpiration()));
        refreshTokenRepository.save(tokenEntity);
        
        return new RefreshTokenResponse(newAccessToken, newRefreshToken, accessTokenExpiration);
    }
    
    public UserInfoResponse getCurrentUser(String email, String role) {
        Optional<User> userOpt = userRepository.findByEmailAndIsActiveTrue(email);
        
        if (userOpt.isEmpty()) {
            throw new RuntimeException("USER_NOT_FOUND");
        }
        
        User user = userOpt.get();
        List<UserInfoResponse.RestaurantInfo> restaurants = new ArrayList<>();
        
        if ("ADMIN".equals(role)) {
            // Для админа получаем все рестораны
            List<Restaurant> allRestaurants = restaurantRepository.findAll();
            
            for (Restaurant restaurant : allRestaurants) {
                // Получаем активную подписку
                List<RestaurantSubscription> subscriptions = restaurantSubscriptionRepository
                        .findByRestaurantIdAndIsActiveTrue(restaurant.getId());
                
                UserInfoResponse.SubscriptionInfo subscriptionInfo = null;
                if (!subscriptions.isEmpty()) {
                    RestaurantSubscription subscription = subscriptions.get(0);
                    LocalDate endDate = subscription.getEndDate();
                    LocalDate now = LocalDate.now();
                    long daysRemaining = ChronoUnit.DAYS.between(now, endDate);
                    boolean isExpiringSoon = daysRemaining <= 7;
                    
                    subscriptionInfo = new UserInfoResponse.SubscriptionInfo(
                            restaurant.getId(),
                            subscription.getIsActive() && endDate.isAfter(now) || endDate.isEqual(now),
                            endDate.toString(),
                            (int) Math.max(0, daysRemaining),
                            isExpiringSoon
                    );
                }
                
                restaurants.add(new UserInfoResponse.RestaurantInfo(
                        restaurant.getId(),
                        restaurant.getName(),
                        restaurant.getIsActive(),
                        subscriptionInfo
                ));
            }
        } else if ("MANAGER".equals(role)) {
            // Получаем рестораны менеджера
            List<UserRestaurant> userRestaurants = userRestaurantRepository.findByUserId(user.getId());
            
            for (UserRestaurant userRestaurant : userRestaurants) {
                Restaurant restaurant = userRestaurant.getRestaurant();
                if (restaurant != null) {
                    
                    // Получаем активную подписку
                    List<RestaurantSubscription> subscriptions = restaurantSubscriptionRepository
                            .findByRestaurantIdAndIsActiveTrue(restaurant.getId());
                    
                    UserInfoResponse.SubscriptionInfo subscriptionInfo = null;
                    if (!subscriptions.isEmpty()) {
                        RestaurantSubscription subscription = subscriptions.get(0);
                        LocalDate endDate = subscription.getEndDate();
                        LocalDate now = LocalDate.now();
                        long daysRemaining = ChronoUnit.DAYS.between(now, endDate);
                        boolean isExpiringSoon = daysRemaining <= 7;
                        
                        subscriptionInfo = new UserInfoResponse.SubscriptionInfo(
                                restaurant.getId(),
                                subscription.getIsActive() && endDate.isAfter(now) || endDate.isEqual(now),
                                endDate.toString(),
                                (int) Math.max(0, daysRemaining),
                                isExpiringSoon
                        );
                    }
                    
                    restaurants.add(new UserInfoResponse.RestaurantInfo(
                            restaurant.getId(),
                            restaurant.getName(),
                            restaurant.getIsActive(),
                            subscriptionInfo
                    ));
                }
            }
        }
        
        return new UserInfoResponse(user.getId(), user.getEmail(), role, restaurants);
    }
}

