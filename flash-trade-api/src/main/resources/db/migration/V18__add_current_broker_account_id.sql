-- ===================================================
-- V18: 添加用户与券商账户一对一绑定关系
-- 用户表添加当前绑定的券商账户 ID
-- ===================================================

ALTER TABLE users
    ADD COLUMN current_broker_account_id BIGINT NULL COMMENT '当前绑定的券商账户 ID',
    ADD CONSTRAINT fk_users_current_broker_account
        FOREIGN KEY (current_broker_account_id)
        REFERENCES t_broker_account(id) ON DELETE SET NULL;
