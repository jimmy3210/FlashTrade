-- ===================================================
-- V19: 审计日志和订单管理增强
-- 包括：订单表增强、订单汇总表、审计日志表
-- ===================================================

-- 1. 增强 orders 表 - 添加管理员相关字段（仅补充 V16/V3 之后未覆盖的字段）
ALTER TABLE orders
    ADD COLUMN broker_account_id BIGINT COMMENT '券商账户ID',
    ADD COLUMN limit_price DECIMAL(15,2) COMMENT '限价',
    ADD COLUMN filled_quantity INT DEFAULT 0 COMMENT '已成交数量',
    ADD COLUMN average_price DECIMAL(10,2) COMMENT '平均成交价',
    ADD COLUMN total_value DECIMAL(15,2) COMMENT '订单总价值',
    ADD COLUMN commission DECIMAL(10,2) COMMENT '佣金',
    ADD COLUMN admin_note VARCHAR(500) COMMENT '管理员备注',
    ADD COLUMN risk_flag VARCHAR(20) DEFAULT 'NORMAL' COMMENT '风险标记: NORMAL/SUSPICIOUS/BLOCKED/CANCELLED_BY_ADMIN',
    ADD COLUMN admin_id BIGINT COMMENT '操作此订单的管理员ID',
    ADD COLUMN admin_action_time DATETIME COMMENT '管理员操作时间';

-- 为 orders 表创建索引优化查询性能（避免与 V3 / V16 中已有的索引命名与定义重复）
CREATE INDEX idx_orders_admin_status ON orders(status);
CREATE INDEX idx_orders_risk_flag ON orders(risk_flag);
CREATE INDEX idx_orders_broker_account_id ON orders(broker_account_id);
CREATE INDEX idx_orders_admin_id ON orders(admin_id);

