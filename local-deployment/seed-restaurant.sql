-- SQL скрипт для вставки тестового ресторана "Итальянский уголок"
-- Все ID отрицательные для избежания конфликтов с реальными данными
-- Изображения не вставляются (будут загружены через админку)

BEGIN;

-- ============================================================================
-- 0. УДАЛЕНИЕ СУЩЕСТВУЮЩИХ ДАННЫХ С ОТРИЦАТЕЛЬНЫМИ ID
-- ============================================================================
-- Удаляем в порядке от дочерних таблиц к родительским, чтобы не нарушить
-- каскадные связи (foreign keys)

-- 1. Промо-акции (ссылается на restaurants и promotion_types)
DELETE FROM promotions WHERE id < 0;

-- 2. Связи пользователей с ресторанами (ссылается на users и restaurants)
DELETE FROM users_2_restaurants WHERE restaurant_id < 0;

-- 2.1. Подписки ресторанов (ссылается на restaurants и subscription_types)
DELETE FROM restaurant_subscriptions WHERE restaurant_id < 0;

-- 3. Столы (ссылается на rooms)
DELETE FROM tables WHERE id < 0;

-- 4. Залы (ссылается на floors)
DELETE FROM rooms WHERE id < 0;

-- 5. Этажи (ссылается на restaurants)
DELETE FROM floors WHERE id < 0;

-- 6. Блюда меню (ссылается на restaurants и menu_categories)
DELETE FROM menu_items WHERE id < 0;

-- 7. Ресторан (родительская таблица)
DELETE FROM restaurants WHERE id < 0;

-- ============================================================================
-- 1. РЕСТОРАН
-- ============================================================================

INSERT INTO restaurants (
    id,
    name,
    address,
    phone,
    whatsapp,
    instagram,
    latitude,
    longitude,
    description,
    working_hours,
    manager_language_code,
    logo_image_id,
    bg_image_id,
    is_active,
    created_at,
    updated_at
) VALUES (
    -1,
    'Итальянский уголок',
    'г. Алматы, пр. Абая, д. 150',
    '+7 (727) 123-45-67',
    '+7 (727) 123-45-67',
    '@italian_corner_almaty',
    43.2220,
    76.8512,
    'Уютный итальянский ресторан в самом сердце Алматы. Мы предлагаем аутентичную итальянскую кухню, приготовленную по традиционным рецептам. В нашем меню вы найдете классические блюда: пасту, пиццу, ризотто и многое другое. У нас уютная атмосфера, живая музыка по пятницам и отличное вино из Италии.',
    'Понедельник - Четверг: 12:00 - 23:00' || E'\n' || 'Пятница - Суббота: 12:00 - 01:00' || E'\n' || 'Воскресенье: 12:00 - 23:00',
    'ru',
    NULL,
    NULL,
    TRUE,
    NOW(),
    NOW()
);

-- ============================================================================
-- 1.1. ПОДПИСКА РЕСТОРАНА
-- ============================================================================

-- Активная подписка PREMIUM на год вперед
INSERT INTO restaurant_subscriptions (
    id,
    restaurant_id,
    subscription_type_id,
    start_date,
    end_date,
    is_active,
    status,
    description,
    created_at,
    updated_at
) VALUES (
    -70,
    -1,
    (SELECT id FROM subscription_types WHERE code = 'PREMIUM'),
    CURRENT_DATE,
    CURRENT_DATE + INTERVAL '1 year',
    TRUE,
    'ACTIVATED',
    'Тестовая подписка для тестового ресторана',
    NOW(),
    NOW()
);

-- ============================================================================
-- 2. БЛЮДА МЕНЮ (menu_items)
-- ============================================================================

