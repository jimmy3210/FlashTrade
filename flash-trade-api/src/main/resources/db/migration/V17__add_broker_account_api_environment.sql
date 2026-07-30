-- ===================================================
-- V17: 为券商账户添加 API 环境字段
-- ===================================================

ALTER TABLE t_broker_account
    ADD COLUMN api_environment VARCHAR(20) NULL COMMENT 'API 环境：LIVE / SANDBOX';

UPDATE t_broker_account
   SET api_environment = 'LIVE'
 WHERE api_environment IS NULL;
