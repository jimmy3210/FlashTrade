ALTER TABLE orders
    ADD COLUMN client_order_id VARCHAR(100) NULL COMMENT '客户端幂等 ID' AFTER external_order_id,
    ADD UNIQUE KEY uq_orders_idempotency (user_id, account_id, broker_name, client_order_id);
