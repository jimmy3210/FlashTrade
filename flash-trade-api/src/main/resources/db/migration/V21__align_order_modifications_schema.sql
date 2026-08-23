-- ===================================================
-- V21: align order_modifications schema with JPA entity
-- ===================================================

ALTER TABLE order_modifications
    ADD COLUMN new_order_id BIGINT NULL AFTER external_order_id;

ALTER TABLE order_modifications
    ADD COLUMN error_code VARCHAR(50) NULL AFTER error_message;

ALTER TABLE order_modifications
    ADD COLUMN created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) AFTER processed_at;

ALTER TABLE order_modifications
    ADD COLUMN updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) AFTER created_at;
