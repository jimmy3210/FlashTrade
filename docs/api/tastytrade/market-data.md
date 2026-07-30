# Tastytrade 市场数据说明

## 1. 按类型查询行情

### 接口

```http
GET /market-data/by-type
```

### 查询参数

- `equity=AAPL,TSLA`
- `cryptocurrency=BTC/USD`
- `equity-option=SPY 250428P00355000`
- `index=SPX`
- `future=/CLM5`
- `future-option=/MESU5EX3M5 250620C6450`

### 示例

```bash
curl -X GET "https://api.tastyworks.com/market-data/by-type?equity=AAPL,TSLA" \
  -H "Authorization: Bearer <access_token>"
```

### 响应示例

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

## 2. 实时流说明

- Market Data Stream：实时行情
- Account Streamer：账户与订单状态更新

## 3. 使用建议

- 对于订单状态变化，优先使用 Account Streamer
- 对于实时价格，优先使用 Market Data Stream
- 避免高频轮询订单状态接口
