-- ===================================================
-- V9: 券商 API 注册表 & 限流规则
-- t_broker_api / t_broker_api_limit
-- ===================================================

-- API 定义表（每种券商的每个 API 端点）
CREATE TABLE t_broker_api (
    id                 BIGINT          NOT NULL AUTO_INCREMENT,
    broker_id          VARCHAR(50)     NOT NULL COMMENT '券商标识（对应 t_broker.broker_code）',
    api_name           VARCHAR(50)     NOT NULL COMMENT 'API 名称（如 GET_BATCH_MARKET_DATA）',
    endpoint_url       VARCHAR(255)    NULL     COMMENT 'API 端点 URL',
    http_method        VARCHAR(10)     NULL     COMMENT 'HTTP 方法：GET / POST / DELETE',
    rate_limit_per_sec INT             NULL     COMMENT '每秒请求上限',
    rate_limit_per_min INT             NULL     COMMENT '每分钟请求上限',
    auth_type          VARCHAR(20)     NULL     COMMENT '授权类型',
    is_active          TINYINT(1)      NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uq_broker_api (broker_id, api_name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '券商 API 定义';

-- API 限流规则（行情批量请求上限等）
CREATE TABLE t_broker_api_limit (
    broker_id               VARCHAR(50)     NOT NULL,
    api_name                VARCHAR(50)     NOT NULL,
    max_symbols_per_request INT             NOT NULL DEFAULT 30 COMMENT '单次批量请求最大 symbol 数',
    qps_limit               INT             NOT NULL DEFAULT 2  COMMENT '每秒请求次数上限',
    PRIMARY KEY (broker_id, api_name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '券商 API 限流规则';

-- 预填充行情批量请求限制
INSERT INTO t_broker_api_limit (broker_id, api_name, max_symbols_per_request, qps_limit) VALUES
    ('schwab',     'GET_BATCH_MARKET_DATA', 30, 2),
    ('etrade',     'GET_BATCH_MARKET_DATA', 25, 1),
    ('tastyworks', 'GET_BATCH_MARKET_DATA', 30, 2);

-- 预填充 API 定义（行情接口）
INSERT INTO t_broker_api (broker_id, api_name, http_method, rate_limit_per_sec, rate_limit_per_min) VALUES
    ('schwab',     'GET_BATCH_MARKET_DATA', 'GET', 2, 120),
    ('schwab',     'GET_POSITIONS',         'GET', 2, 120),
    ('schwab',     'GET_ORDERS',            'GET', 2, 120),
    ('schwab',     'PLACE_ORDER',           'POST', 1, 60),
    ('schwab',     'CANCEL_ORDER',          'DELETE', 1, 60),
    ('etrade',     'GET_BATCH_MARKET_DATA', 'GET', 1, 60),
    ('etrade',     'GET_POSITIONS',         'GET', 1, 60),
    ('etrade',     'GET_ORDERS',            'GET', 1, 60),
    ('etrade',     'PLACE_ORDER',           'POST', 1, 30),
    ('etrade',     'CANCEL_ORDER',          'DELETE', 1, 30),
    ('tastyworks', 'GET_BATCH_MARKET_DATA', 'GET', 2, 120),
    ('tastyworks', 'GET_POSITIONS',         'GET', 2, 120),
    ('tastyworks', 'GET_ORDERS',            'GET', 2, 120),
    ('tastyworks', 'PLACE_ORDER',           'POST', 1, 60),
    ('tastyworks', 'CANCEL_ORDER',          'DELETE', 1, 60);
