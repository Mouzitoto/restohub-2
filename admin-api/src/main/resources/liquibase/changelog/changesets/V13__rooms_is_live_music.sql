--liquibase formatted sql

--changeset resto-hub:36
--comment: Add is_live_music column to rooms table
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'rooms' AND column_name = 'is_live_music'

ALTER TABLE rooms ADD COLUMN is_live_music BOOLEAN NOT NULL DEFAULT FALSE;
