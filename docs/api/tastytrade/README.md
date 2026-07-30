# Tastytrade API 中文说明书

> 本文档基于 tastytrade 官方开发者文档（Getting Started、API Guides、Order Submission / Order Management）整理，目的是把常用 REST API 按中文方式进行系统化说明，方便在 FlashTrade / QuickTrade 等项目里做接口对接与开发。

---

## 1. 说明范围

本说明书覆盖以下主要模块：

- OAuth2 授权与 Token
- 客户 / 账户信息
- 账户状态、持仓、余额、流水
- 订单提交、撤单、改单、复杂订单
- 保证金要求与 Dry Run
- Instrument / 合约 / 期权 / 加密货币信息
- 市场数据查询
- 实时 Streaming（简要说明）

> 如果你需要按接口完整对照实现，请参考 [full-api-reference.md](full-api-reference.md)，它已整理官方 OpenAPI 的全部可用路径。

---

## 2. 环境与基础约定

### 2.1 环境

tastytrade 提供两个环境：

- Sandbox（测试环境）
  - Base URL: https://api.cert.tastyworks.com
  - 详细说明见 [sandbox-env.md](sandbox-env.md)
- Production（生产环境）
  - Base URL: https://api.tastyworks.com

### 2.2 通用请求头

所有请求通常至少包含：

```http
Authorization: Bearer <access_token>
User-Agent: your-client/1.0
Content-Type: application/json
```

### 2.3 通用响应结构

绝大多数接口都是以下结构：

```json
{
  "data": {},
  "context": "/path/to/resource",
  "api-version": "v1"
}
```

其中：

- `data`: 返回的核心数据内容
- `context`: 请求上下文，通常是当前资源路径
- `api-version`: API 版本
- 部分接口还会带 `pagination` 字段，表示分页信息

### 2.4 通用状态码

- `200 OK`: 请求成功
- `400 Bad Request`: 参数错误
- `401 Unauthorized`: Token 失效或未授权
- `422 Unprocessable Entity`: 业务校验失败，例如订单不合法
- `5xx`: 服务端异常

---

## 3. OAuth2 授权与 Token

### 3.1 生成 OAuth Application

在 tastytrade 个人账户中创建 OAuth 应用，获得：

- `client_id`
- `client_secret`

### 3.2 生成个人授权 Grant

开发者可以在 my.tastytrade.com 中创建 Grant，获得 `refresh_token`。

### 3.3 获取 Access Token

#### 接口

```http
POST https://api.tastyworks.com/oauth/token
```

Sandbox 环境：

```http
POST https://api.cert.tastyworks.com/oauth/token
```

#### 请求

```http
Content-Type: application/x-www-form-urlencoded
```

```text
grant_type=refresh_token
refresh_token=<refresh_token>
client_secret=<client_secret>
```

#### 请求示例（curl）

```bash
curl -X POST "https://api.tastyworks.com/oauth/token" \
  -H "User-Agent: my-client/1.0" \
  -d "grant_type=refresh_token" \
  -d "refresh_token=YOUR_REFRESH_TOKEN" \
  -d "client_secret=YOUR_CLIENT_SECRET"
```

#### 响应

```json
{
  "access_token": "eyJhbGciOi...",
  "token_type": "Bearer",
  "expires_in": 900,
  "refresh_token": "..."
}
```

#### 字段说明

- `access_token`: 访问令牌，用于后续所有 API 请求
- `token_type`: 通常为 `Bearer`
- `expires_in`: 过期秒数，通常 15 分钟
- `refresh_token`: 可用于刷新 token

#### 使用方式

```http
Authorization: Bearer <access_token>
```

---

## 4. 客户与账户信息

### 4.1 获取当前客户信息

#### 接口

```http
GET /customers/me
```

#### 请求

```http
Authorization: Bearer <access_token>
```

#### 响应示例

```json
{
  "data": {
    "id": "me",
    "first-name": "John",
    "last-name": "Doe",
    "email": "johndoe@nowhere.com",
    "external-id": "C0000037211"
  },
  "context": "/customers/me"
}
```

### 4.2 获取当前客户下的账户列表

