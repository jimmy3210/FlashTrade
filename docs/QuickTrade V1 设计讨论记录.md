





领取优惠
简易交易软件的UI布局

如果你的目标是开发简易交易软件（类似 IBKR、Schwab、富途、老虎证券的精简版），我建议遵循一个原则：

80%的屏幕显示行情，20%显示交易功能。

交易员最关心的是价格变化，不是按钮。

参考目前主流交易软件的设计趋势，大多数都采用左侧导航 + 中间行情 + 右侧交易的三栏布局，以减少操作步骤并突出实时数据。

推荐布局（PC版）
┌────────────────────────────────────────────────────────────────────┐
│ Logo     搜索股票         美股 开盘中        用户                  │
├──────────────┬───────────────────────────────┬─────────────────────┤
│              │                               │                     │
│ Watch List   │        K线图                  │     买卖面板        │
│--------------│                               │                     │
│ AAPL         │                               │  最新价             │
│ TSLA         │                               │                     │
│ NVDA         │                               │  买入价格           │
│ AMD          │                               │                     │
│ META         │                               │  数量               │
│              │                               │                     │
│              │                               │ [ 买入 ]            │
│              │                               │ [ 卖出 ]            │
├──────────────┼───────────────────────────────┼─────────────────────┤
│              │                               │                     │
│              │ 成交明细                      │ 当前持仓            │
│              │                               │                     │
└──────────────┴───────────────────────────────┴─────────────────────┘
左侧（20%）
只放最重要的信息。

Watch List

★ AAPL      212.35  +1.20%
★ NVDA      178.32  -0.82%
★ TSLA      305.15  +3.65%
★ AMD       167.88  +0.55%

-----------------------

Position

AAPL
TSLA
功能：

股票搜索

自选股

我的持仓

不用做太复杂。

中间（60%）
这是整个软件最重要的位置。

建议：

AAPL

212.35
+2.35 (+1.12%)

----------------------------------

TradingView Chart

----------------------------------

Volume

MACD

RSI
下面放：

Time & Sales

09:30:01 212.32
09:30:03 212.35
09:30:05 212.37
或者：

Order Book

Bid

212.34

212.33

Ask

212.36

212.37
右侧（20%）
交易区一定保持最简单。

Buy

Price

[212.35        ]

Quantity

[100           ]

Order Type

Market ▼

Duration

Day ▼

----------------

Estimated

$21,235

----------------

[ BUY ]
下面：

Sell

Price

Quantity

[ SELL ]
底部
订单和持仓。

Orders

Filled

Working

Canceled

--------------------------------

Position

AAPL

100

212.15

+25
顶部工具栏
Logo

Search

Market Status

Settings

Logout
例如：

QuickTrade

[ Search ]

US Market Open

09:45:21 ET

Jimmy
配色建议
建议采用深色主题（Dark Mode），长时间盯盘更舒适，也是当前交易软件的主流风格。

背景：

#1E1E1E
文字：

#FFFFFF
上涨：

#00C853
下跌：

#FF5252
按钮：

BUY

绿色

SELL

红色
字体
建议

微软雅黑

或者

Inter
数字：

JetBrains Mono
数字更整齐。

你的项目（QuickTrade）建议
结合你正在开发的 Java + Spring Boot + Vue3 + Tastytrade API 项目，我建议首页保持极简，只保留最常用的五个区域：

┌────────────────────────────────────────────────────────────┐
│ Logo   Search   Market Status          User               │
├─────────────┬──────────────────────┬──────────────────────┤
│             │                      │                      │
│ WatchList   │   Chart              │ Buy / Sell          │
│             │                      │                      │
│             │                      │                      │
├─────────────┼──────────────────────┼──────────────────────┤
│             │                      │                      │
│ Positions   │ Orders               │ Account Summary     │
└─────────────┴──────────────────────┴──────────────────────┘
后续可逐步增加的功能
股票筛选器（Scanner，类似 Trade Ideas）

新闻资讯（News）

自定义预警（Alert）

条件单（Stop、Limit、Trailing Stop）

多账户切换

一键平仓

热门股票排行榜

这套布局既适合日内交易，也便于后续扩展，不会因为功能增加而破坏整体结构。


已生成图片：股票交易平台界面截图


编辑


已生成图片：股票交易仪表盘界面截图


编辑


这是我建议作为 QuickTrade V1 的功能清单，只保留 Schwab API / Tastytrade API 能直接提供的数据，不包含 K 线、Level2、Time&Sales、新闻等需要第三方数据源的功能。

UI模块	功能	Schwab API	Tastytrade API	V1采用	备注
顶部	登录用户	✅ Session	✅ Session	Tastytrade	当前登录账户
顶部	账户号(Account Number)	✅	✅	Tastytrade	脱敏显示
顶部	Cash	✅ Account Balance	✅ Account Balance	Tastytrade	可用现金
顶部	Buying Power	⚠️ 部分账户	✅	Tastytrade	推荐使用
顶部	Market Status	❌	❌	本地计算	根据纽约时间判断
顶部	搜索股票(Symbol Search)	✅ Instruments	✅ Symbol Search	两者均可	股票搜索
左侧 WatchList	自选股列表	❌	❌	数据库	用户维护
左侧 WatchList	最新价(Last)	✅ Quote	✅ Quote	两者均可	实时报价
左侧 WatchList	涨跌额(Change)	✅ Quote	✅ Quote	两者均可	
左侧 WatchList	涨跌幅(Change %)	✅ Quote	✅ Quote	两者均可	
中间 Quote	Symbol	✅	✅	两者均可	
中间 Quote	Company Name	✅ Instruments	✅ Instruments	两者均可	
中间 Quote	Last Price	✅ Quote	✅ Quote	两者均可	
中间 Quote	Bid	✅ Quote	✅ Quote	两者均可	
中间 Quote	Ask	✅ Quote	✅ Quote	两者均可	
中间 Quote	Bid Size	✅ Quote	✅ Quote	两者均可	
中间 Quote	Ask Size	✅ Quote	✅ Quote	两者均可	
中间 Quote	Open	✅ Quote	✅ Quote	两者均可	
中间 Quote	High	✅ Quote	✅ Quote	两者均可	
中间 Quote	Low	✅ Quote	✅ Quote	两者均可	
中间 Quote	Previous Close	✅ Quote	✅ Quote	两者均可	
中间 Quote	Volume	✅ Quote	✅ Quote	两者均可	
中间 Quote	52 Week High	✅ Quote	✅ Quote	两者均可	
中间 Quote	52 Week Low	✅ Quote	✅ Quote	两者均可	
右侧 Order Entry	Buy	✅ Order API	✅ Order API	两者均可	
右侧 Order Entry	Sell	✅ Order API	✅ Order API	两者均可	
右侧 Order Entry	Quantity	✅	✅	两者均可	
右侧 Order Entry	Price	✅	✅	两者均可	
右侧 Order Entry	Order Type	✅	✅	两者均可	Market / Limit / Stop
右侧 Order Entry	Time In Force	✅	✅	两者均可	DAY、GTC 等
右侧 Order Entry	Preview Order	✅	✅	两者均可	下单前确认
右侧 Order Entry	Submit Order	✅	✅	两者均可	
下方 Positions	当前持仓	✅ Positions	✅ Positions	两者均可	
下方 Positions	Qty	✅	✅	两者均可	
下方 Positions	Avg Price	✅	✅	两者均可	
下方 Positions	Market Value	✅	✅	两者均可	
下方 Positions	Unrealized P/L	✅	✅	两者均可	浮盈亏
下方 Positions	Day P/L	⚠️	✅	Tastytrade	日盈亏
下方 Orders	Working Orders	✅	✅	两者均可	
下方 Orders	Filled Orders	✅	✅	两者均可	
下方 Orders	Cancelled Orders	✅	✅	两者均可	
下方 Orders	Cancel Order	✅	✅	两者均可	
下方 Orders	Replace Order	✅	✅	两者均可	修改订单
Account Summary	Net Liquidation	⚠️	✅	Tastytrade	推荐
Account Summary	Account Value	✅	✅	两者均可	
Account Summary	Cash Balance	✅	✅	两者均可	
Account Summary	Buying Power	⚠️	✅	Tastytrade	
Account Summary	Margin Requirement	⚠️	✅	Tastytrade	保证金账户显示
V1 不开发（API 不直接支持）
功能	原因
❌ K线图	需要历史行情数据和图表组件
❌ 分时图	需要历史行情接口
❌ Time & Sales（逐笔成交）	Schwab/Tastytrade 不提供
❌ Level 2 / Market Depth	不提供五档盘口
❌ Heat Map	不提供
❌ Scanner	需要自行扫描全市场行情
❌ News	需第三方新闻 API
❌ Alerts	需自行实现规则引擎和数据库
❌ 技术指标（MACD、RSI 等）	需要历史 K 线数据计算
推荐的 API 分工
为了降低开发复杂度，我建议 QuickTrade V1 明确职责：

功能类别	数据来源
实时行情（Quote）	Schwab Quote API（更新快、字段完整）
股票基础信息	Schwab Instruments API
登录认证	Tastytrade Session API
账户信息	Tastytrade Account API
持仓	Tastytrade Positions API
下单/撤单/改单	Tastytrade Orders API
自选股	QuickTrade 数据库（MySQL）
用户配置	QuickTrade 数据库（MySQL）
市场状态	QuickTrade 本地根据美东时间计算
后续 V2 扩展
等 V1 稳定后，再逐步增加：

历史 K 线（接入历史行情接口）

股票筛选器（Scanner）

自定义价格提醒（Alert）

期权链（Options Chain）

多账户管理

新闻与公告

Level 2（如果接入专业行情数据）

这种路线能够确保 第一版所有界面都有真实数据支撑，不会出现占位功能或空白模块，同时也便于后续扩展。


表格已经很好了，不过纠正一个地方V1采用，不是2选一，而是根据使用账号不同 连接不同的API。。这个表格就是确认，两家api 是否可以实现UI上的功能

我赞成你的设计思路，这样更符合你的项目架构。

你的 QuickTrade 并不是"同时调用两家 API"，而是：

用户登录哪个券商账号，就加载对应 Broker Adapter（Schwab 或 Tastytrade）。

所以 UI 应该完全独立于 Broker，Broker 只是数据提供者。

也就是说，这张表应该回答的是：

UI 上的每一个功能，Schwab API 能否实现？Tastytrade API 能否实现？

而不是 V1 用哪一家。

QuickTrade UI 功能与 Broker API 对照表（V1）
UI模块	UI功能	Schwab API	Tastytrade API	说明
顶部 Header				
Logo	QuickTrade Logo	—	—	本地资源
Search	股票搜索(Symbol Search)	✅	✅	Symbol / Instrument Search
Market Status	Open / Closed	❌	❌	本地根据美东时间计算
User	当前登录用户	✅	✅	Session
Account	当前交易账户	✅	✅	Account API
Cash	Cash Balance	✅	✅	Account Balance
Buying Power	Buying Power	⚠️	✅	Schwab 部分账户类型支持
Watch List				
Watch List	自选股列表	❌	❌	QuickTrade 数据库维护
Last	最新价	✅	✅	Quote
Change	涨跌额	✅	✅	Quote
Change %	涨跌幅	✅	✅	Quote
Quote Panel				
Symbol	股票代码	✅	✅	Instrument
Company Name	公司名称	✅	✅	Instrument
Exchange	NASDAQ/NYSE	✅	✅	Instrument
Last Price	最新成交价	✅	✅	Quote
Bid	买价	✅	✅	Quote
Ask	卖价	✅	✅	Quote
Bid Size	Bid Size	✅	✅	Quote
Ask Size	Ask Size	✅	✅	Quote
Open	开盘价	✅	✅	Quote
High	最高价	✅	✅	Quote
Low	最低价	✅	✅	Quote
Previous Close	昨收	✅	✅	Quote
Volume	成交量	✅	✅	Quote
52 Week High	年内最高	✅	✅	Quote
52 Week Low	年内最低	✅	✅	Quote
Order Entry				
Buy	买入	✅	✅	Order API
Sell	卖出	✅	✅	Order API
Quantity	数量	✅	✅	Order API
Price	价格	✅	✅	Order API
Order Type	Market / Limit / Stop	✅	✅	Order API
Time In Force	DAY / GTC	✅	✅	Order API
Preview Order	预览订单	✅	✅	Preview API
Submit Order	提交订单	✅	✅	Place Order
Cancel Order	撤单	✅	✅	Cancel Order
Replace Order	修改订单	✅	✅	Replace Order
Positions				
Position List	当前持仓	✅	✅	Positions
Quantity	持仓数量	✅	✅	Positions
Average Price	持仓均价	✅	✅	Positions
Market Value	市值	✅	✅	Positions
Unrealized P/L	浮盈亏	✅	✅	Positions
Day P/L	当日盈亏	⚠️	✅	Schwab 字段依账户类型而定
Orders				
Working Orders	未成交订单	✅	✅	Orders
Filled Orders	已成交订单	✅	✅	Orders
Cancelled Orders	已撤销订单	✅	✅	Orders
Rejected Orders	拒绝订单	✅	✅	Orders
Order Detail	订单详情	✅	✅	Orders
Account Summary				
Net Liquidation	总资产	⚠️	✅	Schwab 字段名称不同
Account Value	账户价值	✅	✅	Account
Cash Balance	现金余额	✅	✅	Account
Buying Power	购买力	⚠️	✅	Schwab 依账户类型
Margin Requirement	保证金	⚠️	✅	保证金账户支持
不纳入 V1 UI（两家 API 都无法直接支持）
UI功能	Schwab	Tastytrade	原因
K线图	❌	❌	无历史K线接口（需第三方或自行维护）
分时走势图	❌	❌	同上
Time & Sales（逐笔成交）	❌	❌	不提供逐笔成交流
Level 2 / Market Depth	❌	❌	不提供五档盘口
Scanner	❌	❌	需自行扫描行情
News	❌	❌	无新闻接口
Heat Map	❌	❌	无相关数据
RSI / MACD / KDJ	❌	❌	需要历史K线计算
Alert	❌	❌	应由 QuickTrade 自行实现
我建议再增加一列："QuickTrade 实现方式"
这会直接指导后端开发。例如：

