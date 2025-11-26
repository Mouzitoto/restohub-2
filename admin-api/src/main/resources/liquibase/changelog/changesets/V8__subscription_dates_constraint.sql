--liquibase formatted sql

--changeset resto-hub:36
--comment: Make start_date nullable (if not already nullable)
ALTER TABLE restaurant_subscriptions 
ALTER COLUMN start_date DROP NOT NULL;

--changeset resto-hub:37
--comment: Make end_date nullable (if not already nullable)
ALTER TABLE restaurant_subscriptions 
ALTER COLUMN end_date DROP NOT NULL;

--changeset resto-hub:38
--comment: Add check constraint: if is_active = true, then start_date and end_date must be NOT NULL
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM restaurant_subscriptions WHERE is_active = true AND (start_date IS NULL OR end_date IS NULL)
ALTER TABLE restaurant_subscriptions
ADD CONSTRAINT ck_subscription_dates_when_active 
CHECK (NOT is_active OR (start_date IS NOT NULL AND end_date IS NOT NULL));

