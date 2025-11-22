--liquibase formatted sql

--changeset resto-hub:24
--comment: Add email_verified field to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verified BOOLEAN NOT NULL DEFAULT FALSE;

--changeset resto-hub:25
--comment: Create email_verification_codes table
CREATE TABLE email_verification_codes (
    id BIGSERIAL PRIMARY KEY,
    user_email VARCHAR(255) NOT NULL,
    code VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    used_at TIMESTAMP NULL
);

CREATE INDEX idx_email_verification_codes_user_email ON email_verification_codes(user_email);
CREATE INDEX idx_email_verification_codes_code ON email_verification_codes(code);
CREATE INDEX idx_email_verification_codes_expires_at ON email_verification_codes(expires_at);
CREATE INDEX idx_email_verification_codes_used ON email_verification_codes(used);

