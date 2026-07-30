可以，而且我建议不仅收集 API 文档，还整理成一份 **Schwab API Reference（类似 OpenAPI 的接口规范）**，后续开发会方便很多。

根据目前公开资料，Schwab 的个人开发者 API（2024 年 TD Ameritrade API 迁移后）主要分成几个部分：

| 模块              | Base Path        |
| --------------- | ---------------- |
| OAuth2          | `/v1/oauth`      |
| Trader API      | `/trader/v1`     |
| Market Data API | `/marketdata/v1` |
| Streaming API   | WebSocket        |

官方采用 **OAuth 2 Authorization Code Flow**，不再使用 E*TRADE 的 OAuth1.0a。Access Token 有效期约 30 分钟，Refresh Token 有效期约 7 天。([Reddit][1])

---

# 一、认证(Authentication)

## Authorization

```
GET https://api.schwabapi.com/v1/oauth/authorize
```

参数

```
client_id
redirect_uri
response_type=code
scope
```

返回

```
code
```

---

## 获取Token

```
POST /v1/oauth/token
```

Body

```
grant_type=authorization_code

code

redirect_uri
```

返回

```json
{
  "access_token": "...",
  "refresh_token": "...",
  "expires_in": 1800
}
```

---

## Refresh Token

```
POST /v1/oauth/token
```

```
grant_type=refresh_token
```

---

# 二、Account API

## 获取账户

```
GET /trader/v1/accounts
```

支持

```
fields=positions
```

返回

```json
[
  {
    "accountNumber": "...",
    "positions":[]
  }
]
```

---

## Account Numbers

Schwab 与 TD 最大区别：

不能直接使用 Account Number。

需要先调用

```
GET /trader/v1/accounts/accountNumbers
```

返回

```json
[
  {
    "accountNumber":"xxxx",
    "hashValue":"ABCD1234..."
  }
]
```

以后所有接口都使用

```
hashValue
```

而不是

```
accountNumber
```

这是 Schwab 新 API 最大变化之一。([Reddit][1])

---

## 单账户

```
GET /trader/v1/accounts/{hash}
```

---

# 三、Orders

获取订单

```
GET /trader/v1/accounts/{hash}/orders
```

---

下单

```
POST /trader/v1/accounts/{hash}/orders
```

Body

```json
{
  "orderType":"LIMIT",
  "price":"120.00",
  "session":"NORMAL",
  "duration":"DAY",
  "orderStrategyType":"SINGLE",
  "orderLegCollection":[]
}
```

---

取消订单

```
DELETE /orders/{orderId}
```

---

修改订单

```
PUT /orders/{orderId}
```

---

# 四、Transactions

```
GET /transactions
```

支持

```
type

startDate

endDate
```

---

# 五、Positions

```
GET /accounts?fields=positions
```

返回

```json
positions
```

---

# 六、Quotes

单个

```
GET /marketdata/v1/{symbol}/quotes
```

多个

```
GET /marketdata/v1/quotes
```

参数

```
symbols=AAPL,MSFT,NVDA
```

---

返回

```json
{
 "AAPL":{
   "bidPrice":...
   "askPrice":...
   "lastPrice":...
 }
}
```

---

# 七、Option Chain

```
GET /marketdata/v1/chains
```

参数很多：

```
symbol

contractType

strike

strikeCount

range

fromDate

toDate

strategy

optionType
```

这是整个 API 参数最多的接口之一。([GitHub][2])

---

# 八、Price History

```
GET /marketdata/v1/pricehistory
```

参数

```
symbol

periodType

period

frequencyType

frequency

startDate

endDate
```

返回

OHLCV

---

# 九、Market Hours

```
GET /marketdata/v1/markets
```

例如

```
markets=equity
```

---

# 十、Movers

```
GET /marketdata/v1/movers/{index}
```

例如

```
$DJI

$SPX

$COMPX
```

---

