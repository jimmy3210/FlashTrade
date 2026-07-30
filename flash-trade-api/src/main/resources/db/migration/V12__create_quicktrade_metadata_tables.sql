-- ===================================================
-- V12: QuickTrade 本地元数据表
-- watchlists / user_settings / session_tokens
-- ===================================================

CREATE TABLE watchlists (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    user_id     BIGINT        NOT NULL,
    symbol      VARCHAR(20)   NOT NULL COMMENT '股票代码',
    sort_order  INT           NOT NULL DEFAULT 0 COMMENT '自选排序',
    created_at  DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uq_watchlists_user_symbol (user_id, symbol),
    INDEX idx_watchlists_user_id (user_id),
    CONSTRAINT fk_watchlists_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '用户自选股表';

CREATE TABLE user_settings (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    user_id           BIGINT        NOT NULL,
    theme             VARCHAR(20)   NOT NULL DEFAULT 'light' COMMENT '界面主题',
    refresh_interval  INT           NOT NULL DEFAULT 30 COMMENT '刷新间隔秒数',
    default_broker    VARCHAR(50)   NULL COMMENT '默认 Broker',
    created_at        DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at        DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                        ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uq_user_settings_user_id (user_id),
    INDEX idx_user_settings_user_id (user_id),
    CONSTRAINT fk_user_settings_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '用户设置表';

CREATE TABLE session_tokens (
    id                 BIGINT        NOT NULL AUTO_INCREMENT,
    user_id            BIGINT        NOT NULL,
    broker_id          BIGINT        NOT NULL,
    broker_account_id  BIGINT        NOT NULL,
    token              TEXT          NOT NULL COMMENT 'Broker 会话 token 或授权凭证',
    expires_at         DATETIME(6)   NULL COMMENT 'Token 过期时间',
    created_at         DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at         DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                        ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    INDEX idx_session_tokens_user_id (user_id),
    INDEX idx_session_tokens_expires_at (expires_at),
    INDEX idx_session_tokens_broker_account_id (broker_account_id),
    CONSTRAINT fk_session_tokens_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_session_tokens_broker FOREIGN KEY (broker_id) REFERENCES t_broker (id),
    CONSTRAINT fk_session_tokens_broker_account FOREIGN KEY (broker_account_id) REFERENCES t_broker_account (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Broker 会话 token 表';
