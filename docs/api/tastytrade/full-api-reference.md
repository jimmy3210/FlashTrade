# Tastytrade 完整 API 参考

> 本文件按官方 OpenAPI 规范整理，覆盖当前 tastytrade 可用的主要 REST API 组。目的是帮助开发者在实装时快速判断“哪个 API 实现哪个功能”。

---

## 1. 环境与认证

- Sandbox: https://api.cert.tastyworks.com
- Production: https://api.tastyworks.com
- 认证方式: OAuth2 Bearer Token
- 请求头:
  - `Authorization: Bearer <access_token>`
  - `User-Agent: your-client/1.0`
  - `Content-Type: application/json`

---

## 2. 账户与客户

### 2.1 /api-quote-tokens

- 方法: GET
- 路径: `/api-quote-tokens`
- 作用: 获取用于行情流或实时行情的 token。

### 2.2 /customers/{customer_id}

- 方法: GET
- 路径: `/customers/{customer_id}`
- 作用: 获取指定客户基础信息。

### 2.3 /customers/{customer_id}/accounts

- 方法: GET
- 路径: `/customers/{customer_id}/accounts`
- 作用: 获取指定客户的账户列表。

### 2.4 /customers/{customer_id}/accounts/{account_number}

- 方法: GET
- 路径: `/customers/{customer_id}/accounts/{account_number}`
- 作用: 获取单个账户详情。

### 2.5 /customers/{customer_id}/orders

- 方法: GET
- 路径: `/customers/{customer_id}/orders`
- 作用: 查询当前客户下所有订单。

### 2.6 /customers/{customer_id}/orders/live

- 方法: GET
- 路径: `/customers/{customer_id}/orders/live`
- 作用: 查询当前客户下的活动订单。

---

## 3. 余额与持仓

### 3.1 /accounts/{account_number}/balances

- 方法: GET
- 路径: `/accounts/{account_number}/balances`
- 作用: 获取账户资金余额和买入力等。

### 3.2 /accounts/{account_number}/balances/{currency}

- 方法: GET
- 路径: `/accounts/{account_number}/balances/{currency}`
- 作用: 获取指定币种的余额信息。

### 3.3 /accounts/{account_number}/balance-snapshots

- 方法: GET
- 路径: `/accounts/{account_number}/balance-snapshots`
- 作用: 获取账户某日或某时点余额快照。

### 3.4 /accounts/{account_number}/positions

- 方法: GET
- 路径: `/accounts/{account_number}/positions`
- 作用: 获取账户当前持仓列表。

### 3.5 /accounts/{accountNumber}/net-liq/history

- 方法: GET
- 路径: `/accounts/{accountNumber}/net-liq/history`
- 作用: 获取账户净清算价值历史数据，用于画净值曲线。

---

## 4. Instrument / 合约查询

### 4.1 /instruments/equities

- 方法: GET
- 路径: `/instruments/equities`
- 作用: 查询股票列表。

### 4.2 /instruments/equities/active

- 方法: GET
- 路径: `/instruments/equities/active`
- 作用: 查询当前活跃股票列表。

### 4.3 /instruments/equities/{symbol}

- 方法: GET
- 路径: `/instruments/equities/{symbol}`
- 作用: 查询单只股票信息。

### 4.4 /instruments/search

- 方法: GET
- 路径: `/instruments/search`
- 作用: 按关键字搜索 Instrument，适合自动补全与模糊匹配。

### 4.5 /instruments/cryptocurrencies

- 方法: GET
- 路径: `/instruments/cryptocurrencies`
- 作用: 获取加密货币列表。

### 4.6 /instruments/cryptocurrencies/{symbol}

- 方法: GET
- 路径: `/instruments/cryptocurrencies/{symbol}`
- 作用: 获取单个加密货币信息。

### 4.7 /instruments/futures

- 方法: GET
- 路径: `/instruments/futures`
- 作用: 查询期货合约。

### 4.8 /instruments/futures/{symbol}

- 方法: GET
- 路径: `/instruments/futures/{symbol}`
- 作用: 查询单个期货合约信息。

### 4.9 /instruments/future-products

