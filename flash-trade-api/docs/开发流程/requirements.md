# FlashTradeAPI — 需求定义书

**文档版本**：1.2.0  
**最后更新**：2026-03-28  
**状态**：Phase 9 功能追加

---

## 1. 项目概述

FlashTradeAPI 是一个面向内部使用的多券商证券交易数据聚合平台。系统通过统一接口对接多家外部券商 REST API，将持仓、订单、成交等数据整合后，以 REST API 和 WebSocket 两种形式提供给前端及内部系统消费。

---

## 2. 目标用户与角色

| 角色 | 描述 | 代表操作 |
|------|------|---------|
| `ROLE_ADMIN` | 系统管理员 | 查看所有用户数据、管理用户账号 |
| `ROLE_TRADER` | 交易员 | 查看自己的持仓、订单、成交记录 |
| `ROLE_VIEWER` | 只读观察员 | 查看持仓和行情，无操作权限 |

---

## 3. 功能需求

### 3.1 用户认证（Phase 3）

| ID | 需求描述 |
|----|---------|
| FR-001 | 用户可通过用户名 + 密码进行登录，系统返回 JWT Access Token 和 Refresh Token |
| FR-002 | Access Token 有效期为 1 小时；Refresh Token 有效期为 7 天 |
| FR-003 | 支持使用 Refresh Token 换取新 Access Token |
| FR-004 | 登出时将 Token 加入 Redis 黑名单，使 Token 立即失效 |
| FR-005 | 所有 API 端点（除 `/api/auth/**` 外）须携带有效 JWT 才可访问 |
| FR-006 | 基于角色的访问控制（RBAC），不同角色访问不同端点 |

### 3.2 持仓与订单查询（Phase 4）

| ID | 需求描述 |
|----|---------|
| FR-010 | `GET /api/portfolio/positions` — 返回当前用户的持仓列表 |
| FR-011 | `GET /api/orders` — 返回当前用户的订单列表（支持分页） |
| FR-012 | 持仓数据每 30 秒从外部券商 API 同步一次 |

### 3.3 实时推送（Phase 4）

| ID | 需求描述 |
|----|---------|
| FR-020 | 系统提供 WebSocket 端点 `/ws` |
| FR-021 | 持仓数据更新时，推送至 STOMP Topic `/topic/positions` |
| FR-022 | 仅认证用户可订阅推送 Topic |

### 3.4 外部券商 API 接入（Phase 5 / Phase 6 / Phase 8）

| ID | 需求描述 |
|----|---------|
| FR-030 | 定义统一 `BrokerClient` 接口，屏蔽不同券商 API 的差异 |
| FR-031 | `getOrders(accountId)` — 获取指定账户的订单列表 |
| FR-032 | `getTrades(accountId)` — 获取指定账户的成交记录 |
| FR-033 | `getPortfolio(accountId)` — 获取指定账户的持仓信息 |
| FR-034 | 嘉信 Schwab 证券 API 具体实现（Phase 6，OAuth 2.0） |
| FR-035 | 外部 API 调用结果缓存至 Redis，TTL 默认 5 分钟 |
| FR-036 | 外部 API 调用失败时自动重试（最多 3 次，指数退避） |
| FR-037 | E\*TRADE API 实现（Phase 8，OAuth 1.0a，HMAC-SHA1 签名） |
| FR-038 | Tastyworks API 实现（Phase 8，REST Session Token，POST /sessions） |
| FR-039 | 三家券商均支持 Mock 模式，可不依赖真实账户进行本地开发和测试 |
| FR-040 | 券商 API Key / OAuth Token 通过 Docker Secret 注入，不写入 `.env` 或 Git |

### 3.5 配置与环境变量管理（Phase 7）

