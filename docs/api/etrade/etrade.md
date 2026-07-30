E*TRADE 提供官方的 **REST API**，可以开发自己的量化交易程序、投资管理工具或账户查询系统。([E*TRADE Developer][1])

## API 支持的功能

E*TRADE API 可以实现：

* 📈 获取实时/延迟行情（Quotes）
* 📊 获取账户余额、持仓（Portfolio）
* 📜 查询订单、交易历史
* 🛒 下单（股票、ETF、期权等）
* ✏️ 修改、取消订单
* 📩 获取消息通知
* 📃 获取期权链（Option Chain）([E*TRADE Developer][2])

---

## API 类型

* REST API
* 返回格式：JSON 或 XML
* 认证方式：**OAuth 1.0a**（不是 OAuth2）([E*TRADE Developer][3])

OAuth 流程大致为：

1. 获取 Consumer Key / Secret
2. Request Token
3. 用户登录 E*TRADE 授权
4. 获取 Access Token
5. 调用 API([E*TRADE Developer][3])

---

## 获取 API Key

首先需要申请开发者账号。

官方开发者网站：

**[E*TRADE Developer Platform](https://developer.etrade.com/home?contentId=306a9d46-58c2-4cac-85f6-7717aea056bd&utm_source=chatgpt.com)**

可以申请：

* Sandbox Key（测试）
* Production Key（正式）

Sandbox 可以免费申请，用于开发测试。([E*TRADE Developer][4])

---

## 常见接口

### 获取账户

```
GET /v1/accounts/list
```

### 获取余额

```
GET /v1/accounts/{accountIdKey}/balance
```

### 获取持仓

```
GET /v1/accounts/{accountIdKey}/portfolio
```

### 获取行情

```
GET /v1/market/quote/{symbol}
```

例如：

```
GET /v1/market/quote/AAPL
```

### 下单

```
POST /v1/accounts/{accountIdKey}/orders/place
```

### 查询订单

```
GET /v1/accounts/{accountIdKey}/orders
```

([E*TRADE Developer][1])

---

## 官方示例

E*TRADE 官方提供了示例项目，包括：

* Python
* Java
* Node.js

可直接下载参考。([E*TRADE Developer][4])

---

## Python 示例

使用 `requests-oauthlib`：

```python
from rauth import OAuth1Service

service = OAuth1Service(
    consumer_key='YOUR_KEY',
    consumer_secret='YOUR_SECRET',
    request_token_url='https://api.etrade.com/oauth/request_token',
    access_token_url='https://api.etrade.com/oauth/access_token',
    authorize_url='https://us.etrade.com/e/t/etws/authorize'
)
```

授权完成后即可调用：

```python
GET https://api.etrade.com/v1/accounts/list.json
```

---

## 是否免费？

对于个人开发者：

* ✅ API 免费使用
* ✅ Sandbox 免费
* ✅ 不收取 API 调用费用
* ❗ 实际交易仍按 E*TRADE 的佣金和费用执行。([E*TRADE Developer][5])

---

## 注意事项

* 使用 **OAuth 1.0a**，认证流程比现代 OAuth2 更复杂。
* Access Token 需要按官方机制续期或重新授权，开发自动化程序时要处理好认证生命周期。([E*TRADE Developer][3])

如果你的目标是**开发自动交易机器人（Python/Java）**，我也可以提供一个完整的 E*TRADE API 项目模板，包括 OAuth 登录、获取账户、获取行情和自动下单的完整代码结构。

[1]: https://developer.etrade.com/getting-started?utm_source=chatgpt.com "Getting Started"
[2]: https://developer.etrade.com/support/frequently-asked-questions?utm_source=chatgpt.com "Frequently Asked Questions"
[3]: https://developer.etrade.com/getting-started/developer-guides?utm_source=chatgpt.com "Developer Guides"
[4]: https://developer.etrade.com/home?contentId=306a9d46-58c2-4cac-85f6-7717aea056bd&utm_source=chatgpt.com "E*TRADE | Welcome Developers"
[5]: https://developer.etrade.com/support?utm_source=chatgpt.com "Support"

如果你想开发一个**完整的自动交易程序**（例如 Java + Spring Boot），推荐按下面的架构来设计。

---

# 一、整体流程

```text
            +----------------------+
            |  Spring Boot App     |
            +----------+-----------+
                       |
                       | OAuth 1.0a
                       |
                E*TRADE API
                       |
     +-----------------+-----------------+
     |                 |                 |
 Accounts         Market Data        Orders
     |                 |                 |
 Portfolio         Quotes         Buy/Sell
```

整个流程分为 5 步：

```
申请 API Key
      ↓
OAuth 登录
      ↓
获取账户
      ↓
获取行情
      ↓
下单
```

E*TRADE 的所有 API 调用都必须先完成 OAuth 1.0a 授权。([E*TRADE Developer][1])

---

# 二、推荐项目结构

```
etrade-api-demo

├── controller
│      OAuthController.java
│      OrderController.java
│      QuoteController.java
│
├── service
│      OAuthService.java
│      AccountService.java
│      QuoteService.java
│      OrderService.java
│
├── config
│      OAuthConfig.java
│
├── model
│      Account.java
│      Quote.java
│      Order.java
│
├── util
│      OAuthUtil.java
│
└── application.yml
```

这种分层结构便于后续扩展策略、定时任务、日志等功能。

---

# 三、OAuth 登录流程

OAuth 1.0a 的典型流程如下：

```text
Consumer Key
      │
      ▼
GET /oauth/request_token
      │
      ▼
oauth_token
      │
      ▼
浏览器打开授权页面

https://us.etrade.com/e/t/etws/authorize
      │
      ▼
用户登录
      │
      ▼
得到 verifier
      │
      ▼
GET /oauth/access_token
      │
      ▼
Access Token
      │
      ▼
调用所有 API
```

需要注意的是：

* Request Token 只有约 **5 分钟**有效。
* Access Token 默认在**美国东部时间当天结束时失效**；如果两小时没有任何请求，还需要调用 Renew API 重新激活。([E*TRADE][2])

---

# 四、获取账户

请求：

```http
GET /v1/accounts/list.json
```

返回示例：

```json
{
  "AccountListResponse": {
    "Accounts": {
      "Account": [
        {
          "accountId": "12345678",
          "accountIdKey": "AbCdEf123456",
          "accountMode": "MARGIN"
        }
      ]
    }
  }
}
```

真正调用其他接口时，一般使用的是 `accountIdKey`，而不是 `accountId`。([E*TRADE Developer][3])

---

# 五、获取股票行情

例如查询 Apple：

```
GET /v1/market/quote/AAPL.json
```

多个股票：

```
GET /v1/market/quote/AAPL,MSFT,NVDA.json
```

返回的数据通常包括：

```json
{
  "QuoteResponse": {
    "QuoteData": [
      {
        "symbol": "AAPL",
        "lastTrade": 228.31,
        "bid": 228.25,
        "ask": 228.35,
        "volume": 31234567
      }
    ]
  }
}
```

---

# 六、查询持仓

```
GET /v1/accounts/{accountIdKey}/portfolio.json
```

可获取：

* 股票代码
* 持仓数量
* 成本价
* 当前市值
* 盈亏情况

例如：

```json
{
    "symbol":"AAPL",
    "quantity":100,
    "pricePaid":175.2
}
```

---

# 七、下单

E*TRADE 的下单通常分两步：

```
Preview Order
      ↓
Place Order
```

也就是说：

```
POST orders/preview
```

确认订单内容后，再执行：

```
POST orders/place
```

这种设计可以减少误下单。社区开发者也经常提到需要先获取 `previewId` 再正式提交订单。([Reddit][4])

一个简化的下单请求示例：

```json
{
  "orderType":"EQ",
  "clientOrderId":"123456",
  "Order":[
      {
          "priceType":"MARKET",
          "orderTerm":"GOOD_FOR_DAY",
          "marketSession":"REGULAR",
          "Instrument":[
              {
                  "symbol":"AAPL",
                  "orderAction":"BUY",
                  "quantity":10
              }
          ]
      }
  ]
}
```

---

# 八、自动交易示例

假设你的策略是：

```
每一分钟：

获取 AAPL 行情

↓

如果价格 < 220

↓

买入 10 股

↓

如果价格 > 240

↓

卖出 10 股
```

Spring Boot 可以配合：

```java
@Scheduled(fixedDelay = 60000)
```

实现定时轮询，再结合 `QuoteService` 和 `OrderService` 完成自动交易。

---

# 九、建议使用的 Java 技术栈

如果使用 Java 17 或更高版本，可以考虑：

* Spring Boot 3
* Spring WebClient（HTTP 客户端）
* OAuth 1.0a（如 ScribeJava 或 Signpost 等库）
* Jackson（JSON）
* Lombok
* Maven

---

## 后续可以继续实现

可以一步一步搭建一个完整项目，例如：

1. **Spring Boot + Maven 项目初始化**
2. **OAuth 1.0a 登录（完整实现）**
3. **获取账户信息**
4. **获取实时行情**
5. **买卖股票**
6. **自动交易策略（定时执行）**
7. **Docker 部署到 VPS 或 AWS**

这样最终可以得到一个可运行的、约 **2000–3000 行代码**的 E*TRADE 自动交易系统。

[1]: https://developer.etrade.com/getting-started/developer-guides?utm_source=chatgpt.com "Developer Guides"
[2]: https://apisb.etrade.com/docs/api/authorization/request_token.html?utm_source=chatgpt.com "E*TRADE API Document"
[3]: https://developer.etrade.com/getting-started?utm_source=chatgpt.com "Getting Started"
[4]: https://www.reddit.com/r/algotrading/comments/isfqwu?utm_source=chatgpt.com "Algo Trading - Etrade"
