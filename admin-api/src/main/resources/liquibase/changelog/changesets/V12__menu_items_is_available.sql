--liquibase formatted sql

--changeset resto-hub:35
--comment: Add is_available column to menu_items table
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'menu_items' AND column_name = 'is_available'

ALTER TABLE menu_items ADD COLUMN is_available BOOLEAN NOT NULL DEFAULT TRUE;

