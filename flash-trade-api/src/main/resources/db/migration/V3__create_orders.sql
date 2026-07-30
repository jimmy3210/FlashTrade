-- ===================================================
-- V3: orders 表
-- 订单信息：账户、方向、数量、状态、时间等
-- ===================================================

CREATE TABLE orders (
    id                BIGINT          NOT NULL AUTO_INCREMENT,
    user_id           BIGINT          NOT NULL,
    account_id        VARCHAR(50)     NOT NULL,
    symbol            VARCHAR(20)     NOT NULL COMMENT '证券代码',
    side              VARCHAR(10)     NOT NULL COMMENT 'BUY / SELL',
    quantity          DECIMAL(15, 4)  NOT NULL COMMENT '委托数量',
    price             DECIMAL(15, 4)  NULL COMMENT '委托价格（市价单为 NULL）',
    status            VARCHAR(20)     NOT NULL DEFAULT 'PENDING'
                                      COMMENT 'PENDING/FILLED/PARTIALLY_FILLED/CANCELLED/REJECTED',
    broker_name       VARCHAR(50)     NOT NULL COMMENT '券商标识',
    external_order_id VARCHAR(100)    NULL COMMENT '券商侧的订单 ID',
    created_at        DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    filled_at         DATETIME(6)     NULL COMMENT '成交时间',
    updated_at        DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                      ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    INDEX idx_orders_user_id         (user_id),
    INDEX idx_orders_account_status  (account_id, status),
    INDEX idx_orders_created_at      (created_at),
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '订单信息表';
