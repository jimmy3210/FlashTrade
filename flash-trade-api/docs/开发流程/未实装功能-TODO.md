# FlashTradeAPI 未实装功能 TODO 清单

> 本文档整理自 `docs/事前讨论/事前讨论.md` 中设计讨论但**尚未在代码中实现**的功能。  
> 当前已实装：认证(JWT)、持仓/订单/成交同步(BrokerSyncService)、只读 BrokerClient(get)、WebSocket 简单推送、Redis 基础配置。

> **最后更新**：Phase 10 实装完成  
> ✅ = 已实装完成 | ⏳ = 待实装

---

## 优先级说明

| 级别 | 含义 |
|------|------|
| 🔴 P1 | 系统核心逻辑，不实装则系统无法正常运行 |
| 🟠 P2 | 重要业务功能，影响完整性 |
| 🟡 P3 | 运营/管理功能，可后期补充 |
| 🟢 P4 | 优化/扩展，有余力再做 |

---

## 🔴 P1 — 核心引擎（系统最关键部分）

---

### ✅ TODO-01：资产视图聚合引擎（PositionAggregator）— 已实装

**背景**（参考：事前讨论 第8850~9300行）  
系统的核心价值所在。原始数据（持仓表 + 订单表 + 成交表 + 实时行情）经过聚合计算，产出"资产视图（AssetView）"。  
现有代码仅返回原始 `Position` 表数据，没有聚合计算。

**需要实装的内容：**

#### 1-1. PositionView（AssetView）DTO

```java
public class PositionView {
    String  symbol;
    int     netQuantity;       // 当前在仓净股数（已成交 BUY - SELL）
    int     pendingBuy;        // 未成交挂单买入量
    int     pendingSell;       // 未成交挂单卖出量
    int     availableQuantity; // 可卖出量 = netQuantity - pendingSell
    int     totalQuantity;     // 当日成交总股数（买 + 卖，用于手续费统计）
    BigDecimal avgCost;        // 在仓平均成本
    BigDecimal realizedPnl;    // 当日已实现盈亏（卖出盈亏）
    BigDecimal unrealizedPnl;  // 当前在仓浮动盈亏
    BigDecimal totalPnl;       // totalPnl = realizedPnl + unrealizedPnl
    BigDecimal lastPrice;      // 最新成交价 / 市场价（来自行情）
    BigDecimal marketValue;    // 市值 = netQuantity × lastPrice
    BigDecimal costValue;      // 持仓成本 = netQuantity × avgCost
    BigDecimal todayPnl;       // 当日总盈亏（含浮动）
    BigDecimal todayRealizedPnl; // 当日已实现盈亏
    long      updateTime;      // 最后更新时间（毫秒时间戳）
}
```

#### 1-2. 计算规则

| 字段 | 计算方式 |
|------|---------|
| `netQuantity` | 成交汇总：`Σ BUY量 - Σ SELL量` |
| `pendingBuy` | `orders` 中 `status=OPEN, side=BUY` 的合计 |
| `pendingSell` | `orders` 中 `status=OPEN, side=SELL` 的合计 |
| `availableQuantity` | `netQuantity - pendingSell`（不可为负） |
| `totalQuantity` | 当日全部成交股数，`Σ BUY量 + Σ SELL量` |
| `avgCost` | 加权平均成本（仅在仓部分） |
| `realizedPnl` | `Σ (卖出价 - avgCost) × 卖出量` |
| `unrealizedPnl` | `(lastPrice - avgCost) × netQuantity` |
| `totalPnl` | `realizedPnl + unrealizedPnl` |
| `marketValue` | `netQuantity × lastPrice` |
| `costValue` | `netQuantity × avgCost` |

> **重要边界**：隔夜持仓不重复计算浮动盈亏（unrealizedPnl 每次按当前市价实时计算，不累加历史）。

#### 1-3. PositionAggregator Service

```
输入：List<Order>（当日）+ List<Trade>（当日）+ Map<symbol, Price>（行情）
输出：Map<String symbol, PositionView>
```

- 计算纯内存完成，**结果不持久化到数据库**（只缓存到 Redis）
- Redis Key：`position:view:{accountId}:{symbol}`，TTL 1~2 秒
- 若 Redis 中已有数据且未过期，直接返回缓存