UI功能	Schwab	Tastytrade	QuickTrade实现
Last Price	✅	✅	BrokerQuoteService.getQuote()
Position	✅	✅	BrokerPositionService.listPositions()
Place Order	✅	✅	BrokerOrderService.placeOrder()
Account Summary	✅	✅	BrokerAccountService.getAccount()
WatchList	❌	❌	WatchListService (MySQL)
Market Status	❌	❌	MarketCalendarService
这样你的架构就会非常清晰：

Vue UI
    │
    ▼
QuickTrade Service（统一接口）
    │
    ├── Schwab Adapter
    │
    └── Tastytrade Adapter
以后如果增加 Interactive Brokers、Robinhood 或其他券商，只需要新增一个 Adapter，UI 和业务层完全不需要修改。这也是我认为最适合 QuickTrade 的整体架构。


生成一份UI 设计 说明文档。 另外再生成一份 功能列表。。还有之前这个（这是我建议作为 QuickTrade V1 的功能清单，只保留 Schwab API / Tastytrade API 能直接提供的数据，不包含 K 线、Level2、Time&Sales、新闻等需要第三方数据源的功能。）

我建议整理成 3 份文档，这样后续开发（数据库、Java、Vue、API 对接）都会非常清晰。

①《QuickTrade V1 UI设计说明书》
目的：
给 UI 设计、Vue 前端开发人员使用。

内容建议：

1. 项目目标
软件定位

设计原则

V1 功能范围

不支持功能

2. UI布局
例如：

┌──────────────────────────────────────────────────────────────┐
│ Header                                                       │
├──────────────┬──────────────────────┬────────────────────────┤
│ WatchList    │ Quote                │ Order Entry            │
├──────────────┼──────────────────────┼────────────────────────┤
│ Positions    │ Orders               │ Account Summary        │
└──────────────┴──────────────────────┴────────────────────────┘
每个区域说明：

例如：

Header
显示：

Logo

Search

Market Status

User

Account

Cash

Buying Power

WatchList
显示：

Symbol

Last

Change

Change %

支持：

点击股票以后

↓

Quote 更新

Quote
显示：

AAPL

212.35

Bid

Ask

Open

High

Low

Prev Close

Volume

52 Week High

52 Week Low
Order Entry
显示：

Buy

Sell

Price

Qty

Order Type

Time In Force

Preview

Submit
Positions
显示

Qty

Avg Price

Market Value

Unrealized P/L
Orders
显示

Working

Filled

Cancelled

Rejected
Account Summary
显示

Cash

Buying Power

Net Liquidation

Account Value
UI颜色
例如：

Dark Theme

Background

#1C1F26

BUY

Green

SELL

Red

Text

White
UI刷新频率
例如

Quote

1 秒

Positions

5 秒

Orders

5 秒

Account

10 秒
这就是完整 UI 设计说明。

大约

15~20页

②《QuickTrade 功能列表》
这是产品经理使用。

格式如下：

编号	模块	功能	描述
QT-001	Login	登录	登录 Broker
QT-002	Quote	股票搜索	搜索 Symbol
QT-003	Quote	实时报价	Last/Bid/Ask
QT-004	Order	买入	Place Order
QT-005	Order	卖出	Place Order
QT-006	Order	撤单	Cancel
QT-007	Position	当前持仓	List Position
QT-008	Order	Working Orders	未成交订单
QT-009	Account	Cash	Cash Balance
QT-010	Account	Buying Power	Buying Power
以后

可以一直增加

QT-011

QT-012

QT-013
方便管理。

③《QuickTrade UI功能 × API对照表》
这是开发最重要的。

例如

UI模块	功能	Schwab	Tastytrade	QuickTrade
Quote	Last	✅	✅	BrokerQuoteService
Quote	Bid	✅	✅	BrokerQuoteService
Quote	Ask	✅	✅	BrokerQuoteService
Order	Buy	✅	✅	BrokerOrderService
Order	Sell	✅	✅	BrokerOrderService
Position	Position	✅	✅	BrokerPositionService
Account	Cash	✅	✅	BrokerAccountService
WatchList	WatchList	❌	❌	MySQL
Market Status	Market Open	❌	❌	Local Time
并且增加：

是否V1	默认显示
YES	YES
YES	NO
NO	NO
以后维护非常方便。

我建议再增加一份（第4份）
实际上企业项目都会有。

《QuickTrade API Mapping Specification（接口映射说明）》

例如：

UI字段	QuickTrade Service	Schwab API	Tastytrade API
Last Price	Quote.lastPrice	quote.lastPrice	quote.mark
Bid	Quote.bid	bidPrice	bid
Ask	Quote.ask	askPrice	ask
Volume	Quote.volume	totalVolume	volume
Cash	Account.cash	currentBalances.cashBalance	cashBalance
Buying Power	Account.buyingPower	currentBalances.buyingPower	buyingPower
这一份以后开发 Adapter 时几乎就是照着写，不需要反复查两家 API 文档。

我建议的项目文档结构
docs/
│
├── 01-QuickTrade-UI设计说明书.md
│
├── 02-QuickTrade-功能列表.xlsx
│
├── 03-QuickTrade-UI功能与BrokerAPI对照表.xlsx
│
├── 04-QuickTrade-API字段映射说明.xlsx
│
├── 05-数据库设计.docx
│
├── 06-系统架构设计.docx
│
└── 07-开发规范.md
我建议我们按照企业级项目标准来整理这套文档。 其中 01～04 可以作为 V1 的基础设计文档，后续数据库设计、Java 后端开发、Vue 前端开发都可以直接引用，避免后续反复修改设计。


继续

我建议直接按**软件开发文档（SDD）**的标准来做，这样以后基本不需要重写。

整个 QuickTrade V1 可以整理成下面这一套文档。

QuickTrade V1 文档目录
QuickTrade
│
├── 01.UI设计说明书.md
│
├── 02.功能列表.xlsx
│
├── 03.UI功能与Broker API对照表.xlsx
│
├── 04.API字段映射说明.xlsx
│
├── 05.数据库设计说明书.md
│
├── 06.后端架构设计.md
│
├── 07.前端架构设计.md
│
├── 08.API接口设计.md
│
├── 09.开发规范.md
│
└── 10.部署说明.md
这十份文档基本就是一个完整的软件设计文档。

第一份
QuickTrade V1 UI设计说明书
1.
项目简介
项目名称

QuickTrade
定位

股票交易客户端
支持

Schwab

Tastytrade
采用

Broker Adapter

Architecture
即

Vue

↓

QuickTrade Service

↓

Broker Adapter

↓

Schwab

或

Tastytrade
Broker 对 UI 完全透明。

2.
设计原则

第一条
UI 不依赖 Broker。

UI

↓

QuickTrade Service

↓

Broker
所以

以后增加

IBKR

Firstrade

Robinhood
UI 不需要修改。

第二条

只显示

Broker API

提供的数据。

例如

Last

Bid

Ask

Position
不要显示

K线

News

Level2
因为 API 没有。

第三条

所有页面支持

Dark Theme。

第四条

页面响应时间

Quote

<1 秒

Order

<500ms

Account

<5 秒

3.
页面布局

Header
↓

WatchList

Quote

Order Entry
↓

Positions

Orders

Account Summary
Header

宽度

100%

高度

60px

包含

Logo

Search

Market Status

Current Account

Cash

Buying Power

Logout
WatchList

宽度

20%

内容

Symbol

Last

Change

Change%
支持

点击股票

↓

Quote更新
Quote

宽度

45%

内容

Company

Symbol

Last

Bid

Ask

Open

High

Low

Previous Close

Volume

52Week High

52Week Low
Order Entry

宽度

35%

内容

Buy

Sell

Price

Qty

Order Type

Time In Force

Preview

Submit
Positions

内容

Qty

Avg Price

Market Value

Unrealized P/L
Orders

Working

Filled

Cancelled

Rejected
Account Summary

Cash

Buying Power

Net Liquidation

Account Value
4.
颜色规范

背景

#1A1D23
文字

#FFFFFF
上涨

#00C853
下跌

#F44336
按钮

BUY

绿色

SELL

红色

5.
刷新策略

Quote

WebSocket

或

Polling
建议

1 秒
Orders

5 秒
Positions

5 秒
Account

10 秒
第二份
功能列表

编号统一。

例如

编号	模块	功能	是否V1
QT-001	Login	登录Broker	✔
QT-002	Quote	股票搜索	✔
QT-003	Quote	实时报价	✔
QT-004	Quote	Bid/Ask	✔
QT-005	Order	买入	✔
QT-006	Order	卖出	✔
QT-007	Order	撤单	✔
QT-008	Order	修改订单	✔
QT-009	Position	当前持仓	✔
QT-010	Position	浮盈亏	✔
QT-011	Orders	未成交订单	✔
QT-012	Orders	已成交订单	✔
QT-013	Account	Cash	✔
QT-014	Account	Buying Power	✔
QT-015	Account	Net Liquidation	✔
QT-016	WatchList	自选股管理	✔
QT-017	User	登出	✔
以后

V2

继续增加

QT-100
Scanner

QT-101
News

QT-102
Alert
不会影响编号。

第三份
Broker API 对照

建议增加两个重要字段。

UI	Schwab	Tasty	必须支持	V1
Last	✔	✔	是	✔
Bid	✔	✔	是	✔
Ask	✔	✔	是	✔
Position	✔	✔	是	✔
Buy	✔	✔	是	✔
Sell	✔	✔	是	✔
Cash	✔	✔	是	✔
Buying Power	⚠	✔	否	✔
Day P/L	⚠	✔	否	✔
Market Status	Local	Local	是	✔
WatchList	DB	DB	是	✔
这样一眼就知道

Broker 是否支持。

第四份（这是我最推荐增加的）
API 字段映射

以后 Java 开发基本照着写。

例如

QuickTrade Model	Schwab 字段	Tastytrade 字段
Quote.symbol	symbol	symbol
Quote.lastPrice	lastPrice	mark
Quote.bidPrice	bidPrice	bid
Quote.askPrice	askPrice	ask
Quote.open	openPrice	open
Quote.high	highPrice	high
Quote.low	lowPrice	low
Quote.close	closePrice	close
Quote.volume	totalVolume	volume
Position.quantity	longQuantity	quantity
Position.averagePrice	averagePrice	averageOpenPrice
Account.cash	cashBalance	cashBalance
Account.buyingPower	buyingPower	buyingPower
我的建议：增加一层「QuickTrade 统一数据模型」
目前你已经决定支持 Schwab 和 Tastytrade 两家券商，因此我建议在文档中增加一份 《QuickTrade 统一数据模型（Domain Model）》，这会成为整个系统最核心的设计。

例如：

QuickTrade 对象	字段
Quote	symbol、lastPrice、bidPrice、askPrice、open、high、low、close、volume
Position	symbol、quantity、averagePrice、marketValue、unrealizedPL
Order	orderId、symbol、side、quantity、price、status、orderType
Account	accountNumber、cashBalance、buyingPower、netLiquidation
WatchList	watchListId、userId、symbol、sortOrder
这样无论底层接的是 Schwab 还是 Tastytrade，UI 和业务逻辑都只操作 QuickTrade 的统一对象，Adapter 负责字段转换。这也是大多数支持多券商平台（如聚合交易平台）的典型架构。

我认为这是我们在开始数据库设计和 Java 接口设计之前，最值得先完成的一份文档，因为后续所有模块都会依赖它。


UI布局 ，应该按照我们之前说好的写

对，我也发现了，上一版我把布局写成了偏传统交易软件，没有完全按照我们前面确认的 QuickTrade V1。

我们之前已经达成了几个原则：

不做 K 线

不做 Time & Sales

不做 Level2

不做 News

UI 只显示 Schwab / Tastytrade API 能提供的数据

根据登录的 Broker（Schwab 或 Tastytrade）连接对应 API，UI 完全一致

所以 UI 设计说明书 应该按照这个布局，而不是传统股票软件布局。

QuickTrade V1 UI布局（最终版）
┌──────────────────────────────────────────────────────────────────────────────────────────────┐
│ Header                                                                               │
│ Logo │ Search │ Market Status │ User │ Account │ Cash │ Buying Power │ Logout       │
├──────────────────────┬────────────────────────────────────┬──────────────────────────┤
│                      │                                    │                          │
│                      │                                    │                          │
│     Watch List       │          Quote Information         │      Order Entry         │
│                      │                                    │                          │
│ Symbol               │ Company Name                      │ Buy / Sell               │
│ Last                 │ Symbol                            │ Qty                      │
│ Change               │ Last Price                        │ Price                    │
│ Change %             │ Bid                               │ Order Type               │
│                      │ Ask                               │ Time In Force            │
│                      │ Open                              │ Preview                  │
│                      │ High                              │ Submit                   │
│                      │ Low                               │                          │
│                      │ Prev Close                        │                          │
│                      │ Volume                            │                          │
│                      │ 52W High                          │                          │
│                      │ 52W Low                           │                          │
├──────────────────────┼────────────────────────────────────┼──────────────────────────┤
│                      │                                    │                          │
│ Positions            │ Orders                            │ Account Summary          │
│                      │                                    │                          │
│ Symbol               │ Working                           │ Cash                     │
│ Qty                  │ Filled                            │ Buying Power             │
│ Avg Price            │ Cancelled                         │ Account Value            │
│ Market Value         │ Rejected                          │ Net Liquidation          │
│ Unrealized P/L       │                                   │                          │
└──────────────────────┴────────────────────────────────────┴──────────────────────────┘
每个区域职责
① Header（顶部）
高度固定 60px

包含：

控件	数据来源
Logo	本地资源
Search	Broker API
Market Status	QuickTrade（本地计算）
User	Broker Session
Account	Broker Account
Cash	Broker Account
Buying Power	Broker Account
Logout	本地
② Watch List（左侧）
宽度约 20%

显示：

字段
Symbol
Last
Change
Change %
点击股票后：

WatchList

↓

Quote Panel 更新

↓

