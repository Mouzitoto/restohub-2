package com.restohub.adminapi.config;

import com.restohub.adminapi.entity.Role;
import com.restohub.adminapi.entity.User;
import com.restohub.adminapi.repository.RoleRepository;
import com.restohub.adminapi.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements ApplicationRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    private static final String ADMIN_EMAIL = "admin@admin.kz";
    private static final String ADMIN_PASSWORD = "admin123";
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public DataInitializer(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        logger.info("Checking for default admin user...");
        
        // Проверяем, существует ли уже пользователь с email "admin"
        if (userRepository.findByEmail(ADMIN_EMAIL).isPresent()) {
            logger.info("Admin user already exists, skipping creation.");
            return;
        }
        
        // Ищем роль ADMIN (должна быть создана через миграцию)
        Role adminRole = roleRepository.findByCodeAndIsActiveTrue("ADMIN")
                .orElseThrow(() -> {
                    logger.error("ADMIN role not found! Please ensure database migrations are executed.");
                    return new RuntimeException("ADMIN role not found. Run database migrations first.");
                });
        
        // Создаем пользователя admin
        logger.info("Creating default admin user...");
        User adminUser = new User();
        adminUser.setEmail(ADMIN_EMAIL);
        adminUser.setPasswordHash(passwordEncoder.encode(ADMIN_PASSWORD));
        adminUser.setRole(adminRole);
        adminUser.setIsActive(true);
        
        userRepository.save(adminUser);
        
        logger.info("Default admin user created successfully. Email: {}, Password: {}", ADMIN_EMAIL, ADMIN_PASSWORD);
    }
}

