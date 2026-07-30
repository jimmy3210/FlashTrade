# 04-UI功能与 Broker API 对照表

## 1. 设计说明

这份文档用于确认 UI 功能是否能够由 Schwab、Tastytrade、E*TRADE 或 Tastyworks 的 API 支持，并明确由 QuickTrade 内部服务层负责实现的部分。它不是“选择哪一家 Broker”，而是说明“每一个 UI 功能对应到哪一类数据源”。

## 2. 核心原则

- UI 只展示 QuickTrade 统一抽象后的能力
- Broker 差异由 Adapter 层处理
- 需要本地维护的数据由 QuickTrade 自己提供
- 仅在 broker 能提供真实数据时，UI 才展示对应功能

## 3. UI 功能与 Broker API 对照表

| UI 模块 | UI 功能 | Schwab API | Tastytrade API | E*TRADE API | Tastyworks API | QuickTrade 实现 |
| --- | --- | --- | --- | --- | --- | --- |
| Header | 搜索股票 | ✅ | ✅ | ✅ | ✅ | BrokerInstrumentService / BrokerQuoteService |
| Header | Market Status | ❌ | ❌ | ❌ | ❌ | MarketCalendarService |
| Header | 当前用户 | ✅ | ✅ | ✅ | ✅ | BrokerSessionService |
| Header | 当前账户 | ✅ | ✅ | ✅ | ✅ | BrokerAccountService |
| Header | Cash | ✅ | ✅ | ✅ | ✅ | BrokerAccountService |
| Header | Buying Power | ⚠️ | ✅ | ⚠️ | ⚠️ | BrokerAccountService |
| Watch List | 自选股列表 | ❌ | ✅ | ❌ | ❌ | WatchListService / BrokerWatchlistService |
| Watch List | 最新价 / 涨跌 / 涨跌幅 | ✅ | ✅ | ✅ | ✅ | BrokerQuoteService |
| Quote | Symbol / Company / Exchange | ✅ | ✅ | ✅ | ✅ | BrokerInstrumentService |
| Quote | Last / Bid / Ask / Open / High / Low / Prev Close / Volume | ✅ | ✅ | ✅ | ✅ | BrokerQuoteService |
| Order Entry | 买入 / 卖出 | ✅ | ✅ | ✅ | ✅ | BrokerOrderService |
| Order Entry | 数量 / 价格 / 类型 / 时效 | ✅ | ✅ | ✅ | ✅ | BrokerOrderService |
| Order Entry | 预览 / 提交 | ✅ | ✅ | ✅ | ✅ | BrokerOrderService |
| Positions | 当前持仓 | ✅ | ✅ | ✅ | ✅ | BrokerPositionService |
| Orders | 未成交 / 已成交 / 已撤销 / 拒绝订单 | ✅ | ✅ | ✅ | ✅ | BrokerOrderService |
| Account Summary | Cash / Buying Power / Net Liquidation | ✅ / ⚠️ | ✅ | ✅ / ⚠️ | ✅ / ⚠️ | BrokerAccountService |

| UI 功能 | Schwab | Tastytrade | 原因 |
| --- | --- | --- | --- |
| K 线图 | ❌ | ❌ | 无历史 K 线接口 |
| 分时图 | ❌ | ❌ | 无历史行情支撑 |
| Time & Sales | ❌ | ❌ | 无逐笔成交能力 |
| Level 2 / Market Depth | ❌ | ❌ | 无盘口深度接口 |
| News | ❌ | ❌ | 无新闻接口 |
| Alert | ❌ | ❌ | 需自行实现规则引擎 |
| Scanner | ❌ | ❌ | 需要自行扫描行情 |
| 技术指标 | ❌ | ❌ | 需历史行情计算 |

## 5. 说明

对于 QuickTrade 来说，V1 的重点不是“同时接入两个 Broker”，而是“根据用户登录的账户动态切换对应 Broker Adapter”。因此，UI 不应该直接依赖 Broker 的字段或接口，而应该依赖 QuickTrade 的统一接口层。

如果外部券商 API 提供 Watchlist 功能，则优先使用该券商的 Watchlist 接口；如果券商不支持 Watchlist，再由 QuickTrade 本地 `WatchListService` 提供自选股存储和管理。
> 当前已完成对 Tastytrade 自选股列表的后端适配实现，并补充了 `flash-trade-mock` 中 Tastytrade Watchlist API 的 mock 支持。