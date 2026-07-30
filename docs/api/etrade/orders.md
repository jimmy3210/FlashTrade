# E*TRADE 订单接口说明

## 1. 订单预览（Order Preview）

### 作用

在实际下单前，验证订单是否合法、是否足够买入力、是否可能被拒绝，以及返回预估费用和保证金要求等风险信息。

### 接口

```http
POST https://apisb.etrade.com/order/v1/accounts/{accountIdKey}/orders/preview
```

### 请求示例（市价单）

```json
{
  "PreviewOrderRequest": {
    "orderType": "EQ",
    "clientOrderId": "123456",
    "Order": [
      {
        "allOrNone": false,
        "stopPrice": 0.00,
        "accountType": "CASH",
        "clientId": "FlashTrade",
        "limitPrice": 0.00,
        "orderType": "Market",
        "symbol": "AAPL",
        "quantity": 100,
        "side": "BUY",
        "timeInForce": "Day",
        "orderId": 0
      }
    ]
  }
}
```

### 请求示例（限价单）

```json
{
  "PreviewOrderRequest": {
    "orderType": "EQ",
    "clientOrderId": "123457",
    "Order": [
      {
        "allOrNone": false,
        "stopPrice": 0.00,
        "accountType": "CASH",
        "clientId": "FlashTrade",
        "limitPrice": 150.25,
        "orderType": "Limit",
        "symbol": "AAPL",
        "quantity": 100,
        "side": "BUY",
        "timeInForce": "Day",
        "orderId": 0
      }
    ]
  }
}
```

### 响应示例（成功）

```json
{
  "PreviewOrderResponse": {
    "orderType": "EQ",
    "previewId": "123456789",
    "previewTime": 1234567890000,
    "deferredCommission": 0.00,
    "totalCommission": 5.00,
    "totalValue": -15005.00,
    "deferredTaxAmount": 0.00,
    "marginCalculated": true,
    "marginBuyPower": 10000.00,
    "marginRequirement": 7502.50,
    "marginTotalBuyPower": 17502.50,
    "warnings": [],
    "Order": [
      {
        "orderId": 0,
        "clientOrderId": "123456",
        "quantity": 100,
        "orderType": "Market",
        "side": "BUY",
        "symbol": "AAPL"
      }
    ]
  }
}
```

### 响应字段说明

| 字段 | 说明 |
|------|------|
| `previewId` | 预览 ID，用于提交订单时引用 |
| `previewTime` | 预览生成时间戳 |
| `totalCommission` | 预估佣金总额 |
| `totalValue` | 订单预估总价值（已包含佣金） |
| `marginRequirement` | 该订单需要的保证金 |
| `marginTotalBuyPower` | 完成该订单后剩余的总买入力 |
| `warnings` | 警告信息列表（如不满足最低头寸、流动性问题等） |

### 错误响应示例

```json
{
  "PreviewOrderResponse": {
    "orderType": "EQ",
    "previewTime": 1234567890000,
    "errors": [
      {
        "code": 10002,
        "description": "Insufficient buying power. Buying power: 1000.00, Requested: 15005.00"
      }
    ]
  }
}
```

## 2. 提交订单（Place Order）

### 接口

```http
POST https://apisb.etrade.com/order/v1/accounts/{accountIdKey}/orders/place
```

### 请求示例

```json
{
  "PlaceOrderRequest": {
    "orderType": "EQ",
    "clientOrderId": "123456",
    "previewId": "123456789",
    "Order": [
      {
        "allOrNone": false,
        "stopPrice": 0.00,
        "accountType": "CASH",
        "clientId": "FlashTrade",
        "limitPrice": 0.00,
        "orderType": "Market",
        "symbol": "AAPL",
        "quantity": 100,
        "side": "BUY",
        "timeInForce": "Day",
        "orderId": 0
      }
    ]
  }
}
```

**重要参数说明**：

- `previewId`：从订单预览接口返回的预览 ID（必填）
- `clientOrderId`：客户端订单 ID，用于跟踪订单（由客户端生成）
- `orderType`：订单类型，通常为 `EQ`（股票）
- `Order[0].orderType`：具体订单类型
  - `Market`：市价单
  - `Limit`：限价单
  - `Stop`：止损单
  - `StopLimit`：止损限价单

### 响应示例（成功）

```json
{
  "PlaceOrderResponse": {
    "orderType": "EQ",
    "clientOrderId": "123456",
    "order": [
      {
        "orderId": 987654321,
        "status": "OPEN",
        "symbol": "AAPL",
        "quantity": 100,
        "side": "BUY",
        "orderType": "Market",
        "timeInForce": "Day",
        "placeTime": 1234567890000
      }
    ]
  }
}
```

### 订单状态说明

| 状态 | 说明 |
|------|------|
| `OPEN` | 订单已提交，等待成交 |
| `EXECUTED` | 订单已完全成交 |
| `PARTIAL` | 订单部分成交 |
| `CANCELLED` | 订单已被取消 |
| `EXPIRED` | 订单已过期 |
| `REJECTED` | 订单被拒绝 |

## 3. 查询订单列表

### 接口

```http
GET https://apisb.etrade.com/order/v1/accounts/{accountIdKey}/orders?status=OPEN
```

