-- ===================================================
-- V4: trades 表
-- 成交信息：关联订单、成交价、数量、手续费、时间
-- ===================================================

CREATE TABLE trades (
    id                BIGINT          NOT NULL AUTO_INCREMENT,
    order_id          BIGINT          NULL COMMENT '关联订单（外部导入时可为 NULL）',
    account_id        VARCHAR(50)     NOT NULL,
    symbol            VARCHAR(20)     NOT NULL COMMENT '证券代码',
    side              VARCHAR(10)     NOT NULL COMMENT 'BUY / SELL',
    quantity          DECIMAL(15, 4)  NOT NULL COMMENT '成交数量',
    price             DECIMAL(15, 4)  NOT NULL COMMENT '成交价格',
    commission        DECIMAL(10, 4)  NOT NULL DEFAULT 0 COMMENT '手续费',
    broker_name       VARCHAR(50)     NOT NULL COMMENT '券商标识',
    external_trade_id VARCHAR(100)    NULL COMMENT '券商侧的成交 ID',
    traded_at         DATETIME(6)     NOT NULL COMMENT '成交时间',
    created_at        DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    INDEX idx_trades_order_id         (order_id),
    INDEX idx_trades_account_traded   (account_id, traded_at),
    CONSTRAINT fk_trades_order FOREIGN KEY (order_id) REFERENCES orders (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '成交信息表';
