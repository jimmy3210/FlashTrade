-- ===================================================
-- V1: users 表
-- 平台用户：账号、密码（BCrypt）、角色、启用状态
-- ===================================================

CREATE TABLE users (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    username     VARCHAR(50)   NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role         VARCHAR(20)   NOT NULL DEFAULT 'ROLE_TRADER',
    enabled      TINYINT(1)   NOT NULL DEFAULT 1,
    created_at   DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at   DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                              ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '平台用户表';