-- Категория: Паста
INSERT INTO menu_items (
    id, restaurant_id, menu_category_id, name, description, ingredients, price,
    discount_percent, spiciness_level, has_sugar, image_id, display_order,
    is_active, created_at, updated_at
) VALUES
(-100, -1, (SELECT id FROM menu_categories WHERE name = 'Паста'), 'Спагетти Карбонара', 'Классическая паста с беконом, яйцами и пармезаном. Аутентичный рецепт из Рима.', 'Спагетти, бекон, яйца, пармезан, черный перец', 850.00, 0, 0, FALSE, NULL, 1, TRUE, NOW(), NOW()),
(-101, -1, (SELECT id FROM menu_categories WHERE name = 'Паста'), 'Лазанья Болоньезе', 'Слоеная паста с мясным соусом болоньезе, бешамель и сыром', 'Листы лазаньи, фарш, томаты, бешамель, моцарелла, пармезан', 1200.00, 0, 0, FALSE, NULL, 2, TRUE, NOW(), NOW()),
(-102, -1, (SELECT id FROM menu_categories WHERE name = 'Паста'), 'Спагетти Болоньезе', 'Спагетти с традиционным мясным соусом болоньезе', 'Спагетти, фарш, томаты, лук, морковь, сельдерей, пармезан', 750.00, 0, 0, FALSE, NULL, 3, TRUE, NOW(), NOW()),
(-103, -1, (SELECT id FROM menu_categories WHERE name = 'Паста'), 'Феттучине Альфредо', 'Широкие макароны в сливочном соусе с пармезаном', 'Феттучине, сливки, пармезан, сливочное масло', 800.00, 0, 0, FALSE, NULL, 4, TRUE, NOW(), NOW());

-- Категория: Пицца
INSERT INTO menu_items (
    id, restaurant_id, menu_category_id, name, description, ingredients, price,
    discount_percent, spiciness_level, has_sugar, image_id, display_order,
    is_active, created_at, updated_at
) VALUES
(-110, -1, (SELECT id FROM menu_categories WHERE name = 'Пицца'), 'Пицца Маргарита', 'Классическая пицца с томатами, моцареллой и базиликом', 'Тесто, томатный соус, моцарелла, базилик, оливковое масло', 650.00, 0, 0, FALSE, NULL, 1, TRUE, NOW(), NOW()),
(-111, -1, (SELECT id FROM menu_categories WHERE name = 'Пицца'), 'Пицца Пепперони', 'Пицца с острой колбасой пепперони и моцареллой', 'Тесто, томатный соус, моцарелла, пепперони', 750.00, 0, 3, FALSE, NULL, 2, TRUE, NOW(), NOW()),
(-112, -1, (SELECT id FROM menu_categories WHERE name = 'Пицца'), 'Пицца Четыре сыра', 'Пицца с четырьмя видами сыра: моцарелла, горгонзола, пармезан, рикотта', 'Тесто, моцарелла, горгонзола, пармезан, рикотта', 850.00, 0, 0, FALSE, NULL, 3, TRUE, NOW(), NOW()),
(-113, -1, (SELECT id FROM menu_categories WHERE name = 'Пицца'), 'Пицца Каприччоза', 'Пицца с ветчиной, грибами, артишоками и оливками', 'Тесто, томатный соус, моцарелла, ветчина, грибы, артишоки, оливки', 900.00, 0, 0, FALSE, NULL, 4, TRUE, NOW(), NOW());

-- Категория: Основные блюда
INSERT INTO menu_items (
    id, restaurant_id, menu_category_id, name, description, ingredients, price,
    discount_percent, spiciness_level, has_sugar, image_id, display_order,
    is_active, created_at, updated_at
) VALUES
(-120, -1, (SELECT id FROM menu_categories WHERE name = 'Основные блюда'), 'Ризотто с грибами', 'Кремовое ризотто с лесными грибами и пармезаном', 'Рис арборио, лесные грибы, лук, белое вино, пармезан, бульон', 950.00, 0, 0, FALSE, NULL, 1, TRUE, NOW(), NOW()),
(-121, -1, (SELECT id FROM menu_categories WHERE name = 'Основные блюда'), 'Оссобуко', 'Тушеная телячья голень с овощами и белым вином, подается с ризотто', 'Телячья голень, лук, морковь, сельдерей, белое вино, томаты, бульон', 1500.00, 0, 0, FALSE, NULL, 2, TRUE, NOW(), NOW()),
(-122, -1, (SELECT id FROM menu_categories WHERE name = 'Основные блюда'), 'Пармиджана', 'Запеченные баклажаны с томатным соусом и моцареллой', 'Баклажаны, томатный соус, моцарелла, пармезан, базилик', 850.00, 0, 0, FALSE, NULL, 3, TRUE, NOW(), NOW()),
(-123, -1, (SELECT id FROM menu_categories WHERE name = 'Основные блюда'), 'Скалоппине', 'Тонкие кусочки телятины в лимонном соусе с каперсами', 'Телятина, лимон, каперсы, белое вино, масло, бульон', 1300.00, 0, 0, FALSE, NULL, 4, TRUE, NOW(), NOW());

