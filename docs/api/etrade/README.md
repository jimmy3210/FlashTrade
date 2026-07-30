# E*TRADE API 集成指南

本目录包含 E*TRADE 交易 API 的完整文档和实装指南。

## 目录结构

### 文档

- **[auth.md](auth.md)** - OAuth 1.0a 认证流程和 Token 管理
- **[orders.md](orders.md)** - 订单接口详解（下单、预览、撤单、改单等）

### 关键文件

- `EtradeOAuthClient` - OAuth 1.0a 认证客户端
- `EtradeRestClient` - REST API 调用客户端
- `EtradeBrokerClient` - Broker 业务逻辑客户端
- `EtradeMapper` - E*TRADE DTO 与统一数据模型映射

## 快速概览

E*TRADE API 提供以下核心功能：

| 功能 | 端点 | 状态 |
|------|------|------|
| 账户列表 | `GET /v1/accounts/list` | ✅ 已实现 |
| 账户余额 | `GET /v1/accounts/{id}/balance` | ✅ 已实现 |
| 持仓信息 | `GET /v1/accounts/{id}/portfolio` | ✅ 已实现 |
| 订单预览 | `POST /v1/orders/preview` | 🚧 实装中 |
| 提交订单 | `POST /v1/orders/place` | 🚧 实装中 |
| 查询订单 | `GET /v1/orders/{id}` | 🚧 实装中 |
| 取消订单 | `DELETE /v1/orders/{id}` | 🚧 实装中 |
| 修改订单 | `PUT /v1/orders/{id}` | 🚧 实装中 |

## 架构与代码组织

本项目遵循“核心域 + Broker 适配器”设计：

- `com.tradex.flashtrade` 仅包含 FlashTrade 自身的通用领域模型与基础组件
- `com.tradex.flashtrade.broker` 下放置每个券商的独立实现模块
- 每个券商实现放在 `flash-trade-api/src/main/java/com/tradex/flashtrade/broker/<broker>`
- E*TRADE 的实现位于 `broker/etrade`，包含：
  - `EtradeOAuthClient`
  - `EtradeRestClient`
  - `EtradeBrokerClient`
  - `EtradeMapper`
  - `EtradeTokenService`

这种结构保证了：

1. 核心业务模型不与券商实现耦合
2. 新券商只需新增一个 `broker/<broker>` 模块
3. 统一业务层消费 `flashtrade` 域对象，而不是直接依赖具体券商接口

## 当前实装进度

### 已完成
- ✅ 核心域模型扩展：`Order` 增加 `orderType/timeInForce/stopPrice`
- ✅ 新增 `SessionToken` 实体，支持 OAuth 1.0a 和 OAuth 2.0 的统一存储
- ✅ 新增 E*TRADE 专用 `OrderPreview` 实体，用于存储 etrade 订单预览结果，已迁移到 `broker/etrade` 模块
- ✅ 新增 `OrderModification` 实体，用于记录订单改单历史
- ✅ 新增 `OrderFill` 实体，用于记录分笔成交明细

### 正在进行
- 🚧 实现 E*TRADE 订单 API 适配层
- 🚧 实现 E*TRADE OAuth 1.0a 认证与签名逻辑
- 🚧 订单预览与提交流程的 DTO、Mapper 与业务联调

### 下步计划
- ➕ 补齐 `broker/etrade` 模块中的 Order DTO 和响应模型
- ➕ 实现 `EtradeBrokerClient` 的 `previewOrder/placeOrder/cancelOrder/replaceOrder`
- ➕ 添加控制器端点并完成集成测试

## 认证方式

E*TRADE 使用 **OAuth 1.0a** 认证（非 OAuth2）。

### 认证流程

```
1. 获取 Consumer Key / Secret
   ↓
2. 获取 Request Token
   ↓
3. 用户授权（通过浏览器）
   ↓
4. 获取 Access Token
   ↓
5. 调用 API（添加 OAuth 签名）
```

详见 [auth.md](auth.md)。

## 环境配置

### Sandbox（测试环境）

```properties
# oauth.properties 或 application.yml
etrade.consumer-key=YOUR_SANDBOX_KEY
etrade.consumer-secret=YOUR_SANDBOX_SECRET
etrade.access-token=YOUR_ACCESS_TOKEN
etrade.token-secret=YOUR_TOKEN_SECRET
etrade.api-base-url=https://apisb.etrade.com
etrade.oauth-base-url=https://etwssandbox.etrade.com/oauth
```

### Production（生产环境）

```properties
etrade.consumer-key=YOUR_PROD_KEY
etrade.consumer-secret=YOUR_PROD_SECRET
etrade.access-token=YOUR_ACCESS_TOKEN
etrade.token-secret=YOUR_TOKEN_SECRET
etrade.api-base-url=https://api.etrade.com
etrade.oauth-base-url=https://us.etrade.com/e/t/etws/authorize
```

## 核心接口

