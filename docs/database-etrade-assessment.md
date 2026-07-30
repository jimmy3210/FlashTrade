# etrade API 开发的数据库设计评估

## 总体评估

✅ **现有数据库设计基本满足 etrade API 开发需求**，但有以下需要补齐的地方。

---

## 1. 现有表结构清单

### 已实现的核心表

| 表名 | 用途 | 备注 |
|------|------|------|
| `users` | 用户基础信息 | ✅ |
| `t_broker` | 券商基础信息 | ✅ 包含 `auth_type = 'OAUTH1A'` 字段 |
| `t_broker_dev_account` | 开发者 API Key/Secret | ✅ 支持 etrade Consumer Key/Secret 存储 |
| `t_broker_account` | 用户券商账户 | ✅ 支持多个账户 |
| `t_broker_account_permission` | 账户权限（TRADE/READ_ONLY） | ✅ |
| `orders` | 订单快照 | ✅ 关键字段完备 |
| `positions` | 持仓快照 | ✅ |
| `trades` | 成交历史 | ✅ |
| `session_tokens` | Broker 会话 token | ✅ 关键：用于存储 OAuth Token |
| `t_broker_api` | API 定义和限流 | ✅ 已预填充 etrade API 定义 |

### 问题诊断

#### 1. ✅ OAuth 1.0a Token 管理

**现状**：`session_tokens` 表支持存储任意 token，但缺乏 OAuth 1.0a 特定字段。

**需要的字段**：
- `consumer_key` - etrade Consumer Key（来自 `t_broker_dev_account`）
- `consumer_secret` - etrade Consumer Secret（来自 `t_broker_dev_account`）
- `oauth_token` - Access Token（当前 `token` 字段可用）
- `oauth_token_secret` - Access Token Secret（当前缺失）
- `oauth_verifier` - 授权验证码（临时存储用）

**建议方案**：

```sql
-- 扩展 session_tokens 以支持 OAuth 1.0a 详细字段
ALTER TABLE session_tokens ADD COLUMN oauth_token_secret VARCHAR(255) NULL COMMENT 'OAuth 1.0a Token Secret';
ALTER TABLE session_tokens ADD COLUMN oauth_verifier VARCHAR(255) NULL COMMENT 'OAuth 1.0a 验证码（临时）';
ALTER TABLE session_tokens ADD COLUMN oauth_consumer_key VARCHAR(255) NULL COMMENT 'Consumer Key（冗余，便于查询）';
ALTER TABLE session_tokens ADD COLUMN oauth_expires_at DATETIME(6) NULL COMMENT 'OAuth Token 过期时间';
```

**评分**：⚠️ **部分满足** - 需要添加特定字段

---

#### 2. ✅ 订单表支持充分

**现状**：`orders` 表结构完善，包括：
- `external_order_id` - etrade 订单 ID
- `status` - 订单状态（PENDING/FILLED/PARTIALLY_FILLED/CANCELLED/REJECTED）
- `symbol`, `side`, `quantity`, `price` - 核心订单字段
- 时间戳字段

**缺失字段**：
- `order_type` - 订单类型（MARKET/LIMIT/STOP/STOP_LIMIT）
- `time_in_force` - 时间类型（DAY/GTC/IOC/FOK）
- `stop_price` - 止损价
- `preview_id` - 订单预览 ID（用于关联预览和下单）

**建议方案**：

```sql
ALTER TABLE orders ADD COLUMN order_type VARCHAR(20) NULL DEFAULT 'MARKET' COMMENT 'MARKET/LIMIT/STOP/STOP_LIMIT';
ALTER TABLE orders ADD COLUMN time_in_force VARCHAR(10) NULL DEFAULT 'DAY' COMMENT 'DAY/GTC/IOC/FOK';
ALTER TABLE orders ADD COLUMN stop_price DECIMAL(15, 4) NULL COMMENT '止损价';
ALTER TABLE orders ADD COLUMN preview_id VARCHAR(100) NULL COMMENT '订单预览 ID';
```

**评分**：✅ **基本满足** - 核心字段完备，需补齐订单类型字段

---

#### 3. 📋 订单预览表缺失

**现状**：无专门的订单预览表。订单预览（Dry Run）是 etrade API 的重要功能，用于验证订单合法性和返回预估费用。

**需要的字段**：
- 预览 ID
- 用户 ID
- 账户 ID
- 预估佣金
- 预估税费
- 预估总费用
- 保证金要求
- 风险警告
- 预览时间

**建议方案**：创建专门的预览表