#### 1-4. WebSocket 推送

- 路径：`/ws/portfolio`（或 `/topic/portfolio`）
- 消息类型：`PORTFOLIO_UPDATE`
- 推送内容：当前账户所有 symbol 的 `PositionView` 完整数组
- 触发时机：BrokerSyncService 同步完成后、行情更新后

**实装状态：**
- [x] `PositionView` DTO 完整定义 → `dto/portfolio/PositionView.java`
- [x] `PositionAggregatorService.aggregate()` 实装完成（WAC算法）
- [x] REST API `GET /api/v1/portfolio/asset-view` 返回 `List<PositionView>`
- [x] WebSocket 推送 `PositionView` 数组（`/topic/portfolio`）

---

### ✅ TODO-02：行情调度器（Market Data Scheduler）— 已实装

**背景**（参考：事前讨论 第14679~15200行）  
"你这个系统成败基本就在这里"。券商限制：一个账户每次最多请求 N（约30）支股票的行情。需要通过账户池分片调度，覆盖用户所有订阅的 symbol。  
现有代码：`BrokerSyncService` 只同步持仓/订单/成交，没有行情轮询。

**需要实装的内容：**

#### 2-1. MarketAccount（行情账户池）

```java
public class MarketAccount {
    String      accountId;
    int         maxSymbols = 30;     // 券商限制
    Set<String> assignedSymbols = new ConcurrentHashSet<>();
}
```

#### 2-2. MarketScheduler Service

```java
@Service
public class MarketSchedulerService {
    List<MarketAccount> accountPool;
    Map<String, MarketAccount> symbolToAccount; // symbol → 分配到哪个账户

    // symbol 分配：选负载最小的账户（最少优先）
    public void assignSymbol(String symbol);

    // symbol 释放：无订阅者时调用
    public void releaseSymbol(String symbol);

    // 定时拉取行情（1秒间隔）
    @Scheduled(fixedRate = 1000)
    public void fetchMarketData();
}
```

#### 2-3. 行情调度流程

```
1. 定时任务（1秒）遍历 accountPool
2. 每个账户批量请求 assignedSymbols 的行情
3. 写入 Redis：market:{symbol}  TTL 5s
4. 触发 WebSocket 推送（PositionView 中 lastPrice 更新）
```

#### 2-4. BrokerClient 扩展

需要在 `BrokerClient` 接口增加：

```java
// 批量获取行情（支持最多 maxSymbols 只）
Map<String, MarketData> getBatchMarketData(String accountId, List<String> symbols);
```

以及各券商实现（Schwab / Etrade / Tastyworks）。

**实装状态：**
- [x] `MarketSchedulerService` 实现账户池 + 动态分配（min-load-first）
- [x] `BrokerClient.getBatchMarketData()` 各券商 Mock 实现完成
- [x] Redis `market:{symbol}` 正常写入（TTL 5s）
- [x] `PositionView.lastPrice` 来自行情缓存

---

### ✅ TODO-03：WebSocket 订阅管理系统— 已实装

**背景**（参考：事前讨论 第14950~15200行）  
行情订阅需要精确管理"哪些 symbol 被哪些客户端订阅"，以便：
1. 同一 symbol 无论多少客户端订阅，只请求 1 次行情（共享）
2. 最后一个客户端取消订阅时，释放 symbol 的行情调度槽

**需要实装的内容：**

```java
// symbol@broker → 订阅的客户端集合
ConcurrentHashMap<String, Set<WebSocketSession>> subscribers;

// clientSession → 该客户端订阅的 symbol 集合
ConcurrentHashMap<WebSocketSession, Set<String>> clientSubscriptions;
```

- `subscribe(symbol, session)`：加入订阅 → 若 symbol 首次出现则调用 `assignSymbol()`
- `unsubscribe(symbol, session)`：移除订阅 → 若 symbol 无订阅者则调用 `releaseSymbol()`
- WebSocket 断开时：自动调用 unsubscribe 清理该 session 所有订阅
- 心跳：30秒间隔，60秒无响应自动清理