-- Категория: Салаты
INSERT INTO menu_items (
    id, restaurant_id, menu_category_id, name, description, ingredients, price,
    discount_percent, spiciness_level, has_sugar, image_id, display_order,
    is_active, created_at, updated_at
) VALUES
(-130, -1, (SELECT id FROM menu_categories WHERE name = 'Салаты'), 'Капрезе', 'Классический итальянский салат с моцареллой, томатами и базиликом', 'Моцарелла, томаты, базилик, оливковое масло, бальзамический уксус', 550.00, 0, 0, FALSE, NULL, 1, TRUE, NOW(), NOW()),
(-131, -1, (SELECT id FROM menu_categories WHERE name = 'Салаты'), 'Салат с рукколой', 'Свежая руккола с пармезаном, орехами и бальзамическим соусом', 'Руккола, пармезан, кедровые орехи, бальзамический соус, оливковое масло', 450.00, 0, 0, FALSE, NULL, 2, TRUE, NOW(), NOW()),
(-132, -1, (SELECT id FROM menu_categories WHERE name = 'Салаты'), 'Салат с моцареллой', 'Свежая моцарелла с томатами черри, оливками и зеленью', 'Моцарелла, томаты черри, оливки, руккола, оливковое масло', 500.00, 0, 0, FALSE, NULL, 3, TRUE, NOW(), NOW()),
(-133, -1, (SELECT id FROM menu_categories WHERE name = 'Салаты'), 'Салат Цезарь', 'Классический салат Цезарь с курицей, пармезаном и соусом', 'Салат романо, куриная грудка, пармезан, сухарики, соус Цезарь', 650.00, 0, 0, FALSE, NULL, 4, TRUE, NOW(), NOW());

-- Категория: Десерты
INSERT INTO menu_items (
    id, restaurant_id, menu_category_id, name, description, ingredients, price,
    discount_percent, spiciness_level, has_sugar, image_id, display_order,
    is_active, created_at, updated_at
) VALUES
(-140, -1, (SELECT id FROM menu_categories WHERE name = 'Десерты'), 'Тирамису', 'Классический итальянский десерт с кофе, маскарпоне и какао', 'Печенье савоярди, маскарпоне, кофе эспрессо, какао, яйца, сахар', 450.00, 0, 0, TRUE, NULL, 1, TRUE, NOW(), NOW()),
(-141, -1, (SELECT id FROM menu_categories WHERE name = 'Десерты'), 'Панна-котта', 'Нежный сливочный десерт с ягодным соусом', 'Сливки, желатин, ваниль, сахар, ягодный соус', 400.00, 0, 0, TRUE, NULL, 2, TRUE, NOW(), NOW()),
(-142, -1, (SELECT id FROM menu_categories WHERE name = 'Десерты'), 'Канноли', 'Хрустящие трубочки с начинкой из рикотты и шоколада', 'Тесто, рикотта, сахар, шоколад, цукаты', 350.00, 0, 0, TRUE, NULL, 3, TRUE, NOW(), NOW()),
(-143, -1, (SELECT id FROM menu_categories WHERE name = 'Десерты'), 'Джелато', 'Итальянское мороженое (на выбор: ваниль, шоколад, клубника, фисташка)', 'Молоко, сливки, сахар, ваниль/шоколад/клубника/фисташки', 300.00, 0, 0, TRUE, NULL, 4, TRUE, NOW(), NOW());

-- Категория: Алкогольные напитки
INSERT INTO menu_items (
    id, restaurant_id, menu_category_id, name, description, ingredients, price,
    discount_percent, spiciness_level, has_sugar, image_id, display_order,
    is_active, created_at, updated_at
) VALUES
(-150, -1, (SELECT id FROM menu_categories WHERE name = 'Алкогольные напитки'), 'Кьянти', 'Итальянское красное вино из Тосканы, 0.75 л', 'Виноград санджовезе', 2500.00, 0, 0, FALSE, NULL, 1, TRUE, NOW(), NOW()),
(-151, -1, (SELECT id FROM menu_categories WHERE name = 'Алкогольные напитки'), 'Просекко', 'Итальянское игристое вино, 0.75 л', 'Виноград глера', 2200.00, 0, 0, FALSE, NULL, 2, TRUE, NOW(), NOW()),
(-152, -1, (SELECT id FROM menu_categories WHERE name = 'Алкогольные напитки'), 'Лимончелло', 'Итальянский ликер из лимонов, 0.5 л', 'Лимоны, спирт, сахар', 1800.00, 0, 0, TRUE, NULL, 3, TRUE, NOW(), NOW()),
(-153, -1, (SELECT id FROM menu_categories WHERE name = 'Алкогольные напитки'), 'Граппа', 'Итальянская виноградная водка, 0.5 л', 'Виноградные выжимки', 2000.00, 0, 0, FALSE, NULL, 4, TRUE, NOW(), NOW());

