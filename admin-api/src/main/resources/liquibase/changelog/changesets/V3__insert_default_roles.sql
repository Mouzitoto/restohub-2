--liquibase formatted sql

--changeset resto-hub:23
--comment: Insert default roles (ADMIN and MANAGER)
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM roles WHERE code = 'ADMIN'
INSERT INTO roles (code, name, description, is_active, created_at, updated_at)
VALUES 
    ('ADMIN', 'Администратор', 'Полный доступ ко всем функциям системы', TRUE, NOW(), NOW());

--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM roles WHERE code = 'MANAGER'
INSERT INTO roles (code, name, description, is_active, created_at, updated_at)
VALUES 
    ('MANAGER', 'Менеджер', 'Управление рестораном', TRUE, NOW(), NOW());

