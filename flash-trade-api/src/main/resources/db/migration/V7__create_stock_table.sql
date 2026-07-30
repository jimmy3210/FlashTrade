-- ===================================================
-- V7: t_stock 股票基础数据表 + 白名单机制
-- ===================================================

CREATE TABLE t_stock (
    id           BIGINT          NOT NULL AUTO_INCREMENT,
    stock_code   VARCHAR(20)     NOT NULL COMMENT '股票代码（如 AAPL）',
    stock_name   VARCHAR(100)    NULL     COMMENT '股票名称',
    exchange     VARCHAR(20)     NULL     COMMENT '交易所（NYSE / NASDAQ / etc.）',
    is_active    TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '是否在白名单中',
    created_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                                 ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_stock_code (stock_code),
    INDEX idx_is_active (is_active)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '股票基础数据表（白名单）';

-- 预填充 Mock 模式常用股票
INSERT INTO t_stock (stock_code, stock_name, exchange) VALUES
    ('AAPL',  'Apple Inc.',                   'NASDAQ'),
    ('MSFT',  'Microsoft Corporation',         'NASDAQ'),
    ('AMZN',  'Amazon.com Inc.',               'NASDAQ'),
    ('NVDA',  'NVIDIA Corporation',            'NASDAQ'),
    ('TSLA',  'Tesla Inc.',                    'NASDAQ'),
    ('GOOGL', 'Alphabet Inc.',                 'NASDAQ'),
    ('META',  'Meta Platforms Inc.',           'NASDAQ'),
    ('SPY',   'SPDR S&P 500 ETF Trust',        'NYSE'),
    ('QQQ',   'Invesco QQQ Trust',             'NASDAQ'),
    ('BRK.B', 'Berkshire Hathaway Inc. Class B','NYSE');
