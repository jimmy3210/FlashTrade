-- ===================================================
-- V23: align session_tokens schema with JPA entity
-- ===================================================

ALTER TABLE session_tokens
    DROP FOREIGN KEY fk_session_tokens_broker_account;

ALTER TABLE session_tokens
    MODIFY COLUMN broker_account_id VARCHAR(100) NOT NULL;
