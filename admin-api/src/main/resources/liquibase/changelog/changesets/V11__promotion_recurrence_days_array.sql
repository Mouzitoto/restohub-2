--liquibase formatted sql

--changeset resto-hub:34
--comment: Change recurrence_day_of_week from INTEGER to INTEGER[] to support multiple days
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'promotions' AND column_name = 'recurrence_day_of_week' AND data_type = 'integer'

-- Для PostgreSQL используем INTEGER[], для H2 используем ARRAY
-- Сначала создаем временную колонку с массивом
-- В H2 используем ARRAY, в PostgreSQL INTEGER[]
ALTER TABLE promotions ADD COLUMN recurrence_days_of_week INTEGER[];

-- Мигрируем данные: если есть старое значение, создаем массив с одним элементом
-- Используем совместимый синтаксис
UPDATE promotions 
SET recurrence_days_of_week = ARRAY[recurrence_day_of_week]
WHERE recurrence_day_of_week IS NOT NULL;

-- Удаляем старую колонку
ALTER TABLE promotions DROP COLUMN recurrence_day_of_week;

-- Переименовываем новую колонку
ALTER TABLE promotions RENAME COLUMN recurrence_days_of_week TO recurrence_day_of_week;

