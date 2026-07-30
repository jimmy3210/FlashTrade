-- ===================================================
-- V16: etrade API 数据库扩展
-- 支持 OAuth 1.0a 认证和完整的订单生命周期管理
-- ===================================================

-- 1. 扩展 session_tokens 表 - 统一支持多种认证方式（OAuth 1.0a / OAuth 2.0 / API Key / Session）
-- 注意：该表采用"统一表+类型字段"设计，不同 auth_type 使用不同的字段组合
ALTER TABLE session_tokens 
-- 认证类型标识（用于区分 OAuth 2.0 / OAuth 1.0a / API Key / Session）
ADD COLUMN auth_type VARCHAR(20) NULL DEFAULT 'BEARER' COMMENT 'BEARER(OAuth2默认) / OAUTH1A / APIKEY / SESSION' AFTER token,

-- === OAuth 2.0 特定字段 (Schwab 等) ===
ADD COLUMN refresh_token TEXT NULL COMMENT 'OAuth 2.0 Refresh Token（用于刷新 access_token）' AFTER auth_type,
ADD COLUMN scope VARCHAR(255) NULL COMMENT 'OAuth 2.0 授权 Scope（如 PlaceTrades ReadAccount）' AFTER refresh_token,

-- === OAuth 1.0a 特定字段 (etrade 等) ===
ADD COLUMN token_secret VARCHAR(255) NULL COMMENT 'OAuth 1.0a Token Secret（签名用）' AFTER scope,
ADD COLUMN consumer_key VARCHAR(255) NULL COMMENT 'Consumer Key（冗余，供查询用）' AFTER token_secret,
ADD COLUMN oauth_verifier VARCHAR(255) NULL COMMENT 'OAuth 1.0a Verifier（授权流程中临时使用）' AFTER consumer_key,

-- 通用过期管理
ADD COLUMN oauth_expires_at DATETIME(6) NULL COMMENT 'OAuth Token 过期时间（auth_type=OAUTH2/OAUTH1A 时使用）' AFTER oauth_verifier,
ADD COLUMN token_type VARCHAR(20) NULL DEFAULT 'BEARER' COMMENT 'Token 传输方式：BEARER / OAUTH1A_HEADER' AFTER oauth_expires_at,

-- 使用追踪（用于监控和维护）
ADD COLUMN last_used_at DATETIME(6) NULL COMMENT '最后一次使用时间' AFTER token_type,

-- 索引优化
ADD INDEX idx_session_tokens_auth_type (auth_type),
ADD INDEX idx_session_tokens_oauth_expires (oauth_expires_at),
ADD INDEX idx_session_tokens_user_auth (user_id, auth_type);

-- 更新表注释
ALTER TABLE session_tokens COMMENT = 'Broker 会话 token 表（统一支持 OAuth 2.0 / OAuth 1.0a / API Key / Session）';

-- 2. 扩展 orders 表 - 支持复杂订单类型
ALTER TABLE orders
ADD COLUMN order_type VARCHAR(20) NULL DEFAULT 'MARKET' COMMENT 'MARKET/LIMIT/STOP/STOP_LIMIT' AFTER price,
ADD COLUMN time_in_force VARCHAR(10) NULL DEFAULT 'DAY' COMMENT 'DAY/GTC/IOC/FOK' AFTER order_type,
ADD COLUMN stop_price DECIMAL(15, 4) NULL COMMENT '止损价（针对 STOP 和 STOP_LIMIT 订单）' AFTER time_in_force,
ADD COLUMN preview_id VARCHAR(100) NULL COMMENT '订单预览 ID（etrade Dry Run ID，用于关联预览）' AFTER stop_price,
ADD INDEX idx_orders_preview_id (preview_id),
ADD INDEX idx_orders_order_type (order_type),
ADD INDEX idx_orders_status_user (status, user_id);

