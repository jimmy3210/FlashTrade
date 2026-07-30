# Tastytrade 账户与账户数据说明

## 1. 获取当前客户信息

### 接口

```http
GET /customers/me
```

### 响应示例

```json
{
  "data": {
    "id": "me",
    "first-name": "John",
    "last-name": "Doe",
    "email": "johndoe@nowhere.com"
  },
  "context": "/customers/me"
}
```

## 2. 获取当前客户账户列表

### 接口

```http
GET /customers/me/accounts
```

### 响应示例

```json
{
  "data": {
    "items": [
      {
        "account": {
          "account-number": "5WT00001",
          "nickname": "Individual",
          "margin-or-cash": "Margin"
        },
        "authority-level": "owner"
      }
    ]
  },
  "context": "/customers/me/accounts"
}
```

## 3. 获取单个账户详情

### 接口

```http
GET /customers/me/accounts/{account_number}
```

## 4. 获取账户交易状态

### 接口

```http
GET /accounts/{account_number}/trading-status
```

### 响应示例

```json
{
  "data": {
    "account-number": "5WT00001",
    "is-closed": false,
    "is-frozen": false,
    "is-closing-only": false,
    "is-cryptocurrency-enabled": true
  },
  "context": "/accounts/5WT00001/trading-status"
}
```

## 5. 获取账户持仓

### 接口

```http
GET /accounts/{account_number}/positions
```

### 响应示例

```json
{
  "data": {
    "items": [
      {
        "symbol": "AAPL",
        "instrument-type": "Equity",
        "quantity": "100",
        "quantity-direction": "Long"
      }
    ]
  },
  "context": "/accounts/5WT00000/positions"
}
```

## 6. 获取账户余额

### 接口

```http
GET /accounts/{account_number}/balances
```

### 响应示例

```json
{
  "data": {
    "account-number": "5WT00000",
    "cash-balance": "7218.997",
    "net-liquidating-value": "32520.734",
    "equity-buying-power": "45657.048"
  },
  "context": "/accounts/5WT00000/balances"
}
```

## 7. 获取余额快照

### 接口

```http
GET /accounts/{account_number}/balance-snapshots
```

### 查询参数

- `snapshot-date`
- `time-of-day`: `BOD` / `EOD`