Order Entry 默认 Symbol 更新
WatchList 数据保存在 QuickTrade 数据库，行情数据实时来自 Broker API。

③ Quote Information（中间）
宽度约 45%

这里只放 API 能直接提供的报价数据。

字段
Company Name
Symbol
Last Price
Bid
Ask
Open
High
Low
Previous Close
Volume
52 Week High
52 Week Low
明确不包含：

❌ K线

❌ 分时图

❌ RSI

❌ MACD

❌ Bollinger

❌ Time & Sales

❌ Level2

④ Order Entry（右侧）
宽度约 35%

显示：

字段
Buy
Sell
Quantity
Price
Order Type
Time In Force
Preview
Submit
后续如果 API 支持，可以增加：

Replace Order

Cancel Order

⑤ Positions（左下）
显示：

字段
Symbol
Quantity
Average Price
Market Value
Unrealized P/L
数据全部来自 Broker Position API。

⑥ Orders（中下）
显示：

字段
Working
Filled
Cancelled
Rejected
点击订单可查看详情。

⑦ Account Summary（右下）
显示：

字段
Cash
Buying Power
Account Value
Net Liquidation
数据来自 Broker Account API。

页面刷新策略
模块	刷新方式	建议频率
Quote Information	WebSocket（优先）/ Polling	1 秒
Watch List	同 Quote	1 秒
Positions	Polling	5 秒
Orders	Polling	5 秒
Account Summary	Polling	10 秒
Market Status	本地计算	60 秒
我建议文档再增加一节："设计原则"
放在 UI 设计说明书最前面，作为整个项目的约束：

UI 与 Broker 解耦：所有页面通过 Broker Adapter 获取数据，不直接依赖 Schwab 或 Tastytrade。

只展示 API 能直接提供的数据：不预留无数据来源的模块，不显示占位功能。

统一 UI：无论用户登录 Schwab 还是 Tastytrade，界面保持一致，仅数据来源不同。

模块化布局：Watch List、Quote、Order Entry、Positions、Orders、Account Summary 均为独立组件，便于后续扩展和维护。

我认为这版布局可以作为 QuickTrade V1 的最终 UI 基线，后续数据库设计、API 映射、Vue 页面和 Java 服务都可以围绕这份布局展开。


继续

很好，我建议我们不要再把它当成普通的 UI 文档，而是按照**软件设计说明书（Software Design Specification，SDS）**来写。

下面是我建议的 《QuickTrade V1 UI设计说明书》 正式版本结构。

QuickTrade V1 UI设计说明书
Version：1.0

1. 项目简介
1.1 项目名称
QuickTrade

1.2 项目目标
QuickTrade 是一个支持多券商（Broker）的股票交易客户端。

V1 支持：

Schwab

Tastytrade

系统采用 Broker Adapter Architecture。

无论用户登录哪个 Broker，

QuickTrade 的 UI 保持完全一致。

Broker 仅负责提供数据。

1.3 V1设计原则
原则一
UI 与 Broker 完全解耦。

Vue UI

↓

QuickTrade Service

↓

Broker Adapter

↓

Schwab
或
Tastytrade
原则二
UI 只展示 Broker API 能直接提供的数据。

例如：

✔ Quote

✔ Position

✔ Orders

✔ Account

✘ K Line

✘ News

✘ Scanner

✘ Level2

原则三
所有页面采用 Dark Theme。

原则四
所有数据实时刷新。

Quote

≈1 秒

Order

≈5 秒

Account

≈10 秒

2. 页面布局
整个页面采用：

Header

↓

Main

↓

Footer
其中

Main

划分为

Left

Center

Right
以及

Bottom
整体如下：

┌──────────────────────────────────────────────────────────────────────────────┐
│ Header                                                                       │
├────────────────────┬───────────────────────────────────┬─────────────────────┤
│                    │                                   │                     │
│                    │                                   │                     │
│ Watch List         │ Quote Information                 │ Order Entry         │
│                    │                                   │                     │
│                    │                                   │                     │
├────────────────────┼───────────────────────────────────┼─────────────────────┤
│ Positions          │ Orders                            │ Account Summary     │
└────────────────────┴───────────────────────────────────┴─────────────────────┘
3. Header设计
高度：

60px
宽度：

100%
包含：

控件	描述
Logo	系统Logo
Search	股票搜索
Market Status	Open / Closed
User	当前用户
Account	当前账户
Cash	Cash Balance
Buying Power	Buying Power
Logout	登出
Header排列
┌──────────────────────────────────────────────────────────────┐

Logo

Search

Market

Account

Cash

Buying Power

Logout

└──────────────────────────────────────────────────────────────┘
4. Watch List
位于左侧。

宽度：

20%
作用：

维护用户关注股票。

显示：

字段
Symbol
Last
Change
Change %
点击股票：

Watch List

↓

更新 Quote

↓

更新 Order Entry
数据：

WatchList

↓

QuickTrade DB

Quote

↓

Broker API
5. Quote Information
位于中间。

宽度：

45%
显示：

字段
Company Name
Symbol
Last Price
Bid
Ask
Open
High
Low
Previous Close
Volume
52 Week High
52 Week Low
注意：

Quote 页面

不出现

K线

News

Time&Sales

Level2
Quote布局
Company Name

AAPL

Last Price

Bid

Ask

Open

High

Low

Prev Close

Volume

52W High

52W Low
6. Order Entry
位于右侧。

宽度：

35%
包含：

Buy

Sell

Qty

Price

Order Type

Time In Force

Preview

Submit
建议布局：

BUY

SELL

Qty

Price

Order Type

Time In Force

Preview

Submit
7. Positions
左下。

字段：

Symbol

Qty

Avg Price

Market Value

Unrealized P/L
8. Orders
中下。

字段：

Working

Filled

Cancelled

Rejected
支持：

点击

↓

查看详情

9. Account Summary
右下。

字段：

Cash

Buying Power

Account Value

Net Liquidation
10. 页面刷新
页面	刷新
Quote	1 秒
WatchList	1 秒
Positions	5 秒
Orders	5 秒
Account	10 秒
11. 不包含的功能
V1 明确不开发：

功能	原因
K线图	API 不提供历史行情
分时图	API 不提供
Time & Sales	API 不提供
Level2	API 不提供
Scanner	后续版本
News	后续版本
Alert	后续版本
Heat Map	后续版本
我建议增加第 12 章："组件编号（Component ID）"
这是很多项目容易忽略，但对开发和测试非常有帮助的一部分。给每个 UI 模块一个固定编号，便于需求、开发、测试和 Bug 跟踪统一引用。

Component ID	模块	Vue 组件建议
UI-001	Header	Header.vue
UI-002	Watch List	WatchList.vue
UI-003	Quote Information	QuotePanel.vue
UI-004	Order Entry	OrderEntry.vue
UI-005	Positions	PositionsPanel.vue
UI-006	Orders	OrdersPanel.vue
UI-007	Account Summary	AccountSummary.vue
后续数据库设计、接口设计、API 映射文档都可以直接引用这些组件编号，例如：

UI-003 调用 BrokerQuoteService.getQuote()

UI-004 调用 BrokerOrderService.placeOrder()

UI-005 调用 BrokerPositionService.listPositions()

这样整套文档会形成统一的编号体系，更接近企业级项目的开发规范。


好的 ，接下来是功能一览。也是之前讨论好的

好的，我建议功能一览不要只是简单列功能，而是作为产品需求文档（PRD）的核心内容。

它应该回答四个问题：

这个功能是什么？

V1 是否开发？

Schwab 是否支持？

Tastytrade 是否支持？

注意这里不是 V1 使用哪家 API，而是确认 UI 是否可以实现。

QuickTrade V1 功能一览
一、设计原则
V1 仅开发 Schwab API 与 Tastytrade API 共同能够支撑的交易功能。

UI 保持统一，根据用户登录的 Broker 自动连接对应 Adapter。

QuickTrade 不直接依赖任何 Broker API。

没有 API 数据支持的功能，不纳入 V1。

二、功能列表
编号	模块	功能	描述	Schwab	Tastytrade	V1
QT-001	Login	Broker 登录	登录交易账户	✅	✅	✅
QT-002	Login	Broker 登出	退出当前账户	✅	✅	✅
QT-003	Header	当前用户	显示登录用户	✅	✅	✅
QT-004	Header	当前账户	显示交易账户	✅	✅	✅
QT-005	Header	Cash	显示现金余额	✅	✅	✅
QT-006	Header	Buying Power	显示购买力	⚠️	✅	✅
QT-007	Header	Market Status	显示市场开闭状态	本地	本地	✅
QT-008	Search	股票搜索	根据代码搜索股票	✅	✅	✅
QT-009	WatchList	查看自选股	查看用户自选股票	DB	DB	✅
QT-010	WatchList	新增自选股	添加股票到 WatchList	DB	DB	✅
QT-011	WatchList	删除自选股	删除自选股票	DB	DB	✅
QT-012	WatchList	选择股票	更新 Quote 面板	DB	DB	✅
QT-013	Quote	股票代码	Symbol	✅	✅	✅
QT-014	Quote	公司名称	Company Name	✅	✅	✅
QT-015	Quote	最新价格	Last Price	✅	✅	✅
QT-016	Quote	买价	Bid	✅	✅	✅
QT-017	Quote	卖价	Ask	✅	✅	✅
QT-018	Quote	开盘价	Open	✅	✅	✅
QT-019	Quote	最高价	High	✅	✅	✅
QT-020	Quote	最低价	Low	✅	✅	✅
QT-021	Quote	昨收	Previous Close	✅	✅	✅
QT-022	Quote	成交量	Volume	✅	✅	✅
QT-023	Quote	52 周最高	52 Week High	✅	✅	✅
QT-024	Quote	52 周最低	52 Week Low	✅	✅	✅
QT-025	Order	买入	Buy Order	✅	✅	✅
QT-026	Order	卖出	Sell Order	✅	✅	✅
QT-027	Order	Preview Order	下单预览	✅	✅	✅
QT-028	Order	Submit Order	提交订单	✅	✅	✅
QT-029	Order	Cancel Order	撤单	✅	✅	✅
QT-030	Order	Replace Order	修改订单	✅	✅	✅
QT-031	Position	查看持仓	当前持仓	✅	✅	✅
QT-032	Position	持仓数量	Quantity	✅	✅	✅
QT-033	Position	持仓均价	Average Price	✅	✅	✅
QT-034	Position	市值	Market Value	✅	✅	✅
QT-035	Position	浮动盈亏	Unrealized P/L	✅	✅	✅
QT-036	Orders	未成交订单	Working Orders	✅	✅	✅
QT-037	Orders	已成交订单	Filled Orders	✅	✅	✅
QT-038	Orders	已撤销订单	Cancelled Orders	✅	✅	✅
QT-039	Orders	已拒绝订单	Rejected Orders	✅	✅	✅
QT-040	Account	Account Summary	账户概要	✅	✅	✅
QT-041	Account	Net Liquidation	总资产	⚠️	✅	✅
QT-042	Account	Account Value	账户价值	✅	✅	✅
三、V1 不开发功能
这些功能不是因为不重要，而是不符合 QuickTrade V1 的设计原则。

编号	功能	Schwab	Tastytrade	原因
FUT-001	K线图	❌	❌	无历史行情数据
FUT-002	分时走势图	❌	❌	无历史行情数据
FUT-003	Time & Sales	❌	❌	无逐笔成交数据
FUT-004	Level 2 / Market Depth	❌	❌	无五档盘口数据
FUT-005	Scanner	❌	❌	需自行实现全市场扫描
FUT-006	News	❌	❌	无新闻接口
FUT-007	Alert	❌	❌	需 QuickTrade 自行实现
FUT-008	技术指标（MACD、RSI 等）	❌	❌	需历史行情计算
FUT-009	Heat Map	❌	❌	无数据来源
四、功能模块关系
Login
   │
   ├── Header
   │
   ├── WatchList
   │      │
   │      ▼
   │   Quote Information
   │      │
   │      ├─────────────┐
   │      ▼             ▼
   │  Order Entry   Positions
   │      │             │
   │      ▼             ▼
   └── Orders     Account Summary
我建议再增加一个企业项目常用的维度——优先级（Priority）
后续开发排期时会非常有用。

优先级	含义
P0	核心功能，没有它系统无法使用（登录、报价、下单、持仓）
P1	重要功能，应在 V1 完成（订单列表、账户信息、WatchList）
P2	可延后功能（未来版本，如 Scanner、Alert）
这样开发团队就可以直接按照 P0 → P1 → P2 的顺序实施，而不需要每次再讨论开发优先级。对于后续编写开发计划、任务拆分（Sprint）和测试计划，这套编号和优先级体系都可以直接复用。


继续

很好，接下来建议进入第三份文档。

这一份不是给产品经理，而是整个项目最重要的设计文档。

如果说：

UI设计说明书 → 给前端

功能列表 → 给产品

那么这一份就是：

开发说明书

以后 Java、Vue、DB 都照着它开发。

《QuickTrade V1 UI功能 × Broker API 对照表》
Version 1.0

文档目的
确认：

QuickTrade UI 的每一个功能，Schwab API 是否支持，Tastytrade API 是否支持。

注意：

这里不是比较哪家 Broker 好。

而是回答：

这个 UI 能不能实现。

如果：

两家 API 都没有。

那么：

V1 不开发。

设计原则
整个 QuickTrade

采用

Vue UI

↓

QuickTrade Service

↓

Broker Adapter

↓

Schwab
Tastytrade
所以

UI

完全不知道

下面连接的是哪一家 Broker。

