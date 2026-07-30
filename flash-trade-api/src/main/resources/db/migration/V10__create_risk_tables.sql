-- ===================================================
-- V10: 风控相关表
-- t_risk_rule / t_account_risk
-- ===================================================

-- 风控规则定义
CREATE TABLE t_risk_rule (
    id                BIGINT          NOT NULL AUTO_INCREMENT,
    name              VARCHAR(100)    NOT NULL COMMENT '规则名称',
    rule_type         VARCHAR(50)     NULL     COMMENT 'POSITION_LIMIT / TRADE_VOLUME / CUSTOM',
    threshold         DECIMAL(18, 4)  NULL     COMMENT '触发阈值',
    scope_type        VARCHAR(20)     NULL     COMMENT '作用范围：COMPANY / GROUP / ACCOUNT',
    scope_id          BIGINT          NULL     COMMENT '对应范围 ID',
    action_on_trigger VARCHAR(30)     NULL     COMMENT '触发动作：DISABLE / LIMIT / LIQUIDATE / NOTIFY',
    is_active         TINYINT(1)      NOT NULL DEFAULT 1,
    created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_risk_rule_active (is_active)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '风控规则';

-- 账户实时风控状态
CREATE TABLE t_account_risk (
    account_id         VARCHAR(64)     NOT NULL COMMENT '券商账户 ID',
    risk_status        VARCHAR(20)     NOT NULL DEFAULT 'NORMAL'
                                       COMMENT 'NORMAL / DISABLED / LIMITED / LIQUIDATED',
    triggered_rule_id  BIGINT          NULL     COMMENT '触发的规则 ID',
    triggered_at       DATETIME        NULL     COMMENT '触发时间',
    operator_id        BIGINT          NULL     COMMENT '操作人（手动操作场景）',
    note               VARCHAR(255)    NULL     COMMENT '备注',
    updated_at         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                                       ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (account_id),
    INDEX idx_account_risk_status (risk_status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '账户风控状态';
