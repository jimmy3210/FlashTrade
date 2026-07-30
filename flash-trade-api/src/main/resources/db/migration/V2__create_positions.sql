-- ===================================================
-- V2: positions 表
-- 持仓信息：账户、证券代码、数量、成本、市值、盈亏
-- ===================================================

CREATE TABLE positions (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         BIGINT          NOT NULL,
    account_id      VARCHAR(50)     NOT NULL,
    symbol          VARCHAR(20)     NOT NULL COMMENT '证券代码',
    quantity        DECIMAL(15, 4)  NOT NULL DEFAULT 0 COMMENT '持仓数量',
    average_cost    DECIMAL(15, 4)  NOT NULL DEFAULT 0 COMMENT '平均成本价',
    current_price   DECIMAL(15, 4)  NULL COMMENT '当前价格',
    market_value    DECIMAL(15, 4)  NULL COMMENT '市值',
    unrealized_pnl  DECIMAL(15, 4)  NULL COMMENT '未实现盈亏',
    broker_name     VARCHAR(50)     NOT NULL COMMENT '券商标识',
    updated_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                    ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_positions (user_id, account_id, symbol, broker_name),
    CONSTRAINT fk_positions_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '持仓信息表';