#### 接口

```http
GET /customers/me/accounts
```

#### 响应示例

```json
{
  "data": {
    "items": [
      {
        "account": {
          "account-number": "5WT00001",
          "nickname": "Individual",
          "account-type-name": "Individual",
          "margin-or-cash": "Margin",
          "is-futures-approved": true
        },
        "authority-level": "owner"
      }
    ]
  },
  "context": "/customers/me/accounts"
}
```

### 4.3 获取单个账户详情

#### 接口

```http
GET /customers/me/accounts/{account_number}
```

#### 请求示例

```bash
curl -X GET "https://api.tastyworks.com/customers/me/accounts/5WT00001" \
  -H "Authorization: Bearer <access_token>"
```

#### 响应示例

```json
{
  "data": {
    "account-number": "5WT00001",
    "nickname": "Individual",
    "margin-or-cash": "Margin",
    "is-futures-approved": true,
    "is-closed": false
  },
  "context": "/customers/me/accounts/5WT00001"
}
```

---

## 5. 账户状态、持仓与余额

### 5.1 获取账户交易状态

#### 接口

```http
GET /accounts/{account_number}/trading-status
```

#### 作用

用于判断账户是否允许下单、是否是 closing only、是否被冻结、是否允许加密货币交易等。

#### 响应示例

```json
{
  "data": {
    "account-number": "5WT00001",
    "is-closed": false,
    "is-frozen": false,
    "is-closing-only": false,
    "is-cryptocurrency-enabled": true,
    "options-level": "No Restrictions"
  },
  "context": "/accounts/5WT00001/trading-status"
}
```

### 5.2 获取账户持仓

#### 接口

```http
GET /accounts/{account_number}/positions
```

#### 作用

返回当前账户下所有持仓，适用于做账户总览、盈亏计算、持仓监控。

#### 响应示例

```json
{
  "data": {
    "items": [
      {
        "account-number": "5WT00000",
        "symbol": "AAPL",
        "instrument-type": "Equity",
        "quantity": "100",
        "quantity-direction": "Long",
        "average-open-price": "288.7",
        "close-price": "282.48"
      }
    ]
  },
  "context": "/accounts/5WT00000/positions"
}
```

#### 关键字段说明

- `symbol`: 标的代码
- `instrument-type`: 合约类型，如 Equity / Equity Option / Future
- `quantity`: 数量
- `quantity-direction`: 持仓方向，Long / Short / Zero
- `average-open-price`: 平均开仓价
- `close-price`: 前收盘价或当前市场价

### 5.3 获取账户余额

#### 接口

```http
GET /accounts/{account_number}/balances
```

#### 作用

返回余额和资金维度，例如现金余额、净清算价值、买入力、维持保证金等。

#### 响应示例

```json
{
  "data": {
    "account-number": "5WT00000",
    "cash-balance": "7218.997",
    "net-liquidating-value": "32520.734",
    "equity-buying-power": "45657.048",
    "derivative-buying-power": "22828.524",
    "cash-available-to-withdraw": "22828.53"
  },
  "context": "/accounts/5WT00000/balances"
}
```

### 5.4 获取余额快照

#### 接口

```http
GET /accounts/{account_number}/balance-snapshots
```

#### 查询参数

- `snapshot-date`: 日期，例如 `2023-01-01`
- `time-of-day`: `BOD` 或 `EOD`

#### 示例

```bash
curl -X GET "https://api.tastyworks.com/accounts/5WT00000/balance-snapshots?snapshot-date=2023-01-01&time-of-day=EOD" \
  -H "Authorization: Bearer <access_token>"
```

---

## 6. 账户流水与费用

### 6.1 获取账户流水

#### 接口

```http
GET /accounts/{account_number}/transactions
```

#### 查询参数

- `sort`: `Asc` / `Desc`
- `type`: 交易类型，例如 `Trade`, `Money Movement`, `Receive Deliver`
- `sub-type`: 子类型，例如 `Dividend`, `Fee`, `Deposit`
- `start-date`, `end-date`
- `instrument-type`
- `symbol`
- `underlying-symbol`

#### 响应示例

