--liquibase formatted sql

--changeset resto-hub:27
--comment: Add status column to restaurant_subscriptions table
ALTER TABLE restaurant_subscriptions 
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';

--changeset resto-hub:28
--comment: Add payment_reference column to restaurant_subscriptions table
ALTER TABLE restaurant_subscriptions 
ADD COLUMN payment_reference VARCHAR(50);

--changeset resto-hub:29
--comment: Add external_transaction_id column to restaurant_subscriptions table
ALTER TABLE restaurant_subscriptions 
ADD COLUMN external_transaction_id VARCHAR(100);

--changeset resto-hub:30
--comment: Add unique constraint on payment_reference
ALTER TABLE restaurant_subscriptions 
ADD CONSTRAINT uk_restaurant_subscriptions_payment_reference UNIQUE (payment_reference);

--changeset resto-hub:31
--comment: Create indexes for subscription payment fields
CREATE INDEX idx_restaurant_subscriptions_payment_reference ON restaurant_subscriptions(payment_reference);
CREATE INDEX idx_restaurant_subscriptions_status ON restaurant_subscriptions(status);

--changeset resto-hub:32
--comment: Make start_date nullable for DRAFT subscriptions
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'RESTAURANT_SUBSCRIPTIONS' AND COLUMN_NAME = 'START_DATE' AND IS_NULLABLE = 'NO'
ALTER TABLE restaurant_subscriptions 
ALTER COLUMN start_date DROP NOT NULL;

--changeset resto-hub:33
--comment: Make end_date nullable for DRAFT subscriptions
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'RESTAURANT_SUBSCRIPTIONS' AND COLUMN_NAME = 'END_DATE' AND IS_NULLABLE = 'NO'
ALTER TABLE restaurant_subscriptions 
ALTER COLUMN end_date DROP NOT NULL;

--changeset resto-hub:34
--comment: Create subscription_payments table
CREATE TABLE subscription_payments (
    id BIGSERIAL PRIMARY KEY,
    subscription_id BIGINT NOT NULL REFERENCES restaurant_subscriptions(id) ON DELETE CASCADE,
    amount DECIMAL(10, 2) NOT NULL,
    payment_date TIMESTAMP NOT NULL,
    external_transaction_id VARCHAR(100) UNIQUE,
    payment_reference VARCHAR(50),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

--changeset resto-hub:35
--comment: Create indexes for subscription_payments table
CREATE INDEX idx_subscription_payments_subscription_id ON subscription_payments(subscription_id);
CREATE INDEX idx_subscription_payments_external_transaction_id ON subscription_payments(external_transaction_id);
CREATE INDEX idx_subscription_payments_payment_reference ON subscription_payments(payment_reference);

