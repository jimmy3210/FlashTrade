-- ===================================================
-- V6: 每日盈亏快照表
-- ===================================================

-- 每日个股盈亏快照
CREATE TABLE t_daily_pnl_stock (
    id               BIGINT          NOT NULL AUTO_INCREMENT,
    broker_account_id VARCHAR(64)    NOT NULL COMMENT '券商账户 ID',
    broker_name      VARCHAR(50)     NOT NULL COMMENT '券商标识',
    symbol           VARCHAR(20)     NOT NULL COMMENT '股票代码',
    snapshot_date    DATE            NOT NULL COMMENT '快照日期',
    net_quantity     INT             NOT NULL DEFAULT 0 COMMENT '当日在仓净股数',
    total_quantity   INT             NOT NULL DEFAULT 0 COMMENT '当日成交总股数（买+卖）',
    avg_cost         DECIMAL(18, 4)  NOT NULL DEFAULT 0 COMMENT '加权平均成本',
    realized_pnl     DECIMAL(18, 4)  NOT NULL DEFAULT 0 COMMENT '当日已实现盈亏',
    unrealized_pnl   DECIMAL(18, 4)  NOT NULL DEFAULT 0 COMMENT '收盘时未实现盈亏',
    last_price       DECIMAL(18, 4)  NULL COMMENT '收盘价',
    market_value     DECIMAL(18, 4)  NULL COMMENT '收盘市值',
    cost_value       DECIMAL(18, 4)  NULL COMMENT '持仓成本',
    created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_daily_pnl_stock (broker_account_id, symbol, snapshot_date),
    INDEX idx_snapshot_date (snapshot_date),
    INDEX idx_symbol_date (symbol, snapshot_date)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '每日个股盈亏快照';

-- 每日账户总盈亏快照
CREATE TABLE t_daily_pnl_account (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    broker_account_id   VARCHAR(64)     NOT NULL COMMENT '券商账户 ID',
    broker_name         VARCHAR(50)     NOT NULL COMMENT '券商标识',
    snapshot_date       DATE            NOT NULL COMMENT '快照日期',
    total_pnl           DECIMAL(18, 4)  NOT NULL DEFAULT 0 COMMENT '账户总盈亏',
    total_realized_pnl  DECIMAL(18, 4)  NOT NULL DEFAULT 0 COMMENT '账户总已实现盈亏',
    total_market_value  DECIMAL(18, 4)  NOT NULL DEFAULT 0 COMMENT '账户总市值',
    total_cost          DECIMAL(18, 4)  NOT NULL DEFAULT 0 COMMENT '账户总持仓成本',
    position_count      INT             NOT NULL DEFAULT 0 COMMENT '持仓股票数',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_daily_pnl_account (broker_account_id, snapshot_date),
    INDEX idx_account_date (broker_account_id, snapshot_date)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '每日账户总盈亏快照';

-- 隔夜持仓滚存快照（供次日计算用）
CREATE TABLE t_position_history (
    id               BIGINT          NOT NULL AUTO_INCREMENT,
    broker_account_id VARCHAR(64)    NOT NULL COMMENT '券商账户 ID',
    broker_name      VARCHAR(50)     NOT NULL COMMENT '券商标识',
    symbol           VARCHAR(20)     NOT NULL COMMENT '股票代码',
    snapshot_date    DATE            NOT NULL COMMENT '快照日期',
    net_quantity     INT             NOT NULL DEFAULT 0 COMMENT '在仓净股数',
    avg_cost         DECIMAL(18, 4)  NOT NULL DEFAULT 0 COMMENT '加权平均成本',
    realized_pnl     DECIMAL(18, 4)  NOT NULL DEFAULT 0 COMMENT '累计已实现盈亏',
    created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_position_history (broker_account_id, symbol, snapshot_date),
    INDEX idx_ph_date (snapshot_date)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '隔夜持仓滚存快照';