Header
UI功能	Schwab	Tastytrade	QuickTrade实现	备注
Login User	✅	✅	BrokerSessionService	
Current Account	✅	✅	BrokerAccountService	
Cash	✅	✅	BrokerAccountService	
Buying Power	⚠️	✅	BrokerAccountService	Schwab 部分账户支持
Search Symbol	✅	✅	BrokerInstrumentService	
Market Status	❌	❌	MarketCalendarService	本地计算
Logout	✅	✅	BrokerSessionService	
Watch List
UI功能	Schwab	Tastytrade	QuickTrade实现	备注
WatchList	❌	❌	WatchListService	MySQL
Add Symbol	❌	❌	WatchListService	
Remove Symbol	❌	❌	WatchListService	
Sort	❌	❌	WatchListService	
Last Price	✅	✅	BrokerQuoteService	
Change	✅	✅	BrokerQuoteService	
Change %	✅	✅	BrokerQuoteService	
Quote Information
UI功能	Schwab	Tastytrade	QuickTrade实现	备注
Company Name	✅	✅	BrokerQuoteService	
Symbol	✅	✅	BrokerQuoteService	
Last Price	✅	✅	BrokerQuoteService	
Bid	✅	✅	BrokerQuoteService	
Ask	✅	✅	BrokerQuoteService	
Bid Size	✅	✅	BrokerQuoteService	
Ask Size	✅	✅	BrokerQuoteService	
Open	✅	✅	BrokerQuoteService	
High	✅	✅	BrokerQuoteService	
Low	✅	✅	BrokerQuoteService	
Previous Close	✅	✅	BrokerQuoteService	
Volume	✅	✅	BrokerQuoteService	
52 Week High	✅	✅	BrokerQuoteService	
52 Week Low	✅	✅	BrokerQuoteService	
Order Entry
UI功能	Schwab	Tastytrade	QuickTrade实现	备注
Buy	✅	✅	BrokerOrderService	
Sell	✅	✅	BrokerOrderService	
Quantity	✅	✅	BrokerOrderService	
Price	✅	✅	BrokerOrderService	
Order Type	✅	✅	BrokerOrderService	
Time In Force	✅	✅	BrokerOrderService	
Preview Order	✅	✅	BrokerOrderService	
Submit Order	✅	✅	BrokerOrderService	
Cancel Order	✅	✅	BrokerOrderService	独立操作
Replace Order	✅	✅	BrokerOrderService	独立操作
Positions
UI功能	Schwab	Tastytrade	QuickTrade实现	备注
Symbol	✅	✅	BrokerPositionService	
Quantity	✅	✅	BrokerPositionService	
Average Price	✅	✅	BrokerPositionService	
Market Value	✅	✅	BrokerPositionService	
Unrealized P/L	✅	✅	BrokerPositionService	
Day P/L	⚠️	✅	BrokerPositionService	Schwab 依账户类型
Orders
UI功能	Schwab	Tastytrade	QuickTrade实现	备注
Working Orders	✅	✅	BrokerOrderService	
Filled Orders	✅	✅	BrokerOrderService	
Cancelled Orders	✅	✅	BrokerOrderService	
Rejected Orders	✅	✅	BrokerOrderService	
Order Detail	✅	✅	BrokerOrderService	
Account Summary
UI功能	Schwab	Tastytrade	QuickTrade实现	备注
Cash	✅	✅	BrokerAccountService	
Buying Power	⚠️	✅	BrokerAccountService	Schwab 依账户类型
Account Value	✅	✅	BrokerAccountService	
Net Liquidation	⚠️	✅	BrokerAccountService	字段映射不同
V1 不开发
UI功能	Schwab	Tastytrade	原因
K Line	❌	❌	API 不提供
Intraday Chart	❌	❌	API 不提供
Time & Sales	❌	❌	API 不提供
Level2	❌	❌	API 不提供
News	❌	❌	API 不提供
Scanner	❌	❌	QuickTrade 后续实现
Alert	❌	❌	QuickTrade 后续实现
HeatMap	❌	❌	API 不提供
最后增加一章（企业项目都会有）
Broker Adapter 接口一览
这是整个 QuickTrade 最核心的设计，也是后续支持更多券商的基础。

建议统一定义以下接口：

接口	职责	Schwab	Tastytrade
BrokerSessionService	登录、登出、Token 管理	✔	✔
BrokerInstrumentService	股票搜索、基础资料	✔	✔
BrokerQuoteService	实时报价	✔	✔
BrokerOrderService	下单、改单、撤单、查询订单	✔	✔
BrokerPositionService	查询持仓	✔	✔
BrokerAccountService	查询账户、余额、购买力	✔	✔
建议的项目接口结构
quicktrade-broker-api/
├── BrokerSessionService
├── BrokerInstrumentService
├── BrokerQuoteService
├── BrokerOrderService
├── BrokerPositionService
└── BrokerAccountService

quicktrade-broker-schwab/
└── Schwab 实现上述接口

quicktrade-broker-tastytrade/
└── Tastytrade 实现上述接口
这一份文档之后，下一份我建议就是《QuickTrade 统一领域模型（Domain Model）》。它会定义 Quote、Order、Position、Account 等 Java DTO 和数据库实体，是整个系统的数据标准，也是数据库设计和 API 字段映射的基础。这个文档完成后，基本就可以开始正式编码了。


继续

我认为下一份应该是整个项目最重要的一份文档。

它的重要性甚至高于数据库。

QuickTrade V1 统一领域模型（Domain Model）
Version 1.0

为什么要有 Domain Model？
因为：

Schwab

和

Tastytrade

返回的数据结构完全不同。

例如

Schwab

{
    "bidPrice": 212.31,
    "askPrice": 212.35,
    "lastPrice":212.34
}
Tastytrade

{
    "bid":"212.31",
    "ask":"212.35",
    "mark":"212.33"
}
如果 UI 直接使用 Broker 的数据：

以后增加 Broker

整个 Vue

全部都要改。

这是企业开发最忌讳的。

所以

QuickTrade

应该定义：

自己的对象。

Broker Adapter

负责转换。

即

Schwab JSON
        │
        ▼
Schwab Adapter
        │
        ▼
QuickTrade Quote
        │
        ▼
Vue UI
另一家

Tastytrade JSON
        │
        ▼
Tasty Adapter
        │
        ▼
QuickTrade Quote
        │
        ▼
Vue UI
所以

整个系统

UI

Java

DB

全部使用

QuickTrade Model。

Domain Model
建议

只有几个核心对象。

不要太多。

Quote

Order

Position

Account

Instrument

WatchList

User
以后

所有 Service

全部返回这些对象。

例如

BrokerQuoteService

不是返回

SchwabQuote

而是

QuickTradeQuote
1
Quote

统一行情对象。

建议如下：

字段	类型	说明
symbol	String	股票代码
companyName	String	公司名称
exchange	String	NASDAQ
currency	String	USD
lastPrice	BigDecimal	最新价
bidPrice	BigDecimal	Bid
askPrice	BigDecimal	Ask
bidSize	Integer	Bid Size
askSize	Integer	Ask Size
openPrice	BigDecimal	开盘
highPrice	BigDecimal	最高
lowPrice	BigDecimal	最低
previousClose	BigDecimal	昨收
volume	Long	成交量
week52High	BigDecimal	52周最高
week52Low	BigDecimal	52周最低
updateTime	LocalDateTime	更新时间
Java

建议

Quote
不要

QuoteDTO
Domain

就是 Domain。

2
Position

统一持仓。

建议：

字段	类型
symbol	String
quantity	BigDecimal
averagePrice	BigDecimal
marketPrice	BigDecimal
marketValue	BigDecimal
unrealizedPL	BigDecimal
realizedPL	BigDecimal
3
Order

统一订单。

建议：

字段	类型
orderId	String
symbol	String
side	BUY/SELL
quantity	BigDecimal
price	BigDecimal
orderType	Enum
timeInForce	Enum
status	Enum
createTime	LocalDateTime
updateTime	LocalDateTime
OrderStatus

统一：

NEW

WORKING

PARTIAL_FILLED

FILLED

CANCELLED

REJECTED

EXPIRED
不要直接使用

Broker

自己的状态。

统一转换。

4
Account

建议：

字段	类型
accountNumber	String
accountName	String
broker	Enum
cashBalance	BigDecimal
buyingPower	BigDecimal
accountValue	BigDecimal
netLiquidation	BigDecimal
currency	String
Broker

统一

SCHWAB

TASTYTRADE
以后

增加

IBKR

Firstrade
不用改数据库。

5
Instrument

股票信息。

字段	类型
symbol	String
companyName	String
exchange	String
assetType	Enum
currency	String
AssetType

统一

STOCK

ETF

OPTION

INDEX
以后

支持

Option

不用改。

6
WatchList

这是

QuickTrade

自己的。

Broker

没有。

建议

WatchList
包含

字段	类型
id	Long
userId	Long
symbol	String
sortOrder	Integer
createTime	LocalDateTime
以后

支持

多个 WatchList。

例如

Tech

AI

China

ETF
不用改。

7
User

建议不要复杂。

Broker

已经有用户。

QuickTrade

只保存：

字段	类型
id	Long
username	String
defaultBroker	Enum
theme	Enum
language	Enum
不要保存

Broker

密码。

统一枚举（Enums）
建议一开始就统一，不要使用各 Broker 的原始值。

BrokerType
SCHWAB
TASTYTRADE
OrderSide
BUY
SELL
OrderStatus
NEW
WORKING
PARTIAL_FILLED
FILLED
CANCELLED
REJECTED
EXPIRED
OrderType
MARKET
LIMIT
STOP
STOP_LIMIT
TimeInForce
DAY
GTC
IOC
FOK
AssetType
STOCK
ETF
OPTION
INDEX
我建议再增加一个核心对象：BrokerSession
这是之前几份文档没有体现，但实际开发一定需要的。

它负责管理当前登录券商的会话，而不是 UI 或 User 自己管理。

建议字段如下：

字段	类型	说明
broker	BrokerType	当前券商
accountNumber	String	当前账户
accessToken	String	当前访问令牌（内存/缓存，不落库）
refreshToken	String	刷新令牌（按安全策略存储）
tokenExpireTime	LocalDateTime	Token 过期时间
loginTime	LocalDateTime	登录时间
isConnected	Boolean	当前连接状态
这样整个系统的数据模型就完整了：

User
   │
   ▼
BrokerSession
   │
   ├── Account
   ├── Position
   ├── Order
   ├── Quote
   └── Instrument

WatchList
   │
   ▼
Quote
这是我建议作为 QuickTrade V1 的统一领域模型。 后续数据库设计、REST API、Broker Adapter 和 Vue 页面都围绕这些对象开发，不直接依赖 Schwab 或 Tastytrade 的原始 JSON，从而保证后续增加新的 Broker 时，UI 和业务逻辑几乎不需要修改。


继续

我建议不要继续写数据库。

真正企业开发，一定是在 Domain Model → 系统架构 → 数据库 这个顺序。

下一份应该是：

《QuickTrade V1 系统架构设计说明书》

这份文档会决定整个项目以后还能不能继续扩展。

QuickTrade V1 系统架构设计说明书
Version 1.0

1. 系统目标
QuickTrade 是一个 Broker Independent Trading Platform。

支持：

Schwab

Tastytrade

未来支持：

IBKR

Firstrade

Alpaca

Robinhood

UI 不需要修改。

2. 整体架构
整个系统建议采用 分层架构（Layered Architecture）。

┌─────────────────────────────────────────────┐
│                Vue UI                       │
└─────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────┐
│          REST Controller                    │
└─────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────┐
│          Business Service                   │
└─────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────┐
│          Broker Service                     │
└─────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────┐
│          Broker Adapter                     │
├─────────────────────┬───────────────────────┤
│ Schwab Adapter      │ Tastytrade Adapter    │
└─────────────────────┴───────────────────────┘
                     │
                     ▼
┌─────────────────────┬───────────────────────┐
│ Schwab API          │ Tastytrade API        │
└─────────────────────┴───────────────────────┘
3. 模块划分
建议采用 Maven Multi-Module。

quicktrade-parent
│
├── quicktrade-common
│
├── quicktrade-core
│
├── quicktrade-api
│
├── quicktrade-admin
│
├── quicktrade-broker-api
│
├── quicktrade-broker-schwab
│
├── quicktrade-broker-tastytrade
│
└── quicktrade-web
quicktrade-common
公共模块。

包含：

Constant

Enum

Utils

Exception

Response

BaseEntity
quicktrade-core
核心业务。

例如：

QuoteService

OrderService

PositionService

AccountService

WatchListService
这里只处理业务。

不知道 Broker。

quicktrade-api
REST API。

例如：

QuoteController

OrderController

AccountController

PositionController
这里只负责：

HTTP

↓

JSON

↓

Service

quicktrade-admin
后台管理。

例如：

User

WatchList

Broker Config

System Config

Log
quicktrade-broker-api
这是整个项目最重要。

只放接口。

例如：

BrokerQuoteService

BrokerOrderService

BrokerAccountService

BrokerPositionService

BrokerSessionService

BrokerInstrumentService
这里只有

Interface。

没有实现。

quicktrade-broker-schwab
全部都是：

Schwab

实现。

例如：

SchwabQuoteService

SchwabOrderService

SchwabPositionService

SchwabAccountService
quicktrade-broker-tastytrade
同理：

TastyQuoteService

TastyOrderService

TastyPositionService

TastyAccountService
4. Broker Adapter
建议采用 Strategy Pattern。

BrokerService

↓

BrokerFactory

↓

Schwab

↓

Tastytrade
例如：

BrokerQuoteService quoteService =
    brokerFactory.getQuoteService(brokerType);
然后：

Quote quote =
    quoteService.getQuote("AAPL");
UI 永远不知道：

下面是谁。

5. Service 分层
建议：

Controller
↓

Business Service
↓

Broker Service
↓

Broker Adapter
↓

Broker API
不要：

Controller

直接调用

Broker。

6. Repository
数据库。

建议：

WatchListRepository

UserRepository

SystemConfigRepository
Broker 数据

不要缓存数据库。

例如：

Position

Order

Account

Quote

全部实时获取。

数据库

只保存：

WatchList

Config

User

Preference

Log
7. Cache
建议：

Redis。

缓存：

Broker Token

Market Calendar

Instrument

Exchange
不要缓存：

Quote

Order

Position
这些必须实时。

8. WebSocket
Quote

建议：

Broker API

↓

QuoteService

↓

WebSocket

↓

Vue
不要：

Vue

每秒

请求 REST。

9. Exception
统一：

BusinessException
↓

BrokerException
↓