| ID | 需求描述 |
|----|---------|
| FR-050 | 部署独立的 Spring Cloud Config Server（端口 8888） |
| FR-051 | Config Server 支持多环境配置文件：`application-dev.yml`、`application-prod.yml`、`application-test.yml` |
| FR-052 | 应用端使用 `@RefreshScope` + `/actuator/refresh` 支持不停机动态刷新配置 |
| FR-053 | 数据库密码、券商 API Key 等敏感信息通过 Docker Secret 或 systemd 环境变量注入，不存放在 `.env` 文件或 Git 仓库中 |
| FR-054 | `.env` 文件仅存放非敏感部署变量（`SPRING_PROFILES_ACTIVE`、`CONFIG_SERVER_URL`、`REDIS_HOST`、`REDIS_PORT` 等） |
| FR-055 | Docker Compose 使用 `secrets` 节点管理敏感信息，挂载至容器内 `/run/secrets/` |
| FR-056 | 生产环境推荐使用 Ubuntu 服务器 + systemd 或 Docker Secrets 方式管理密钥 |

### 3.6 资产视图聚合（Phase 9）

| ID | 需求描述 |
|----|---------|
| FR-060 | `GET /api/v1/portfolio/asset-view` — 返回聚合计算的 `PositionView` 列表，可按 `accountId` 过滤 |
| FR-061 | `PositionView` 包含字段：`netQuantity`（净持仓量）、`pendingBuy/Sell`（未成交挂单）、`availableQuantity`（可卖数量）、`totalQuantity`（当日总成交量）、`avgCost`（加权均成本）、`realizedPnl/unrealizedPnl/totalPnl`（盈亏三维）、`lastPrice`、`marketValue`、`costValue` |
| FR-062 | 聚合计算从 `trades` + `orders` 表实时计算，**结果不持久化**；行情价格优先取 Redis（`market:{symbol}`），fallback 至 `positions` 表的 `currentPrice` |
| FR-063 | 同步完成后，向 WebSocket `/topic/portfolio` 推送最新 `PositionView` 数组 |

### 3.7 实时行情调度（Phase 9）

| ID | 需求描述 |
|----|---------|
| FR-070 | 行情调度器（Market Scheduler）每 1 秒向行情账户池中各账户批量请求当前订阅 symbol 的行情 |
| FR-071 | 单账户一次最多请求 30 只股票（可通过 `t_broker_api_limit` 或 `app.market.max-symbols-per-account` 配置） |
| FR-072 | 符号分配策略：新 symbol 分配给当前负载最小的账户（最少优先）；最后一个订阅者取消后自动释放调度槽 |
| FR-073 | `BrokerClient.getBatchMarketData()` — 各券商实现批量行情接口；Mock 模式返回合成价格数据 |

### 3.8 WebSocket 行情订阅管理（Phase 9）

| ID | 需求描述 |
|----|---------|
| FR-080 | STOMP 订阅 `/topic/market/{symbol}` 自动触发 symbol 的行情调度（通过 `SubscriptionManager`） |
| FR-081 | 同一 symbol 多 session 订阅时，行情仅拉取一次（共享调度槽） |
| FR-082 | WebSocket 断开连接时，自动清理该 session 的所有行情订阅 |

### 3.9 交易操作接口（Phase 9）

| ID | 需求描述 |
|----|---------|
| FR-090 | `POST /api/v1/trade/order?accountId=` — 下单（支持 `clientOrderId` 幂等控制，Redis SETNX，TTL 24h） |
| FR-091 | `DELETE /api/v1/trade/order/{externalOrderId}?accountId=` — 撤单 |
| FR-092 | `GET /api/v1/trade/order/{externalOrderId}/status?accountId=` — 实时查询委托状态 |
| FR-093 | `OrderRequest` DTO 字段：`symbol`、`side`（BUY/SELL）、`orderType`（MARKET/LIMIT）、`quantity`、`limitPrice`、`clientOrderId` |
| FR-094 | 各券商（Schwab / E\*TRADE / Tastyworks）Mock 模式下 `placeOrder` / `cancelOrder` 返回模拟结果，真实模式留 `UnsupportedOperationException` 待实现 |