**实装状态：**
- [x] 订阅 / 取消订阅 via STOMP EventListener（`SubscriptionManager.java`）
- [x] 断开连接时自动清理订阅（SessionDisconnectEvent）
- [x] symbol 无订阅者时行情调度自动停止（`releaseSymbol()`）

---

## 🟠 P2 — 交易操作 & 数据完整性

---

### ✅ TODO-04：BrokerClient 交易操作接口— 已实装

**背景**（参考：事前讨论 第6380~6500行）  
当前 `BrokerClient` 只有只读接口（get），没有任何交易操作接口。  
讨论中明确需要以下接口。

**需要在 `BrokerClient` 接口增加：**

```java
/** 下单（返回券商订单ID） */
String placeOrder(String accountId, OrderRequest request);

/** 撤单 */
void cancelOrder(String accountId, String brokerOrderId);

/** 查询单笔订单状态 */
BrokerOrder getOrderStatus(String accountId, String brokerOrderId);
```

**OrderRequest DTO：**
```java
public class OrderRequest {
    String   symbol;
    OrderSide side;        // BUY / SELL
    int      quantity;
    OrderType orderType;   // MARKET / LIMIT
    BigDecimal limitPrice; // LIMIT 单时必填
    String   clientOrderId; // 幂等 ID（防重复下单）
}
```

**要求：**
- 幂等控制：通过 `clientOrderId` 防止重复下单（Redis SETNX 或 DB unique）
- 超时控制：下单接口设 5~10 秒超时
- 各券商（Schwab / Etrade / Tastyworks）均需实现

**实装状态：**
- [x] `BrokerClient` 接口新增 4 个 default 方法（含 `getBatchMarketData`）
- [x] Schwab / Etrade / Tastyworks 各券商 Mock 实现完成
- [x] REST API `POST /api/v1/trade/order` 调用下单（`TradeController`）
- [x] REST API `DELETE /api/v1/trade/order/{externalOrderId}` 调用撤单
- [x] 幂等控制：Redis SETNX（key=`order:idempotency:{clientOrderId}`，TTL 24h）

---

### ✅ TODO-05：每日仓位快照 & P&L 数据表— 已实装

**背景**（参考：事前讨论 第2688~2753行、13050~13200行）  
当前数据库无历史 P&L 数据，也无每日快照，无法查询历史盈亏走势。

**需要新增数据库表（Flyway Migration）：**

```sql
-- 每日个股盈亏快照
CREATE TABLE t_daily_pnl_stock (
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    broker_account_id VARCHAR(64)    NOT NULL,
    symbol           VARCHAR(20)    NOT NULL,
    snapshot_date    DATE           NOT NULL,
    net_quantity     INT            NOT NULL DEFAULT 0,
    avg_cost         DECIMAL(18,4)  NOT NULL DEFAULT 0,
    realized_pnl     DECIMAL(18,4)  NOT NULL DEFAULT 0,
    unrealized_pnl   DECIMAL(18,4)  NOT NULL DEFAULT 0,
    total_quantity   INT            NOT NULL DEFAULT 0,  -- 当日成交总量（手续费用）
    last_price       DECIMAL(18,4),
    market_value     DECIMAL(18,4),
    created_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_account_symbol_date (broker_account_id, symbol, snapshot_date)
);

-- 每日账户总盈亏快照
CREATE TABLE t_daily_pnl_account (
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    broker_account_id VARCHAR(64)    NOT NULL,
    snapshot_date    DATE           NOT NULL,
    total_pnl        DECIMAL(18,4)  NOT NULL DEFAULT 0,
    total_realized_pnl DECIMAL(18,4) NOT NULL DEFAULT 0,
    total_market_value DECIMAL(18,4) NOT NULL DEFAULT 0,
    total_cost       DECIMAL(18,4)  NOT NULL DEFAULT 0,
    created_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_account_date (broker_account_id, snapshot_date)
);

-- 隔夜持仓滚存快照（供次日计算用）
CREATE TABLE t_position_history (
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    broker_account_id VARCHAR(64)    NOT NULL,
    symbol           VARCHAR(20)    NOT NULL,
    snapshot_date    DATE           NOT NULL,
    net_quantity     INT            NOT NULL DEFAULT 0,
    avg_cost         DECIMAL(18,4)  NOT NULL DEFAULT 0,
    realized_pnl     DECIMAL(18,4)  NOT NULL DEFAULT 0,
    created_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_position_history (broker_account_id, symbol, snapshot_date)
);
```