# 十一、Instruments

CUSIP 查询

```
GET /marketdata/v1/instruments/{cusip}
```

Symbol 查询

```
GET /marketdata/v1/instruments
```

---

# 十二、Option Expiration

```
GET /marketdata/v1/expirationchain
```

---

# 十三、User Preference

```
GET /trader/v1/userPreference
```

---

# 十四、Streaming API

WebSocket 登录后支持订阅：

```
LEVELONE_EQUITIES

LEVELONE_OPTIONS

LEVELONE_FUTURES

NASDAQ_BOOK

NYSE_BOOK

OPTIONS_BOOK

CHART_EQUITY

CHART_FUTURES

ACCOUNT_ACTIVITY
```

实时推送：

```
Quote

Trade

Bid

Ask

Volume

Order Update

Account Activity
```

---

# 十五、Order Builder（核心 JSON）

几乎所有订单都围绕：

```json
{
  "orderType":"",
  "session":"",
  "duration":"",
  "price":"",
  "orderStrategyType":"",
  "orderLegCollection":[]
}
```

复杂订单：

```
SINGLE

OCO

TRIGGER

TRIGGER+OCO

BRACKET
```

---

## 我可以帮你整理成完整的 API 参考文档

我可以把 Schwab API 整理成一份接近官方 SDK 的开发文档，包含：

* **约 80–100 个 REST 接口**（按模块分类）
* 每个接口的 **URL、Method、参数、响应结构**
* 完整的 **JSON Schema**
* 所有 **Order JSON 模板**（股票、期权、止损、Bracket、OCO 等）
* **Streaming API** 的消息格式
* OAuth2 流程与刷新机制
* **Java（Spring Boot）调用示例**
* 与 **E*TRADE API** 的一一对应关系（便于迁移）

整理完成后基本可以作为一份离线的 Schwab API 开发手册使用。

[1]: https://www.reddit.com/r/Schwab/comments/1sgglar/schwab_api_endpoint/?utm_source=chatgpt.com "Schwab API endpoint"
[2]: https://github.com/slimandslam/schwab-client-js/blob/main/docs/DeveloperReference.md?utm_source=chatgpt.com "schwab-client-js/docs/DeveloperReference.md at main · slimandslam/schwab-client-js · GitHub"

可以，而且我建议不要只是整理成一份笔记，而是做成一份**可以直接作为开发参考的 SDK Reference**。这样以后无论是 Java、Python、Go 还是 Node，都可以直接按照文档开发。

建议的文档结构如下（约 150～250 页）：

# Schwab Trader API Developer Reference

## Part 1 基础

### 1. API Overview

介绍

```
Base URL

Sandbox（如果有）

Production

Authentication

Rate Limit

HTTP Status

Error Code

Version
```

---

### 2. OAuth2

完整流程

```
Authorization Code

PKCE（如果支持）

Refresh Token

Token Expire

Revoke
```

包括

```
时序图

HTTP Request

HTTP Response

Java Example

Python Example

curl Example
```

---

### 3. Common Types

例如

```
AccountHash

OrderId

Cusip

Symbol

OptionSymbol

Session

Duration

AssetType

Instruction
```

全部整理。

---

# Part 2 Trader API

## Accounts

每一个接口独立一章。

例如

---

### Get Account Numbers

```
GET

/trader/v1/accounts/accountNumbers
```

#### Description

获取 Account Hash。

#### Request

Headers

```
Authorization
Accept
```

#### Response

```json
[
  {
    "accountNumber":"...",
    "hashValue":"..."
  }
]
```

#### Field Description

| Field         | Type   | Description |
| ------------- | ------ | ----------- |
| accountNumber | string | 原始账号        |
| hashValue     | string | API使用Hash   |

#### Java Example

#### Python Example

#### Error

```
401

403

500
```

---

所有 Accounts API 都这样整理。

包括