```json
{
  "data": {
    "items": [
      {
        "id": 252640963,
        "account-number": "5WT0001",
        "symbol": "KBWD",
        "instrument-type": "Equity",
        "transaction-type": "Receive Deliver",
        "description": "Received 1.68074 Long KBWD via Dividend",
        "value": "0.0",
        "value-effect": "None"
      }
    ]
  },
  "context": "/accounts/5WT0001/transactions"
}
```

### 6.2 获取单笔流水

#### 接口

```http
GET /accounts/{account_number}/transactions/{id}
```

#### 响应示例

```json
{
  "data": {
    "id": 250411000,
    "account-number": "5WT00001",
    "symbol": "DIV",
    "transaction-type": "Receive Deliver",
    "transaction-sub-type": "Dividend",
    "description": "Received 0.99302 Long DIV via Dividend"
  },
  "context": "/accounts/5WT00001/transactions/250411000"
}
```

### 6.3 获取当日总费用

#### 接口

```http
GET /accounts/{account_number}/transactions/total-fees
```

#### 查询参数

- `date`: 日期，默认为当天

#### 响应示例

```json
{
  "data": {
    "total-fees": "100.0",
    "total-fees-effect": "Debit"
  },
  "context": "/accounts/5WT00001/transactions/total-fees"
}
```

---

## 7. 订单管理

### 7.1 搜索订单

#### 接口

```http
GET /accounts/{account_number}/orders
```

#### 查询参数

- `start-date`, `end-date`
- `underlying-symbol`
- `status`
- `futures-symbol`
- `underlying-instrument-type`
- `sort`
- `per-page`, `page-offset`

#### 示例

```bash
curl -X GET "https://api.tastyworks.com/accounts/5WT00001/orders?status[]=Live&status[]=Filled&per-page=10" \
  -H "Authorization: Bearer <access_token>"
```

### 7.2 获取当日/当前活跃订单

#### 接口

```http
GET /accounts/{account_number}/orders/live
```

#### 作用

返回当天相关订单，包括 Live、Cancelled、Filled 等状态。注意：官方文档不建议频繁轮询，推荐使用 Account Streamer.

#### 响应示例

```json
{
  "data": {
    "items": [
      {
        "id": 54758826,
        "account-number": "5WT00001",
        "order-type": "Limit",
        "time-in-force": "GTC",
        "status": "Live",
        "price-effect": "Debit",
        "legs": [
          {
            "instrument-type": "Equity Option",
            "symbol": "QQQ   191115C00187000",
            "quantity": 1,
            "remaining-quantity": 1,
            "action": "Buy to Close",
            "fills": []
          }
        ]
      }
    ]
  },
  "context": "/accounts/5WT00001/orders/live"
}
```

### 7.3 Dry Run（仅校验，不真正下单）

#### 接口

```http
POST /accounts/{account_number}/orders/dry-run
```

#### 作用

校验订单是否合法、是否有足够买入力、是否会被拒绝。适合先做预检。

#### 请求体示例

```json
{
  "time-in-force": "Day",
  "order-type": "Limit",
  "price": 150.25,
  "price-effect": "Credit",
  "legs": [
    {
      "instrument-type": "Equity",
      "symbol": "AAPL",
      "quantity": 1,
      "action": "Sell to Close"
    }
  ]
}
```

#### 响应示例

```json
{
  "data": {
    "order": {
      "account-number": "5WT00001",
      "status": "Received",
      "order-type": "Limit"
    },
    "warnings": [],
    "buying-power-effect": {
      "impact": "2.302",
      "effect": "Debit"
    },
    "fee-calculation": {
      "total-fees": "2.302",
      "total-fees-effect": "Debit"
    }
  },
  "context": "/accounts/5WT00001/orders/dry-run"
}
```

### 7.4 提交订单

#### 接口

```http
POST /accounts/{account_number}/orders
```

#### 作用

真正发送订单到交易系统。

#### 请求示例：买入股票

```json
{
  "time-in-force": "Day",
  "order-type": "Market",
  "legs": [
    {
      "instrument-type": "Equity",
      "symbol": "AAPL",
      "quantity": 100,
      "action": "Buy to Open"
    }
  ]
}
```

