-- ===================================================
-- V24: align watchlists schema with JPA entity
-- ===================================================

ALTER TABLE watchlists
    ADD COLUMN broker_name VARCHAR(50) NULL AFTER symbol;
