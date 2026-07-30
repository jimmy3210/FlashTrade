# Phase 9 完成文件一览

**日期**：2026-03-28  
**说明**：基于 `docs/事前讨论/事前讨论.md` 中的设计讨论，实装所有讨论过但尚未实现的核心功能。

---

## 竞合分析结论

| TODO 项 | 与现有代码关系 | 处理方式 |
|---------|---------------|---------|
| PositionAggregator（资产视图） | 与 `PositionResponse`（券商快照）概念不同 | **共存**：新增 `/asset-view` 端点，旧 `/positions` 不变 |
| MarketSchedulerService（行情调度） | 与 `BrokerSyncService`（数据同步）职责不同 | **共存**：不同调度目标，互不干扰 |
| SubscriptionManager（WS 订阅） | 现有 STOMP `/topic/positions` 继续保留 | **扩展**：新增 `/topic/portfolio` 和 `/topic/market/{symbol}` |
| BrokerClient 取引方法 | 现有接口只有 get 方法 | **追加**：新增 default 方法，向后兼容 |
| 每日快照表 | 数据库中无此表 | **新增**：V6 Migration 创建 3 张表 |
| t_stock 表 | 数据库中无此表 | **新增**：V7 Migration 创建 + 预填充数据 |

---

## 新增文件

### DTO 层

| 文件 | 说明 |
|------|------|
| `src/main/java/com/tradex/flashtrade/dto/portfolio/PositionView.java` | 资产视图 DTO（16 个字段，完整聚合结果） |
| `src/main/java/com/tradex/flashtrade/broker/dto/MarketData.java` | 行情快照 DTO（symbol / lastPrice / bid / ask / timestamp） |
| `src/main/java/com/tradex/flashtrade/broker/dto/OrderRequest.java` | 下单请求 DTO（含 @Valid 校验、clientOrderId 幂等字段） |
| `src/main/java/com/tradex/flashtrade/broker/dto/PlaceOrderResult.java` | 下单 / 撤单结果 DTO |

### Service 层

| 文件 | 说明 |
|------|------|
| `src/main/java/com/tradex/flashtrade/service/MarketDataService.java` | Redis 行情缓存服务（Key: `market:{symbol}`，TTL 5s） |
| `src/main/java/com/tradex/flashtrade/service/PositionAggregatorService.java` | **核心**：资产视图聚合引擎（trades + orders + 行情 → PositionView） |
| `src/main/java/com/tradex/flashtrade/service/MarketSchedulerService.java` | 行情调度器（账户池 + 30 symbol/账户限制 + 1 秒轮询） |
| `src/main/java/com/tradex/flashtrade/service/SubscriptionManager.java` | WebSocket 行情订阅管理（自动分配/释放调度槽） |
| `src/main/java/com/tradex/flashtrade/service/TradeExecutionService.java` | 交易执行服务（下单/撤单 + 幂等控制 + DB 持久化） |
| `src/main/java/com/tradex/flashtrade/service/DailySnapshotService.java` | 每日盘后快照服务（写入 t_daily_pnl_stock / _account / t_position_history） |

### Controller 层

| 文件 | 说明 |
|------|------|
| `src/main/java/com/tradex/flashtrade/controller/TradeController.java` | 下单（POST）/ 撤单（DELETE）/ 查询委托状态（GET） |

### 数据库迁移

| 文件 | 说明 |
|------|------|
| `src/main/resources/db/migration/V6__create_daily_pnl_tables.sql` | 创建 `t_daily_pnl_stock`、`t_daily_pnl_account`、`t_position_history` |
| `src/main/resources/db/migration/V7__create_stock_table.sql` | 创建 `t_stock`（白名单）+ 预填充 10 只常用股票 |

---

## 修改文件

### 接口层

| 文件 | 修改内容 |
|------|---------|
| `broker/BrokerClient.java` | 新增 `getBatchMarketData()`、`placeOrder()`、`cancelOrder()`、`getOrderStatus()` 四个 default 方法 |