#### 请求示例：买入期权

```json
{
  "time-in-force": "Day",
  "order-type": "Limit",
  "price": 1.09,
  "price-effect": "Debit",
  "legs": [
    {
      "instrument-type": "Equity Option",
      "symbol": "AAPL  230818C00197500",
      "quantity": 1,
      "action": "Buy to Open"
    }
  ]
}
```

#### 响应示例

```json
{
  "data": {
    "order": {
      "id": 771043,
      "account-number": "5WT0001",
      "status": "Routed",
      "order-type": "Limit",
      "legs": [
        {
          "instrument-type": "Equity Option",
          "symbol": "SPY   191018C00295000",
          "quantity": 1,
          "action": "Sell to Open",
          "fills": []
        }
      ]
    }
  },
  "context": "/accounts/5WT0001/orders/"
}
```

### 7.5 撤单

#### 接口

```http
DELETE /accounts/{account_number}/orders/{id}
```

#### 作用

取消已提交但尚未完全成交的订单。

#### 响应示例

```json
{
  "data": {
    "id": 12345,
    "account-number": "5WT00001",
    "status": "Cancel Requested",
    "order-type": "Limit"
  },
  "context": "/accounts/5WT00001/orders/12345"
}
```

### 7.6 改单（Cancel/Replace）

#### 接口

```http
PUT /accounts/{account_number}/orders/{id}
```

#### 作用

修改价格、时间有效期、订单类型等。注意：并非所有字段都允许修改，通常需要保持其余字段和原订单一致。

#### 请求示例

```json
{
  "time-in-force": "Day",
  "order-type": "Limit",
  "price": "3.0",
  "price-effect": "Credit",
  "legs": [
    {
      "instrument-type": "Equity Option",
      "symbol": "SPY   191018C00299000",
      "quantity": 1,
      "action": "Buy to Open"
    }
  ]
}
```

### 7.7 复杂订单（Bracket / OTO / OCO / OTOCO）

#### 提交复杂订单

```http
POST /accounts/{account_number}/complex-orders
```

#### 示例请求：OTOCO

```json
{
  "type": "OTOCO",
  "trigger-order": {
    "order-type": "Limit",
    "price": 157.97,
    "price-effect": "Debit",
    "time-in-force": "Day",
    "legs": [
      {
        "instrument-type": "Equity",
        "symbol": "AAPL",
        "action": "Buy to Open",
        "quantity": 100
      }
    ]
  },
  "orders": [
    {
      "order-type": "Limit",
      "price": 198.68,
      "price-effect": "Credit",
      "time-in-force": "GTC",
      "legs": [
        {
          "instrument-type": "Equity",
          "symbol": "AAPL",
          "action": "Sell to Close",
          "quantity": 100
        }
      ]
    }
  ]
}
```

#### 响应示例

```json
{
  "data": {
    "complex-order": {
      "id": 2000010530,
      "type": "OTOCO",
      "trigger-order": {
        "id": 2002750870,
        "status": "Contingent"
      },
      "orders": [
        {
          "id": 2002750871,
          "status": "Contingent"
        }
      ]
    }
  },
  "context": "/accounts/5WT00001/complex-orders"
}
```

#### 取消复杂订单

```http
DELETE /accounts/{account_number}/complex-orders/{id}
```

---

## 8. 保证金要求

### 8.1 获取账户保证金要求

#### 接口

```http
GET /margin/accounts/{account_number}/requirements
```

#### 作用

返回当前账户的保证金要求、维持保证金、买入力影响等信息。

#### 响应示例

```json
{
  "data": {
    "account-number": "5WT00001",
    "margin-calculation-type": "Reg T",
    "margin-requirement": "4337.831401355",
    "maintenance-requirement": "3559.174330645",
    "margin-equity": "500007967.877802711",
    "groups": []
  }
}
```

### 8.2 Margin Dry Run

#### 接口

```http
POST /margin/accounts/{account_number}/dry-run
```

#### 作用

测试某个订单对账户保证金和买入力的影响。

#### 请求示例

