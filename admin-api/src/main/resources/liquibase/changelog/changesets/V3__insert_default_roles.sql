--liquibase formatted sql

--changeset resto-hub:23
--comment: Insert default roles (ADMIN and MANAGER)
INSERT INTO roles (code, name, description, is_active, created_at, updated_at)
VALUES 
    ('ADMIN', 'Администратор', 'Полный доступ ко всем функциям системы', TRUE, NOW(), NOW()),
    ('MANAGER', 'Менеджер', 'Управление рестораном', TRUE, NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

