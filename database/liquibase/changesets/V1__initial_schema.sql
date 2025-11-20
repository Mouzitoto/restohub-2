--liquibase formatted sql

--changeset resto-hub:1
--comment: Create roles table
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP NULL
);

--changeset resto-hub:2
--comment: Create images table
CREATE TABLE images (
    id BIGSERIAL PRIMARY KEY,
    image_data BYTEA NOT NULL,
    preview_data BYTEA NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP NULL
);

--changeset resto-hub:3
--comment: Create restaurants table
CREATE TABLE restaurants (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500),
    phone VARCHAR(50),
    whatsapp VARCHAR(50),
    instagram VARCHAR(255),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    description TEXT,
    working_hours TEXT,
    manager_language_code VARCHAR(10) NOT NULL DEFAULT 'ru',
    logo_image_id BIGINT REFERENCES images(id),
    bg_image_id BIGINT REFERENCES images(id),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP NULL
);

--changeset resto-hub:4
--comment: Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role_id BIGINT NOT NULL REFERENCES roles(id),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP NULL
);

--changeset resto-hub:5
--comment: Create users_2_restaurants table
CREATE TABLE users_2_restaurants (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, restaurant_id)
);

--changeset resto-hub:6
--comment: Create subscription_types table
CREATE TABLE subscription_types (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP NULL
);

--changeset resto-hub:7
--comment: Create restaurant_subscriptions table
CREATE TABLE restaurant_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    subscription_type_id BIGINT NOT NULL REFERENCES subscription_types(id),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

--changeset resto-hub:8
--comment: Create menu_categories table
CREATE TABLE menu_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    display_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP NULL
);

--changeset resto-hub:9
--comment: Create menu_items table
CREATE TABLE menu_items (
    id BIGSERIAL PRIMARY KEY,
    restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    menu_category_id BIGINT NOT NULL REFERENCES menu_categories(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    ingredients TEXT,
    price DECIMAL(10, 2) NOT NULL,
    discount_percent INTEGER DEFAULT 0,
    spiciness_level INTEGER DEFAULT 0,
    has_sugar BOOLEAN NOT NULL DEFAULT FALSE,
    image_id BIGINT REFERENCES images(id),
    display_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP NULL
);

--changeset resto-hub:10
--comment: Create floors table
CREATE TABLE floors (
    id BIGSERIAL PRIMARY KEY,
    restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    floor_number VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP NULL,
    UNIQUE(restaurant_id, floor_number)
);

--changeset resto-hub:11
--comment: Create rooms table
CREATE TABLE rooms (
    id BIGSERIAL PRIMARY KEY,
    floor_id BIGINT NOT NULL REFERENCES floors(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_smoking BOOLEAN NOT NULL DEFAULT FALSE,
    is_outdoor BOOLEAN NOT NULL DEFAULT FALSE,
    image_id BIGINT REFERENCES images(id),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP NULL
);

--changeset resto-hub:12
--comment: Create tables table
CREATE TABLE tables (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL REFERENCES rooms(id) ON DELETE CASCADE,
    table_number VARCHAR(50) NOT NULL,
    capacity INTEGER NOT NULL,
    description TEXT,
    image_id BIGINT REFERENCES images(id),
    deposit_amount TEXT,
    deposit_note TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP NULL,
    UNIQUE(room_id, table_number)
);

--changeset resto-hub:13
--comment: Create promotion_types table
CREATE TABLE promotion_types (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP NULL
);

--changeset resto-hub:14
--comment: Create promotions table
CREATE TABLE promotions (
    id BIGSERIAL PRIMARY KEY,
    restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    promotion_type_id BIGINT NOT NULL REFERENCES promotion_types(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE NULL,
    image_id BIGINT REFERENCES images(id),
    is_recurring BOOLEAN NOT NULL DEFAULT FALSE,
    recurrence_type VARCHAR(50) NULL,
    recurrence_day_of_week INTEGER NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP NULL
);

--changeset resto-hub:15
--comment: Create clients table
CREATE TABLE clients (
    id BIGSERIAL PRIMARY KEY,
    phone VARCHAR(50) UNIQUE NOT NULL,
    first_name VARCHAR(255),
    first_booking_date TIMESTAMP,
    last_booking_date TIMESTAMP,
    total_bookings INTEGER NOT NULL DEFAULT 0,
    total_pre_orders INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

--changeset resto-hub:16
--comment: Create booking_statuses table
CREATE TABLE booking_statuses (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

--changeset resto-hub:17
--comment: Create bookings table
CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    table_id BIGINT NOT NULL REFERENCES tables(id),
    client_id BIGINT REFERENCES clients(id),
    client_name VARCHAR(255),
    date DATE NOT NULL,
    time TIME NOT NULL,
    person_count INTEGER NOT NULL,
    special_requests TEXT,
    booking_status_id BIGINT NOT NULL REFERENCES booking_statuses(id),
    whatsapp_message_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

--changeset resto-hub:18
--comment: Create booking_history table
CREATE TABLE booking_history (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    booking_status_id BIGINT NOT NULL REFERENCES booking_statuses(id),
    changed_at TIMESTAMP NOT NULL DEFAULT NOW(),
    changed_by BIGINT REFERENCES users(id),
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

--changeset resto-hub:19
--comment: Create booking_pre_orders table
CREATE TABLE booking_pre_orders (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    menu_item_id BIGINT NOT NULL REFERENCES menu_items(id),
    quantity INTEGER NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    special_requests TEXT,
    whatsapp_message_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