```json
{
  "account-number": "5WT00001",
  "time-in-force": "Day",
  "order-type": "Limit",
  "price": "186.99",
  "price-effect": "Debit",
  "underlying-symbol": "AAPL",
  "underlying-instrument-type": "Equity",
  "legs": [
    {
      "instrument-type": "Equity",
      "symbol": "AAPL",
      "quantity": 1,
      "action": "Buy to Open"
    }
  ]
}
```

---

## 9. Instruments / 合约信息

### 9.1 Equity（股票）

#### 获取股票列表

```http
GET /instruments/equities
```

#### 查询参数

- `symbol[]`
- `lendability`
- `is-index`
- `is-etf`

#### 示例

```bash
curl -X GET "https://api.tastyworks.com/instruments/equities?symbol[]=AAPL&symbol[]=TSLA" \
  -H "Authorization: Bearer <access_token>"
```

#### 响应示例

```json
{
  "data": {
    "items": [
      {
        "symbol": "AAPL",
        "instrument-type": "Equity",
        "listed-market": "XNAS",
        "is-fractional-quantity-eligible": true,
        "active": true
      }
    ]
  },
  "context": "/instruments/equities"
}
```

#### 获取活跃股票列表

```http
GET /instruments/equities/active
```

#### 获取单只股票信息

```http
GET /instruments/equities/{symbol}
```

---

### 9.2 Equity Option（股票期权）

#### 获取期权链（嵌套结构）

```http
GET /option-chains/{underlying_symbol}/nested
```

#### 示例

```bash
curl -X GET "https://api.tastyworks.com/option-chains/AAPL/nested" \
  -H "Authorization: Bearer <access_token>"
```

#### 响应示例

```json
{
  "data": {
    "items": [
      {
        "underlying-symbol": "AAPL",
        "expirations": [
          {
            "expiration-date": "2023-08-18",
            "strikes": [
              {
                "strike-price": "197.5",
                "call": "AAPL  230818C00197500",
                "put": "AAPL  230818P00197500"
              }
            ]
          }
        ]
      }
    ]
  },
  "context": "/option-chains/AAPL/nested"
}
```

#### 获取详细期权链

```http
GET /option-chains/{underlying_symbol}
```

#### 获取简化选项符号列表

```http
GET /option-chains/{underlying_symbol}/compact
```

#### 获取一组期权合约对象

```http
GET /instruments/equity-options
```

#### 获取单个期权对象

```http
GET /instruments/equity-options/{symbol}
```

---

### 9.3 Futures（期货）

#### 获取活跃期货合约

```http
GET /instruments/futures
```

#### 查询参数

- `symbol[]`
- `product-code[]`

#### 示例

```bash
curl -X GET "https://api.tastyworks.com/instruments/futures?product-code[]=ES" \
  -H "Authorization: Bearer <access_token>"
```

#### 获取单个期货合约

```http
GET /instruments/futures/{symbol}
```

#### 获取期货产品列表

```http
GET /instruments/future-products
```

#### 获取单个期货产品

```http
GET /instruments/future-products/{exchange}/{code}
```

---

### 9.4 Future Option（期货期权）

#### 获取期货期权链（嵌套）

```http
GET /futures-option-chains/{product_code}/nested
```

#### 获取详细期货期权链

```http
GET /futures-option-chains/{product_code}
```

#### 获取一组期货期权对象

```http
GET /instruments/future-options
```

#### 获取单个期货期权对象

```http
GET /instruments/future-options/{symbol}
```

#### 获取期货期权产品列表

```http
GET /instruments/future-option-products
```

#### 获取单个期货期权产品

```http
GET /instruments/future-option-products/{exchange}/{root_symbol}
```

---

### 9.5 Cryptocurrency（加密货币）

#### 获取支持的加密货币列表

```http
GET /instruments/cryptocurrencies
```

#### 获取单个加密货币信息

```http
GET /instruments/cryptocurrencies/{symbol}
```

#### 示例

```bash
curl -X GET "https://api.tastyworks.com/instruments/cryptocurrencies/BTC%2FUSD" \
  -H "Authorization: Bearer <access_token>"
```

---

### 9.6 Warrant（权证）