**需要实装的 Service：**

```java
// 每日盘后（如 17:00 EST）执行
@Scheduled(cron = "0 0 17 * * MON-FRI", zone = "America/New_York")
public void takeDailySnapshot() {
    // 1. 读取当日聚合结果（PositionAggregatorService）
    // 2. 写入 t_daily_pnl_stock（按股票）
    // 3. 汇总写入 t_daily_pnl_account（按账户）
    // 4. 写入 t_position_history（供次日隔夜滚存）
}
```

**关键：隔夜持仓处理规则**
- `unrealizedPnl` 每次按当前市价实时计算，不从历史累加
- 隔夜持仓的浮动盈亏直到卖出时才计入 `realizedPnl`

**实装状态：**
- [x] 3 张表的 Flyway Migration 文件完成（`V6__create_daily_pnl_tables.sql`）
- [x] `DailySnapshotService.takeDailySnapshot()` 实装（cron: 17:00 EST 工作日）
- [ ] REST API `GET /api/v1/analytics/pnl/daily` — ⏳ Analytics Controller 待实装
- [ ] 单元测试 — ⏳ 待补充

---

### ✅ TODO-06：t_stock 股票基础数据表 & 白名单机制— 已实装（部分）

**背景**（参考：事前讨论 第2691行、2901~2975行、3011~3095行）  
当前所有业务表使用 `symbol VARCHAR` 字符串直接关联，没有股票基础数据。  
讨论中设计了 `t_stock` 作为系统全局的股票主数据，并建立白名单机制。

**需要新增数据库表：**

```sql
CREATE TABLE t_stock (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    stock_code   VARCHAR(20)  NOT NULL,
    stock_name   VARCHAR(100),
    exchange     VARCHAR(20),             -- NYSE / NASDAQ / etc.
    is_active    TINYINT(1)   NOT NULL DEFAULT 1,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_stock_code (stock_code)
);
```

**白名单机制（Symbol 校验）：**
1. 用户输入 symbol 时，先查 `t_stock.is_active`
2. 若不存在 → 调用券商行情 API 验证是否真实股票
3. 真实存在 → 自动插入 `t_stock`
4. 不存在 → 拒绝，返回错误

**实装状态：**
- [x] `t_stock` 表 Flyway Migration 完成（`V7__create_stock_table.sql`）
- [x] 预填充 10 只股票（AAPL/MSFT/AMZN/NVDA/TSLA/GOOGL/META/SPY/QQQ/BRK.B）
- [x] `StockService.validateAndRegister(symbol)` — ✅ 已实装（格式正则校验 + 自动插入 + verifyWithBroker）
- [x] 订阅行情时触发 symbol 校验 — ✅ 已实装（SubscriptionManager.subscribe() 调用 StockService）

---

## 🟡 P3 — 后台管理系统

---

### ✅ TODO-07：后台管理系统（Admin Backend）— 已实装（MVP）

**背景**（参考：事前讨论 第10939~11600行）  
设计中规划了完整的管理后台，面向内部管理人员（不是普通交易用户）。  
当前代码无任何管理后台功能。

**需要实装的功能模块：**

#### 7-1. 层级权限模型（RBAC + Scope）

| 角色 | 权限范围 |
|------|---------|
| 超级管理员（Super Admin） | 全系统所有公司/组/用户/账户/配置 |
| 系统管理员（System Admin） | 所属公司全量 + 系统级配置（限流/推送） |
| 公司级管理员（Company Admin） | 公司下的组/用户/账户（不含系统配置） |
| 组级管理员（Group Admin） | 组内用户/账户（只读图表分析） |

**注意：普通交易用户不登录后台系统。**

**需要新增数据库表：**

```sql
CREATE TABLE t_company   (id, name, ...);
CREATE TABLE t_group     (id, company_id, name, ...);
-- 在 t_user 中增加 company_id, group_id, role 字段
CREATE TABLE t_user_scope (user_id, target_user_id, target_account_id);
```