-- ============================================================================
-- 3. ЭТАЖИ (floors)
-- ============================================================================

INSERT INTO floors (
    id, restaurant_id, floor_number, is_active, created_at, updated_at
) VALUES
(-20, -1, '1', TRUE, NOW(), NOW()),
(-21, -1, '2', TRUE, NOW(), NOW());

-- ============================================================================
-- 4. ЗАЛЫ (rooms)
-- ============================================================================

INSERT INTO rooms (
    id, floor_id, name, description, is_smoking, is_outdoor, image_id,
    is_active, created_at, updated_at
) VALUES
(-30, -20, 'Основной зал', 'Просторный зал на первом этаже с уютной атмосферой. Идеально подходит для обеда и ужина.', FALSE, FALSE, NULL, TRUE, NOW(), NOW()),
(-31, -20, 'VIP зал', 'Уютный зал для особых случаев и деловых встреч. Отдельная комната с изысканным интерьером.', FALSE, FALSE, NULL, TRUE, NOW(), NOW()),
(-32, -21, 'Терасса на крыше', 'Открытая терраса на втором этаже с панорамным видом. Можно курить. Идеально для вечерних посиделок и романтических ужинов.', TRUE, TRUE, NULL, TRUE, NOW(), NOW());

-- ============================================================================
-- 5. СТОЛЫ (tables)
-- ============================================================================

-- Основной зал (10 столов)
INSERT INTO tables (
    id, room_id, table_number, capacity, description, image_id,
    deposit_amount, deposit_note, position_x1, position_y1, position_x2, position_y2,
    is_active, created_at, updated_at
) VALUES
(-40, -30, '1', 2, 'Уютный столик у окна', NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, NOW(), NOW()),
(-41, -30, '2', 2, 'Столик в центре зала', NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, NOW(), NOW()),
(-42, -30, '3', 4, 'Стол для компании', NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, NOW(), NOW()),
(-43, -30, '4', 4, 'Стол для семьи', NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, NOW(), NOW()),
(-44, -30, '5', 6, 'Большой стол для компании друзей', NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, NOW(), NOW()),
(-45, -30, '6', 2, 'Романтический столик', NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, NOW(), NOW()),
(-46, -30, '7', 4, 'Стол для компании', NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, NOW(), NOW()),
(-47, -30, '8', 4, 'Стол для семьи', NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, NOW(), NOW()),
(-48, -30, '9', 6, 'Большой стол для компании друзей', NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, NOW(), NOW()),
(-49, -30, '10', 2, 'Уютный столик для двоих', NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, NOW(), NOW());

-- VIP зал (1 стол)
INSERT INTO tables (
    id, room_id, table_number, capacity, description, image_id,
    deposit_amount, deposit_note, position_x1, position_y1, position_x2, position_y2,
    is_active, created_at, updated_at
) VALUES
(-50, -31, '1', 10, 'Большой премиальный стол для особых случаев и деловых встреч', NULL, '5000', 'Депозит требуется для бронирования VIP стола', NULL, NULL, NULL, NULL, TRUE, NOW(), NOW());

-- Терасса на крыше (6 столов)
INSERT INTO tables (
    id, room_id, table_number, capacity, description, image_id,
    deposit_amount, deposit_note, position_x1, position_y1, position_x2, position_y2,
    is_active, created_at, updated_at
) VALUES
(-51, -32, '1', 2, 'Романтический столик с видом на город', NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, NOW(), NOW()),
(-52, -32, '2', 2, 'Столик у перил террасы', NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, NOW(), NOW()),
(-53, -32, '3', 4, 'Стол для компании на террасе', NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, NOW(), NOW()),
(-54, -32, '4', 4, 'Стол для вечерних посиделок', NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, NOW(), NOW()),
(-55, -32, '5', 6, 'Большой стол для компании друзей', NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, NOW(), NOW()),
(-56, -32, '6', 4, 'Стол для компании с панорамным видом', NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, NOW(), NOW());

