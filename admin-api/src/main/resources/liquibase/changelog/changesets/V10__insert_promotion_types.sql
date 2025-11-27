--liquibase formatted sql

--changeset resto-hub:33
--comment: Insert promotion types (PROMOTION, DISCOUNT, THEMATIC_EVENT, NEW_ITEM, HAPPY_HOUR, SPECIAL_MENU, HOLIDAY_EVENT, SEASONAL)
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM promotion_types WHERE code = 'PROMOTION'
INSERT INTO promotion_types (code, name, description, is_active, created_at, updated_at)
VALUES ('PROMOTION', 'Акция', 'Общий тип для скидок и специальных предложений', TRUE, NOW(), NOW());

--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM promotion_types WHERE code = 'DISCOUNT'
INSERT INTO promotion_types (code, name, description, is_active, created_at, updated_at)
VALUES ('DISCOUNT', 'Скидка', 'Конкретные скидки на блюда, напитки или меню', TRUE, NOW(), NOW());

--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM promotion_types WHERE code = 'THEMATIC_EVENT'
INSERT INTO promotion_types (code, name, description, is_active, created_at, updated_at)
VALUES ('THEMATIC_EVENT', 'Тематический вечер', 'Тематические мероприятия: живая музыка, кулинарные вечера, мастер-классы', TRUE, NOW(), NOW());

--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM promotion_types WHERE code = 'NEW_ITEM'
INSERT INTO promotion_types (code, name, description, is_active, created_at, updated_at)
VALUES ('NEW_ITEM', 'Новинка', 'Новые блюда или напитки в меню', TRUE, NOW(), NOW());

--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM promotion_types WHERE code = 'HAPPY_HOUR'
INSERT INTO promotion_types (code, name, description, is_active, created_at, updated_at)
VALUES ('HAPPY_HOUR', 'Счастливые часы', 'Скидки и специальные предложения в определенное время дня', TRUE, NOW(), NOW());

--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM promotion_types WHERE code = 'SPECIAL_MENU'
INSERT INTO promotion_types (code, name, description, is_active, created_at, updated_at)
VALUES ('SPECIAL_MENU', 'Специальное меню', 'Дегустационные, праздничные или тематические меню', TRUE, NOW(), NOW());

--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM promotion_types WHERE code = 'HOLIDAY_EVENT'
INSERT INTO promotion_types (code, name, description, is_active, created_at, updated_at)
VALUES ('HOLIDAY_EVENT', 'Праздничное событие', 'Специальные предложения и мероприятия к праздникам', TRUE, NOW(), NOW());

--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM promotion_types WHERE code = 'SEASONAL'
INSERT INTO promotion_types (code, name, description, is_active, created_at, updated_at)
VALUES ('SEASONAL', 'Сезонное предложение', 'Сезонные блюда и меню (летние, зимние, весенние, осенние)', TRUE, NOW(), NOW());