### 3.10 每日快照与历史 P&L（Phase 9）

| ID | 需求描述 |
|----|---------|
| FR-100 | 每个交易日收盘后（默认美东时间 17:00）执行每日快照，写入 `t_daily_pnl_stock`、`t_daily_pnl_account`、`t_position_history` |
| FR-101 | `t_daily_pnl_stock`：按（券商账户 ID + symbol + 日期）唯一，记录 netQuantity、avgCost、realizedPnl、unrealizedPnl 等 |
| FR-102 | `t_position_history`：隔夜持仓滚存快照，供次日基准参考；unrealizedPnl 不累加历史，每次按实时价重算 |
| FR-103 | 快照计划表达式可通过 `app.snapshot.cron`（默认 `0 0 17 * * MON-FRI`）和 `app.snapshot.timezone`（默认 `America/New_York`）配置 |

### 3.11 股票基础数据（Phase 9）

| ID | 需求描述 |
|----|---------|
| FR-110 | 新增 `t_stock` 表：`stock_code`（UNIQUE）、`stock_name`、`exchange`、`is_active` |
| FR-111 | 预填充 10 只常用 Mock 股票代码（AAPL / MSFT / AMZN / NVDA / TSLA 等）

---

## 4. 非功能需求

### 4.1 性能

| ID | 需求描述 | 目标值 |
|----|---------|--------|
| NFR-001 | REST API 响应时间（P99） | ≤ 500ms |
| NFR-002 | WebSocket 推送延迟 | ≤ 1s |
| NFR-003 | 支持并发用户数 | ≥ 100 |

### 4.2 安全

| ID | 需求描述 |
|----|---------|
| NFR-010 | 所有密钥、密码通过环境变量注入，不得硬编码 |
| NFR-011 | JWT 签名密钥长度不低于 256 位 |
| NFR-012 | MySQL、Redis 连接不在日志中输出明文密码 |
| NFR-013 | 生产环境 MySQL 和 Redis 连接启用 SSL/TLS |
| NFR-014 | API 输入参数须进行校验，防止 SQL 注入和 XSS |
| NFR-015 | 遵循 OWASP Top 10 安全规范 |

### 4.3 可维护性

| ID | 需求描述 |
|----|---------|
| NFR-020 | 数据库表结构通过 Flyway 版本管理，禁止手动修改 |
| NFR-021 | 单元测试覆盖率（核心 Service 层）≥ 80% |
| NFR-022 | 关键操作记录结构化日志（JSON 格式，生产环境） |

### 4.4 可用性

| ID | 需求描述 | 目标值 |
|----|---------|--------|
| NFR-030 | 系统可用性（生产） | ≥ 99.9% |
| NFR-031 | 外部券商 API 不可用时，系统降级返回缓存数据 | — |

---

## 5. 约束条件

| 类别 | 内容 |
|------|------|
| 技术约束 | Java 21 / Spring Boot 3.5.7 / MySQL 8.0 / Redis 7.x |
| 外部依赖 | Schwab（OAuth 2.0）、E\*TRADE（OAuth 1.0a）、Tastyworks（REST Session Token） |
| 数据库 | 开发/测试/生产三库分离，Flyway 统一管理迁移 |
| API 风格 | RESTful，JSON 格式，HTTP 状态码语义化 |
| 文档 | API 文档随代码生成（SpringDoc + Swagger UI） |
| 安全约束 | 敏感信息（DB 密码、API Key、Token）不得写入 `.env` 文件或 Git 仓库 |
| 部署环境 | 目标生产环境为 Ubuntu 服务器，使用 Docker Compose 或 systemd 部署 |
| 配置管理 | Spring Cloud Config Server 统一管理多环境配置，支持动态刷新 |

---

## 6. 数据模型概述（详见 database-design.md）