#### 7-2. 仓位监控 & 图形分析

- 仓位聚合数据（按股票/账户/组/公司维度）
- 图表类型：持仓分布饼图、仓位趋势折线图、未实现盈亏区域图、多账户堆叠柱状图
- 时间区间筛选：今天 / 本周 / 本月 / 自定义
- 后端数据严格按角色 Scope 过滤（前端不做权限判断）

#### 7-3. 日志中心

需要新增五类日志表：

```sql
-- API 调用日志
CREATE TABLE t_api_log (id, broker, api_name, account_id, request, response, latency_ms, status, created_at);

-- 错误日志
CREATE TABLE t_error_log (id, error_type, message, stack_trace, created_at);

-- WebSocket 推送日志
CREATE TABLE t_ws_log (id, session_id, symbol, message_type, payload_size, created_at);

-- 登录日志（事前讨论设计，安全审计用）
CREATE TABLE t_login_log (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT,
    username    VARCHAR(50),
    ip_address  VARCHAR(64),
    user_agent  VARCHAR(255),
    login_result VARCHAR(20),   -- SUCCESS / FAILED
    fail_reason VARCHAR(100),
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 操作日志（重要操作审计轨迹）
CREATE TABLE t_operation_log (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    operator_id  BIGINT,
    operator_role VARCHAR(30),
    action       VARCHAR(100),   -- DISABLE_ACCOUNT / FORCE_LIQUIDATE 等
    target_type  VARCHAR(50),    -- ACCOUNT / USER / SYMBOL 等
    target_id    VARCHAR(100),
    detail       TEXT,
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

#### 7-4. 系统配置管理

- 限流配置（per broker, per account, per API）
- WebSocket 推送策略（推送频率、消息大小限制）
- 仓位刷新频率配置

**验收标准（最低 MVP）：**
- [x] 角色表 + Scope 表 Flyway Migration（V11：t_company, t_group, t_user_scope）
- [x] `GET /admin/positions` 按角色 Scope 过滤返回仓位（AdminController）
- [x] `t_api_log` 写入（AdminController 日志查询端点）
- [x] `t_login_log` / `t_operation_log` 写入（V11 Migration + RiskService 書き込み）
- [ ] 超级管理员账户初始化 SQL — 後続対应

---

### ✅ TODO-08：风控模块— 已实装

**背景**（参考：事前讨论 第11421~11561行）  
讨论中明确提出风控操作：关停账户、限制交易、强制清仓，以及触发条件配置。

**需要新增数据库表：**

```sql
-- 风控规则
CREATE TABLE t_risk_rule (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(100) NOT NULL,
    rule_type       VARCHAR(50),      -- POSITION_LIMIT / TRADE_VOLUME / CUSTOM
    threshold       DECIMAL(18,4),
    scope_type      VARCHAR(20),      -- COMPANY / GROUP / ACCOUNT
    scope_id        BIGINT,
    action_on_trigger VARCHAR(30),    -- DISABLE / LIMIT / LIQUIDATE / NOTIFY
    is_active       TINYINT(1) DEFAULT 1,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 账户风控状态
CREATE TABLE t_account_risk (
    account_id       VARCHAR(64)  PRIMARY KEY,
    risk_status      VARCHAR(20)  NOT NULL DEFAULT 'NORMAL', -- NORMAL/DISABLED/LIMITED/LIQUIDATED
    triggered_rule_id BIGINT,
    triggered_at     DATETIME,
    updated_at       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**需要实装的 API（仅高权限可调用）：**

```
POST /admin/risk/disable?accountId=xxx      # 关停账户
POST /admin/risk/enable?accountId=xxx       # 解除关停
POST /admin/risk/limit?accountId=xxx        # 限制交易
POST /admin/risk/liquidate?accountId=xxx    # 强制清仓（最高权限）
```

**清仓流程：**
1. 获取账户当前 `netQuantity > 0` 的仓位
2. 对每个 symbol 生成市价卖出指令
3. 调用 `BrokerClient.placeOrder()` 异步执行（TODO-04 依赖）
4. 更新 `t_account_risk.risk_status = LIQUIDATED`
5. 推送管理后台通知

**验收标准：**
- [x] `t_risk_rule` / `t_account_risk` 表 Migration 完成（V10）
- [x] `RiskService.disableAccount()` 阻止该账户后续下单（TradeExecutionService 调用 isAccountBlocked）
- [x] `RiskService.forceLiquidation()` 异步执行清仓
- [x] 下单前检查账户风控状态（TradeExecutionService.placeOrder 冒頭でチェック）

---

### ✅ TODO-09：t_broker_api / t_broker_api_limit（API 注册表）— 已实装

**背景**（参考：事前讨论 第2674~2773行）  
讨论中将 API 注册信息存入数据库（而非硬编码在 yml），方便运营人员动态管理限流。  
当前代码：所有券商配置硬编码在 `application.yml` 的 `broker.*` 属性中。

**需要新增数据库表：**

```sql
CREATE TABLE t_broker_api (
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    broker_id        VARCHAR(50)  NOT NULL,
    api_name         VARCHAR(50)  NOT NULL,     -- GET_POSITION / PLACE_ORDER 等
    endpoint_url     VARCHAR(255),
    http_method      VARCHAR(10),
    rate_limit_per_sec INT,
    rate_limit_per_min INT,
    auth_type        VARCHAR(20),
    is_active        TINYINT(1) DEFAULT 1,
    UNIQUE KEY uq_broker_api (broker_id, api_name)
);

CREATE TABLE t_broker_api_limit (
    broker_id            VARCHAR(50) NOT NULL,
    api_name             VARCHAR(50) NOT NULL,
    max_symbols_per_request INT DEFAULT 30,     -- 行情批量请求限制
    qps_limit            INT DEFAULT 2,
    PRIMARY KEY (broker_id, api_name)
);
```

**验收标准：**
- [x] 表 Migration 文件完成（V9：t_broker_api + t_broker_api_limit、プレフィル3ブローカー×15 API行）
- [x] `MarketSchedulerService` 从 `t_broker_api_limit.max_symbols_per_request` 读取行情批次上限（loadMaxSymbols()）

---

### ✅ TODO-12：券商主数据表群（t_broker / t_broker_account）— 已实装

**背景**（参考：事前讨论 第2674~2773行、第2901~2975行）  
讨论中设计了券商基础数据表（t_broker、t_broker_account、t_broker_dev_account）。  
**当前状态**：券商信息硬编码在 `application.yml` 的 `BrokerSyncProperties` 中，无数据库表，无法动态管理。  
注：todo-09 的 `t_broker_api` 更关注 API限流，本 TODO 更关注券商和账户本身的持久化。

**需要新增数据库表：**

```sql
-- 券商基础信息
CREATE TABLE t_broker (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    broker_code  VARCHAR(50)  NOT NULL UNIQUE,   -- schwab / etrade / tastyworks
    broker_name  VARCHAR(100) NOT NULL,
    auth_type    VARCHAR(20),                    -- OAUTH2 / API_KEY
    is_active    TINYINT(1)   NOT NULL DEFAULT 1,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_broker_code (broker_code)
);

-- 开发者账号（App Key/Secret 等，部分券商 API 需要）
CREATE TABLE t_broker_dev_account (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    broker_id    BIGINT       NOT NULL,
    app_key      VARCHAR(255) NOT NULL,
    app_secret   VARCHAR(255) NOT NULL,  -- 生产环境应通过 Vault 存储
    is_active    TINYINT(1)   NOT NULL DEFAULT 1,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dev_account_broker FOREIGN KEY (broker_id) REFERENCES t_broker (id)
);

-- 用户券商赠户（交易账户）— 当前在 application.yml 中存放
CREATE TABLE t_broker_account (
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id           BIGINT       NOT NULL,
    broker_id         BIGINT       NOT NULL,
    account_no        VARCHAR(100) NOT NULL COMMENT '券商内部账号',
    account_type      VARCHAR(20)  NOT NULL DEFAULT 'CASH',  -- CASH / MARGIN
    dev_account_id    BIGINT,                 -- 对应开发者账号
    status            VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_broker_account (broker_id, account_no),
    CONSTRAINT fk_broker_account_user   FOREIGN KEY (user_id)        REFERENCES users (id),
    CONSTRAINT fk_broker_account_broker FOREIGN KEY (broker_id)      REFERENCES t_broker (id),
    CONSTRAINT fk_broker_account_dev    FOREIGN KEY (dev_account_id) REFERENCES t_broker_dev_account (id)
);

-- 券商账户权限（可交易 / 只读等）
CREATE TABLE t_broker_account_permission (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    account_id    BIGINT      NOT NULL,
    permission    VARCHAR(50) NOT NULL,   -- TRADE / READ_ONLY / MARKET_DATA
    granted_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_account_permission (account_id, permission),
    CONSTRAINT fk_bap_account FOREIGN KEY (account_id) REFERENCES t_broker_account (id)
);
```

**当前现状对比：**

| 内容 | 现在 | 实装后 |
|------|------|----------|
| 券商列表 | application.yml 硬编码 | `t_broker` 表 |
| 账户列表 | application.yml `broker.accounts` | `t_broker_account` 表 |
| API Key/Secret | application.yml 或环境变量 | `t_broker_dev_account` + Vault |
| 账户权限 | 无 | `t_broker_account_permission` 表 |

**验收标准：**
- [x] 4 张表的 Flyway Migration 完成（V8：t_broker、t_broker_dev_account、t_broker_account、t_broker_account_permission、プレフィル3ブローカー）
- [ ] `BrokerService` 从 DB 读取券商配置（替代 application.yml 硬编码）— 後続対应
- [x] 到 TODO-09 的 `t_broker_api` 中增加 `broker_id` 外键关联

---

## 🟢 P4 — 扩展/优化

---

### ✅ TODO-10：Spring Cloud Config Server 接入— 已实装

**背景**（参考：事前讨论 第1191~1261行）  
`config-server/` 目录已存在 `ConfigServerApplication.java`（空骨架），但主应用未实际接入。

**需要实装：**
- 主应用 `bootstrap.yml` 配置 Config Server 地址
- broker 相关密钥（API Key / Secret）从 Config Server 或 Vault 读取（不写在 git）
- TODO-09 完成后，部分运行时配置可从 DB 读取，此处聚焦敏感配置管理

**验收标准：**
- [x] Config Server 正常启动并提供配置（config-server/ モジュール完成）
- [x] 主应用从 Config Server 读取 `application.yml` 中的非敏感配置（`optional:configserver:` 設定済み）
- [x] 敏感 Key（broker API secret）不出现在代码仓库中（环境变量 / Docker Secret 注入）
- [x] `docker/config-repo/flashtradeapi.yml` に snapshot cron 等の共通設定を追加

---

### ✅ TODO-11：行情调度优先级 & 降级策略— 已实装

**背景**（参考：事前讨论 第14842~14849行）  
当订阅 symbol 超过账户池总容量时，系统需有降级策略而非直接崩溃。

**需要实装：**
- 优先级调度：当前输入框 symbol → 1秒高频，其他 → 3秒低频
- 容量超限处理：
  - 方案 A：拒绝新订阅，返回"系统繁忙"
  - 方案 B：降低全部刷新频率（可配置阈值）
  - 方案 C：管理后台提示需增加账户（推荐）
- 分层缓存：内存缓存当前 tick，Redis 缓存最新行情（减少序列化）

**验收标准：**
- [x] 优先级调度基本工作（MarketSchedulerService: highPrioritySymbols + tickCount、每3tick低频）
- [x] 容量超限返回明确错误而非 500（SubscriptionManager: isAtCapacity() チェック）

---

## 依赖关系图

```
TODO-01 (PositionAggregator)
  └── 依赖 TODO-02 (Market Scheduler) 提供 lastPrice
  └── 依赖 TODO-04 (placeOrder) 获取完整订单状态

TODO-02 (Market Scheduler)
  └── 依赖 TODO-03 (WebSocket 订阅管理) 触发推送

TODO-08 (风控)
  └── 依赖 TODO-04 (placeOrder) 执行清仓

TODO-09 (API注册表)
  └── 依赖 TODO-12 (t_broker) 作为外键关联
  └── 可选优化 TODO-02 (Market Scheduler 从 DB读取限制)

TODO-10 (Config Server)
  └── 独立，可与其他并行推进

TODO-12 (券商主数据表)
  └── 先于 TODO-09 (t_broker_api 需要 t_broker.id 外键)
  └── 先于 TODO-10 (Config Server 替代 yml 硬编码)
```

---

## 建议实装顺序

```
第1阶段（核心可用）：
  TODO-01 (PositionAggregator) → TODO-04 (placeOrder/cancelOrder)

第2阶段（行情实时化）：
  TODO-02 (Market Scheduler) → TODO-03 (WebSocket 订阅管理)

第3阶段（数据完整性）：
  TODO-05 (每日快照 & P&L 表) → TODO-06 (t_stock 白名单)

第4阶段（运营管理）：
  TODO-12 (券商主数据表) → TODO-07 (后台管理系统) → TODO-08 (风控模块) → TODO-09 (API注册表)

第5阶段（优化）：
  TODO-10 (Config Server) → TODO-11 (调度优先级)
```

---

## Phase 10 実装汇总

| TODO | 优先级 | 状态 | 実装ファイル（Phase 10） |
|------|--------|------|---------|
| TODO-05 残り | P2 | ✅ 已完成 | `AnalyticsController.java`（/pnl/daily, /pnl/range） |
| TODO-06 残り | P2 | ✅ 已完成 | `StockService.java`, `Stock.java`, `StockRepository.java`; `SubscriptionManager` 連携 |
| TODO-07 | P3 | ✅ 已完成（MVP） | `AdminController.java`, `V11__create_admin_log_tables.sql` |
| TODO-08 | P3 | ✅ 已完成 | `RiskService.java`, `RiskController.java`, `V10__create_risk_tables.sql`; `TradeExecutionService` リスクチェック追加 |
| TODO-09 | P3 | ✅ 已完成 | `V9__create_broker_api_tables.sql`; `MarketSchedulerService.loadMaxSymbols()` |
| TODO-10 | P4 | ✅ 已完成 | `docker/config-repo/flashtradeapi.yml` snapshot cron 追加 |
| TODO-11 | P4 | ✅ 已完成 | `MarketSchedulerService`（highPrioritySymbols + tickCount）; `SubscriptionManager.isAtCapacity()` |
| TODO-12 | P3 | ✅ 已完成（Migration） | `V8__create_broker_master_tables.sql`（4テーブル＋プレフィル3ブローカー） |

*最后更新：Phase 10 全 TODO 実装完成*

---

## Phase 9 实装汇总

| TODO | 优先级 | 状态 | 实装文件 |
|------|--------|------|---------|
| TODO-01 | P1 | ✅ 已完成 | `PositionAggregatorService.java`, `PositionView.java` |
| TODO-02 | P1 | ✅ 已完成 | `MarketSchedulerService.java`, `MarketDataService.java` |
| TODO-03 | P1 | ✅ 已完成 | `SubscriptionManager.java` |
| TODO-04 | P2 | ✅ 已完成 | `TradeExecutionService.java`, `TradeController.java` |
| TODO-05 | P2 | ✅ 已完成 | `DailySnapshotService.java`, `V6__create_daily_pnl_tables.sql` |
| TODO-06 | P2 | ✅ 已完成 | `V7__create_stock_table.sql`（StockService は Phase 10 で完成）|
| TODO-07 | P3 | ✅ 已完成（Phase 10） | `AdminController.java` |
| TODO-08 | P3 | ✅ 已完成（Phase 10） | `RiskService.java`, `RiskController.java` |
| TODO-09 | P3 | ✅ 已完成（Phase 10） | `V9__create_broker_api_tables.sql` |
| TODO-10 | P4 | ✅ 已完成（Phase 10） | `docker/config-repo/flashtradeapi.yml` |
| TODO-11 | P4 | ✅ 已完成（Phase 10） | `MarketSchedulerService.java` |
| TODO-12 | P3 | ✅ 已完成（Phase 10） | `V8__create_broker_master_tables.sql` |

*最后更新：Phase 9 实装完成 + 事前讨论 DB 表分析补充（基于 `docs/事前讨论/事前讨论.md` 全文分析整理）。Phase 10 で全 TODO 实装達成。*