- 方法: GET
- 路径: `/instruments/future-products`
- 作用: 查询期货产品列表。

### 4.10 /instruments/future-products/{exchange}/{code}

- 方法: GET
- 路径: `/instruments/future-products/{exchange}/{code}`
- 作用: 查询指定交易所与产品代码的期货产品。

### 4.11 /instruments/future-option-products

- 方法: GET
- 路径: `/instruments/future-option-products`
- 作用: 查询期货期权产品列表。

### 4.12 /instruments/future-option-products/{exchange}/{root_symbol}

- 方法: GET
- 路径: `/instruments/future-option-products/{exchange}/{root_symbol}`
- 作用: 查询指定交易所与 root symbol 的期货期权产品。

### 4.13 /instruments/future-option-products/{root_symbol}

- 方法: GET
- 路径: `/instruments/future-option-products/{root_symbol}`
- 作用: 查询指定 root symbol 的期权产品，自动匹配交易所。

### 4.14 /instruments/future-options/{symbol}

- 方法: GET
- 路径: `/instruments/future-options/{symbol}`
- 作用: 查询单个期货期权合约。

### 4.15 /instruments/equity-options/{symbol}

- 方法: GET
- 路径: `/instruments/equity-options/{symbol}`
- 作用: 查询单个股票期权合约。

### 4.16 /instruments/warrants

- 方法: GET
- 路径: `/instruments/warrants`
- 作用: 查询权证列表。

### 4.17 /instruments/warrants/{symbol}

- 方法: GET
- 路径: `/instruments/warrants/{symbol}`
- 作用: 查询单个权证信息。

### 4.18 /instruments/quantity-decimal-precisions

- 方法: GET
- 路径: `/instruments/quantity-decimal-precisions`
- 作用: 查询各类 Instrument 的数量精度。

### 4.19 /option-chains/{symbol}

- 方法: GET
- 路径: `/option-chains/{symbol}`
- 作用: 查询指定标的的期权链。

### 4.20 /option-chains/{symbol}/compact

- 方法: GET
- 路径: `/option-chains/{symbol}/compact`
- 作用: 查询简化期权链，适合快速加载列表。

### 4.21 /option-chains/{symbol}/nested

- 方法: GET
- 路径: `/option-chains/{symbol}/nested`
- 作用: 查询完整嵌套期权链。

### 4.22 /futures-option-chains/{symbol}

- 方法: GET
- 路径: `/futures-option-chains/{symbol}`
- 作用: 查询期货期权链。

### 4.23 /futures-option-chains/{symbol}/nested

- 方法: GET
- 路径: `/futures-option-chains/{symbol}/nested`
- 作用: 查询嵌套期货期权链。

---

## 5. 订单与复杂订单

### 5.1 /accounts/{account_number}/orders

- 方法: GET
- 路径: `/accounts/{account_number}/orders`
- 作用: 搜索订单，支持按状态、时间、标的、分页等过滤。

### 5.2 /accounts/{account_number}/orders/live

- 方法: GET
- 路径: `/accounts/{account_number}/orders/live`
- 作用: 获取账户的当前活动订单。

### 5.3 /accounts/{account_number}/orders/{id}

- 方法: GET
- 路径: `/accounts/{account_number}/orders/{id}`
- 作用: 获取单笔订单详情。

### 5.4 /accounts/{account_number}/orders/dry-run

- 方法: POST
- 路径: `/accounts/{account_number}/orders/dry-run`
- 作用: 校验订单是否合法，不实际下单。

### 5.5 /accounts/{account_number}/orders

- 方法: POST
- 路径: `/accounts/{account_number}/orders`
- 作用: 提交订单。

### 5.6 /accounts/{account_number}/orders/{id}/dry-run

- 方法: POST
- 路径: `/accounts/{account_number}/orders/{id}/dry-run`
- 作用: 对单笔订单进行改单前的 Dry Run 检查。

### 5.7 /accounts/{account_number}/orders/{id}

- 方法: PUT / PATCH
- 路径: `/accounts/{account_number}/orders/{id}`
- 作用: 修改/替换订单。

