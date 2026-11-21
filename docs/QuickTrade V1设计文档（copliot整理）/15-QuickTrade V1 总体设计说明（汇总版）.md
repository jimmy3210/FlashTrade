# QuickTrade V1 总体设计说明（汇总版）

## 1. 项目背景

QuickTrade 是一个面向美股交易场景的交易系统，提供自有 UI 和统一 API。UI 的交易与查询通过这套 API 调用同一套后台服务；其他获授权的 API 消费方也共用该 API 和后台，不另设 UI 专属后台。后台通过统一服务层和 Broker Adapter 封装不同券商及其他交易 API 提供方的差异。当前产品范围仅限美股交易，具体提供方支持情况以已完成并验证的 API 接入为准。

## 2. 设计目标

QuickTrade V1 的目标包括：

- 以实时行情和交易操作为核心
- 通过自有 UI 和统一 API 提供美股交易与查询能力，并由同一后台服务处理
- 保持界面简洁，减少不必要的操作步骤
- 优先保证可用性和稳定性，而非追求复杂分析能力
- 通过统一服务层与 Adapter 设计，实现与不同券商及交易 API 提供方的解耦，UI 与 API 消费方共用该服务层
- 为后续扩展打下可维护的基础

## 3. V1 范围

V1 重点覆盖以下能力：

- 登录 Broker 账户
- 股票搜索
- 实时报价
- 买入 / 卖出
- 撤单 / 改单
- 当前持仓查看
- 未成交与已成交订单查看
- 现金、购买力与资产摘要查看
- 自选股管理

## 4. V1 不做内容

以下内容不纳入 V1：

- K 线图
- 分时图
- Time & Sales
- Level 2 / Market Depth
- 新闻资讯
- Scanner
- Alerts
- 复杂条件单
- 期权链
- 高级技术指标分析

## 5. UI 设计说明

QuickTrade V1 的界面建议采用三栏主布局，并在底部提供持仓、订单与账户摘要区域。

```text
┌──────────────────────────────────────────────────────────────┐
│ Header                                                       │
├──────────────┬──────────────────────┬────────────────────────┤
│ Watch List   │ Quote / Chart        │ Order Entry            │
├──────────────┼──────────────────────┼────────────────────────┤
│ Positions    │ Orders               │ Account Summary        │
└──────────────┴──────────────────────┴────────────────────────┘
```

### 5.1 页面区域

- Header：显示 Logo、搜索、市场状态、用户及账户摘要
- Watch List：显示自选股及关键行情
- Quote：显示选中股票的实时行情与基础信息
- Order Entry：显示买入 / 卖出操作面板
- Positions / Orders / Account Summary：展示底部状态信息

### 5.2 视觉规范

- 深色主题
- 重点信息突出
- 买入按钮使用绿色，卖出按钮使用红色
- 刷新频率建议：行情 1 秒，订单与持仓 5 秒，账户 10 秒

## 6. 功能清单

| 编号 | 模块 | 功能 | 是否 V1 |
| --- | --- | --- | --- |
| QT-001 | Login | 登录 Broker | 是 |
| QT-002 | Quote | 股票搜索 | 是 |
| QT-003 | Quote | 实时报价 | 是 |
| QT-004 | Quote | 基础行情字段 | 是 |
| QT-005 | WatchList | 自选股管理 | 是 |
| QT-006 | Order | 买入 | 是 |
| QT-007 | Order | 卖出 | 是 |
| QT-008 | Order | 撤单 | 是 |
| QT-009 | Order | 改单 | 是 |
| QT-010 | Position | 当前持仓 | 是 |
| QT-011 | Position | 浮盈亏 | 是 |
| QT-012 | Orders | 未成交订单 | 是 |
| QT-013 | Orders | 已成交订单 | 是 |
| QT-014 | Account | 现金余额 | 是 |
| QT-015 | Account | 购买力 | 是 |
| QT-016 | Account | 资产摘要 | 是 |
| QT-017 | User | 登出 | 是 |

## 7. UI 功能与 Broker API 对照

QuickTrade 的核心思路不是“同时对接两家 Broker”，而是根据用户登录的账户动态切换 Broker Adapter。

| UI 模块 | UI 功能 | Schwab API | Tastytrade API | QuickTrade 实现 |
| --- | --- | --- | --- | --- |
| Header | 搜索股票 | ✅ | ✅ | BrokerInstrumentService |
| Header | 当前账户 | ✅ | ✅ | BrokerAccountService |
| Watch List | 自选股列表 | ❌ | ✅ | WatchListService / BrokerWatchlistService |
| Quote | 行情信息 | ✅ | ✅ | BrokerQuoteService |
| Order Entry | 买入 / 卖出 | ✅ | ✅ | BrokerOrderService |

