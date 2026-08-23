-- ===================================================
-- V20: align order_fills schema with JPA entity definition
-- ===================================================

-- Ensure legacy databases created by earlier V16 migration receive the fields expected by OrderFill entity.
ALTER TABLE order_fills
    ADD COLUMN created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) AFTER filled_at;

ALTER TABLE order_fills
    ADD COLUMN cumulative_quantity DECIMAL(15,4) NOT NULL DEFAULT 0.0000 AFTER created_at;

ALTER TABLE order_fills
    ADD COLUMN cumulative_commission DECIMAL(15,4) NULL DEFAULT 0.0000 AFTER cumulative_quantity;

ALTER TABLE order_fills
    ADD COLUMN cumulative_value DECIMAL(15,4) NULL DEFAULT 0.0000 AFTER cumulative_commission;

-- Backfill any legacy NULL external fill IDs before enforcing the model contract.
UPDATE order_fills
SET external_fill_id = CONCAT('legacy-fill-', id)
WHERE external_fill_id IS NULL;

ALTER TABLE order_fills
    MODIFY external_fill_id VARCHAR(100) NOT NULL;

-- Ensure the table has the expected JPA defaults for the required data fields.
UPDATE order_fills
SET cumulative_quantity = 0.0000
WHERE cumulative_quantity IS NULL;

UPDATE order_fills
SET cumulative_commission = 0.0000
WHERE cumulative_commission IS NULL;

UPDATE order_fills
SET cumulative_value = 0.0000
WHERE cumulative_value IS NULL;