### Repository 层

| 文件 | 修改内容 |
|------|---------|
| `repository/OrderRepository.java` | 新增 `findByUserIdAndAccountIdAndStatusIn()`、`findByExternalOrderId()` |
| `repository/TradeRepository.java` | 新增 `findByUserIdAndAccountIdOrderByTradedAtAsc()`、修正 `findByAccountIdAndTradedAtBetween` 方法签名 |

### Service 层

| 文件 | 修改内容 |
|------|---------|
| `service/PositionPushService.java` | 注入 `PositionAggregatorService`；`doPush()` 同时推送 `/topic/portfolio` PositionView 数组 |

### Controller 层

| 文件 | 修改内容 |
|------|---------|
| `controller/PortfolioController.java` | 新增 `GET /api/v1/portfolio/asset-view` 端点，注入 `PositionAggregatorService` |

### 券商客户端（均追加 Mock 实现）

| 文件 | 修改内容 |
|------|---------|
| `broker/schwab/SchwabBrokerClient.java` | 实现 `getBatchMarketData()`、`placeOrder()`、`cancelOrder()`、`getOrderStatus()` |
| `broker/etrade/EtradeBrokerClient.java` | 同上 |
| `broker/tastyworks/TastyworksBrokerClient.java` | 同上 |

### 文档

| 文件 | 修改内容 |
|------|---------|
| `docs/开发流程/requirements.md` | 版本升至 1.2.0；新增第 3.6～3.11 节需求（FR-060～FR-111）；更新数据模型概述、API 端点概览、阶段概览 |
| `docs/未实装功能-TODO.md` | 本次实装了 TODO-01/02/03/04/05/06（P1 全部 + P2 全部） |

---

## 新 API 端点总结

| HTTP | 路径 | 描述 |
|------|------|------|
| `GET` | `/api/v1/portfolio/asset-view` | 资产视图（聚合计算，可按 accountId 过滤） |
| `POST` | `/api/v1/trade/order?accountId=` | 下单（MARKET / LIMIT） |
| `DELETE` | `/api/v1/trade/order/{externalOrderId}?accountId=` | 撤单 |
| `GET` | `/api/v1/trade/order/{externalOrderId}/status?accountId=` | 查询委托实时状态 |

## 新 WebSocket Topic 总结

| Topic | 推送内容 | 触发时机 |
|-------|---------|---------|
| `/topic/portfolio` | `List<PositionView>`（全账户聚合）| BrokerSyncService 同步完成后 |
| `/topic/market/{symbol}` | `MarketData`（订阅行情后由 MarketScheduler 推送） | 行情调度器 1 秒轮询 |

---

## 架构分层示意（Phase 9 追加）

```
REST Client / WebSocket Client
        ↓
TradeController          PortfolioController (+ /asset-view)
        ↓                        ↓
TradeExecutionService    PositionAggregatorService
        ↓                        ↓
BrokerClient             TradeRepository + OrderRepository
  placeOrder()           MarketDataService (Redis: market:{symbol})
  cancelOrder()
        ↑
MarketSchedulerService (1s 定时 + 账户池分配)
        ↑
SubscriptionManager (STOMP /topic/market/{symbol} 自动分配/释放)
        ↑
DailySnapshotService (17:00 EST, t_daily_pnl_stock / _account / t_position_history)
```

---

## 未实装项（TODO-07 ~ TODO-11）

以下功能因范围较大，留待后续 Phase 实装：

| TODO | 描述 | 优先级 |
|------|------|-------|
| TODO-07 | 后台管理系统（RBAC 多层级 + 图表分析） | P3 |
| TODO-08 | 风控模块（关停账户、强制清仓、规则配置） | P3 |
| TODO-09 | `t_broker_api` / `t_broker_api_limit` 数据库化 API 注册表 | P3 |
| TODO-10 | Spring Cloud Config Server 完整接入 | P4 |
| TODO-11 | 行情调度优先级 + 容量超限降级策略 | P4 |