-- 3. 创建订单预览表 - 存储 etrade Order Preview 结果
CREATE TABLE IF NOT EXISTS order_previews (
    id                      BIGINT          NOT NULL AUTO_INCREMENT,
    user_id                 BIGINT          NOT NULL,
    account_id              VARCHAR(50)     NOT NULL,
    broker_name             VARCHAR(50)     NOT NULL,
    preview_id              VARCHAR(100)    NOT NULL UNIQUE COMMENT '券商返回的预览 ID',
    symbol                  VARCHAR(20)     NOT NULL,
    side                    VARCHAR(10)     NOT NULL COMMENT 'BUY / SELL',
    quantity                DECIMAL(15, 4)  NOT NULL,
    order_type              VARCHAR(20)     NOT NULL DEFAULT 'MARKET' COMMENT 'MARKET/LIMIT/STOP/STOP_LIMIT',
    price                   DECIMAL(15, 4)  NULL COMMENT '限价或止损价',
    stop_price              DECIMAL(15, 4)  NULL COMMENT '止损价',
    time_in_force           VARCHAR(10)     NOT NULL DEFAULT 'DAY' COMMENT 'DAY/GTC/IOC/FOK',
    
    -- 预估费用（来自 Preview API 的响应）
    estimated_commission    DECIMAL(15, 4)  NULL COMMENT '预估佣金',
    estimated_tax           DECIMAL(15, 4)  NULL COMMENT '预估税费',
    estimated_fees          DECIMAL(15, 4)  NULL COMMENT '预估其他费用',
    estimated_total         DECIMAL(15, 4)  NULL COMMENT '预估总费用（不含佣金/税费）',
    
    -- 保证金要求（Margin 账户）
    required_margin         DECIMAL(15, 4)  NULL COMMENT '该订单所需保证金',
    available_margin        DECIMAL(15, 4)  NULL COMMENT '完成订单后剩余可用保证金',
    margin_buy_power        DECIMAL(15, 4)  NULL COMMENT '保证金购买力',
    
    -- 风险和警告信息
    warnings                JSON            NULL COMMENT '风险警告列表 JSON',
    can_place               TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '是否可以下单（based on preview）',
    
    -- 关联关系
    used_for_order_id       BIGINT          NULL COMMENT '该预览对应的订单 ID（下单后填充）',
    
    -- 时间戳
    created_at              DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    expires_at              DATETIME(6)     NULL COMMENT '预览过期时间（通常 30-60 秒，需要定期清理）',
    used_at                 DATETIME(6)     NULL COMMENT '预览被使用的时间',
    
    PRIMARY KEY (id),
    UNIQUE KEY uq_preview_id (broker_name, preview_id),
    INDEX idx_preview_user_id (user_id),
    INDEX idx_preview_account_id (account_id),
    INDEX idx_preview_expires_at (expires_at),
    INDEX idx_preview_created_at (created_at),
    INDEX idx_preview_order_id (used_for_order_id),
    CONSTRAINT fk_preview_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_preview_order FOREIGN KEY (used_for_order_id) REFERENCES orders (id) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '订单预览表（etrade Order Preview / Dry Run 结果）';

-- 4. 创建订单修改历史表 - 跟踪订单修改（Replace Order）
CREATE TABLE IF NOT EXISTS order_modifications (
    id                      BIGINT          NOT NULL AUTO_INCREMENT,
    order_id                BIGINT          NOT NULL,
    modification_sequence   INT             NOT NULL DEFAULT 1 COMMENT '修改序列号（第一次修改=1）',
    modification_type       VARCHAR(20)     NOT NULL COMMENT 'PRICE_CHANGE / QUANTITY_CHANGE / CANCELLED',
    
    -- 原始值
    original_price          DECIMAL(15, 4)  NULL,
    original_quantity       DECIMAL(15, 4)  NULL,
    original_order_type     VARCHAR(20)     NULL,
    original_time_in_force  VARCHAR(10)     NULL,
    
    -- 修改后的值
    new_price               DECIMAL(15, 4)  NULL,
    new_quantity            DECIMAL(15, 4)  NULL,
    new_order_type          VARCHAR(20)     NULL,
    new_time_in_force       VARCHAR(10)     NULL,
    
    -- 券商侧响应（改单通常生成新订单 ID）
    external_order_id       VARCHAR(100)    NULL COMMENT 'etrade 返回的新订单 ID（改单会生成新订单）',
    external_modify_id      VARCHAR(100)    NULL COMMENT 'etrade 修改请求 ID',
    
    -- 状态
    status                  VARCHAR(20)     NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING / ACCEPTED / REJECTED',
    error_message           TEXT            NULL COMMENT '修改失败原因',
    
    -- 时间戳
    modified_at             DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    processed_at            DATETIME(6)     NULL COMMENT '处理完成时间',
    
    PRIMARY KEY (id),
    INDEX idx_modification_order (order_id),
    INDEX idx_modification_sequence (order_id, modification_sequence),
    INDEX idx_modification_modified_at (modified_at),
    CONSTRAINT fk_modification_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '订单修改历史表（Replace Order 操作记录）';

-- 5. 创建订单成交详情表 - 分笔成交信息
CREATE TABLE IF NOT EXISTS order_fills (
    id                      BIGINT          NOT NULL AUTO_INCREMENT,
    order_id                BIGINT          NOT NULL,
    external_fill_id        VARCHAR(100)    NULL COMMENT 'etrade 成交单 ID',
    fill_sequence           INT             NOT NULL DEFAULT 1 COMMENT '成交序列号',
    fill_quantity           DECIMAL(15, 4)  NOT NULL COMMENT '本笔成交数量',
    fill_price              DECIMAL(15, 4)  NOT NULL COMMENT '本笔成交价格',
    fill_commission         DECIMAL(15, 4)  NOT NULL DEFAULT 0 COMMENT '本笔成交佣金',
    fill_value              DECIMAL(15, 4)  NOT NULL COMMENT '成交金额（数量*价格）',
    filled_at               DATETIME(6)     NOT NULL COMMENT '成交时间',
    
    -- 成交时的市场状态
    market_price            DECIMAL(15, 4)  NULL COMMENT '成交时市价',
    
    PRIMARY KEY (id),
    UNIQUE KEY uq_fill_id (order_id, external_fill_id),
    INDEX idx_fill_order (order_id),
    INDEX idx_fill_sequence (order_id, fill_sequence),
    INDEX idx_fill_filled_at (filled_at),
    CONSTRAINT fk_fill_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '订单成交详情表（分笔成交信息）';

-- 6. 添加清理过期预览数据的索引
-- 定期清理 order_previews 中 expires_at 已过期的记录
CREATE EVENT IF NOT EXISTS cleanup_expired_previews
ON SCHEDULE EVERY 1 MINUTE
DO
  DELETE FROM order_previews WHERE expires_at IS NOT NULL AND expires_at < NOW();

-- ===================================================
-- 迁移说明
-- ===================================================
-- 本迁移脚本实现以下功能：
--
-- === 认证架构说明 ===
-- 采用"统一表 + 类型字段"设计，而非多表分离。
-- 理由：
--   1. 简化代码逻辑（1 个 Entity 而非 3 个）
--   2. 便于公共查询（如"用户的所有 token"）
--   3. 易于扩展（新认证方式只需添加字段）
--   4. 与现有设计一致（t_broker.auth_type 已有此模式）
--
-- 认证类型使用 auth_type 字段区分：
--   - BEARER     : OAuth 2.0（Schwab）- 使用 access_token / refresh_token
--   - OAUTH1A    : OAuth 1.0a（etrade）- 使用 token / token_secret
--   - APIKEY     : API Key（Tastytrade）- 使用 token
--   - SESSION    : Session Token（Tastyworks）- 使用 token
--
-- 1. 扩展 session_tokens 表（统一的会话 Token 表）
--    核心改动：
--    - 添加 auth_type 字段标识认证方式
--    - 添加 OAuth 2.0 字段：refresh_token, scope
--    - 添加 OAuth 1.0a 字段：token_secret, consumer_key, oauth_verifier
--    - 添加 last_used_at 追踪使用情况
--    - 改进索引以支持高效查询
--
--    字段映射：
--    ┌─ OAuth 2.0 (Schwab)
--    │  ├─ token = access_token
--    │  ├─ refresh_token = refresh_token
--    │  ├─ expires_at = token 过期时间
--    │  ├─ scope = 授权范围
--    │  └─ auth_type = 'BEARER'
--    │
--    ├─ OAuth 1.0a (etrade)
--    │  ├─ token = oauth_token
--    │  ├─ token_secret = oauth_token_secret（签名用）
--    │  ├─ consumer_key = Consumer Key（冗余）
--    │  ├─ oauth_expires_at = token 过期时间
--    │  ├─ oauth_verifier = 授权验证码（临时）
--    │  └─ auth_type = 'OAUTH1A'
--    │
--    ├─ API Key (Tastytrade)
--    │  ├─ token = api_key
--    │  ├─ expires_at = 长期有效或 NULL
--    │  └─ auth_type = 'APIKEY'
--    │
--    └─ Session Token (Tastyworks)
--       ├─ token = session_token
--       ├─ expires_at = 会话过期时间
--       └─ auth_type = 'SESSION'
--
-- 2. 扩展 orders 表
--    - 添加 order_type 支持复杂订单类型（MARKET/LIMIT/STOP/STOP_LIMIT）
--    - 添加 time_in_force 支持订单时间类型（DAY/GTC/IOC/FOK）
--    - 添加 stop_price 支持止损单和止损限价单
--    - 添加 preview_id 关联订单预览
--
-- 3. 创建 order_previews 表
--    - 存储 etrade Order Preview（Dry Run）的结果
--    - 包含预估费用、保证金要求、风险警告等信息
--    - 支持预览过期自动清理（1 分钟一次）
--
-- 4. 创建 order_modifications 表
--    - 记录订单修改（Replace Order）的历史
--    - 跟踪修改前后的值
--    - 支持改单失败原因的记录
--
-- 5. 创建 order_fills 表
--    - 记录订单的分笔成交信息
--    - 支持部分成交的详细追踪
--    - 便于准确计算成本基础
--
-- === 向后兼容性 ===
-- 所有修改都是向后兼容的（使用 ADD COLUMN / CREATE TABLE）
-- 现有 session_tokens 中的数据不受影响（新字段都是 NULL）
-- ===================================================