### 订单预览

**需求**：在下单前验证订单合法性，返回预估费用和风险信息。

```java
OrderPreviewResponse previewOrder(String accountId, BrokerOrder order)
  throws BrokerApiException;
```

**返回值示例**：

```json
{
  "previewId": "123456789",
  "estimatedCommission": 5.00,
  "estimatedTax": 0.00,
  "estimatedTotal": -15005.00,
  "requiredMargin": 7502.50,
  "availableMargin": 10000.00,
  "warnings": ["Low liquidity detected"],
  "canPlace": true
}
```

### 提交订单

```java
BrokerOrder placeOrder(String accountId, BrokerOrder order)
  throws BrokerApiException;
```

### 取消订单

```java
void cancelOrder(String accountId, String orderId)
  throws BrokerApiException;
```

### 修改订单

```java
BrokerOrder replaceOrder(String accountId, String orderId, BrokerOrder newOrder)
  throws BrokerApiException;
```

## 数据模型映射

### 统一订单模型（QuickTrade）

```java
class BrokerOrder {
  String orderId;           // 订单 ID
  String symbol;            // 交易代码
  BuySell side;             // BUY / SELL
  OrderType orderType;      // MARKET / LIMIT / STOP / STOP_LIMIT
  Decimal quantity;         // 数量
  Decimal price;            // 限价 / 止损价
  OrderStatus status;       // PENDING / FILLED / PARTIAL / CANCELLED / REJECTED
  TimeInForce timeInForce;  // DAY / GTC / IOC / FOK
}
```

### E*TRADE 订单模型

```json
{
  "Order": {
    "orderId": 987654321,
    "symbol": "AAPL",
    "quantity": 100,
    "side": "BUY",
    "orderType": "Limit",
    "limitPrice": 150.25,
    "stopPrice": 0.00,
    "timeInForce": "Day",
    "status": "OPEN"
  }
}
```

### 映射规则

| BrokerOrder | E*TRADE Order |
|-------------|----------------|
| side: BUY | side: "BUY" |
| side: SELL | side: "SELL" |
| orderType: MARKET | orderType: "Market" |
| orderType: LIMIT | orderType: "Limit", limitPrice: price |
| orderType: STOP | orderType: "Stop", stopPrice: price |
| orderType: STOP_LIMIT | orderType: "StopLimit", stopPrice: price, limitPrice: price |
| timeInForce: DAY | timeInForce: "Day" |
| timeInForce: GTC | timeInForce: "GTC" |
| status: PENDING | status: "OPEN" |
| status: FILLED | status: "EXECUTED" |
| status: CANCELLED | status: "CANCELLED" |

## 实装进度

### Phase 1: OAuth 认证框架 🚧
- [ ] 分析官方 Java 例子
- [ ] 实现 EtradeOAuthClient
- [ ] 集成 OAuth 签名到 EtradeRestClient
- [ ] 单元测试

### Phase 2: 订单 DTO 与映射 🚧
- [ ] 创建订单 Request/Response DTO
- [ ] 实现 EtradeMapper 订单映射
- [ ] 创建 OrderPreviewResponse 模型
- [ ] 单元测试

### Phase 3: 业务逻辑实现 🚧
- [ ] 实现 EtradeBrokerClient 订单方法
- [ ] 实现 BrokerService 订单方法
- [ ] 错误处理和异常转换
- [ ] 单元测试

### Phase 4: API 端点 🚧
- [ ] 创建 OrderController
- [ ] 实现下单预览端点
- [ ] 实现下单端点
- [ ] 实现撤单/改单端点
- [ ] 集成测试

## 开发清单

详见：[../../todo.md](../../todo.md)

## 参考资源

- [E*TRADE Developer Platform](https://developer.etrade.com/home)
- [E*TRADE API 官方文档](https://apisb.etrade.com/docs)
- [OAuth 1.0a 规范](https://oauth.net/core/1.0a/)
- [官方 Java 示例](C:\Users\Administrator\Downloads\java_example_app\example-app-java)

## 常见问题

**Q: OAuth 1.0a 和 OAuth 2.0 有什么区别？**

A: E*TRADE 采用 OAuth 1.0a，与 OAuth 2.0 不同，需要：
- 所有请求手工签名（HMAC-SHA1）
- 维护 Consumer Secret 和 Token Secret
- 不支持 Bearer Token，需要 Authorization Header

**Q: Sandbox 和 Production 如何切换？**

A: 修改 `application.yml` 中的 API 和 OAuth 基础 URL，以及 Consumer Key/Secret。

**Q: 订单预览 API 是否必须调用？**

A: 强烈建议调用，可以在实际下单前验证订单合法性，避免被拒绝。

**Q: 如何处理 Access Token 过期？**

A: 当收到 `401 Unauthorized` 时，重新执行 OAuth 认证流程获取新的 Access Token。

## 联系方式

如有问题，请联系开发团队或查看项目 Wiki。
