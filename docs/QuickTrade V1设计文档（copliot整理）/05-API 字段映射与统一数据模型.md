# 05-API 字段映射与统一数据模型

## 1. 目标

为前后端开发提供统一的数据模型，确保 UI 与业务层只依赖 QuickTrade 自定义对象，而不直接绑定 Broker 的字段名称与接口格式。这样一来，前端和业务逻辑可以独立于 Broker 的实现细节。

## 2. 统一数据模型

QuickTrade V1 建议统一使用以下核心对象：

| QuickTrade 对象 | 核心字段 | 字段类型建议 |
| --- | --- | --- |
| Quote | symbol、lastPrice、bidPrice、askPrice、open、high、low、close、volume | string、decimal、decimal、decimal、decimal、decimal、decimal、decimal、long |
| Position | symbol、quantity、averagePrice、marketValue、unrealizedPL | string、decimal、decimal、decimal、decimal |
| Order | orderId、symbol、side、quantity、price、status、orderType | string、string、enum、decimal、decimal、enum、enum |
| Account | accountNumber、cashBalance、buyingPower、netLiquidation | string、decimal、decimal、decimal |
| WatchList | watchListId、userId、symbol、sortOrder | string、string、string、int |
| Session | brokerType、accountId、token、expiresAt | enum、string、string、datetime |

### 2.1 建议的枚举值

- Order.side：BUY / SELL
- Order.status：PENDING / FILLED / PARTIAL / CANCELLED / REJECTED
- Order.orderType：MARKET / LIMIT / STOP
- Session.brokerType：SCHWAB / TASTYTRADE

## 3. 字段映射示例

| UI 字段 | QuickTrade 对象 | Schwab API | Tastytrade API |
| --- | --- | --- | --- |
| Last Price | Quote.lastPrice | quote.lastPrice | quote.mark |
| Bid | Quote.bidPrice | bidPrice | bid |
| Ask | Quote.askPrice | askPrice | ask |
| Volume | Quote.volume | totalVolume | volume |
| Cash | Account.cashBalance | currentBalances.cashBalance | cashBalance |
| Buying Power | Account.buyingPower | currentBalances.buyingPower | buyingPower |
| Position Qty | Position.quantity | position.quantity | position.quantity |
| Order Status | Order.status | order.status | order.status |

### 3.1 典型映射规则

- 价格类字段统一使用 decimal 类型
- 数量类字段统一使用 decimal 或 int，优先按业务含义选择
- 状态类字段统一转换为 QuickTrade 的标准枚举
- 用户可见的字符串字段尽量保持统一命名，减少前端适配成本

## 4. Adapter 的职责

Adapter 层负责将 Broker 的原始字段映射为 QuickTrade 的统一对象：

- Schwab Adapter：将 Schwab 的数据结构映射为 QuickTrade 对象
- Tastytrade Adapter：将 Tastytrade 的数据结构映射为 QuickTrade 对象

其基本职责包括：

- 字段命名统一
- 类型转换
- 业务字段补齐
- 异常字段兜底
- 统一错误码与异常信息

### 4.1 建议的 Adapter 输出

每个 Adapter 输出都应保持一致的结构：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "symbol": "AAPL",
    "lastPrice": 212.35,
    "bidPrice": 212.34,
    "askPrice": 212.36,
    "volume": 1250000
  }
}
```

## 5. 请求与响应建议

### 5.1 Quote 查询

请求示例：

```http
GET /api/quotes/AAPL
```

响应示例：

```json
{
  "symbol": "AAPL",
  "lastPrice": 212.35,
  "bidPrice": 212.34,
  "askPrice": 212.36,
  "open": 210.10,
  "high": 213.20,
  "low": 209.90,
  "close": 211.80,
  "volume": 1250000
}
```

### 5.2 Order 提交

请求示例：

```http
POST /api/orders
```

```json
{
  "symbol": "AAPL",
  "side": "BUY",
  "quantity": 100,
  "price": 212.35,
  "orderType": "LIMIT"
}
```

响应示例：

```json
{
  "orderId": "ord-001",
  "symbol": "AAPL",
  "side": "BUY",
  "quantity": 100,
  "price": 212.35,
  "status": "PENDING",
  "orderType": "LIMIT"
}
```

## 6. 架构说明

前端与业务层统一通过 QuickTrade Service 调用，底层由 Adapter 负责字段转换：

```text
UI / Frontend
    ↓
QuickTrade Service
    ↓
Broker Adapter
    ↓
Schwab / Tastytrade API
```

## 7. 设计价值

这套统一数据模型可以带来三点价值：

- 降低前端对 Broker 细节的依赖
- 让后端接口更稳定、可维护
- 便于后续新增其他 Broker 时复用现有业务逻辑
- 让接口文档、前端调用和 Adapter 实现保持一致
