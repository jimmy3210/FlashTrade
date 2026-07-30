-- ===================================================
-- V5: 向 trades 表添加 user_id 字段
--     + 为 external_trade_id / external_order_id 添加唯一约束（用于 Upsert）
-- ===================================================

ALTER TABLE trades
    ADD COLUMN user_id BIGINT NULL COMMENT '用户ID（由 broker sync 回写）' AFTER order_id;

ALTER TABLE trades
    ADD CONSTRAINT fk_trades_user FOREIGN KEY (user_id) REFERENCES users (id);

CREATE INDEX idx_trades_user_id ON trades (user_id);

-- 为 external_trade_id + broker_name 添加唯一约束，用于 Upsert 判断
ALTER TABLE trades
    ADD CONSTRAINT uk_trades_external UNIQUE (external_trade_id, broker_name);

-- 同样为 orders 表的 external_order_id + broker_name 添加唯一约束
ALTER TABLE orders
    ADD CONSTRAINT uk_orders_external UNIQUE (external_order_id, broker_name);