SchwabException

TastyException
统一返回：

ApiResponse
10. 日志
建议：

INFO

WARN

ERROR
Broker

调用

单独：

broker.log
Order

单独：

order.log
Authentication

单独：

auth.log
11. 配置
建议：

application.yml

application-dev.yml

application-test.yml

application-prod.yml
Broker

单独：

broker-schwab.yml

broker-tasty.yml
12. 安全
Token

不要放：

Session。

建议：

Redis
所有 Broker

统一：

BrokerSessionManager
管理。

我建议对整体架构做一个调整
结合我们前面的讨论，我建议 Business Service 和 Broker Service 不要混在一起，而是明确分成两层：

Vue UI
    │
REST Controller
    │
Application Service   ← 编排业务流程（登录、查询、下单等）
    │
Domain Service        ← Quote、Order、Position 等业务规则
    │
Broker Adapter        ← Schwab / Tastytrade 实现
    │
Broker API
这样：

Application Service 负责协调多个领域对象，例如“登录后加载账户、持仓、自选股”。

Domain Service 负责单一业务能力，例如 Quote、Order、Account。

Broker Adapter 只负责协议转换和 API 调用。

这种职责划分更清晰，也更符合你希望支持多个 Broker 的长期目标。等这份架构文档定稿后，再开始数据库设计，后续基本不会因为架构调整而推翻已有代码。


继续

我觉得，到这里我们应该停一下重新整理。

原因不是没有内容，而是已经到了决定整个项目质量的关键阶段。

目前我们已经完成了：

✅ UI设计说明
✅ 功能一览
✅ API对照
✅ Domain Model
✅ 系统架构
下一步绝对不是数据库。

为什么？
很多项目都是：

想到什么

↓

开始建表

↓

写Java

↓

后来发现架构不对

↓

数据库全部重做
这是最常见的失败原因。

我建议下一份应该是
QuickTrade V1 模块设计说明（Module Design）
这一份才是真正决定整个项目。

例如我们整个项目到底有哪些模块？

我建议不要按照 Controller 来划分。

而是按照业务。

例如：

QuickTrade
│
├── Authentication
│
├── Broker
│
├── Market
│
├── Trading
│
├── Portfolio
│
├── WatchList
│
├── User
│
├── System
│
└── Notification（V2）
是不是比

QuoteController

OrderController

PositionController
高级很多？

企业都是这样设计。

Authentication
负责：

Broker Login

Broker Logout

Refresh Token

Token Check

Session
这一整个模块。

而不是

LoginController

LogoutController

Broker
负责：

Broker Factory

Broker Adapter

Broker Config

Broker Manager
这是整个系统最核心。

Market
负责：

Search Symbol

Quote

Instrument

Exchange

Market Calendar
注意：

Quote

不是 Trading。

很多人这里会设计错。

Trading
负责：

Preview

Buy

Sell

Cancel

Replace

Order Validation
只处理：

订单。

Portfolio
负责：

Position

Account

Balance

Buying Power
Portfolio

不是 Position。

Position

只是其中一个。

WatchList
负责：

Create

Delete

Sort

Load

Favorite
全部来自数据库。

User
负责：

Theme

Language

Preference

Default Broker
以后：

Dark Mode

都在这里。

System
负责：

Config

Dictionary

Log

Version
为什么这样划分？
因为以后

Quote

越来越复杂。

例如：

Quote

↓

WebSocket

↓

Snapshot

↓

Streaming

↓

Cache
都还是：

Market

模块。

不会跑到 Trading。

第二步
每个模块

再拆 Service。

例如

Market

Market

│

├── QuoteService

├── InstrumentService

├── ExchangeService

└── CalendarService
Trading

Trading

│

├── OrderService

├── PreviewService

├── CancelService

└── ReplaceService
Portfolio

Portfolio

│

├── PositionService

├── AccountService

└── BalanceService
这样

代码就不会乱。

第三步
Service

对应 Domain。

例如：

QuoteService

↓

Quote
OrderService

↓

Order

PositionService

↓

Position

整个项目

就形成：

Module

↓

Service

↓

Domain

↓

Repository
这一层。

我建议调整整个文档顺序
经过前面的讨论，我建议把文档顺序调整成下面这样，比之前更加符合大型项目的设计流程。

文档编号	文档名称	状态
01	UI设计说明书	✅
02	功能一览	✅
03	UI功能 × Broker API 对照	✅
04	系统架构设计	✅
05	模块设计（Module Design）	下一步
06	Domain Model（统一领域模型）	✅（建议放在模块设计之后）
07	API 字段映射（Schwab ↔ Tastytrade ↔ QuickTrade）	待完成
08	数据库设计	待完成
09	REST API 设计	待完成
10	Broker Adapter 设计	待完成
11	Vue 页面设计	待完成
12	部署与运维	待完成
我还有一个建议：增加《状态流转设计》
除了上面的文档，我建议再增加一份专门描述状态变化的设计文档，而不是把这些规则散落在代码里。

例如：

订单状态流转：NEW → WORKING → PARTIAL_FILLED → FILLED / CANCELLED / REJECTED

Broker Session 状态：DISCONNECTED → CONNECTING → CONNECTED → TOKEN_REFRESHING → DISCONNECTED

UI 页面状态：Loading → Loaded → Refreshing → Error

这类状态机对于交易系统非常重要，后续实现 Broker Adapter、前端状态管理（Vue）以及异常处理时都会直接参考它。我建议把它作为独立文档，而不是附属在某个章节里，这样整个 QuickTrade 的设计会更加完整和易于维护。


好的。继续

很好，我建议我们从现在开始完全按照企业级交易软件（Bloomberg、IBKR、Thinkorswim、TradeStation 等）的设计方式来整理文档。

我重新梳理了一遍，我认为 QuickTrade 最大的优势 是：

它不是 Schwab Client，也不是 Tastytrade Client。

QuickTrade 是 Broker Independent Trading Platform（券商无关交易平台）。

这句话应该写进第一章。

第五份文档
QuickTrade V1 模块设计说明（Module Design Specification）
Version 1.0

1. 设计目标
QuickTrade 采用**模块化（Modular Architecture）**设计。

每个模块负责单一职责（Single Responsibility）。

模块之间通过 Service Interface 通信，不直接依赖 Broker API。

Broker API 仅由 Broker Adapter 模块访问。

2. 模块关系
建议整个项目分成 8 个一级模块。

QuickTrade
│
├── Authentication
│
├── Broker
│
├── Market
│
├── Trading
│
├── Portfolio
│
├── WatchList
│
├── User
│
└── System
以后增加：

Notification

Scanner

Strategy

Option

History

Chart
全部作为新模块。

不会影响已有代码。

3. Authentication
负责：

Broker 登录。

Broker 登出。

Token 生命周期。

Session。

子模块
Authentication

│

├── Login

├── Logout

├── Refresh Token

├── Session

└── Authorization
建议 Service

AuthenticationService
负责：

login()

logout()

refreshToken()

checkSession()

getCurrentBroker()
Domain
BrokerSession
不允许：

Quote

直接操作

Token。

全部经过：

Authentication。

4. Broker
Broker

不是交易。

Broker

只是：

适配层。

负责：

Broker Factory

Broker Manager

Broker Adapter

Broker Config
Service

建议：

BrokerFactory

BrokerManager
Adapter

建议：

SchwabAdapter

TastytradeAdapter
统一实现：

BrokerQuoteService

BrokerOrderService

BrokerAccountService
Broker

不允许：

处理业务。

例如：

不能：

计算盈亏
不能：

计算成本
不能：

排序
这些：

全部属于

Business。

5. Market
这是：

整个系统最大的模块。

负责：

市场数据。

不是：

交易。

包含：

Quote

Instrument

Search

Exchange

Market Calendar
以后：

WebSocket

Streaming

也属于：

Market。

建议：

Service：

QuoteService

InstrumentService

SearchService

CalendarService
Domain：

Quote

Instrument
Repository：

没有。

因为：

实时数据。

6. Trading
Trading

负责：

订单。

建议：

子模块：

Preview

Order

Cancel

Replace
Service：

OrderService

PreviewService

CancelService

ReplaceService
Domain：

Order
Trading

不能：

查询：

Quote。

必须：

调用：

Market。

7. Portfolio
很多项目

这里只有：

Position。

这是错误。

Portfolio

应该包含：

Account

Balance

Buying Power

Position

Equity
建议：

Service：

AccountService

PositionService

BalanceService
Domain：

Account

Position
8. WatchList
WatchList

属于：

QuickTrade。

不是：

Broker。

建议：

支持：

Create

Update

Delete

Sort
以后：

支持：

Folder
例如：

AI

ETF

Dividend

China
数据库

不用修改。

9. User
这里只保存：

QuickTrade

自己的数据。

例如：

Theme

Language

Timezone

Preference

Default Broker
不要保存：

Broker

密码。

不要保存：

Broker

Token。

10. System
负责：

Dictionary

Config

Version

Health

Log
以后：

License

也在这里。

11. 模块依赖关系（重点）
这里是整个项目最重要的一张图。

建议明确规定：

                 Authentication
                        │
                        ▼
                   Broker Adapter
                        │
        ┌───────────────┴───────────────┐
        ▼                               ▼
     Market                         Trading
        │                               │
        └───────────────┬───────────────┘
                        ▼
                   Portfolio
                        │
        ┌───────────────┴───────────────┐
        ▼                               ▼
    WatchList                        User
                        │
                        ▼
                     System
模块依赖原则
Authentication 是 Broker 的唯一入口。

Broker Adapter 只负责调用外部 API，不包含业务规则。

Trading 可以调用 Market 获取行情，但不能直接访问 Broker API。

Portfolio 可以依赖 Market（例如显示市值），但不依赖 Trading。

WatchList 只维护用户配置，通过 Market 获取实时行情。

System 提供公共配置、日志、健康检查等基础能力，不依赖业务模块。

12. 模块职责矩阵
模块	负责	不负责
Authentication	登录、登出、Token、Session	行情、下单
Broker	API 调用、数据转换	业务逻辑、数据库
Market	股票搜索、报价、市场状态	下单、账户
Trading	下单、撤单、改单、订单预览	行情计算、持仓计算
Portfolio	持仓、账户、购买力	下单、股票搜索
WatchList	自选股管理	实时行情存储
User	用户偏好、主题、语言	Broker 认证
System	配置、日志、版本、健康检查	业务功能
我建议下一份文档不要直接写数据库，而是先完成《Broker Adapter 设计规范》
这是因为 QuickTrade 的核心竞争力就是多 Broker 支持。如果先把 Adapter 接口、工厂模式、数据转换规则和异常处理规范设计好，那么：

数据库字段就知道应该存哪些数据；

Java 接口可以直接按照规范实现；

Schwab 和 Tastytrade 的适配器也能保持一致的风格；

将来增加 IBKR、Firstrade 等新券商时，几乎不需要改业务层和 UI。

我认为这比直接开始设计数据库更有价值，因为数据库最终应该服务于整体架构，而不是反过来限制架构。


继续

我认为，到这里可以确定 QuickTrade 的核心已经不是 UI，而是 Broker Adapter。

实际上，整个系统以后最稳定、最重要的一层，就是 Broker Adapter Layer。

所以我建议下一份文档命名为：

QuickTrade V1 Broker Adapter Design Specification（Broker 适配器设计规范）

这份文档会成为以后所有 Broker（Schwab、Tastytrade、IBKR……）都必须遵守的规范。

1. 设计目标
Broker Adapter 是 QuickTrade 与外部 Broker API 的唯一连接层。

所有业务模块（Market、Trading、Portfolio 等）禁止直接调用外部 API。

必须通过统一的 Broker Adapter 接口。

架构如下：

Vue UI
    │
REST Controller
    │
Application Service
    │
Domain Service
    │
Broker Adapter Interface
    │
┌───────────────┬────────────────┐
│               │                │
Schwab Adapter  Tastytrade Adapter  （未来可增加更多）
    │               │
Schwab API     Tastytrade API
2. Adapter 设计原则
原则一：统一接口
业务层只认识 QuickTrade 的接口，例如：

BrokerQuoteService
BrokerOrderService
BrokerAccountService
BrokerPositionService
BrokerSessionService
BrokerInstrumentService
不知道下面是哪一家 Broker。

原则二：统一 Domain Model
Adapter 返回的一律是：

Quote
Order
Position
Account
Instrument
BrokerSession
不能返回：

SchwabQuote
TastyQuote
这些对象只能存在于 Adapter 内部。

原则三：负责转换，不负责业务
Adapter 的职责只有：

调用 Broker API

JSON 解析

字段映射

异常转换

不能：

计算盈亏

排序

过滤

做业务判断

这些都属于 Domain Service。

3. Adapter 接口
建议统一定义如下接口。

BrokerSessionService
负责：

login()

logout()

refreshToken()

isConnected()

getCurrentSession()
BrokerInstrumentService
负责：

search()

getInstrument()
BrokerQuoteService
负责：

getQuote()

getQuotes()
说明：

getQuote(symbol)

getQuotes(List<String>)

建议以后统一支持批量报价。

BrokerOrderService
负责：

previewOrder()

placeOrder()

cancelOrder()

replaceOrder()

getOrder()

listOrders()
BrokerPositionService
负责：

listPositions()

getPosition()
BrokerAccountService
负责：

getAccount()

listAccounts()
4. Adapter Factory
所有 Adapter 都通过 Factory 获取。

禁止：

new SchwabAdapter()
必须：

brokerFactory.getQuoteService(BrokerType.SCHWAB);
这样：

以后增加：

IBKR
Firstrade
无需修改业务代码。

5. Broker Context
建议增加：

BrokerContext
作用：

保存：

当前 Broker

当前 Account

当前 Session
例如：

BrokerContext

broker = SCHWAB

account = ****1234

session = xxxxxxxxx
所有 Service

通过：

BrokerContext
获得：

当前 Broker。

不要：

Controller

传来传去。

6. Adapter 生命周期
建议统一。

Disconnected

↓

Connecting

↓

Connected

↓

Refreshing Token

↓

Connected

↓

Disconnected
不要：