### 5.8 /accounts/{account_number}/orders/{id}

- 方法: DELETE
- 路径: `/accounts/{account_number}/orders/{id}`
- 作用: 取消订单。

### 5.9 /accounts/{account_number}/complex-orders

- 方法: GET
- 路径: `/accounts/{account_number}/complex-orders`
- 作用: 查询复杂订单列表。

### 5.10 /accounts/{account_number}/complex-orders/live

- 方法: GET
- 路径: `/accounts/{account_number}/complex-orders/live`
- 作用: 查询活动复杂订单。

### 5.11 /accounts/{account_number}/complex-orders/{id}

- 方法: GET
- 路径: `/accounts/{account_number}/complex-orders/{id}`
- 作用: 查询复杂订单详情。

### 5.12 /accounts/{account_number}/complex-orders/dry-run

- 方法: POST
- 路径: `/accounts/{account_number}/complex-orders/dry-run`
- 作用: 校验复杂订单。

### 5.13 /accounts/{account_number}/complex-orders

- 方法: POST
- 路径: `/accounts/{account_number}/complex-orders`
- 作用: 提交复杂订单。

### 5.14 /accounts/{account_number}/complex-orders/{id}/dry-run

- 方法: POST
- 路径: `/accounts/{account_number}/complex-orders/{id}/dry-run`
- 作用: 复杂订单改动前校验。

### 5.15 /accounts/{account_number}/complex-orders/{id}

- 方法: PATCH
- 路径: `/accounts/{account_number}/complex-orders/{id}`
- 作用: 修改复杂订单。

### 5.16 /accounts/{account_number}/complex-orders/{id}

- 方法: DELETE
- 路径: `/accounts/{account_number}/complex-orders/{id}`
- 作用: 取消复杂订单。

---

## 6. 交易流水

### 6.1 /accounts/{account_number}/transactions

- 方法: GET
- 路径: `/accounts/{account_number}/transactions`
- 作用: 查询账户交易流水。

### 6.2 /accounts/{account_number}/transactions/{id}

- 方法: GET
- 路径: `/accounts/{account_number}/transactions/{id}`
- 作用: 查询单笔交易流水。

### 6.3 /accounts/{account_number}/transactions/total-fees

- 方法: GET
- 路径: `/accounts/{account_number}/transactions/total-fees`
- 作用: 查询当日费用汇总。

---

## 7. 市场数据与行情

### 7.1 /market-data/by-type

- 方法: GET
- 路径: `/market-data/by-type`
- 作用: 查询多类型行情。

---

## 8. 符号搜索

### 8.1 /symbols/search/{symbol}

- 方法: GET
- 路径: `/symbols/search/{symbol}`
- 作用: 按符号或部分符号搜索交易标的。

---

## 9. 市场指标

### 9.1 /market-metrics

- 方法: GET
- 路径: `/market-metrics`
- 作用: 查询市场指标数据，例如 IV、历史波动率等。

### 9.2 /market-metrics/historic-corporate-events/dividends/{symbol}

- 方法: GET
- 路径: `/market-metrics/historic-corporate-events/dividends/{symbol}`
- 作用: 查询指定标的的历史分红事件。

### 9.3 /market-metrics/historic-corporate-events/earnings-reports/{symbol}

- 方法: GET
- 路径: `/market-metrics/historic-corporate-events/earnings-reports/{symbol}`
- 作用: 查询指定标的的历史财报事件。

---

## 10. 市场时间与交易时段

### 10.1 /market-time/sessions

- 方法: GET
- 路径: `/market-time/sessions`
- 作用: 查询交易所会话。

### 10.2 /market-time/sessions/current

- 方法: GET
- 路径: `/market-time/sessions/current`
- 作用: 查询当前全局市场时间。

### 10.3 /market-time/equities/sessions/current

- 方法: GET
- 路径: `/market-time/equities/sessions/current`
- 作用: 查询当前股票交易时段。

### 10.4 /market-time/equities/sessions/next

- 方法: GET
- 路径: `/market-time/equities/sessions/next`
- 作用: 查询下一次股票交易时段。