```sql
-- 订单预览表
CREATE TABLE order_previews (
    id                 BIGINT          NOT NULL AUTO_INCREMENT,
    user_id            BIGINT          NOT NULL,
    account_id         VARCHAR(50)     NOT NULL,
    broker_name        VARCHAR(50)     NOT NULL,
    preview_id         VARCHAR(100)    NOT NULL UNIQUE COMMENT '券商返回的预览 ID',
    symbol             VARCHAR(20)     NOT NULL,
    side               VARCHAR(10)     NOT NULL COMMENT 'BUY / SELL',
    quantity           DECIMAL(15, 4)  NOT NULL,
    order_type         VARCHAR(20)     NOT NULL DEFAULT 'MARKET',
    price              DECIMAL(15, 4)  NULL,
    stop_price         DECIMAL(15, 4)  NULL,
    time_in_force      VARCHAR(10)     NOT NULL DEFAULT 'DAY',
    
    -- 预估费用
    estimated_commission DECIMAL(15, 4) NULL COMMENT '预估佣金',
    estimated_tax      DECIMAL(15, 4)  NULL COMMENT '预估税费',
    estimated_total    DECIMAL(15, 4)  NULL COMMENT '预估总费用',
    
    -- 保证金要求
    required_margin    DECIMAL(15, 4)  NULL COMMENT '保证金要求',
    available_margin   DECIMAL(15, 4)  NULL COMMENT '可用保证金',
    
    -- 风险信息
    warnings           JSON            NULL COMMENT '风险警告列表',
    can_place          TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '是否可下单',
    
    -- 时间戳
    created_at         DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    expires_at         DATETIME(6)     NULL COMMENT '预览过期时间（通常 30-60 秒）',
    used_for_order_id  BIGINT          NULL COMMENT '关联的订单 ID',
    
    PRIMARY KEY (id),
    UNIQUE KEY uq_preview_id (broker_name, preview_id),
    INDEX idx_preview_user (user_id),
    INDEX idx_preview_account (account_id),
    INDEX idx_preview_expires (expires_at),
    CONSTRAINT fk_preview_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_preview_order FOREIGN KEY (used_for_order_id) REFERENCES orders (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '订单预览表（etrade Dry Run 结果）';
```

**评分**：❌ **缺失** - 需要新建表

---

#### 4. ✅ 账户和权限管理

**现状**：
- `t_broker_account` - 支持多个账户、账户类型（CASH/MARGIN）、状态管理
- `t_broker_account_permission` - 支持权限管理（TRADE/READ_ONLY）

**缺失**：
- 账户等级/风险等级标记
- 交易限制（如单日买入数量、最大持仓等）

**评分**：✅ **满足** - 基本需求充分

---

#### 5. ✅ 交易历史和快照

**现状**：
- `trades` - 成交历史完备
- `positions` - 持仓快照完备
- `orders` - 订单历史完备

**评分**：✅ **满足** - 充分

---

## 2. etrade API 开发需求对标

### 需求清单

| 功能 | etrade 需要 | 现有支持 | 状态 |
|------|-----------|--------|------|
| OAuth 1.0a 认证 | Consumer Key/Secret 存储 | session_tokens 部分支持 | ⚠️ 需补齐 |
| Access Token 管理 | Token 和 Secret 存储 | session_tokens.token 支持 | ⚠️ 需添加 secret 字段 |
| Token 过期管理 | 过期时间追踪 | expires_at 支持 | ✅ |
| 订单预览 | 预览 ID、费用、保证金 | **无** | ❌ 需建表 |
| 下单 | 订单信息存储 | orders 表支持 | ⚠️ 需补齐 order_type/time_in_force |
| 撤单 | 订单状态更新 | orders.status 支持 | ✅ |
| 改单 | 订单版本跟踪 | **无** | ❌ 需补字段或建表 |
| 账户绑定 | 多账户管理 | t_broker_account 支持 | ✅ |
| 权限管理 | 交易权限控制 | t_broker_account_permission 支持 | ✅ |
| 交易历史 | 成交和订单历史 | trades / orders 支持 | ✅ |

---

## 3. 建议的改进方案

### 3.1 优先级 1：关键缺失（必须）

#### 修改 1：扩展 session_tokens 表

```sql
-- 添加 OAuth 1.0a 特定字段
ALTER TABLE session_tokens 
ADD COLUMN oauth_token_secret VARCHAR(255) NULL COMMENT 'OAuth 1.0a Token Secret',
ADD COLUMN oauth_verifier VARCHAR(255) NULL COMMENT 'OAuth 1.0a 验证码（临时）',
ADD COLUMN oauth_consumer_key VARCHAR(255) NULL COMMENT 'Consumer Key（冗余供查询）',
ADD COLUMN oauth_expires_at DATETIME(6) NULL COMMENT 'OAuth Token 过期时间',
ADD COLUMN token_type VARCHAR(20) NULL DEFAULT 'BEARER' COMMENT 'Token 类型：BEARER / OAUTH1A',
ADD INDEX idx_oauth_expires (oauth_expires_at);
```

**为什么**：etrade OAuth 1.0a 需要管理 Consumer Key/Secret 和 Access Token/Secret，现有的通用 token 字段不足。

#### 修改 2：扩展 orders 表

```sql
-- 添加订单类型和时间类型字段
ALTER TABLE orders
ADD COLUMN order_type VARCHAR(20) NULL DEFAULT 'MARKET' COMMENT 'MARKET/LIMIT/STOP/STOP_LIMIT',
ADD COLUMN time_in_force VARCHAR(10) NULL DEFAULT 'DAY' COMMENT 'DAY/GTC/IOC/FOK',
ADD COLUMN stop_price DECIMAL(15, 4) NULL COMMENT '止损价',
ADD COLUMN preview_id VARCHAR(100) NULL COMMENT '订单预览 ID',
ADD INDEX idx_orders_preview_id (preview_id);
```

