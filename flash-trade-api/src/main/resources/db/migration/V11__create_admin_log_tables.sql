-- ===================================================
-- V11: 后台管理系统表群
-- t_company / t_group / t_user_scope
-- t_api_log / t_error_log / t_ws_log / t_login_log / t_operation_log
-- ===================================================

-- 公司（租户）
CREATE TABLE t_company (
    id           BIGINT          NOT NULL AUTO_INCREMENT,
    name         VARCHAR(100)    NOT NULL COMMENT '公司名称',
    is_active    TINYINT(1)      NOT NULL DEFAULT 1,
    created_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_company_name (name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '公司（租户）';

-- 组
CREATE TABLE t_group (
    id           BIGINT          NOT NULL AUTO_INCREMENT,
    company_id   BIGINT          NOT NULL,
    name         VARCHAR(100)    NOT NULL COMMENT '组名称',
    is_active    TINYINT(1)      NOT NULL DEFAULT 1,
    created_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_group_company (company_id),
    CONSTRAINT fk_group_company FOREIGN KEY (company_id) REFERENCES t_company (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '用户组';

-- 管理员权限范围（Scope）
CREATE TABLE t_user_scope (
    id                BIGINT          NOT NULL AUTO_INCREMENT,
    user_id           BIGINT          NOT NULL COMMENT '管理员用户 ID',
    target_user_id    BIGINT          NULL     COMMENT '可管理的目标用户',
    target_account_id VARCHAR(100)    NULL     COMMENT '可管理的目标账户',
    PRIMARY KEY (id),
    INDEX idx_user_scope_user (user_id),
    CONSTRAINT fk_scope_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '管理员权限范围';

-- API 调用日志
CREATE TABLE t_api_log (
    id             BIGINT          NOT NULL AUTO_INCREMENT,
    broker         VARCHAR(50)     NULL     COMMENT '券商标识',
    api_name       VARCHAR(100)    NULL     COMMENT 'API 名称',
    account_id     VARCHAR(100)    NULL     COMMENT '账户 ID',
    request_body   TEXT            NULL     COMMENT '请求体（脱敏）',
    response_body  TEXT            NULL     COMMENT '响应体摘要（脱敏）',
    latency_ms     INT             NULL     COMMENT '响应耗时（毫秒）',
    status         VARCHAR(20)     NULL     COMMENT 'SUCCESS / ERROR',
    created_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_api_log_broker (broker),
    INDEX idx_api_log_created_at (created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'API 调用日志';

-- 错误日志
CREATE TABLE t_error_log (
    id           BIGINT          NOT NULL AUTO_INCREMENT,
    error_type   VARCHAR(50)     NULL     COMMENT '错误类型',
    message      TEXT            NULL     COMMENT '错误消息',
    stack_trace  TEXT            NULL     COMMENT '堆栈跟踪',
    created_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_error_log_type (error_type),
    INDEX idx_error_log_created_at (created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '错误日志';

-- WebSocket 推送日志
CREATE TABLE t_ws_log (
    id            BIGINT          NOT NULL AUTO_INCREMENT,
    session_id    VARCHAR(100)    NULL     COMMENT 'WebSocket 会话 ID',
    symbol        VARCHAR(20)     NULL     COMMENT '股票代码',
    message_type  VARCHAR(50)     NULL     COMMENT '消息类型',
    payload_size  INT             NULL     COMMENT '消息体字节数',
    created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_ws_log_session (session_id),
    INDEX idx_ws_log_created_at (created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'WebSocket 推送日志';

-- 登录日志
CREATE TABLE t_login_log (
    id            BIGINT          NOT NULL AUTO_INCREMENT,
    user_id       BIGINT          NULL     COMMENT '用户 ID',
    username      VARCHAR(50)     NULL     COMMENT '用户名',
    ip_address    VARCHAR(64)     NULL     COMMENT '客户端 IP',
    user_agent    VARCHAR(255)    NULL     COMMENT 'User-Agent',
    login_result  VARCHAR(20)     NULL     COMMENT 'SUCCESS / FAILED',
    fail_reason   VARCHAR(100)    NULL     COMMENT '失败原因',
    created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_login_log_user (user_id),
    INDEX idx_login_log_created_at (created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '登录日志';

-- 操作日志（重要操作审计轨迹）
CREATE TABLE t_operation_log (
    id            BIGINT          NOT NULL AUTO_INCREMENT,
    operator_id   BIGINT          NULL     COMMENT '操作人 ID',
    operator_role VARCHAR(30)     NULL     COMMENT '操作人角色',
    action        VARCHAR(100)    NULL     COMMENT '操作类型（DISABLE_ACCOUNT / LIQUIDATE 等）',
    target_type   VARCHAR(50)     NULL     COMMENT '目标类型：ACCOUNT / USER / SYMBOL',
    target_id     VARCHAR(100)    NULL     COMMENT '目标 ID',
    detail        TEXT            NULL     COMMENT '操作详情（JSON）',
    created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_operation_log_operator (operator_id),
    INDEX idx_operation_log_action (action),
    INDEX idx_operation_log_created_at (created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '操作日志';