| 表名 | 描述 |
|------|------|
| `users` | 平台用户：账号、密码（BCrypt）、角色 |
| `positions` | 持仓信息：账户、证券代码、数量、成本等（券商同步快照） |
| `orders` | 订单信息：账户、下单方向、状态、时间等 |
| `trades` | 成交信息：订单关联、成交价、数量、时间等 |
| `t_daily_pnl_stock` | 每日个股盈亏快照（Phase 9 新增） |
| `t_daily_pnl_account` | 每日账户总盈亏快照（Phase 9 新增） |
| `t_position_history` | 隔夜持仓滚存快照（Phase 9 新增） |
| `t_stock` | 股票基础数据 / 白名单（Phase 9 新增） |

---

## 7. API 端点概览（详见 api-spec.md）

| 端点 | 方法 | 描述 | 认证 |
|------|------|------|------|
| `/api/auth/login` | POST | 用户登录，返回 JWT | 不需要 |
| `/api/auth/refresh` | POST | 刷新 Access Token | 不需要 |
| `/api/auth/logout` | POST | 登出（Token 加入黑名单） | 需要 |
| `/api/v1/portfolio/positions` | GET | 查询当前用户持仓（券商快照） | 需要 |
| `/api/v1/portfolio/asset-view` | GET | 资产视图（聚合计算，NEW） | 需要 |
| `/api/v1/orders` | GET | 查询当前用户订单 | 需要 |
| `/api/v1/trades` | GET | 查询当前用户成交 | 需要 |
| `/api/v1/trade/order` | POST | 下单（NEW） | 需要 |
| `/api/v1/trade/order/{id}` | DELETE | 撤单（NEW） | 需要 |
| `/api/v1/trade/order/{id}/status` | GET | 查询委托状态（NEW） | 需要 |
| `/ws` | WS | WebSocket 连接端点（STOMP） | 需要 |
| `/topic/positions` | WS Topic | 持仓实时推送（券商快照） | 需要 |
| `/topic/portfolio` | WS Topic | 资产视图实时推送（NEW） | 需要 |
| `/topic/market/{symbol}` | WS Topic | 单只股票行情推送（NEW） | 需要 |
| `/swagger-ui.html` | GET | Swagger UI 文档 | 不需要 |

---

## 8. 实现阶段概览

| 阶段 | 主要工作 | 状态 |
|------|---------|------|
| Phase 0 | 项目初始化、包结构、`application.yml` | ✅ 已完成 |
| Phase 1 | Spring Boot 依赖配置、主类 | ✅ 已完成 |
| Phase 2 | Flyway 数据库迁移（4 张核心表） | ✅ 已完成 |
| Phase 3 | JWT 用户认证（登录 / 刷新 / 黑名单） | ✅ 已完成 |
| Phase 4 | 持仓 REST API + WebSocket 实时推送 | ✅ 已完成 |
| Phase 5 | `BrokerClient` 统一接口 + Redis 缓存 + 异步 | ✅ 已完成 |
| Phase 6 | 嘉信 Schwab API 实现（OAuth 2.0） | ✅ 已完成 |
| Phase 7 | Spring Cloud Config + Docker Secret + 动态刷新 | ✅ 已完成 |
| Phase 8 | E\*TRADE（OAuth 1.0a）+ Tastyworks（Session Token） | ✅ 已完成 |
| Phase 9 | 资产视图聚合引擎 + 行情调度器 + 下单/撤单接口 + 快照 DB | ✅ 已完成 |

---

## 9. 修订历史

| 版本 | 日期 | 内容 | 作者 |
|------|------|------|------|
| 1.0.0 | 2026-03-28 | 初版（Phase 0 创建） | — |
| 1.1.0 | 2026-03-28 | 追加 Phase 7–8 需求（环境变量管理、E\*TRADE、Tastyworks） | — |
| 1.2.0 | 2026-03-28 | 追加 Phase 9 需求（FR-060～FR-111）：资产视图聚合、行情调度、WS 订阅管理、下单/撤单、每日快照、t_stock | — |