### 10.5 /market-time/equities/sessions/previous

- 方法: GET
- 路径: `/market-time/equities/sessions/previous`
- 作用: 查询前一次股票交易时段。

### 10.6 /market-time/equities/holidays

- 方法: GET
- 路径: `/market-time/equities/holidays`
- 作用: 查询股票市场假期。

### 10.7 /market-time/futures/sessions/current

- 方法: GET
- 路径: `/market-time/futures/sessions/current`
- 作用: 查询当前期货交易时段。

### 10.8 /market-time/futures/sessions/current/{instrument_collection}

- 方法: GET
- 路径: `/market-time/futures/sessions/current/{instrument_collection}`
- 作用: 查询指定期货集合当前时段。

### 10.9 /market-time/futures/sessions/next/{instrument_collection}

- 方法: GET
- 路径: `/market-time/futures/sessions/next/{instrument_collection}`
- 作用: 查询指定期货集合下一时段。

### 10.10 /market-time/futures/sessions/previous/{instrument_collection}

- 方法: GET
- 路径: `/market-time/futures/sessions/previous/{instrument_collection}`
- 作用: 查询指定期货集合前一时段。

### 10.11 /market-time/futures/holidays/{instrument_collection}

- 方法: GET
- 路径: `/market-time/futures/holidays/{instrument_collection}`
- 作用: 查询指定期货集合假期。

---

## 11. 报价提醒

### 11.1 /quote-alerts

- 方法: GET
- 路径: `/quote-alerts`
- 作用: 查询当前用户的报价提醒。

### 11.2 /quote-alerts

- 方法: POST
- 路径: `/quote-alerts`
- 作用: 创建新的报价提醒。

### 11.3 /quote-alerts/{alert_external_id}

- 方法: DELETE
- 路径: `/quote-alerts/{alert_external_id}`
- 作用: 删除报价提醒。

---

## 12. 观察列表（Watchlists）

### 12.1 /pairs-watchlists

- 方法: GET
- 路径: `/pairs-watchlists`
- 作用: 查询配对观察列表。

### 12.2 /pairs-watchlists/{pairs_watchlist_name}

- 方法: GET
- 路径: `/pairs-watchlists/{pairs_watchlist_name}`
- 作用: 查询单个配对观察列表。

### 12.3 /public-watchlists

- 方法: GET
- 路径: `/public-watchlists`
- 作用: 查询公开观察列表。

### 12.4 /public-watchlists/{watchlist_name}

- 方法: GET
- 路径: `/public-watchlists/{watchlist_name}`
- 作用: 查询公开观察列表详情。

### 12.5 /watchlists

- 方法: GET
- 路径: `/watchlists`
- 作用: 查询用户观察列表。

### 12.6 /watchlists/{watchlist_name}

- 方法: GET
- 路径: `/watchlists/{watchlist_name}`
- 作用: 查询单个用户观察列表。

### 12.7 /watchlists

- 方法: POST
- 路径: `/watchlists`
- 作用: 创建观察列表。

### 12.8 /watchlists/{watchlist_name}

- 方法: PUT
- 路径: `/watchlists/{watchlist_name}`
- 作用: 修改观察列表。

### 12.9 /watchlists/{watchlist_name}

- 方法: DELETE
- 路径: `/watchlists/{watchlist_name}`
- 作用: 删除观察列表。

---

## 13. 其他模块

### 13.1 Backtesting

- 官方目录中存在 Backtesting 模块，但当前项目主要交易实现不一定会直接使用。
- 如果你需要，我可以继续补齐 Backtesting 所有相关路径。

---

## 14. 建议

- 核心实装优先顺序：
  1. 账户信息与持仓/余额
  2. 订单管理与复杂订单
  3. 市场数据与期权链
  4. 保证金与 Dry Run
  5. 交易流水
  6. 交易时段、市场指标、搜索、watchlists

- 如果只做交易执行功能，`orders`、`accounts`、`balances/positions`、`instruments` 是最关键。
- `watchlists`、`quote-alerts`、`market-metrics`、`market-sessions` 属于增强型功能，可后续补齐。
