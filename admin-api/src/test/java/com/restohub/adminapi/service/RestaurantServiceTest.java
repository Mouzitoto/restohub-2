package com.restohub.adminapi.service;

import com.restohub.adminapi.dto.CreateRestaurantRequest;
import com.restohub.adminapi.dto.ImageResponse;
import com.restohub.adminapi.dto.RestaurantResponse;
import com.restohub.adminapi.entity.*;
import com.restohub.adminapi.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {
    
    @Mock
    private RestaurantRepository restaurantRepository;
    
    @Mock
    private ImageRepository imageRepository;
    
    @Mock
    private UserRestaurantRepository userRestaurantRepository;
    
    @Mock
    private RestaurantSubscriptionRepository restaurantSubscriptionRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private ImageService imageService;
    
    @InjectMocks
    private RestaurantService restaurantService;
    
    private CreateRestaurantRequest request;
    private Restaurant restaurant;
    private User managerUser;
    private User adminUser;
    private Role managerRole;
    private Role adminRole;
    
    @BeforeEach
    void setUp() {
        // Настройка ролей
        managerRole = new Role();
        managerRole.setId(2L);
        managerRole.setCode("MANAGER");
        managerRole.setName("Менеджер");
        
        adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setCode("ADMIN");
        adminRole.setName("Администратор");
        
        // Настройка пользователей
        managerUser = new User();
        managerUser.setId(10L);
        managerUser.setEmail("manager@test.com");
        managerUser.setRole(managerRole);
        managerUser.setIsActive(true);
        
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setEmail("admin@test.com");
        adminUser.setRole(adminRole);
        adminUser.setIsActive(true);
        
        // Настройка ресторана
        restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setName("Test Restaurant");
        restaurant.setAddress("Test Address");
        restaurant.setPhone("+79991234567");
        restaurant.setIsActive(true);
        
        // Настройка request
        request = new CreateRestaurantRequest();
        request.setName("Test Restaurant");
        request.setAddress("Test Address");
        request.setPhone("+79991234567");
        
        // Очистка SecurityContext перед каждым тестом
        SecurityContextHolder.clearContext();
    }
    
    @Test
    void createRestaurant_AsManager_AutoLinksToManager() {
        // Arrange
        setupSecurityContext("manager@test.com", "ROLE_MANAGER");
        
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(invocation -> {
            Restaurant r = invocation.getArgument(0);
            r.setId(1L);
            return r;
        });
        when(userRepository.findByEmailAndIsActiveTrue("manager@test.com"))
                .thenReturn(Optional.of(managerUser));
        when(userRestaurantRepository.findByRestaurantId(1L))
                .thenReturn(Collections.emptyList());
        
        // Act
        RestaurantResponse response = restaurantService.createRestaurant(request);
        
        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
        verify(userRepository, times(1)).findByEmailAndIsActiveTrue("manager@test.com");
        verify(userRestaurantRepository, times(1)).save(any(UserRestaurant.class));
    }
    
    @Test
    void createRestaurant_AsAdmin_WithUserId_LinksToUser() {
        // Arrange
        setupSecurityContext("admin@test.com", "ROLE_ADMIN");
        request.setUserId(10L); // ID менеджера
        
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(invocation -> {
            Restaurant r = invocation.getArgument(0);
            r.setId(1L);
            return r;
        });
        when(userRepository.findByIdAndIsActiveTrue(10L))
                .thenReturn(Optional.of(managerUser));
        when(userRestaurantRepository.findByRestaurantId(1L))
                .thenReturn(Collections.emptyList());
        
        // Act
        RestaurantResponse response = restaurantService.createRestaurant(request);
        
        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
        verify(userRepository, times(1)).findByIdAndIsActiveTrue(10L);
        verify(userRestaurantRepository, times(1)).save(any(UserRestaurant.class));
    }
    
    @Test
    void createRestaurant_AsAdmin_WithoutUserId_NoLink() {
        // Arrange
        setupSecurityContext("admin@test.com", "ROLE_ADMIN");
        // userId не указан
        
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(invocation -> {
            Restaurant r = invocation.getArgument(0);
            r.setId(1L);
            return r;
        });
        
        // Act
        RestaurantResponse response = restaurantService.createRestaurant(request);
        
        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
        verify(userRepository, never()).findByIdAndIsActiveTrue(any());
        verify(userRepository, never()).findByEmailAndIsActiveTrue(any());
        verify(userRestaurantRepository, never()).save(any(UserRestaurant.class));
    }
    
    @Test
    void createRestaurant_AsManager_InvalidUserId_IgnoresUserId() {
        // Arrange
        setupSecurityContext("manager@test.com", "ROLE_MANAGER");
        request.setUserId(999L); // Несуществующий userId, должен быть проигнорирован
        
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(invocation -> {
            Restaurant r = invocation.getArgument(0);
            r.setId(1L);
            return r;
        });
        when(userRepository.findByEmailAndIsActiveTrue("manager@test.com"))
                .thenReturn(Optional.of(managerUser));
        when(userRestaurantRepository.findByRestaurantId(1L))
                .thenReturn(Collections.emptyList());
        
        // Act
        RestaurantResponse response = restaurantService.createRestaurant(request);
        
        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
        // Должен использовать email менеджера, а не userId
        verify(userRepository, times(1)).findByEmailAndIsActiveTrue("manager@test.com");
        verify(userRepository, never()).findByIdAndIsActiveTrue(999L);
        verify(userRestaurantRepository, times(1)).save(any(UserRestaurant.class));
    }
    
    @Test
    void uploadRestaurantImage_Logo_Success() {
        // Arrange
        MultipartFile file = new MockMultipartFile(
                "file",
                "logo.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
        
        ImageResponse imageResponse = new ImageResponse();
        imageResponse.setId(123L);
        imageResponse.setMimeType("image/jpeg");
        imageResponse.setFileSize(1024L);
        
        Image image = new Image();
        image.setId(123L);
        image.setIsActive(true);
        
        when(restaurantRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(Optional.of(restaurant));
        try {
            doReturn(imageResponse).when(imageService).uploadImage(any(MultipartFile.class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        when(imageRepository.findByIdAndIsActiveTrue(123L))
                .thenReturn(Optional.of(image));
        when(restaurantRepository.save(any(Restaurant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        RestaurantResponse response;
        try {
            response = restaurantService.uploadRestaurantImage(1L, file, "logo");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        verify(restaurantRepository, times(1)).findByIdAndIsActiveTrue(1L);
        try {
            verify(imageService, times(1)).uploadImage(any(MultipartFile.class));
        } catch (IOException e) {
            // verify не бросает IOException, это для компилятора
        }
        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
    }
    
    @Test
    void uploadRestaurantImage_Background_Success() {
        // Arrange
        MultipartFile file = new MockMultipartFile(
                "file",
                "background.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
        
        ImageResponse imageResponse = new ImageResponse();
        imageResponse.setId(124L);
        imageResponse.setMimeType("image/jpeg");
        imageResponse.setFileSize(2048L);
        
        Image image = new Image();
        image.setId(124L);
        image.setIsActive(true);
        
        when(restaurantRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(Optional.of(restaurant));
        try {
            doReturn(imageResponse).when(imageService).uploadImage(any(MultipartFile.class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        when(imageRepository.findByIdAndIsActiveTrue(124L))
                .thenReturn(Optional.of(image));
        when(restaurantRepository.save(any(Restaurant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        RestaurantResponse response;
        try {
            response = restaurantService.uploadRestaurantImage(1L, file, "background");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        verify(restaurantRepository, times(1)).findByIdAndIsActiveTrue(1L);
        try {
            verify(imageService, times(1)).uploadImage(any(MultipartFile.class));
        } catch (IOException e) {
            // verify не бросает IOException, это для компилятора
        }
        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
    }
    
    @Test
    void uploadRestaurantImage_InvalidType_ThrowsException() {
        // Arrange
        MultipartFile file = new MockMultipartFile(
                "file",
                "image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
        
        when(restaurantRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(Optional.of(restaurant));
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            try {
                restaurantService.uploadRestaurantImage(1L, file, "invalid");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        
        verify(restaurantRepository, times(1)).findByIdAndIsActiveTrue(1L);
        try {
            verify(imageService, never()).uploadImage(any());
        } catch (IOException e) {
            // verify не бросает IOException, это для компилятора
        }
    }
    
    @Test
    void uploadRestaurantImage_RestaurantNotFound_ThrowsException() {
        // Arrange
        MultipartFile file = new MockMultipartFile(
                "file",
                "logo.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
        
        when(restaurantRepository.findByIdAndIsActiveTrue(999L))
                .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            try {
                restaurantService.uploadRestaurantImage(999L, file, "logo");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        
        verify(restaurantRepository, times(1)).findByIdAndIsActiveTrue(999L);
        try {
            verify(imageService, never()).uploadImage(any());
        } catch (IOException e) {
            // verify не бросает IOException, это для компилятора
        }
    }
    
    @Test
    void deleteRestaurantImage_Logo_Success() {
        // Arrange
        Image oldLogo = new Image();
        oldLogo.setId(123L);
        oldLogo.setIsActive(true);
        restaurant.setLogoImage(oldLogo);
        
        when(restaurantRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(Optional.of(restaurant));
        when(restaurantRepository.save(any(Restaurant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(imageService).deleteImage(123L);
        
        // Act
        RestaurantResponse response = restaurantService.deleteRestaurantImage(1L, "logo");
        
        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        verify(restaurantRepository, times(1)).findByIdAndIsActiveTrue(1L);
        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
        verify(imageService, times(1)).deleteImage(123L);
    }
    
    @Test
    void deleteRestaurantImage_Background_Success() {
        // Arrange
        Image oldBg = new Image();
        oldBg.setId(124L);
        oldBg.setIsActive(true);
        restaurant.setBgImage(oldBg);
        
        when(restaurantRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(Optional.of(restaurant));
        when(restaurantRepository.save(any(Restaurant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(imageService).deleteImage(124L);
        
        // Act
        RestaurantResponse response = restaurantService.deleteRestaurantImage(1L, "background");
        
        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        verify(restaurantRepository, times(1)).findByIdAndIsActiveTrue(1L);
        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
        verify(imageService, times(1)).deleteImage(124L);
    }
    
    @Test
    void deleteRestaurantImage_InvalidType_ThrowsException() {
        // Arrange
        when(restaurantRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(Optional.of(restaurant));
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            restaurantService.deleteRestaurantImage(1L, "invalid");
        });
        
        verify(restaurantRepository, times(1)).findByIdAndIsActiveTrue(1L);
        verify(imageService, never()).deleteImage(any());
    }
    
    @Test
    void deleteRestaurantImage_RestaurantNotFound_ThrowsException() {
        // Arrange
        when(restaurantRepository.findByIdAndIsActiveTrue(999L))
                .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            restaurantService.deleteRestaurantImage(999L, "logo");
        });
        
        verify(restaurantRepository, times(1)).findByIdAndIsActiveTrue(999L);
        verify(imageService, never()).deleteImage(any());
    }
    
    private void setupSecurityContext(String email, String role) {
        Authentication authentication = new Authentication() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return Collections.singletonList(new SimpleGrantedAuthority(role));
            }
            
            @Override
            public Object getCredentials() {
                return null;
            }
            
            @Override
            public Object getDetails() {
                return null;
            }
            
            @Override
            public Object getPrincipal() {
                return email;
            }
            
            @Override
            public boolean isAuthenticated() {
                return true;
            }
            
            @Override
            public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
            }
            
            @Override
            public String getName() {
                return email;
            }
        };
        
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}