#### 获取权证列表

```http
GET /instruments/warrants
```

#### 获取单个权证

```http
GET /instruments/warrants/{symbol}
```

---

### 9.7 Quantity Decimal Precisions（数量精度）

#### 接口

```http
GET /instruments/quantity-decimal-precisions
```

#### 作用

用于确定某个 Instrument 的数量精度，例如加密货币可能支持小数位数量。

---

## 10. 市场数据

### 10.1 按类型查询行情

#### 接口

```http
GET /market-data/by-type
```

#### 查询参数

- `equity=AAPL,TSLA`
- `cryptocurrency=BTC/USD`
- `equity-option=SPY 250428P00355000`
- `index=SPX`
- `future=/CLM5`
- `future-option=/MESU5EX3M5 250620C6450`

#### 示例

```bash
curl -X GET "https://api.tastyworks.com/market-data/by-type?equity=AAPL,TSLA&cryptocurrency=BTC/USD" \
  -H "Authorization: Bearer <access_token>"
```

#### 响应示例

```json
{
  "data": {
    "items": [
      {
        "symbol": "AAPL",
        "instrument-type": "Equity",
        "bid": "210.55",
        "ask": "210.6",
        "mid": "210.575",
        "mark": "210.55",
        "last": "210.511"
      }
    ]
  }
}
```

---

## 11. 实时 Streaming（简要说明）

除了 REST 之外，tastytrade 还提供实时流：

- Streaming Market Data：实时行情
- Streaming Account Data：账户更新、订单状态变化

#### 使用建议

- 对于订单状态变更，优先使用 Account Streamer
- 对于实时价格，优先使用 Market Data Stream
- 避免对 `GET /orders/live` 做高频轮询

---

## 12. 常见订单字段说明

### 12.1 订单类型

- `Limit`: 限价单，必须包含 `price` 和 `price-effect`
- `Market`: 市价单，不能包含 `price` 或 `price-effect`
- `Stop`: 止损市价单
- `Stop Limit`: 止损限价单
- `Notional Market`: 按金额买卖，而非按数量

### 12.2 价格影响

- `Debit`: 资金支出
- `Credit`: 资金收入

### 12.3 Time In Force

- `Day`
- `GTC`
- `GTD`
- `Ext`
- `GTC Ext`
- `Ext Overnight`
- `GTC Ext Overnight`

### 12.4 Leg 字段

每个订单 leg 需要包含：

- `instrument-type`: 合约类型
- `symbol`: 合约代码
- `quantity`: 数量（加密货币允许小数）
- `action`: `Buy to Open` / `Sell to Open` / `Buy to Close` / `Sell to Close`

---

## 13. 典型开发流程建议

### 13.1 登录并获取账户列表

1. 获取 OAuth Token
2. 调用 `/customers/me`
3. 调用 `/customers/me/accounts`
4. 选择目标账户

### 13.2 获取账户视图

1. 调用 `/accounts/{account_number}/balances`
2. 调用 `/accounts/{account_number}/positions`
3. 调用 `/accounts/{account_number}/trading-status`

### 13.3 提交下单

1. 先调用 `/accounts/{account_number}/orders/dry-run`
2. 若通过，再调用 `/accounts/{account_number}/orders`
3. 通过 `/accounts/{account_number}/orders/live` 或 Streaming 监听状态

---

## 14. 备注

- 由于 tastytrade 官方接口会随时间更新，文档里的字段名称和示例可能与实际环境略有差异。
- 在生产环境中，建议使用正式 OAuth 应用和受控权限。
- 实盘交易会涉及实际风险，请务必先在 Sandbox 中测试。

---

## 15. 参考链接

- 官方 Getting Started: https://developer.tastytrade.com/getting-started/
- OAuth2: https://developer.tastytrade.com/api-guides/oauth/
- Account Status / Positions / Balances / Transactions
- Order Submission / Order Management
- Instruments / Market Data

如果你愿意，我也可以继续把这个文档再细化成“按模块拆分成多个文件”的版本，例如：

- auth.md
- account.md
- orders.md
- instruments.md
- market-data.md

这样后续维护和阅读会更方便。