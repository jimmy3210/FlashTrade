# Tastytrade 订单接口说明

## 1. 下单前校验（Dry Run）

### 接口

```http
POST /accounts/{account_number}/orders/dry-run
```

### 作用

用于验证订单是否合法、是否足够买入力、是否可能被拒绝，不真正下单。

### 请求示例

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

### 响应示例

```json
{
  "data": {
    "order": {
      "account-number": "5WT00001",
      "status": "Received"
    },
    "warnings": [],
    "buying-power-effect": {
      "impact": "2.302",
      "effect": "Debit"
    }
  },
  "context": "/accounts/5WT00001/orders/dry-run"
}
```

## 2. 提交订单

### 接口

```http
POST /accounts/{account_number}/orders
```

### 示例：股票市价单

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

### 示例：股票期权限价单

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

### 响应示例

```json
{
  "data": {
    "order": {
      "id": 771043,
      "status": "Routed",
      "order-type": "Limit"
    }
  },
  "context": "/accounts/5WT0001/orders/"
}
```

## 3. 撤单

### 接口

```http
DELETE /accounts/{account_number}/orders/{id}
```

### 响应示例

```json
{
  "data": {
    "id": 12345,
    "status": "Cancel Requested"
  },
  "context": "/accounts/5WT00001/orders/12345"
}
```

## 4. 改单（Cancel/Replace）

### 接口

```http
PUT /accounts/{account_number}/orders/{id}
```

### 请求示例

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

## 5. 复杂订单

### 接口

```http
POST /accounts/{account_number}/complex-orders
```

### 示例：OTOCO

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

### 取消复杂订单

```http
DELETE /accounts/{account_number}/complex-orders/{id}
```

## 6. 订单字段说明

### 订单类型

- `Limit`
- `Market`
- `Stop`
- `Stop Limit`
- `Notional Market`

### 价格影响

- `Debit`
- `Credit`

### Time In Force

- `Day`
- `GTC`
- `GTD`
- `Ext`
- `GTC Ext`
- `Ext Overnight`
- `GTC Ext Overnight`

### Leg 字段

- `instrument-type`
- `symbol`
- `quantity`
- `action`
