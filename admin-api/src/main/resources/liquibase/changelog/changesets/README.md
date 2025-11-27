# Правила написания миграций Liquibase

## Важно: Совместимость с H2 и PostgreSQL

Все миграции должны быть совместимы как с **PostgreSQL** (продакшн), так и с **H2** (тесты). H2 используется в тестах в режиме совместимости с PostgreSQL (`MODE=PostgreSQL`), но имеет некоторые ограничения.

## Основные правила

### 1. Information Schema

**❌ НЕ используйте:**
- `udt_name` - эта колонка отсутствует в H2
- Специфичные для PostgreSQL типы данных в `information_schema`

**✅ Используйте:**
- `data_type` - работает в обеих БД
- Базовые типы данных (INTEGER, VARCHAR, TEXT, BOOLEAN, TIMESTAMP)

**Пример:**
```sql
-- ❌ Плохо (не работает в H2)
--precondition-sql-check expectedResult:1 
SELECT COUNT(*) FROM information_schema.columns 
WHERE table_name = 'promotions' 
AND column_name = 'recurrence_day_of_week' 
AND (data_type = 'integer' OR udt_name = 'int4')

-- ✅ Хорошо (работает в обеих БД)
--precondition-sql-check expectedResult:1 
SELECT COUNT(*) FROM information_schema.columns 
WHERE table_name = 'promotions' 
AND column_name = 'recurrence_day_of_week' 
AND data_type = 'integer'
```

### 2. Типы данных

**✅ Совместимые типы:**
- `INTEGER`, `BIGINT`, `SMALLINT`
- `VARCHAR(n)`, `TEXT`
- `BOOLEAN`
- `TIMESTAMP`, `DATE`
- `DECIMAL`, `NUMERIC`
- `BYTEA` (для бинарных данных)

**⚠️ Осторожно с массивами:**
- `INTEGER[]` - работает в PostgreSQL, но требует специальной обработки в H2
- Используйте `@JdbcTypeCode(SqlTypes.ARRAY)` в JPA entity для правильного маппинга
- В миграциях можно использовать `INTEGER[]`, H2 обработает это через Hibernate

**Пример:**
```sql
-- ✅ Работает в обеих БД
ALTER TABLE promotions ADD COLUMN recurrence_days_of_week INTEGER[];

-- В entity используйте:
-- @Column(name = "recurrence_days_of_week")
-- @JdbcTypeCode(SqlTypes.ARRAY)
-- private List<Integer> recurrenceDaysOfWeek;
```

### 3. Функции и операторы

**✅ Совместимые функции:**
- `NOW()` - текущая дата/время
- `ARRAY[...]` - создание массива (работает в обеих БД)
- Базовые SQL функции (COUNT, SUM, MAX, MIN и т.д.)

**❌ Избегайте:**
- PostgreSQL-специфичные функции (например, `array_agg`, `jsonb_*`)
- Расширенные типы данных (JSONB, UUID без кавычек)

### 4. Preconditions

**✅ Рекомендуемый подход:**
```sql
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:1 
SELECT COUNT(*) FROM information_schema.columns 
WHERE table_name = 'table_name' 
AND column_name = 'column_name' 
AND data_type = 'integer'
```

**Правила:**
- Всегда используйте `onFail:MARK_RAN` для предотвращения ошибок при повторном запуске
- Используйте только базовые проверки `information_schema`
- Избегайте проверок специфичных для PostgreSQL колонок

### 5. Синтаксис DDL

**✅ Совместимые операции:**
- `CREATE TABLE`, `ALTER TABLE`, `DROP TABLE`
- `ADD COLUMN`, `DROP COLUMN`, `RENAME COLUMN`
- `CREATE INDEX`, `DROP INDEX`
- `ALTER TABLE ... ADD CONSTRAINT`, `DROP CONSTRAINT`

**Пример миграции:**
```sql
--liquibase formatted sql

--changeset resto-hub:XX
--comment: Описание изменений
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 
SELECT COUNT(*) FROM information_schema.tables 
WHERE table_name = 'new_table'

CREATE TABLE new_table (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

### 6. Миграция данных

**✅ Безопасный подход:**
```sql
-- Создаем новую колонку
ALTER TABLE table_name ADD COLUMN new_column INTEGER[];

-- Мигрируем данные
UPDATE table_name 
SET new_column = ARRAY[old_column]
WHERE old_column IS NOT NULL;

-- Удаляем старую колонку
ALTER TABLE table_name DROP COLUMN old_column;
```

### 7. Тестирование

**Перед коммитом:**
1. Убедитесь, что миграция проходит в тестах (используется H2)
2. Проверьте, что миграция работает на PostgreSQL
3. Убедитесь, что `onFail:MARK_RAN` предотвращает ошибки при повторном запуске

**Запуск тестов:**
```bash
mvn test
```

## Частые проблемы и решения

### Проблема: "Column UDT_NAME not found"
**Решение:** Уберите проверку `udt_name` из preconditions, используйте только `data_type`

### Проблема: "Array type not supported"
**Решение:** Используйте `@JdbcTypeCode(SqlTypes.ARRAY)` в JPA entity вместо `columnDefinition`

### Проблема: "Syntax error in SQL"
**Решение:** Проверьте, что используете только совместимый SQL синтаксис, избегайте специфичных для PostgreSQL конструкций

## Примеры хороших миграций

Смотрите существующие миграции:
- `V1__initial_schema.sql` - создание базовых таблиц
- `V11__promotion_recurrence_days_array.sql` - работа с массивами
- `V7__insert_subscription_types.sql` - вставка данных

## Дополнительные ресурсы

- [Liquibase Documentation](https://docs.liquibase.com/)
- [H2 Database Compatibility](https://www.h2database.com/html/features.html#compatibility)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

