-- ===================================================
-- V15: 券商运行时 API 模式与 URL 配置
-- ===================================================

CREATE TABLE t_broker_runtime_config (
    id           BIGINT          NOT NULL AUTO_INCREMENT,
    broker_name  VARCHAR(50)     NOT NULL COMMENT '内部券商标识，与 BrokerClient.getBrokerName() 一致',
    mode         VARCHAR(20)     NOT NULL DEFAULT 'PRODUCTION' COMMENT '运行模式：MOCK / SANDBOX / PRODUCTION',
    base_url     VARCHAR(255)    NULL COMMENT '自定义 API 基础 URL，若为空使用默认配置',
    mock_enabled TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否启用 Mock 模式',
    updated_at   DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                        ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uq_broker_runtime_config_broker (broker_name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '券商运行时模式与 API URL 定制配置';