每家 Broker

自己管理状态。

统一：

BrokerSessionManager。

7. Adapter 异常
统一：

BrokerException
例如：

AuthenticationException

AuthorizationException

RateLimitException

NetworkException

BrokerApiException
Schwab

和

Tastytrade

全部转换。

不要：

把：

HTTP404

直接抛出来。

8. Adapter 返回值
所有接口：

统一：

Quote

Position

Order

Account
不要：

JSONObject
更不要：

Map<String,Object>
9. Adapter 日志
建议：

每次调用：

记录：

Broker

API

耗时

HTTP Status

RequestId

ErrorCode
例如：

[Broker]

SCHWAB

GET /quotes

Elapsed

82ms

Status

200
方便：

以后排查问题。

10. Adapter 包结构
建议：

quicktrade-broker-api
│
├── BrokerSessionService
├── BrokerQuoteService
├── BrokerOrderService
├── BrokerAccountService
├── BrokerPositionService
└── BrokerInstrumentService
Schwab：

quicktrade-broker-schwab
│
├── SchwabSessionService
├── SchwabQuoteService
├── SchwabOrderService
├── SchwabAccountService
└── SchwabPositionService
Tastytrade：

quicktrade-broker-tastytrade
│
├── TastySessionService
├── TastyQuoteService
├── TastyOrderService
├── TastyAccountService
└── TastyPositionService
11. Broker 能力（Capability）设计（建议新增）
这是我建议增加的一项设计，也是为了避免在业务层写大量 if (broker == SCHWAB)。

每个 Adapter 都声明自己的能力，例如：

能力	Schwab	Tastytrade
登录	✔	✔
股票报价	✔	✔
股票下单	✔	✔
期权下单	✔	✔（若启用）
批量报价	✔	✔
订单预览	✔	✔
修改订单	✔	✔
购买力	⚠️	✔
定义一个统一接口：

interface BrokerCapability {
    boolean supportsBuyingPower();
    boolean supportsOrderPreview();
    boolean supportsReplaceOrder();
    boolean supportsBatchQuote();
}
业务层可以这样写：

if (brokerCapability.supportsOrderPreview()) {
    ...
}
而不是：

if (broker == BrokerType.SCHWAB) {
    ...
}
这样，当以后新增 Broker 时，只需要实现自己的 Capability，不需要修改已有业务代码。

我建议下一份文档
完成 Adapter 规范之后，我建议进入：

《QuickTrade API 字段映射规范（API Mapping Specification）》

这份文档将逐字段定义：

QuickTrade Domain 字段名称

Schwab API 对应字段

Tastytrade API 对应字段

是否必填

转换规则（类型、单位、默认值）

这是 Adapter 实现时最直接的开发依据，也会让整个多 Broker 架构真正落地。


继续

很好，我认为现在正式进入最重要的一份开发文档。

前面的文档都是设计。

这一份开始，就是真正指导编码。

QuickTrade API 字段映射规范（API Mapping Specification）
Version 1.0

1. 文档目的
定义：

QuickTrade Domain Model ←→ Broker API

之间的映射关系。

以后：

所有 Adapter

必须按照这份文档实现。

不能：

自己定义字段。

例如：

不能：

Schwab

叫

lastPrice
Tastytrade

叫

mark
Vue

还要判断：

if (broker == SCHWAB)
这是禁止的。

Vue

永远只认识：

quote.lastPrice
2.
整体流程

Broker JSON

↓

Broker DTO

↓

Adapter

↓

QuickTrade Domain

↓

Service

↓

REST

↓

Vue
注意：

Broker DTO

不能：

进入：

Business。

3.
Quote Mapping

建议

统一

QuickTrade

Quote。

QuickTrade	Schwab	Tastytrade	类型	必填
symbol	symbol	symbol	String	✔
companyName	description	description	String	✔
exchange	exchange	exchange	String	✔
lastPrice	lastPrice	mark	BigDecimal	✔
bidPrice	bidPrice	bid	BigDecimal	✔
askPrice	askPrice	ask	BigDecimal	✔
bidSize	bidSize	bidSize	Integer	✔
askSize	askSize	askSize	Integer	✔
openPrice	openPrice	open	BigDecimal	✔
highPrice	highPrice	high	BigDecimal	✔
lowPrice	low	low	BigDecimal	✔
previousClose	closePrice	close	BigDecimal	✔
volume	totalVolume	volume	Long	✔
week52High	52WeekHigh	yearHigh	BigDecimal	✔
week52Low	52WeekLow	yearLow	BigDecimal	✔
以后：

Vue

全部：

quote.lastPrice
不知道：

Broker。

4.
Position Mapping

统一：

Position。

QuickTrade	Schwab	Tastytrade
symbol	symbol	symbol
quantity	longQuantity	quantity
averagePrice	averagePrice	averageOpenPrice
marketPrice	marketPrice	mark
marketValue	marketValue	marketValue
unrealizedPL	currentDayProfitLoss	unrealizedDayGain
realizedPL	realizedProfitLoss	realizedDayGain
5.
Account Mapping

统一：

Account。

QuickTrade	Schwab	Tastytrade
accountNumber	accountNumber	accountNumber
accountName	nickname	nickname
cashBalance	cashBalance	cashBalance
buyingPower	buyingPower	buyingPower
accountValue	liquidationValue	accountValue
netLiquidation	liquidationValue	netLiquidation
6.
Order Mapping

统一：

Order。

QuickTrade	Schwab	Tastytrade
orderId	orderId	id
symbol	symbol	symbol
side	instruction	side
quantity	quantity	quantity
price	price	price
orderType	orderType	orderType
timeInForce	duration	timeInForce
status	status	status
7.
Instrument Mapping

统一：

Instrument。

QuickTrade	Schwab	Tastytrade
symbol	symbol	symbol
companyName	description	description
exchange	exchange	exchange
assetType	assetType	instrumentType
currency	currency	currency
8.
统一 Enum Mapping

例如：

Schwab

BUY

SELL_SHORT

SELL
Tasty

可能：

Buy

Sell
QuickTrade

统一：

BUY

SELL
Adapter

负责转换。

不要：

Business

处理。

OrderType

统一：

MARKET

LIMIT

STOP

STOP_LIMIT
OrderStatus

统一：

NEW

WORKING

PARTIAL_FILLED

FILLED

CANCELLED

REJECTED

EXPIRED
AssetType

统一：

STOCK

ETF

OPTION

INDEX
9.
空值策略

例如：

Schwab

有：

BuyingPower
某些账户：

没有。

QuickTrade

统一：

BigDecimal.ZERO
或者：

Optional<BigDecimal>
不要：

NullPointerException。

10.
精度

建议：

所有金额：

统一：

BigDecimal
不要：

double
不要：

float
数量：

统一：

BigDecimal
因为：

以后：

Option

Fractional Share

都会支持。

11.
时间

统一：

所有时间：

UTC

保存。

UI：

转换：

用户时区。

不要：

数据库：

保存：

纽约时间。

不要：

Java：

保存：

东京时间。

统一：

UTC。

12.
Currency

统一：

USD

HKD

JPY
不要：

字符串：

$

USDollar

Dollar
统一：

ISO4217。

13.
统一转换器（建议新增）

我建议不要让每个 Adapter 自己写字段转换，而是统一采用 Mapper 设计。

例如：

Schwab Quote JSON
        │
        ▼
SchwabQuoteDTO
        │
        ▼
SchwabQuoteMapper
        │
        ▼
QuickTrade Quote
Tastytrade 同样：

TastyQuoteDTO
        │
        ▼
TastyQuoteMapper
        │
        ▼
QuickTrade Quote
这样有几个好处：

DTO 与 Domain 解耦；

映射逻辑集中管理，便于测试；

后续 API 字段变更时，只修改 Mapper，不影响业务层；

可以统一使用 MapStruct（或手写 Mapper）管理转换规则。

这里我要纠正前面文档中的一个地方
经过我们一路设计，我发现前面的 Broker Adapter 其实还可以再细分一层，这也是很多大型 Java 项目的做法。

建议结构调整为：

Schwab API
      │
      ▼
HTTP Client
      │
      ▼
Schwab DTO
      │
      ▼
Mapper
      │
      ▼
QuickTrade Domain
      │
      ▼
Domain Service
也就是说：

HTTP Client：只负责 HTTP 请求和响应；

DTO：只映射 Broker 原始 JSON；

Mapper：负责 DTO → QuickTrade Domain；

Domain Service：负责业务逻辑。

这样职责划分更清晰，也方便单元测试和未来维护。

我建议下一份文档
到这里，我建议不是马上设计数据库。

而是先完成：

《QuickTrade REST API 设计规范》

因为：

UI 已经确定；

Domain 已经确定；

Adapter 已经确定；

REST API 就成为前后端之间唯一契约。

等 REST API 定下来之后，再设计数据库，就不会出现数据库字段影响接口设计的问题，也能保证 Vue、Java 后端和 Broker Adapter 都围绕同一套接口开发。


继续

我建议这里我们做一个方向性的调整。

前面我们的文档越来越偏 Java 技术实现了。

但是REST API 设计还不是最重要的。

对于交易系统来说，真正应该先固定的是：

系统业务流程（Business Flow）

IBM、Bloomberg、TradeStation、IBKR 这些系统，都是先设计业务流程，再设计 API。

我建议重新调整文档顺序
文档	状态
UI设计	✅
功能一览	✅
API能力对照	✅
系统架构	✅
模块设计	✅
Domain Model	✅
Broker Adapter	✅
API Mapping	✅
业务流程(Business Flow)	← 下一份
REST API	后面
DB Design	后面
为什么？

因为：

REST API

其实就是：

业务流程

HTTP化。

第九份
QuickTrade Business Flow
Version 1.0

整个系统

其实只有几个业务。

不像 ERP。

交易软件

非常简单。

我建议：

整个系统

只有

6 个业务流程。

Flow 1
Broker Login

整个流程：

User
    │
点击 Login
    │
    ▼
Authentication Module
    │
    ▼
Broker Adapter
    │
    ▼
Broker API
    │
返回 Token
    │
    ▼
BrokerSession
    │
    ▼
加载默认 Account
    │
    ▼
进入首页
这里

完成：

登录

Token

Account

首页

不要：

立即：

加载：

100 个接口。

建议：

只加载：

Account

WatchList
其它：

按需加载。

Flow 2
Search Symbol

Search

↓

Broker Instrument API

↓

Instrument

↓

UI
这里：

不查数据库。

因为：

股票：

来自：

Broker。

Flow 3
Select WatchList

用户：

点击：

AAPL。

整个流程：

WatchList

↓

QuoteService

↓

Broker Quote

↓

Quote

↓

Vue
同时：

Order Entry

自动：

切换：

Symbol=AAPL
这里：

不要：

刷新：

Position。

不要：

刷新：

Order。

只刷新：

Quote。

Flow 4
Place Order

整个流程：

User

↓

Order Entry

↓

Validation

↓

Preview

↓

Broker

↓

Order Result

↓

Orders

↓

Position Refresh
注意：

这里：

Position

不要：

立即：

刷新：

整个页面。

建议：

Order Success

↓

Refresh Position

↓

Refresh Account

↓

Refresh Orders
这样：

不会：

重复刷新。

Flow 5
Portfolio Refresh

建议：

后台：

Timer。

例如：

Every 5 Seconds
刷新：

Account

↓

Position

↓

Orders
Quote

不要：

一起刷新。

Quote

独立。

Flow 6
Logout

Logout

↓

Broker Logout

↓

Token Remove

↓

Session Clear

↓

Login Page
不要：

只：

删：

Cookie。

页面生命周期
建议：

Home

打开：

Load WatchList

↓

Load Account

↓

Done
然后：

Quote

只有：

点击：

股票：

才加载。

不是：

进入：

首页：

全部：

Quote。

Quote 生命周期
No Symbol

↓

Select Symbol

↓

Loading

↓

Loaded

↓

Refreshing

↓

Loaded
不要：

Loading

↓

Loading

↓

Loading
一直：

Spinner。

Order 生命周期
建议：

Edit

↓

Validate

↓

Preview

↓

Confirm

↓

Submitting

↓

Completed
如果：

失败：

Submitting

↓

Failed

↓

Edit
Session 生命周期
Disconnected

↓

Connecting

↓

Connected

↓

Refreshing Token

↓

Connected

↓

Expired

↓

Disconnected
页面初始化
建议：

启动：

顺序：

Login

↓

Account

↓

WatchList

↓

Select First Symbol

↓

Quote

↓

Ready
这里：

自动：

选择：

第一只股票。

用户体验：

更好。

为什么这一份文档重要？
因为：

以后：

REST API

就是：

按照：

Flow

写。

例如：

Flow4：

Place Order

就会：

对应：

POST

/api/order/preview
↓

POST

/api/order
↓

GET

/api/orders
↓

GET

/api/positions
所以：

REST

其实：

只是：

Flow

HTTP 化。

我建议再增加两份非常重要的文档
经过我们已经完成的设计，我认为还缺 两份企业级交易系统几乎都会有的文档：

① 状态机设计（State Machine Specification）
专门定义：

Order 状态流转

Broker Session 状态流转

UI 页面状态流转

WebSocket 连接状态

避免状态判断散落在代码里。

② 时序图（Sequence Diagram）
针对几个核心流程绘制时序图，例如：

登录 Broker

查询报价

下单

撤单

刷新持仓

例如，下单流程可以表示为：

User
 │
 ▼
Vue UI
 │
 ▼
OrderController
 │
 ▼
OrderService
 │
 ▼
Broker Adapter
 │
 ▼
Broker API
 │
 ▼
OrderService
 │
 ▼
Vue UI
我的建议是：先完成《Business Flow》和《State Machine》这两份文档，再开始 REST API 和数据库设计。 因为交易系统最容易出问题的不是数据库，而是业务流程和状态管理。把这两部分先设计好，后续编码会顺畅得多。


继续 

我认为，我们现在已经不是在设计一个 Demo，而是在设计一个真正可以长期维护的交易平台。

所以我建议，从这里开始，文档要采用企业架构文档的写法，而不是继续罗列功能。

