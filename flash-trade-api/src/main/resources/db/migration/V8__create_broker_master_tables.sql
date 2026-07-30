-- ===================================================
-- V8: 券商主数据表群
-- t_broker / t_broker_dev_account / t_broker_account / t_broker_account_permission
-- ===================================================

-- 券商基础信息
CREATE TABLE t_broker (
    id           BIGINT          NOT NULL AUTO_INCREMENT,
    broker_code  VARCHAR(50)     NOT NULL COMMENT '内部标识（如 schwab / etrade）',
    broker_name  VARCHAR(100)    NOT NULL COMMENT '显示名称',
    auth_type    VARCHAR(20)     NULL     COMMENT '授权类型：OAUTH2 / OAUTH1A / API_KEY',
    is_active    TINYINT(1)      NOT NULL DEFAULT 1,
    created_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_broker_code (broker_code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '券商基础信息';

-- 预填充已知券商
INSERT INTO t_broker (broker_code, broker_name, auth_type) VALUES
    ('schwab',     'Charles Schwab', 'OAUTH2'),
    ('etrade',     'E*TRADE',        'OAUTH1A'),
    ('tastyworks', 'Tastyworks',     'API_KEY');

-- 开发者账号（App Key/Secret — 生产环境通过 Vault 管理）
CREATE TABLE t_broker_dev_account (
    id           BIGINT          NOT NULL AUTO_INCREMENT,
    broker_id    BIGINT          NOT NULL,
    app_key      VARCHAR(255)    NOT NULL COMMENT 'App Key（可明文）',
    app_secret   VARCHAR(255)    NOT NULL COMMENT 'App Secret（生产环境通过 Vault 注入）',
    is_active    TINYINT(1)      NOT NULL DEFAULT 1,
    created_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_dev_account_broker (broker_id),
    CONSTRAINT fk_dev_account_broker FOREIGN KEY (broker_id) REFERENCES t_broker (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '券商开发者账号（App Key / App Secret）';

-- 用户券商账户（交易账户）—— 当前通过 application.yml 管理，此表为 DB 化迁移目标
CREATE TABLE t_broker_account (
    id                BIGINT          NOT NULL AUTO_INCREMENT,
    user_id           BIGINT          NOT NULL,
    broker_id         BIGINT          NOT NULL,
    account_no        VARCHAR(100)    NOT NULL COMMENT '券商内部账号',
    account_type      VARCHAR(20)     NOT NULL DEFAULT 'CASH' COMMENT 'CASH / MARGIN',
    dev_account_id    BIGINT          NULL     COMMENT '对应的开发者账号',
    status            VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE / SUSPENDED / CLOSED',
    created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_broker_account (broker_id, account_no),
    INDEX idx_broker_account_user (user_id),
    CONSTRAINT fk_broker_account_user   FOREIGN KEY (user_id)        REFERENCES users (id),
    CONSTRAINT fk_broker_account_broker FOREIGN KEY (broker_id)      REFERENCES t_broker (id),
    CONSTRAINT fk_broker_account_dev    FOREIGN KEY (dev_account_id) REFERENCES t_broker_dev_account (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '用户券商账户';

-- 券商账户权限
CREATE TABLE t_broker_account_permission (
    id            BIGINT          NOT NULL AUTO_INCREMENT,
    account_id    BIGINT          NOT NULL,
    permission    VARCHAR(50)     NOT NULL COMMENT 'TRADE / READ_ONLY / MARKET_DATA',
    granted_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_account_permission (account_id, permission),
    CONSTRAINT fk_bap_account FOREIGN KEY (account_id) REFERENCES t_broker_account (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '券商账户权限';