> 如果外部券商 API 提供 Watchlist 接口，则优先使用该券商的 Watchlist 服务；无论是否使用券商 Watchlist，本地都应同步保存 `broker_id` 和 `symbol`，以保持统一数据模型与本地缓存一致性。
> 
> 当前已实现 Tastytrade Watchlist 后端适配，并补充了 mock 服务支持。
| Positions | 当前持仓 | ✅ | ✅ | BrokerPositionService |
| Orders | 订单状态 | ✅ | ✅ | BrokerOrderService |
| Account Summary | 账户摘要 | ✅ / ⚠️ | ✅ | BrokerAccountService |

## 8. 统一数据模型与字段映射

建议使用统一的数据模型，避免 UI 和业务层直接依赖 Broker 字段。

### 8.1 核心对象

| 对象 | 核心字段 |
| --- | --- |
| Quote | symbol、lastPrice、bidPrice、askPrice、open、high、low、close、volume |
| Position | symbol、quantity、averagePrice、marketValue、unrealizedPL |
| Order | orderId、symbol、side、quantity、price、status、orderType |
| Account | accountNumber、cashBalance、buyingPower、netLiquidation |
| WatchList | watchListId、userId、symbol、sortOrder |
| Session | brokerType、accountId、token、expiresAt |

### 8.2 字段映射示例

| UI 字段 | QuickTrade 对象 | Schwab API | Tastytrade API |
| --- | --- | --- | --- |
| Last Price | Quote.lastPrice | quote.lastPrice | quote.mark |
| Bid | Quote.bidPrice | bidPrice | bid |
| Ask | Quote.askPrice | askPrice | ask |
| Volume | Quote.volume | totalVolume | volume |
| Cash | Account.cashBalance | currentBalances.cashBalance | cashBalance |
| Buying Power | Account.buyingPower | currentBalances.buyingPower | buyingPower |

## 9. 接口设计说明

建议所有接口都采用统一返回结构：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 9.1 关键接口

- POST /api/auth/login：登录 Broker
- GET /api/quotes/{symbol}：获取行情
- POST /api/orders：提交订单
- GET /api/positions：获取持仓
- GET /api/account：获取账户摘要
- GET /api/watchlist：获取自选股列表

## 10. 系统架构

建议采用分层架构：

```text
QuickTrade UI ─────┐
                   ├──→ QuickTrade API（认证 / 授权 / 限流）
Other API Clients ─┤                  ↓
MCP Host → MCP Server ┘
                              Service Layer
                                     ↓
                              Broker Adapter
                                     ↓
                         Schwab / Tastytrade APIs
```

API 访问应认证并按用户、客户端、账户和接口成本实施授权、频率限制、周期配额及并发控制。额度应依据券商限制、系统容量和运行监控配置；超限返回 `429`，适用时提供 `Retry-After`。交易请求应支持幂等键，并记录审计信息。UI 与其他获授权的 API 消费方共用这些访问控制和同一套后台业务服务。

MCP 是面向 AI 客户端的可选工具接入层，通过 QuickTrade API 复用行情、账户、持仓和订单能力，不直连 Broker 或绕过后台控制。建议先开放只读工具；下单、撤单和改单需在独立交易授权、受信任的完整参数确认、幂等处理和审计通过后再启用。详见 [MCP 接入设计](18-MCP接入设计.md)。

## 11. 数据库设计草案

V1 的数据库主要用于保存：

- 用户信息
- Broker 账户绑定
- 自选股列表
- 用户偏好设置
- 会话缓存

建议的核心表包括：

- users
- broker_accounts
- watchlists
- user_settings
- session_tokens

## 12. 开发任务与里程碑

### 12.1 推荐阶段

1. 需求与设计确认
2. 后端基础能力开发
3. 前端页面开发
4. 联调与测试
5. V1 验收与交付

### 12.2 关键风险

- Broker API 差异过大
- 实时行情接口不稳定
- 订单流程复杂
- 前后端接口定义不一致

## 13. V1 验收标准

V1 可视为完成的标准包括：

- 用户可登录 Broker 账户
- 用户可查看实时行情
- 用户可完成买入 / 卖出
- 用户可查看持仓与账户摘要
- 前后端联调通过
- 文档与实现基本一致

## 14. 总结

QuickTrade V1 的重点不是“做很多功能”，而是建立一个稳定、可扩展、可接入多 Broker 的交易客户端基础框架。只要把行情、下单、持仓和账户这几个核心闭环打通，V1 就具备真正的交付价值。