第十份文档
QuickTrade State Machine Specification（状态机设计规范）
Version 1.0

1. 文档目的
本规范定义 QuickTrade 中所有核心对象的生命周期（Lifecycle）和状态转换规则。

统一状态机可以保证：

UI、后端、Broker Adapter 对状态的理解一致；

避免大量 if/else 判断；

便于日志记录和问题排查；

为未来支持 WebSocket 推送做好准备。

2. Broker Session 状态机
状态定义
状态	说明
DISCONNECTED	未连接 Broker
CONNECTING	正在登录 Broker
CONNECTED	登录成功，可正常调用 API
REFRESHING	Token 刷新中
EXPIRED	Token 已失效
ERROR	登录失败或网络异常
状态流转
DISCONNECTED
      │
      ▼
CONNECTING
      │
      ├───────────────┐
      ▼               ▼
CONNECTED          ERROR
      │
      ▼
REFRESHING
      │
      ├───────────────┐
      ▼               ▼
CONNECTED         EXPIRED
      │
      ▼
DISCONNECTED
3. Quote 状态机
Quote 不是一直刷新。

只有：

用户正在查看。

状态
状态	说明
IDLE	未选择股票
LOADING	正在获取报价
READY	已获取最新报价
REFRESHING	周期刷新
ERROR	获取失败
IDLE
 │
 ▼
LOADING
 │
 ▼
READY
 │
 ▼
REFRESHING
 │
 ├───────────┐
 ▼           ▼
READY      ERROR
4. Order 状态机（UI）
注意：

这里不是 Broker。

而是：

UI。

状态	说明
EDITING	用户填写订单
VALIDATING	本地检查
PREVIEWING	Broker Preview
CONFIRMING	用户确认
SUBMITTING	提交订单
COMPLETED	下单成功
FAILED	下单失败
EDITING
    │
    ▼
VALIDATING
    │
    ▼
PREVIEWING
    │
    ▼
CONFIRMING
    │
    ▼
SUBMITTING
    │
 ┌──┴───────────┐
 ▼              ▼
COMPLETED     FAILED
                  │
                  ▼
              EDITING
5. Broker Order 状态机
Broker 返回的订单状态统一映射到 QuickTrade。

QuickTrade	含义
NEW	已创建
WORKING	挂单中
PARTIAL_FILLED	部分成交
FILLED	全部成交
CANCELLED	已撤单
REJECTED	被拒绝
EXPIRED	已过期
NEW
 │
 ▼
WORKING
 │
 ├─────────────┐
 ▼             ▼
PARTIAL      CANCELLED
 │
 ▼
FILLED
或：

WORKING
    │
    ▼
REJECTED
6. Position 状态机
Position 没有复杂状态。

建议：

状态	说明
LOADING	加载中
READY	已加载
REFRESHING	刷新中
ERROR	获取失败
7. WatchList 状态机
状态	说明
EMPTY	没有股票
READY	已加载
ADDING	添加股票
REMOVING	删除股票
REORDERING	排序
ERROR	操作失败
8. Application 状态机
整个程序：

STARTING
     │
     ▼
LOGIN
     │
     ▼
HOME
     │
     ▼
TRADING
     │
     ▼
LOGOUT
     │
     ▼
EXIT
9. 页面状态
建议：

Vue

不要：

大量：

loading=true
loading=false
建议：

统一：

enum PageState {

INIT,

LOADING,

READY,

EMPTY,

ERROR

}
所有页面：

统一。

10. 网络状态
建议：

统一：

ONLINE

OFFLINE

RECONNECTING

FAILED
以后：

WebSocket

直接：

使用。

11. 刷新状态
建议：

Quote

不要：

每秒：

REST。

统一：

STOPPED

STARTING

RUNNING

PAUSED
以后：

支持：

WebSocket：

不用：

改。

12. UI 状态管理建议（Vue）
建议所有页面使用统一状态模型，而不是每个页面自己维护不同变量。

例如：

PageState
├── INIT
├── LOADING
├── READY
├── EMPTY
└── ERROR
对应界面表现：

PageState	UI 表现
INIT	页面初始化
LOADING	Loading 动画
READY	显示数据
EMPTY	空状态提示
ERROR	错误提示 + 重试按钮
这样所有页面（WatchList、Quote、Orders、Portfolio）都遵循同一套规范。

13. 事件（Event）规范（建议新增）
状态机最好配套统一事件，而不是直接修改状态。

例如：

Event	触发
LOGIN_SUCCESS	登录成功
LOGIN_FAILED	登录失败
SYMBOL_SELECTED	用户选择股票
QUOTE_REFRESHED	报价更新
ORDER_SUBMITTED	订单提交成功
ORDER_REJECTED	订单被拒绝
POSITION_UPDATED	持仓刷新完成
SESSION_EXPIRED	Token 失效
状态变化由事件驱动，而不是页面直接修改状态。

我建议下一步：开始真正的接口设计
到这里，我认为 QuickTrade 的整体架构已经基本稳定：

✅ UI 布局

✅ 功能定义

✅ Broker 能力分析

✅ Domain Model

✅ 模块设计

✅ Broker Adapter

✅ 字段映射

✅ Business Flow

✅ State Machine

下一份文档建议进入《QuickTrade REST API Specification》。

这份文档将定义：

REST URL 规范（例如 /api/v1/orders）。

请求/响应 JSON 结构。

错误码规范。

分页、排序、过滤规则。

与 Vue 前端的完整接口契约。

完成后，前端、后端、Broker Adapter 三方就可以并行开发，而数据库设计也可以根据这些接口自然落地。


继续

我认为我们现在已经完成了系统设计（System Design），下面应该进入接口设计（Interface Design）。

不过，我建议不要按照普通 CRUD 的方式设计 REST API。

交易软件和后台管理系统不一样。

例如：

后台：

POST /user

PUT /user

DELETE /user
交易软件：

POST /order/preview

POST /order

POST /order/cancel

POST /order/replace
它更像是Command API（命令式 API）。

QuickTrade REST API Specification
Version 1.0

1. REST API 设计原则
整个 QuickTrade API 建议遵循以下原则：

原则一：UI 只调用 QuickTrade API
Vue 永远不知道：

Schwab

Tastytrade
Vue 只调用：