### 查询参数

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `status` | 查询订单状态，可选值：`OPEN`、`EXECUTED`、`CANCELLED`、`EXPIRED`、`REJECTED`、`ALL` | `OPEN` |
| `marker` | 分页标记 | - |
| `count` | 返回订单数量（最多 25） | 25 |

### 响应示例

```json
{
  "Order": [
    {
      "orderId": 987654321,
      "status": "OPEN",
      "symbol": "AAPL",
      "quantity": 100,
      "side": "BUY",
      "orderType": "Market",
      "timeInForce": "Day",
      "limitPrice": 0.00,
      "stopPrice": 0.00,
      "placeTime": 1234567890000
    },
    {
      "orderId": 987654322,
      "status": "EXECUTED",
      "symbol": "MSFT",
      "quantity": 50,
      "side": "SELL",
      "orderType": "Market",
      "timeInForce": "Day",
      "executedTime": 1234567900000
    }
  ]
}
```

## 4. 查询订单详情

### 接口

```http
GET https://apisb.etrade.com/order/v1/accounts/{accountIdKey}/orders/{orderId}
```

### 响应示例

```json
{
  "Order": [
    {
      "orderId": 987654321,
      "status": "PARTIAL",
      "symbol": "AAPL",
      "quantity": 100,
      "executedQuantity": 60,
      "side": "BUY",
      "orderType": "Limit",
      "limitPrice": 150.25,
      "timeInForce": "Day",
      "placeTime": 1234567890000,
      "Legs": [
        {
          "legId": 0,
          "symbol": "AAPL",
          "side": "BUY",
          "quantity": 100,
          "executedQuantity": 60,
          "price": 150.23,
          "fills": [
            {
              "fillId": 1,
              "quantity": 30,
              "price": 150.22,
              "fillTime": 1234567895000
            },
            {
              "fillId": 2,
              "quantity": 30,
              "price": 150.24,
              "fillTime": 1234567897000
            }
          ]
        }
      ]
    }
  ]
}
```

## 5. 取消订单

### 接口

```http
DELETE https://apisb.etrade.com/order/v1/accounts/{accountIdKey}/orders/{orderId}
```

或

```http
POST https://apisb.etrade.com/order/v1/accounts/{accountIdKey}/orders/{orderId}/cancel
```

### 响应示例（成功）

```json
{
  "CancelOrderResponse": {
    "orderId": 987654321,
    "cancelTime": 1234567905000,
    "cancelStatus": "SUCCESS"
  }
}
```

### 错误响应示例

```json
{
  "CancelOrderResponse": {
    "orderId": 987654321,
    "errors": [
      {
        "code": 10005,
        "description": "Order cannot be cancelled. Current status: EXECUTED"
      }
    ]
  }
}
```

## 6. 修改订单（Replace Order）

### 接口

```http
PUT https://apisb.etrade.com/order/v1/accounts/{accountIdKey}/orders/{orderId}
```

### 请求示例

```json
{
  "PlaceOrderRequest": {
    "orderType": "EQ",
    "clientOrderId": "123456-modified",
    "Order": [
      {
        "allOrNone": false,
        "stopPrice": 0.00,
        "accountType": "CASH",
        "clientId": "FlashTrade",
        "limitPrice": 151.00,
        "orderType": "Limit",
        "symbol": "AAPL",
        "quantity": 100,
        "side": "BUY",
        "timeInForce": "Day",
        "orderId": 987654321
      }
    ]
  }
}
```

### 响应示例（成功）

```json
{
  "PlaceOrderResponse": {
    "orderType": "EQ",
    "clientOrderId": "123456-modified",
    "order": [
      {
        "orderId": 987654321,
        "status": "OPEN",
        "symbol": "AAPL",
        "quantity": 100,
        "limitPrice": 151.00,
        "side": "BUY",
        "orderType": "Limit",
        "timeInForce": "Day",
        "modifyTime": 1234567910000
      }
    ]
  }
}
```

## 7. 常见错误码

| 错误码 | 说明 |
|--------|------|
| `10002` | 买入力不足 |
| `10003` | 订单参数无效 |
| `10005` | 订单无法取消 |
| `10006` | 账户被锁定 |
| `10007` | 交易符号无效 |
| `10008` | 账户不存在 |

## 8. 订单时间类型 (Time In Force)

| 类型 | 说明 |
|------|------|
| `Day` | 当天有效，市场收盘时自动取消 |
| `GTC` | 持续有效 (Good Till Cancelled)，直到用户手动取消 |
| `IOC` | 立即成交或取消 (Immediate or Cancel) |
| `FOK` | 全部成交或全部取消 (Fill or Kill) |

## 9. 订单类型 (Order Type)

| 类型 | 说明 |
|------|------|
| `Market` | 市价单，以当前最优价格成交 |
| `Limit` | 限价单，指定价格或更优价格成交 |
| `Stop` | 止损单，到达指定价格后变为市价单 |
| `StopLimit` | 止损限价单，止损价和限价结合 |

## 10. 官方文档参考

- Orders API：[https://apisb.etrade.com/docs/api/order/api-order-v1.html](https://apisb.etrade.com/docs/api/order/api-order-v1.html)
- Order Preview API：[https://apisb.etrade.com/docs/api/order/api-order-v1.html#/preview](https://apisb.etrade.com/docs/api/order/api-order-v1.html#/preview)
