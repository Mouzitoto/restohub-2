--liquibase formatted sql

--changeset resto-hub:32
--comment: Insert subscription types (Standard, Pro, Premium)
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM subscription_types WHERE code = 'STANDARD'
INSERT INTO subscription_types (code, name, description, price, is_active, created_at, updated_at)
VALUES ('STANDARD', 'Стандарт', 'Базовый тариф с основными функциями', 10000.00, TRUE, NOW(), NOW());

--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM subscription_types WHERE code = 'PRO'
INSERT INTO subscription_types (code, name, description, price, is_active, created_at, updated_at)
VALUES ('PRO', 'Про', 'Расширенный тариф с дополнительными возможностями', 20000.00, TRUE, NOW(), NOW());

--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM subscription_types WHERE code = 'PREMIUM'
INSERT INTO subscription_types (code, name, description, price, is_active, created_at, updated_at)
VALUES ('PREMIUM', 'Премиум', 'Премиальный тариф с полным доступом ко всем функциям', 30000.00, TRUE, NOW(), NOW());

