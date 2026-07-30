# Tastytrade 保证金接口说明

## 1. 获取账户保证金要求

### 接口

```http
GET /margin/accounts/{account_number}/requirements
```

### 作用

返回账户的保证金要求、维持保证金、买入力影响等信息。

### 响应示例

```json
{
  "data": {
    "account-number": "5WT00001",
    "margin-calculation-type": "Reg T",
    "margin-requirement": "4337.831401355",
    "maintenance-requirement": "3559.174330645",
    "margin-equity": "500007967.877802711"
  }
}
```

## 2. Margin Dry Run

### 接口

```http
POST /margin/accounts/{account_number}/dry-run
```

### 作用

测试某个订单对账户保证金和买入力的影响。

### 请求示例

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
