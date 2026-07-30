# FlashTradeAPI — 数据库设计文档

**文档版本**：1.0.0  
**最后更新**：2026-03-28  
**状态**：阶段 2 初版  
**迁移管理**：Flyway（脚本位于 `src/main/resources/db/migration/`）

---

## 1. 数据库环境

| 环境 | 数据库名 | 用途 |
|------|---------|------|
| 开发 | `flash_trade_dev` | 本地开发 / Docker |
| 测试 | `flash_trade_test` | 自动化测试 |
| 生产 | `flash_trade_prod` | 生产环境 |

- 字符集：`utf8mb4`
- 排序规则：`utf8mb4_unicode_ci`
- 存储引擎：InnoDB

---

## 2. ER 图（文字表示）

```
users
  │
  ├─── positions (user_id → users.id)
  │
  └─── orders (user_id → users.id)
         │
         └─── trades (order_id → orders.id, nullable)
```

---

## 3. 表结构详细说明

### 3.1 users — 用户表

**Flyway 脚本**：`V1__create_users.sql`

| 列名 | 类型 | NULL | 默认值 | 说明 |
|------|------|------|--------|------|
| `id` | BIGINT | NOT NULL | AUTO_INCREMENT | 主键 |
| `username` | VARCHAR(50) | NOT NULL | — | 用户名（唯一） |
| `password_hash` | VARCHAR(255) | NOT NULL | — | BCrypt 加密密码 |
| `role` | VARCHAR(20) | NOT NULL | `ROLE_TRADER` | 用户角色 |
| `enabled` | TINYINT(1) | NOT NULL | `1` | 是否启用 |
| `created_at` | DATETIME(6) | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| `updated_at` | DATETIME(6) | NOT NULL | CURRENT_TIMESTAMP | 更新时间 |

**索引**
- `PRIMARY KEY (id)`
- `UNIQUE KEY uk_users_username (username)`

**角色枚举值**：`ROLE_ADMIN` / `ROLE_TRADER` / `ROLE_VIEWER`

---

### 3.2 positions — 持仓表

**Flyway 脚本**：`V2__create_positions.sql`

| 列名 | 类型 | NULL | 默认值 | 说明 |
|------|------|------|--------|------|
| `id` | BIGINT | NOT NULL | AUTO_INCREMENT | 主键 |
| `user_id` | BIGINT | NOT NULL | — | 关联用户（FK → users.id）|
| `account_id` | VARCHAR(50) | NOT NULL | — | 券商账户 ID |
| `symbol` | VARCHAR(20) | NOT NULL | — | 证券代码 |
| `quantity` | DECIMAL(15,4) | NOT NULL | `0` | 持仓数量 |
| `average_cost` | DECIMAL(15,4) | NOT NULL | `0` | 平均成本价 |
| `current_price` | DECIMAL(15,4) | NULL | — | 当前价格 |
| `market_value` | DECIMAL(15,4) | NULL | — | 市值 |
| `unrealized_pnl` | DECIMAL(15,4) | NULL | — | 未实现盈亏 |
| `broker_name` | VARCHAR(50) | NOT NULL | — | 券商标识 |
| `updated_at` | DATETIME(6) | NOT NULL | CURRENT_TIMESTAMP | 更新时间 |

**索引**
- `PRIMARY KEY (id)`
- `UNIQUE KEY uk_positions (user_id, account_id, symbol, broker_name)`
- `FOREIGN KEY fk_positions_user (user_id) → users(id)`

---

### 3.3 orders — 订单表

**Flyway 脚本**：`V3__create_orders.sql`

