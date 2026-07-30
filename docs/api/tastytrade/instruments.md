# Tastytrade 合约与行情信息说明

## 1. Equity（股票）

### 获取股票列表

```http
GET /instruments/equities
```

### 查询参数

- `symbol[]`
- `lendability`
- `is-index`
- `is-etf`

### 响应示例

```json
{
  "data": {
    "items": [
      {
        "symbol": "AAPL",
        "instrument-type": "Equity",
        "listed-market": "XNAS",
        "active": true
      }
    ]
  },
  "context": "/instruments/equities"
}
```

### 获取单只股票信息

```http
GET /instruments/equities/{symbol}
```

## 2. Equity Option（股票期权）

### 获取期权链（嵌套）

```http
GET /option-chains/{underlying_symbol}/nested
```

### 响应示例

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

## 3. Futures（期货）

### 获取活跃期货合约

```http
GET /instruments/futures
```

### 获取单个期货合约

```http
GET /instruments/futures/{symbol}
```

## 4. Future Option（期货期权）

### 获取期货期权链

```http
GET /futures-option-chains/{product_code}
```

### 获取期货期权对象

```http
GET /instruments/future-options
```

## 5. Cryptocurrency（加密货币）

### 获取支持的加密货币

```http
GET /instruments/cryptocurrencies
```

### 获取单个加密货币信息

```http
GET /instruments/cryptocurrencies/{symbol}
```

## 6. Quantity Decimal Precisions

```http
GET /instruments/quantity-decimal-precisions
```