-- 2. 订单每日汇总表 - 用于快速查询统计
CREATE TABLE order_daily_summary (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '汇总ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    summary_date DATE NOT NULL COMMENT '汇总日期',
    
    -- 交易统计
    total_orders INT DEFAULT 0 COMMENT '订单总数',
    buy_orders INT DEFAULT 0 COMMENT '买入订单数',
    sell_orders INT DEFAULT 0 COMMENT '卖出订单数',
    filled_orders INT DEFAULT 0 COMMENT '已成交订单数',
    cancelled_orders INT DEFAULT 0 COMMENT '已取消订单数',
    rejected_orders INT DEFAULT 0 COMMENT '已拒绝订单数',
    
    -- 成交统计
    total_buy_qty INT DEFAULT 0 COMMENT '总买入数量',
    total_sell_qty INT DEFAULT 0 COMMENT '总卖出数量',
    total_buy_amount DECIMAL(15,2) DEFAULT 0 COMMENT '总买入金额',
    total_sell_amount DECIMAL(15,2) DEFAULT 0 COMMENT '总卖出金额',
    
    -- 费用统计
    total_commission DECIMAL(10,2) DEFAULT 0 COMMENT '总佣金',
    total_tax DECIMAL(10,2) DEFAULT 0 COMMENT '总税费',
    
    -- 盈亏统计
    realized_pnl DECIMAL(15,2) DEFAULT 0 COMMENT '已实现盈亏',
    
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_user_date (user_id, summary_date),
    INDEX idx_user_date (user_id, summary_date),
    INDEX idx_date (summary_date)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '订单每日汇总统计表';

-- 3. 操作日志表 - 记录所有管理员操作
CREATE TABLE operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    
    -- 操作人信息
    operator_id BIGINT COMMENT '操作员ID（管理员ID）',
    operator_username VARCHAR(100) COMMENT '操作员用户名',
    
    -- 操作信息
    operation VARCHAR(100) NOT NULL COMMENT '操作类型：CREATE_USER, UPDATE_ACCOUNT, CANCEL_ORDER等',
    target_type VARCHAR(50) NOT NULL COMMENT '目标资源类型：USER, BROKER_ACCOUNT, ORDER等',
    target_id BIGINT COMMENT '目标资源ID',
    target_name VARCHAR(255) COMMENT '目标资源名称（用户名、订单ID等）',
    
    -- 操作详情（JSON 格式保存前后值对比）
    old_value JSON COMMENT '修改前的值',
    new_value JSON COMMENT '修改后的值',
    change_summary VARCHAR(500) COMMENT '修改摘要（人类可读）',
    
    -- 请求信息
    request_ip VARCHAR(45) COMMENT '请求IP地址',
    request_path VARCHAR(255) COMMENT 'API路径',
    request_method VARCHAR(10) COMMENT 'HTTP方法',
    http_status INT COMMENT 'HTTP响应码',
    
    -- 客户端信息
    user_agent VARCHAR(500) COMMENT '用户代理',
    device_info VARCHAR(500) COMMENT '设备信息',
    
    -- 时间信息
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    
    INDEX idx_operator_id (operator_id),
    INDEX idx_target_type_id (target_type, target_id),
    INDEX idx_created_at (created_at),
    INDEX idx_operation (operation)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '操作审计日志表';

-- 4. 登录日志表 - 记录所有登录事件
CREATE TABLE login_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    
    -- 登录用户信息
    username VARCHAR(100) NOT NULL COMMENT '用户名',
    user_id BIGINT COMMENT '用户ID（登录成功时）',
    
    -- 登录状态
    login_status VARCHAR(20) NOT NULL COMMENT '登录状态：SUCCESS/FAILED',
    failed_reason VARCHAR(200) COMMENT '失败原因：INVALID_PASSWORD/USER_DISABLED等',
    
    -- 请求信息
    request_ip VARCHAR(45) COMMENT '请求IP地址',
    request_path VARCHAR(255) COMMENT '登录路径',
    
    -- 客户端信息
    user_agent VARCHAR(500) COMMENT '用户代理',
    device_info VARCHAR(500) COMMENT '设备信息',
    
    -- 登录方式
    login_method VARCHAR(50) COMMENT '登录方式：PASSWORD/OAUTH/LDAP等',
    
    -- 时间信息
    login_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    session_id VARCHAR(100) COMMENT '会话ID',
    
    INDEX idx_username (username),
    INDEX idx_user_id (user_id),
    INDEX idx_login_status (login_status),
    INDEX idx_login_time (login_time),
    INDEX idx_request_ip (request_ip)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '登录审计日志表';

-- 5. API 调用日志表 - 记录关键 API 调用
CREATE TABLE api_call_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    
    -- 用户信息
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(100) COMMENT '用户名',
    
    -- API 信息
    http_method VARCHAR(10) NOT NULL COMMENT 'HTTP方法：GET/POST/PUT/DELETE',
    endpoint VARCHAR(255) NOT NULL COMMENT 'API端点',
    request_params VARCHAR(1000) COMMENT '请求参数（JSON格式）',
    
    -- 请求和响应
    request_body JSON COMMENT '请求体',
    response_code INT COMMENT 'HTTP响应码',
    response_time_ms INT COMMENT '响应时间（毫秒）',
    
    -- 客户端信息
    request_ip VARCHAR(45) COMMENT '请求IP',
    user_agent VARCHAR(500) COMMENT '用户代理',
    
    -- 时间信息
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '请求时间',
    
    -- 标记关键操作
    is_critical BOOLEAN DEFAULT FALSE COMMENT '是否是关键操作（交易等）',
    error_flag BOOLEAN DEFAULT FALSE COMMENT '是否出错',
    
    INDEX idx_user_id (user_id),
    INDEX idx_endpoint (endpoint),
    INDEX idx_created_at (created_at),
    INDEX idx_is_critical (is_critical),
    INDEX idx_error_flag (error_flag),
    INDEX idx_response_code (response_code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'API调用日志表';

-- 6. 系统事件日志表 - 记录系统错误、异常、告警
CREATE TABLE system_event_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    
    -- 事件类型
    event_type VARCHAR(50) NOT NULL COMMENT '事件类型：ERROR/WARN/INFO/ALERT',
    severity VARCHAR(20) NOT NULL COMMENT '严重级别：LOW/MEDIUM/HIGH/CRITICAL',
    component VARCHAR(100) NOT NULL COMMENT '组件名称：OrderService, BrokerClient等',
    
    -- 事件详情
    message VARCHAR(500) NOT NULL COMMENT '事件消息',
    stack_trace LONGTEXT COMMENT '堆栈跟踪（仅错误时）',
    
    -- 关联信息
    related_user_id BIGINT COMMENT '相关用户ID',
    related_order_id BIGINT COMMENT '相关订单ID',
    related_resource VARCHAR(255) COMMENT '相关资源描述',
    
    -- 时间信息
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '事件时间',
    
    -- 处理状态
    resolved BOOLEAN DEFAULT FALSE COMMENT '是否已解决',
    resolved_at DATETIME COMMENT '解决时间',
    resolved_by BIGINT COMMENT '解决人ID',
    resolution_note VARCHAR(500) COMMENT '解决备注',
    
    INDEX idx_event_type (event_type),
    INDEX idx_severity (severity),
    INDEX idx_component (component),
    INDEX idx_created_at (created_at),
    INDEX idx_resolved (resolved),
    INDEX idx_severity_created (severity, created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '系统事件日志表';

-- 7. 日志保留策略表 - 配置不同类型日志的保留时间
CREATE TABLE log_retention_policy (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '策略ID',
    log_type VARCHAR(50) NOT NULL COMMENT '日志类型：OPERATION/LOGIN/API_CALL/SYSTEM_EVENT',
    retention_days INT COMMENT '保留天数',
    archive_flag BOOLEAN DEFAULT TRUE COMMENT '是否归档旧日志',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_log_type (log_type)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '日志保留策略表';

-- 初始化保留策略
INSERT INTO log_retention_policy (log_type, retention_days, archive_flag) 
VALUES 
    ('OPERATION', 365, TRUE),   -- 操作日志保留 1 年
    ('LOGIN', 180, TRUE),        -- 登录日志保留 6 个月
    ('API_CALL', 90, TRUE),      -- API调用日志保留 3 个月
    ('SYSTEM_EVENT', 180, TRUE)  -- 系统事件日志保留 6 个月
ON DUPLICATE KEY UPDATE retention_days=VALUES(retention_days);