/api/v1/*
原则二：Broker API 不暴露
禁止：

/api/schwab/quote

/api/tastytrade/order
整个系统只有：

/api/v1
原则三：统一 JSON
例如：

{
  "code": 0,
  "message": "Success",
  "data": {}
}
不要：

每个接口：

不同格式。

2.
API 分类

建议：

Authentication

Market

Trading

Portfolio

WatchList

User

System
与模块保持一致。

Authentication
Base：

/api/v1/auth
Login

POST

/auth/login
请求：

{
    "broker":"SCHWAB",
    "username":"xxx",
    "password":"xxx"
}
返回：

{
    "sessionId":"xxxxx",
    "broker":"SCHWAB"
}
Logout

POST

/auth/logout
Refresh

POST

/auth/refresh
Current Session

GET

/auth/session
Market
Base：

/api/v1/market
Search

GET

/market/search
参数：

keyword=AAPL
Quote

GET

/market/quote
参数：

symbol=AAPL
Batch Quote

建议：

POST

/market/quotes
请求：

[
"AAPL",

"TSLA",

"NVDA"
]
Instrument

GET

/market/instrument
Trading
Base：

/api/v1/trading
Preview

POST

/trading/order/preview
Submit

POST

/trading/order
Cancel

POST

/trading/order/cancel
Replace

POST

/trading/order/replace
Orders

GET

/trading/orders
Order Detail

GET

/trading/order/{orderId}
Portfolio
Base：

/api/v1/portfolio
Account

GET

/account
Position

GET

/positions
Single Position

GET

/position/{symbol}
WatchList
Base：

/api/v1/watchlist
List

GET

/watchlist
Create

POST

/watchlist
Delete

DELETE

/watchlist/{id}
Move

PUT

/watchlist/order
User
Base：

/api/v1/user
Preference

GET

/user/preference
Update Preference

PUT

/user/preference
System
GET

/system/version
GET

/system/health
Response
统一：

{
    "code":0,
    "message":"Success",
    "timestamp":"2026-07-11T10:30:00Z",
    "data":{}
}
Error Code
建议：

Code	说明
0	Success
1000	Authentication Failed
1001	Token Expired
2000	Broker Connection Failed
3000	Order Rejected
3001	Order Validation Failed
4000	Quote Not Found
5000	Internal Error
不要：

HTTP

里面：

放：

所有错误。

HTTP

建议：

只：

200

400

401

403

404

500
业务：

全部：

Code。

Version
建议：

/api/v1
以后：

/api/v2
不会：

影响：

Vue。

我建议对 API 设计做一个重要优化
结合我们前面定义的 Application Service + Domain Service 架构，我建议 REST API 也保持**资源（Resource）+ 动作（Action）**的一致性。

例如：

功能	建议 API
获取报价	GET /api/v1/market/quotes/{symbol}
批量报价	POST /api/v1/market/quotes
订单预览	POST /api/v1/orders/preview
下单	POST /api/v1/orders
撤单	POST /api/v1/orders/{orderId}/cancel
改单	POST /api/v1/orders/{orderId}/replace
查询订单	GET /api/v1/orders/{orderId}
查询持仓	GET /api/v1/portfolio/positions
查询账户	GET /api/v1/portfolio/account
这样 URL 更统一，也更符合 REST 风格，同时保留交易系统命令式操作（如 Preview、Cancel、Replace）的特点。

接下来建议进入数据库设计，但不是直接画表
数据库设计建议分成三份文档：

数据库设计原则（哪些数据存、哪些数据不存）。

ER 图与表结构设计（User、WatchList、SystemConfig 等）。

索引、缓存、Redis 设计。

这样数据库会完全服务于我们已经确定的架构，而不会反过来约束业务。根据我们之前讨论的原则，Broker 的实时数据（Quote、Order、Position、Account）原则上不落库，数据库主要保存 QuickTrade 自身的数据（用户偏好、自选股、配置、日志等），这会让整个系统更轻量，也更容易维护。


继续

我认为我们现在进入数据库设计，但是我要推翻一个传统交易软件的设计。

为什么？
很多交易软件数据库长这样：

Account

Position

Order

Trade

Quote

WatchList

User
这种设计是 错误的。

为什么？

因为：

Schwab、Tastytrade 已经保存了这些数据。

QuickTrade 不应该再保存。

否则：

Broker

↓

QuickTrade DB

↓

Vue
以后：

Broker 数据

不同步。

你会碰到：

为什么 Broker 已经成交了，

QuickTrade 还是 Working？

这是所有交易软件都会遇到的问题。

所以：

QuickTrade 应该是：

Broker

↓

QuickTrade Memory

↓

Vue
不是：

Broker

↓

DB

↓

Vue
所以数据库应该重新定义。
数据库原则
一句话：

数据库只保存 QuickTrade 自己的数据。

Broker 的数据：

全部实时获取。

哪些数据可以存？
建议：

User

WatchList

System Config

Preference

Log
只有这些。

哪些不能存？
建议：

Quote

Order

Position

Account

Balance
全部不要存。

因为：

Broker 已经有。

所以：
数据库其实很小。

QuickTrade Database
建议：

quicktrade
只有几个表。

1
qt_user

不是：

Broker 用户。

只是：

QuickTrade。

建议：

字段	类型
id	bigint
username	varchar
nickname	varchar
language	varchar
timezone	varchar
theme	varchar
default_broker	varchar
create_time	datetime
注意：

不要：

password
因为：

登录：

Broker。

2
qt_watchlist

这是：

最重要。

字段	类型
id	bigint
user_id	bigint
list_name	varchar
symbol	varchar
sort_order	int
remark	varchar
create_time	datetime
以后：

支持：

多个：

WatchList。

例如：

AI

ETF

China

Dividend
不用：

改表。

3
qt_preference

建议：

以后：

所有：

UI

配置：

全部：

这里。

例如：

字段	类型
id	bigint
user_id	bigint
key	varchar
value	text
例如：

Theme

Dark
Language

English
以后：

不用：

改数据库。

4
qt_system_config

建议：

系统配置。

例如：

| key | value |

Version

1.0
RefreshInterval

2
MarketTimezone

America/New_York
5
qt_log

建议：

这里只保存：

QuickTrade。

例如：

Login

Logout

Setting

WatchList
不要：

Broker API

Log。

Broker

Log

应该：

logback。

6
qt_favorite

以后：

收藏。

例如：

Favorite Order

Favorite Symbol

Favorite Strategy
我建议增加两张表（这是 V1 就值得考虑的）
qt_broker_account
虽然 Account 信息不落库，但是用户绑定过哪些 Broker 账户 建议保存。

例如：

字段	类型	说明
id	bigint	主键
user_id	bigint	QuickTrade 用户
broker	varchar	SCHWAB / TASTYTRADE
account_number_masked	varchar	掩码账户（如 *****1234）
account_alias	varchar	用户自定义名称（如“美股主账户”）
is_default	boolean	默认账户
create_time	datetime	创建时间
注意：

不保存 Access Token。

不保存 Refresh Token。

不保存 Broker 密码。

这里只保存 QuickTrade 自己需要的显示和偏好信息。

qt_recent_symbol
很多交易软件都会记住最近查看的股票。

建议：

字段	类型
id	bigint
user_id	bigint
symbol	varchar
last_view_time	datetime
这样：

下次：

打开：

QuickTrade。

自动：

恢复：

最后：

查看：

股票。

Redis 才是真正的数据中心
结合我们前面的架构，我建议：

Vue

↓

REST

↓

Application Service

↓

Redis

↓

Broker API
Redis

缓存：

Broker Session

Market Calendar

Instrument

Exchange

Rate Limit
如果：

以后：

WebSocket。

Redis

还能：

做：

Quote Publish

↓

Multiple Users
这样：

Broker

只请求：

一次。

最后，我建议调整整个数据库理念
以前很多交易系统都是：

Database First
而 QuickTrade 应该是：

Broker First

↓

Memory First

↓

Redis Cache

↓

Database Last
数据库只负责QuickTrade 自己的数据，Broker 才是交易数据的唯一权威来源（Single Source of Truth）。

接下来我建议进入最后一份总体设计文档
到这里，我们实际上已经完成了：

UI

功能

架构

Domain

Adapter

API

Business Flow

State Machine

Database

还缺最后一份：

《QuickTrade V1 开发规范（Development Specification）》

这份文档不是功能，而是统一整个项目的编码规则，包括：

Java 包结构规范

DTO / Domain / Entity 命名规范

Controller / Service / Repository 职责

日志规范

异常处理规范

单元测试规范

Git 分支规范

Maven 模块规范

我建议把它作为整个设计文档的收尾。完成之后，这套文档就足以作为 QuickTrade V1 的正式开发蓝图。


继续

我觉得，现在我们已经不是在写"设计文档"了。

而是在制定 QuickTrade Coding Standard（开发规范）。

这一份文档会直接决定项目以后会不会乱。

根据我们前面几十份设计，我建议整个项目采用阿里巴巴 + Spring Boot + 金融交易系统的开发规范。

QuickTrade V1 Development Specification
Version 1.0

1. 开发原则（Core Principles）
整个 QuickTrade 必须遵循以下原则：

1.1 Broker Independence（券商无关）
业务代码不能依赖任何 Broker。

禁止：

if (broker == SCHWAB) {
    ...
}
正确：

BrokerQuoteService service = brokerFactory.getQuoteService(brokerType);
Quote quote = service.getQuote(symbol);
Broker 差异只能出现在 Adapter 层。

1.2 Single Responsibility（单一职责）
一个类只做一件事情。

例如：

QuoteController
只负责：

Quote API。

不要：

QuoteOrderController
1.3 Domain First
所有业务：

围绕：

Domain。

例如：

Quote

Order

Position

Account
不要：

围绕：

Broker JSON。

1.4 Interface First
Business

全部：

调用：

Interface。

例如：

BrokerOrderService
不要：

new SchwabOrderService()
2.
Package

规范

建议：

com.quicktrade
│
├── common
├── config
├── controller
├── service
├── domain
├── repository
├── adapter
├── broker
├── websocket
├── exception
├── util
└── constant
Broker

建议：

broker
│
├── api
├── schwab
└── tastytrade
以后：

增加：

ibkr
不用：

改。

3.
Controller

规范

Controller

只负责：

HTTP

↓

Service
禁止：

QuoteController

↓

Broker API
Controller

不要：

计算：

业务。

不要：

SQL。

不要：

Redis。

4.
Application Service

Application Service

负责：

业务流程。

例如：

Login：

Authentication

↓

Load Account

↓

Load WatchList
Order：

Validate

↓

Preview

↓

Submit
Application Service

可以：

调用：

多个：

Domain Service。

5.
Domain Service

例如：

QuoteService

OrderService

AccountService

WatchListService
一个：

Service

对应：

一个：

Domain。

6.
Repository

Repository

只负责：

数据库。

不要：

HTTP。

不要：

Redis。

建议：

WatchListRepository

PreferenceRepository
7.
DTO

Broker

DTO：

例如：

SchwabQuoteDTO

TastyQuoteDTO
只能：

Adapter

使用。

不要：

Controller

返回：

DTO。

Controller

返回：

Quote
不是：

SchwabQuoteDTO
8.
Entity

建议：

Entity

只对应：

数据库。

例如：

WatchListEntity

PreferenceEntity
不要：

QuoteEntity
因为：

Quote

不存数据库。

9.
命名规范

Domain：

Quote

Order

Position
DTO：

QuoteDTO
Entity：

WatchListEntity
Request：

PlaceOrderRequest
Response：

PlaceOrderResponse
不要：

Bean

VO

PO

Model
混着来。

10.
Exception

统一：

BusinessException
↓

BrokerException
↓

ValidationException
↓

SystemException
Controller

统一：

GlobalExceptionHandler
11.
Log

建议：

统一：

INFO

WARN

ERROR
不要：

Debug：

上线。

Order

单独：

order.log
Broker

单独：

broker.log
Authentication

单独：

auth.log
12.
JSON

统一：

camelCase。

例如：

{
    "lastPrice":210.55
}
不要：

snake_case。

13.
DateTime

统一：

Java：

Instant
或：

OffsetDateTime
数据库：

UTC。

Vue：

转换：

Local Time。

14.
Money

全部：

BigDecimal
不要：

double
15.
Enum

全部：

Enum。

不要：

Magic String。

例如：

BUY

SELL
不要：

"BUY"
16.
REST

统一：

ApiResponse<T>
例如：

ApiResponse<Quote>

ApiResponse<List<Order>>
不要：

Map。

17.
Configuration

建议：

application.yml

application-dev.yml

application-test.yml

application-prod.yml
Broker：

broker-schwab.yml

broker-tastytrade.yml
18.
Redis

Key

统一：

QT:
例如：

QT:SESSION

QT:CALENDAR

QT:INSTRUMENT
以后：

容易：

管理。

19.
Git

建议：

main

develop

feature/*

release/*

hotfix/*
不要：

所有人：

直接：

main。

20.
单元测试

建议：

每个：

Service

都有：

Test。

例如：

QuoteServiceTest

OrderServiceTest
Broker

Mock。

不要：

直接：

调用：

真实：

Broker。

21. Maven 模块规范（建议补充）
结合我们之前确定的模块，我建议统一如下：

quicktrade-parent
│
├── quicktrade-common
├── quicktrade-core
├── quicktrade-api
├── quicktrade-admin
├── quicktrade-broker-api
├── quicktrade-broker-schwab
├── quicktrade-broker-tastytrade
└── quicktrade-web
依赖方向必须保持单向：

web
  │
api
  │
core
  │
broker-api
  │
broker-*
禁止循环依赖。

22. 代码审查（Code Review）规范（建议新增）
所有合并到 develop 或 main 的代码建议遵循以下检查项：

检查项	要求
是否直接调用 Broker SDK	❌ 禁止
是否出现 if (broker == ...)	❌ 禁止（Adapter 层除外）
是否返回 Broker DTO	❌ 禁止
是否使用 Domain Model	✅ 必须
是否有单元测试	✅ 建议
是否记录关键日志	✅ 必须（登录、下单、异常）
是否影响公共接口	⚠️ 需评审
我建议在这里正式结束总体设计阶段
经过这一系列文档，我们实际上已经形成了一套完整的企业级设计体系：

产品设计（UI、功能、业务流程）

架构设计（模块、Domain、Adapter）

接口设计（REST、Mapping）

数据设计（数据库、Redis）

开发规范（编码、命名、测试、Git）

接下来，我建议不要再继续写设计文档，而是进入真正的开发阶段。

我的建议开发顺序
阶段	内容
Phase 1	创建 Maven 多模块工程（quicktrade-parent）
Phase 2	建立 quicktrade-common 与 quicktrade-domain（Domain Model、Enum、ApiResponse）
Phase 3	定义 quicktrade-broker-api 接口（所有 Broker Interface）
Phase 4	实现 quicktrade-broker-tastytrade（建议先做 Tastytrade，因为认证流程相对简单）
Phase 5	实现 quicktrade-broker-schwab
Phase 6	实现 quicktrade-core（Application Service、Domain Service）
Phase 7	开发 REST API（Spring Boot Controller）
Phase 8	开发 Vue UI，并接入 REST API
Phase 9	集成测试与性能优化
我还有一个建议（也是整个项目最后一个架构建议）
在我们之前的文档中，Domain 放在 quicktrade-core 里。我建议调整为独立模块：

quicktrade-parent
│
├── quicktrade-common
├── quicktrade-domain      ← 新增
├── quicktrade-core
├── quicktrade-api
├── quicktrade-admin
├── quicktrade-broker-api
├── quicktrade-broker-schwab
├── quicktrade-broker-tastytrade
└── quicktrade-web
理由：

Quote、Order、Position、Account 等领域模型是整个系统共享的，不属于任何具体业务模块。

quicktrade-core、quicktrade-broker-*、quicktrade-api 都可以依赖 quicktrade-domain，避免重复定义 DTO 或循环依赖。

未来增加新的 Broker（IBKR、Firstrade 等）时，可以直接复用统一的 Domain Model。

我认为，这是整个 QuickTrade 架构中最后一个值得调整的地方，也是最符合长期维护和扩展性的模块划分。


继续

我认真把我们前面所有内容重新梳理了一遍。

我认为，真正开始写代码之前，还有一份比代码更重要的文档。

这份文档很多团队都会忽略，但实际上它决定了以后项目是否容易维护。

QuickTrade V1 项目目录规范（Project Structure Specification）
这份文档不是讲 Java，而是讲整个项目应该长什么样。

我建议按照下面的结构组织，而不是把所有内容都塞进一个仓库根目录。

1. 整体目录
quicktrade/
│
├── docs/                         # 项目文档
│   ├── 01-ui/
│   ├── 02-requirements/
│   ├── 03-architecture/
│   ├── 04-api/
│   ├── 05-database/
│   ├── 06-deployment/
│   └── images/
│
├── backend/                      # Java 后端
│   ├── quicktrade-parent/
│   └── pom.xml
│
├── frontend/                     # Vue 前端
│   ├── web/
│   └── package.json
│
├── scripts/                      # 构建、部署脚本
│
├── docker/                       # Docker 文件
│
├── nginx/                        # Nginx 配置
│
├── sql/                          # 数据库脚本
│
├── tools/                        # 开发辅助工具
│
└── README.md
这样做的好处：

文档、代码、部署分离；

前后端互不影响；

CI/CD 更容易配置。

2. docs 目录建议
不要把所有设计文档放在一个目录。

建议按主题分类。

docs/
│
├── 01-ui/
│
│   UI设计说明.md
│   页面流程.md
│
├── 02-requirements/
│
│   功能一览.md
│   Broker API 对照.md
│
├── 03-architecture/
│
│   系统架构.md
│   模块设计.md
│   Domain Model.md
│   Broker Adapter.md
│   State Machine.md
│
├── 04-api/
│
│   REST API.md
│   API Mapping.md
│
├── 05-database/
│
│   数据库设计.md
│   Redis设计.md
│
└── 06-deployment/
    部署说明.md
以后维护成本非常低。

3. backend 建议
Java 工程不要直接放仓库根目录。

建议：

backend/
    quicktrade-parent/
以后：

backend/
下面还可以放：

python-tools/

gateway/

batch/
4. frontend 建议
不要：

vue/
建议：

frontend/web
以后：

frontend/mobile

frontend/admin
都可以共存。

5. sql 建议
建议：

sql/

001-init.sql

002-watchlist.sql

003-index.sql

004-data.sql
不要：

create.sql
一个几千行。

6. scripts
建议：

scripts/

build.sh

deploy.sh

backup.sh

start.sh

stop.sh
Windows：

build.bat
7. Docker
建议：

docker/

backend/

frontend/

redis/

mysql/
以后：

Docker Compose。

8. Git Ignore
建议：

统一。

例如：

target/

.idea/

.vscode/

node_modules/

logs/
9. README
建议：

README

不要：

只有一句：

QuickTrade
建议：

包括：

项目介绍

架构图

模块说明

环境要求

快速启动

开发规范

常见问题

10. 版本规划（Roadmap）
这一点是我们之前一直没有写，但我建议现在就固定下来。

V1（当前）
目标：实现稳定、统一的股票交易客户端。

范围：

Schwab Adapter

Tastytrade Adapter

登录

行情

下单

持仓

账户

WatchList

不包含：

K 线

新闻

Scanner

Alert

Strategy

Heat Map

V2
在 V1 稳定后增加：

Alert（价格提醒）

Scanner（条件扫描）

多 WatchList Folder

WebSocket 推送（如果 Broker 能力允许）

更丰富的用户偏好

V3
扩展平台能力：

新 Broker（IBKR、Firstrade 等）

多账户同时登录

插件式 Broker Adapter

策略接口

历史分析（如果接入第三方行情）

11. 架构冻结（Architecture Freeze）
这是企业项目常见的一步，我建议在开始编码前明确。

冻结内容：

UI 模块划分

Domain Model

REST API 路径

Broker Adapter 接口

枚举定义

数据库表结构（QuickTrade 自有数据）

允许调整：

内部实现

性能优化

日志格式

缓存策略

这样可以避免开发过程中不断修改基础设计。

12. 我建议增加一个非常重要的原则
Single Source of Truth（唯一数据来源）
这是整个 QuickTrade 最核心的架构原则。

数据	唯一来源
Quote	Broker API
Order	Broker API
Position	Broker API
Account	Broker API
WatchList	QuickTrade DB
User Preference	QuickTrade DB
System Config	QuickTrade DB
Session	Redis / 内存
任何数据都应该只有一个权威来源，不要在多个地方维护同一份数据，否则容易出现同步问题。

我认为，我们已经完成了 QuickTrade V1 的总体设计
从产品、架构到开发规范，已经形成了一套完整的蓝图。

我建议下一步不再写文档，而是进入真正的实施阶段。
建议按下面的顺序推进：

第一阶段：基础工程（约 1 周）
创建 Git 仓库与目录结构

创建 Maven 多模块工程

建立 quicktrade-domain

建立 quicktrade-common

建立 quicktrade-broker-api

配置 Spring Boot、日志、统一异常

第二阶段：Broker 接入（约 2～3 周）
完成 Tastytrade Adapter

完成 Schwab Adapter

编写 Adapter 单元测试

完成字段映射与能力验证

第三阶段：业务层（约 2 周）
Authentication

Market

Trading

Portfolio

WatchList

第四阶段：前端（约 2 周）
登录

主界面

WatchList

Quote

Order Entry

Portfolio

第五阶段：集成测试（约 1 周）
多 Broker 验证

下单流程验证

Token 刷新

异常恢复

性能测试

我建议，从下一次开始，我们就不要继续扩展设计文档了，而是正式进入代码实现。
第一步可以从 quicktrade-parent 的 Maven 工程和模块搭建 开始，然后逐步实现 quicktrade-domain、quicktrade-common 和 quicktrade-broker-api，让整个项目真正落地。


