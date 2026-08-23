-- ===================================================
-- V22: align order quantity columns with JPA entity
-- ===================================================

ALTER TABLE orders
    MODIFY COLUMN filled_quantity DECIMAL(15,4) NOT NULL DEFAULT 0.0000;