-- ============================================================================
-- 6. АКЦИИ/ПРОМО (promotions)
-- ============================================================================

-- 1. Скидка 20% на все блюда
INSERT INTO promotions (
    id, restaurant_id, promotion_type_id, title, description,
    start_date, end_date, image_id, is_recurring, recurrence_type,
    recurrence_day_of_week, is_active, created_at, updated_at
) VALUES (
    -60,
    -1,
    (SELECT id FROM promotion_types WHERE code = 'DISCOUNT'),
    'Скидка 20% на все блюда',
    'Специальное предложение! Скидка 20% на все блюда из меню. Акция действует до конца месяца.',
    '2024-01-01',
    '2024-01-31',
    NULL,
    FALSE,
    NULL,
    NULL,
    TRUE,
    NOW(),
    NOW()
);

-- 2. Живая музыка каждую пятницу
INSERT INTO promotions (
    id, restaurant_id, promotion_type_id, title, description,
    start_date, end_date, image_id, is_recurring, recurrence_type,
    recurrence_day_of_week, is_active, created_at, updated_at
) VALUES (
    -61,
    -1,
    (SELECT id FROM promotion_types WHERE code = 'THEMATIC_EVENT'),
    'Живая музыка каждую пятницу',
    'Каждую пятницу с 20:00 до 23:00 в нашем ресторане играет живая музыка. Приходите насладиться итальянскими мелодиями!',
    '2024-01-01',
    NULL,
    NULL,
    TRUE,
    'WEEKLY',
    ARRAY[5],
    TRUE,
    NOW(),
    NOW()
);

-- 3. Счастливые часы 17:00-19:00
INSERT INTO promotions (
    id, restaurant_id, promotion_type_id, title, description,
    start_date, end_date, image_id, is_recurring, recurrence_type,
    recurrence_day_of_week, is_active, created_at, updated_at
) VALUES (
    -62,
    -1,
    (SELECT id FROM promotion_types WHERE code = 'HAPPY_HOUR'),
    'Счастливые часы 17:00-19:00',
    'Каждый день с 17:00 до 19:00 скидка 30% на все напитки и закуски!',
    '2024-01-01',
    NULL,
    NULL,
    TRUE,
    'DAILY',
    NULL,
    TRUE,
    NOW(),
    NOW()
);

-- 4. Новое блюдо в меню
INSERT INTO promotions (
    id, restaurant_id, promotion_type_id, title, description,
    start_date, end_date, image_id, is_recurring, recurrence_type,
    recurrence_day_of_week, is_active, created_at, updated_at
) VALUES (
    -63,
    -1,
    (SELECT id FROM promotion_types WHERE code = 'NEW_ITEM'),
    'Новое блюдо в меню',
    'Мы рады представить новое блюдо - Труффеле паста с трюфелями и пармезаном. Попробуйте первыми!',
    '2024-01-15',
    '2024-02-15',
    NULL,
    FALSE,
    NULL,
    NULL,
    TRUE,
    NOW(),
    NOW()
);

-- ============================================================================
-- 7. СВЯЗЬ ПОЛЬЗОВАТЕЛЕЙ С РЕСТОРАНОМ (users_2_restaurants)
-- ============================================================================

-- Связываем всех пользователей с ролью ADMIN с рестораном
INSERT INTO users_2_restaurants (user_id, restaurant_id, created_at)
SELECT 
    u.id,
    -1,
    NOW()
FROM users u
INNER JOIN roles r ON u.role_id = r.id
WHERE r.code = 'ADMIN'
  AND u.is_active = TRUE
  AND u.deleted_at IS NULL
  AND NOT EXISTS (
      SELECT 1 
      FROM users_2_restaurants u2r 
      WHERE u2r.user_id = u.id 
        AND u2r.restaurant_id = -1
  );

COMMIT;

-- ============================================================================
-- ПРИМЕЧАНИЯ:
-- ============================================================================
-- 1. Все ID отрицательные для избежания конфликтов с реальными данными
-- 2. Изображения не вставляются (image_id = NULL), будут загружены через админку
-- 3. Категории меню и типы промо-акций должны быть созданы миграциями перед запуском этого скрипта
-- 4. Для повторяющихся промо-акций используется массив recurrence_day_of_week (PostgreSQL)
-- 5. Даты в промо-акциях можно изменить на текущие при необходимости