| 列名 | 类型 | NULL | 默认值 | 说明 |
|------|------|------|--------|------|
| `id` | BIGINT | NOT NULL | AUTO_INCREMENT | 主键 |
| `user_id` | BIGINT | NOT NULL | — | 关联用户（FK → users.id）|
| `account_id` | VARCHAR(50) | NOT NULL | — | 券商账户 ID |
| `symbol` | VARCHAR(20) | NOT NULL | — | 证券代码 |
| `side` | VARCHAR(10) | NOT NULL | — | 方向（BUY / SELL）|
| `quantity` | DECIMAL(15,4) | NOT NULL | — | 委托数量 |
| `price` | DECIMAL(15,4) | NULL | — | 委托价格（市价单为 NULL）|
| `status` | VARCHAR(20) | NOT NULL | `PENDING` | 订单状态 |
| `broker_name` | VARCHAR(50) | NOT NULL | — | 券商标识 |
| `external_order_id` | VARCHAR(100) | NULL | — | 券商侧订单 ID |
| `created_at` | DATETIME(6) | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| `filled_at` | DATETIME(6) | NULL | — | 成交时间 |
| `updated_at` | DATETIME(6) | NOT NULL | CURRENT_TIMESTAMP | 更新时间 |

**索引**
- `PRIMARY KEY (id)`
- `INDEX idx_orders_user_id (user_id)`
- `INDEX idx_orders_account_status (account_id, status)`
- `INDEX idx_orders_created_at (created_at)`
- `FOREIGN KEY fk_orders_user (user_id) → users(id)`

**订单状态枚举值**：`PENDING` / `FILLED` / `PARTIALLY_FILLED` / `CANCELLED` / `REJECTED`

---

### 3.4 trades — 成交表

**Flyway 脚本**：`V4__create_trades.sql`

| 列名 | 类型 | NULL | 默认值 | 说明 |
|------|------|------|--------|------|
| `id` | BIGINT | NOT NULL | AUTO_INCREMENT | 主键 |
| `order_id` | BIGINT | NULL | — | 关联订单（FK → orders.id，外部导入时可为空）|
| `account_id` | VARCHAR(50) | NOT NULL | — | 券商账户 ID |
| `symbol` | VARCHAR(20) | NOT NULL | — | 证券代码 |
| `side` | VARCHAR(10) | NOT NULL | — | 方向（BUY / SELL）|
| `quantity` | DECIMAL(15,4) | NOT NULL | — | 成交数量 |
| `price` | DECIMAL(15,4) | NOT NULL | — | 成交价格 |
| `commission` | DECIMAL(10,4) | NOT NULL | `0` | 手续费 |
| `broker_name` | VARCHAR(50) | NOT NULL | — | 券商标识 |
| `external_trade_id` | VARCHAR(100) | NULL | — | 券商侧成交 ID |
| `traded_at` | DATETIME(6) | NOT NULL | — | 成交时间 |
| `created_at` | DATETIME(6) | NOT NULL | CURRENT_TIMESTAMP | 记录创建时间 |

**索引**
- `PRIMARY KEY (id)`
- `INDEX idx_trades_order_id (order_id)`
- `INDEX idx_trades_account_traded (account_id, traded_at)`
- `FOREIGN KEY fk_trades_order (order_id) → orders(id)`

---

## 4. Flyway 迁移规则

| 规则 | 内容 |
|------|------|
| 命名规则 | `V{版本}__{描述}.sql`，版本号递增，不可跳号 |
| 不可修改 | 已执行的脚本严禁修改，只能新增版本 |
| 失败处理 | 迁移失败后须手动修复，不使用 `cleanOnValidationError`（生产） |
| 校验 | `validate-on-migrate: true`，启动时自动校验 checksum |

---

## 5. Java 实体类对应

| 表名 | 实体类 | Repository |
|------|--------|-----------|
| `users` | `User` | `UserRepository` |
| `positions` | `Position` | `PositionRepository` |
| `orders` | `Order` | `OrderRepository` |
| `trades` | `Trade` | `TradeRepository` |

**关联枚举类**

| 枚举类 | 值 | 用途 |
|--------|----|------|
| `Role` | ROLE_ADMIN / ROLE_TRADER / ROLE_VIEWER | User.role |
| `OrderSide` | BUY / SELL | Order.side, Trade.side |
| `OrderStatus` | PENDING / FILLED / PARTIALLY_FILLED / CANCELLED / REJECTED | Order.status |

---

## 6. 修订历史

| 版本 | 日期 | 内容 | 阶段 |
|------|------|------|------|
| 1.0.0 | 2026-03-28 | 初版：4张表设计 + Flyway 迁移脚本 + Entity 对应 | Phase 2 |