```
Accounts

Account

Preferences

Transactions

Balances

Positions
```

---

# Part 3 Orders

这是最大的章节。

每个接口：

```
Create Order

Replace Order

Cancel Order

Get Orders

Get Order

Orders by Account
```

然后建立一个章节

## Order JSON Reference

例如

Limit Order

```json
{
  ...
}
```

---

Market Order

```json
{
 ...
}
```

---

Stop

---

Stop Limit

---

Trailing Stop

---

OCO

---

Bracket

---

Trigger

---

Option Single

---

Option Vertical

---

Covered Call

---

Iron Condor

等等全部整理。

---

# Part 4 Market Data

每一个接口

```
Quotes

Quote

Price History

Movers

Market Hours

Option Chain

Expiration Chain

Instrument Search
```

每章包含

```
URL

Method

Parameters

Enum

Example

Response

Field Description
```

---

例如 Quote

```
GET

/marketdata/v1/{symbol}/quotes
```

参数

```
fields

indicative

entitlement
```

全部解释。

返回JSON逐字段解释。

---

# Part 5 Streaming API

重点整理。

包括

```
Login

Logout

Heartbeat

Subscribe

Unsubscribe
```

然后分别介绍

```
LEVELONE_EQUITIES

LEVELONE_OPTIONS

LEVELONE_FUTURES

BOOK

ACCOUNT_ACTIVITY

NEWS

CHART
```

每一种消息

```
JSON格式

字段解释

更新频率
```

---

# Part 6 Enumerations

官方API有大量Enum。

整理成统一章节。

例如

Session

```
NORMAL

AM

PM

SEAMLESS
```

---

Duration

```
DAY

GOOD_TILL_CANCEL

FILL_OR_KILL

IMMEDIATE_OR_CANCEL
```

---

Instruction

```
BUY

SELL

BUY_TO_OPEN

SELL_TO_CLOSE

SELL_SHORT

BUY_TO_COVER
```

等等全部列出来。

---

# Part 7 Error Codes

整理

```
400

401

403

404

409

422

429

500
```

每一种

```
什么时候出现

如何解决

官方Message

SDK建议处理方式
```

---

# Part 8 Complete Object Model

这是开发最有价值的部分。

例如

Order

```
Order
 ├── OrderLeg
 ├── Instrument
 ├── Execution
 ├── Activity
 ├── ChildOrder
```

全部对象展开。

每个字段

```
名称

类型

是否必须

说明

示例
```

---

# Part 9 SDK Examples（Java）

因为你之前一直在做 **Spring Boot / Maven 多模块项目**，这一部分可以重点整理成 Java SDK 风格。

例如：

```
SchwabClient

AccountService

OrderService

QuoteService

MarketDataService

StreamingClient

OAuthManager
```

每个接口提供完整调用示例，包括：

* 使用 `HttpClient` 或 `Spring WebClient` 发起请求
* OAuth2 Token 自动刷新
* 请求与响应对象（POJO）
* 异常处理
* 重试策略

---

## 附录

最后增加几个非常实用的索引：

* **所有 REST Endpoint 一览表**（URL、Method、功能）
* **所有 JSON 对象索引**
* **所有枚举索引**
* **与 TD Ameritrade API 的差异**
* **与 E*TRADE API 的差异**
* **开发踩坑与注意事项**（如 Account Hash 的使用、Token 生命周期、订单 JSON 构造等）

---

**最终目标**是形成一份类似官方 SDK 手册的文档，而不是简单的接口列表，具备以下特点：

* 完整覆盖 Trader API、Market Data API、Streaming API。
* 每个接口包含：功能、请求、响应、字段说明、错误码、示例。
* 完整整理所有数据模型（DTO）和枚举。
* 提供 Java/Spring Boot 实战示例，方便直接集成到项目中。
* 提供 API 迁移说明（尤其适合从 TD Ameritrade 或 E*TRADE 迁移）。
