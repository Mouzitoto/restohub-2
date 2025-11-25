--liquibase formatted sql

--changeset resto-hub:26
--comment: Insert default menu categories in restaurant menu order
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM menu_categories WHERE name = 'Холодные закуски'
INSERT INTO menu_categories (name, description, display_order, is_active, created_at, updated_at) VALUES ('Холодные закуски', 'Холодные закуски, овощные закуски, холодные мясные и рыбные закуски', 10, TRUE, NOW(), NOW());

--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM menu_categories WHERE name = 'Салаты'
INSERT INTO menu_categories (name, description, display_order, is_active, created_at, updated_at) VALUES ('Салаты', 'Овощные, мясные, рыбные салаты', 20, TRUE, NOW(), NOW());

--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM menu_categories WHERE name = 'Горячие закуски'
INSERT INTO menu_categories (name, description, display_order, is_active, created_at, updated_at) VALUES ('Горячие закуски', 'Горячие закуски, мини-блюда', 30, TRUE, NOW(), NOW());

--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM menu_categories WHERE name = 'Супы'
INSERT INTO menu_categories (name, description, display_order, is_active, created_at, updated_at) VALUES ('Супы', 'Первые блюда', 40, TRUE, NOW(), NOW());

--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM menu_categories WHERE name = 'Основные блюда'
INSERT INTO menu_categories (name, description, display_order, is_active, created_at, updated_at) VALUES ('Основные блюда', 'Горячие основные блюда', 50, TRUE, NOW(), NOW());

--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM menu_categories WHERE name = 'Мясо'
INSERT INTO menu_categories (name, description, display_order, is_active, created_at, updated_at) VALUES ('Мясо', 'Блюда из говядины, свинины, баранины', 60, TRUE, NOW(), NOW());

--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM menu_categories WHERE name = 'Птица'
INSERT INTO menu_categories (name, description, display_order, is_active, created_at, updated_at) VALUES ('Птица', 'Блюда из курицы, индейки, утки', 70, TRUE, NOW(), NOW());

--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM menu_categories WHERE name = 'Рыба и морепродукты'
INSERT INTO menu_categories (name, description, display_order, is_active, created_at, updated_at) VALUES ('Рыба и морепродукты', 'Рыбные блюда и морепродукты', 80, TRUE, NOW(), NOW());

--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM menu_categories WHERE name = 'Пицца'
INSERT INTO menu_categories (name, description, display_order, is_active, created_at, updated_at) VALUES ('Пицца', 'Пицца разных видов', 90, TRUE, NOW(), NOW());

--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM menu_categories WHERE name = 'Паста'
INSERT INTO menu_categories (name, description, display_order, is_active, created_at, updated_at) VALUES ('Паста', 'Макаронные изделия', 100, TRUE, NOW(), NOW());

--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM menu_categories WHERE name = 'Бургеры'
INSERT INTO menu_categories (name, description, display_order, is_active, created_at, updated_at) VALUES ('Бургеры', 'Бургеры и сэндвичи', 110, TRUE, NOW(), NOW());

--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM menu_categories WHERE name = 'Гарниры'
INSERT INTO menu_categories (name, description, display_order, is_active, created_at, updated_at) VALUES ('Гарниры', 'Картофель, рис, овощи, крупы', 120, TRUE, NOW(), NOW());

--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM menu_categories WHERE name = 'Десерты'
INSERT INTO menu_categories (name, description, display_order, is_active, created_at, updated_at) VALUES ('Десерты', 'Сладкие блюда, торты, пирожные', 130, TRUE, NOW(), NOW());

--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM menu_categories WHERE name = 'Напитки'
INSERT INTO menu_categories (name, description, display_order, is_active, created_at, updated_at) VALUES ('Напитки', 'Безалкогольные напитки', 140, TRUE, NOW(), NOW());

--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM menu_categories WHERE name = 'Алкогольные напитки'
INSERT INTO menu_categories (name, description, display_order, is_active, created_at, updated_at) VALUES ('Алкогольные напитки', 'Вино, пиво, коктейли, крепкие напитки', 150, TRUE, NOW(), NOW());

--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM menu_categories WHERE name = 'Завтраки'
INSERT INTO menu_categories (name, description, display_order, is_active, created_at, updated_at) VALUES ('Завтраки', 'Блюда для завтрака', 160, TRUE, NOW(), NOW());

--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM menu_categories WHERE name = 'Детское меню'
INSERT INTO menu_categories (name, description, display_order, is_active, created_at, updated_at) VALUES ('Детское меню', 'Блюда для детей', 170, TRUE, NOW(), NOW());

--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM menu_categories WHERE name = 'Вегетарианские блюда'
INSERT INTO menu_categories (name, description, display_order, is_active, created_at, updated_at) VALUES ('Вегетарианские блюда', 'Вегетарианские опции', 180, TRUE, NOW(), NOW());

--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM menu_categories WHERE name = 'Веганские блюда'
INSERT INTO menu_categories (name, description, display_order, is_active, created_at, updated_at) VALUES ('Веганские блюда', 'Веганские опции', 190, TRUE, NOW(), NOW());

--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM menu_categories WHERE name = 'Соусы и добавки'
INSERT INTO menu_categories (name, description, display_order, is_active, created_at, updated_at) VALUES ('Соусы и добавки', 'Соусы, приправы, дополнительные ингредиенты', 200, TRUE, NOW(), NOW());