**为什么**：完善订单信息，支持复杂订单类型（止损单、限价单等）。

#### 修改 3：创建 order_previews 表

见上文的完整 SQL。

**为什么**：etrade 要求下单前调用 Preview API，需要独立表存储预估费用和保证金信息。

> 说明：`order_previews` 属于 E*TRADE 专用扩展数据，已从核心实体模块迁移到 `broker/etrade` 模块，实现上不应把券商特有流程泄露到核心领域模型。

---

### 3.2 优先级 2：增强功能（推荐）

#### 修改 4：订单版本/修改历史表

```sql
-- 订单修改历史表（支持改单功能）
CREATE TABLE order_modifications (
    id                 BIGINT          NOT NULL AUTO_INCREMENT,
    order_id           BIGINT          NOT NULL,
    modification_type  VARCHAR(20)     NOT NULL COMMENT 'PRICE_CHANGE / QUANTITY_CHANGE / CANCELLED',
    old_price          DECIMAL(15, 4)  NULL,
    new_price          DECIMAL(15, 4)  NULL,
    old_quantity       DECIMAL(15, 4)  NULL,
    new_quantity       DECIMAL(15, 4)  NULL,
    external_order_id  VARCHAR(100)    NULL COMMENT 'etrade 新订单 ID（改单会生成新订单）',
    modified_at        DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    INDEX idx_modification_order (order_id),
    CONSTRAINT fk_modification_order FOREIGN KEY (order_id) REFERENCES orders (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '订单修改历史表';
```

**为什么**：etrade 改单可能会生成新订单ID，需要追踪修改历史。

#### 修改 5：订单成本基础表

```sql
-- 订单详细成本表（支持分析订单的真实成本）
CREATE TABLE order_details (
    id                 BIGINT          NOT NULL AUTO_INCREMENT,
    order_id           BIGINT          NOT NULL,
    fill_id            VARCHAR(100)    NULL COMMENT 'etrade 成交单 ID',
    fill_quantity      DECIMAL(15, 4)  NOT NULL,
    fill_price         DECIMAL(15, 4)  NOT NULL,
    fill_commission    DECIMAL(15, 4)  NOT NULL DEFAULT 0,
    filled_at          DATETIME(6)     NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_order_details_order (order_id),
    CONSTRAINT fk_order_details_order FOREIGN KEY (order_id) REFERENCES orders (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '订单详细成交信息表';
```

**为什么**：订单可能部分成交，需要追踪每笔成交的具体信息。

---

## 4. 实装优先级建议

### 立即执行（Phase 1）

1. ✅ 修改 `session_tokens` 表（添加 OAuth 特定字段）
2. ✅ 修改 `orders` 表（添加订单类型字段）
3. ✅ 创建 `order_previews` 表

**工作量**：~2-3 小时（SQL + JPA Entity 更新）

### 后续优化（Phase 2）

4. 创建 `order_modifications` 表
5. 创建 `order_details` 表

**工作量**：~2-3 小时

---

## 5. JPA Entity 更新清单

| Entity | 修改内容 |
|--------|---------|
| `SessionToken` | 添加 OAuth 特定字段 |
| `Order` | 添加 order_type、time_in_force、stop_price、preview_id |
| `OrderPreview` | 新增（对应 order_previews 表） |
| `OrderModification` | 新增（对应 order_modifications 表，Phase 2） |
| `OrderDetail` | 新增（对应 order_details 表，Phase 2） |

---

## 6. 总体风险评估

| 风险项 | 评级 | 说明 |
|--------|------|------|
| 数据完整性 | 🟢 低 | 现有结构合理，修改为扩展式，无破坏性 |
| 迁移难度 | 🟢 低 | 都是新增列或新建表，旧数据无影响 |
| 性能影响 | 🟢 低 | 添加索引时要注意，不会有性能衰减 |
| 向后兼容 | 🟢 低 | 所有修改都是向后兼容的 |

---

## 7. 后续行动

### 立即（本周）

- [ ] 执行 SQL 修改脚本（session_tokens、orders 表）
- [ ] 创建 order_previews 表和 migration 文件
- [ ] 更新 JPA Entity 和对应的 Repository

### 本周后

- [ ] 集成测试：确保新字段能正确存储和查询
- [ ] 更新 Mapper 层（将 etrade DTO 映射到扩展的 Entity）
- [ ] 更新文档

---

## 结论

✅ **现有数据库设计基本满足 etrade API 开发需求**，只需进行**有限的扩展**（三处改动）：

1. 扩展 `session_tokens` 表（OAuth 字段）
2. 扩展 `orders` 表（订单类型字段）
3. 新建 `order_previews` 表（预览数据存储）

这些修改都是**低风险、低迁移成本**的扩展式改动，不会影响现有功能，且能完整支持 etrade 完整交易流程（预览 → 下单 → 查询 → 撤单/改单）。
