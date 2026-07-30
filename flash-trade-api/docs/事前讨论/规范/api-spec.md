# FlashTradeAPI — API 规格书

**文档版本**：1.0.0  
**最后更新**：2026-03-28  
**状态**：阶段 1 初版  
**Swagger UI**：`http://localhost:8080/swagger-ui.html`（应用启动后可访问）  
**OpenAPI JSON**：`http://localhost:8080/api-docs`

---

## 1. 公共规范

### 1.1 Base URL

| 环境 | URL |
|------|-----|
| 开发 | `http://localhost:8080` |
| 测试 | `http://localhost:8081` |
| 生产 | `https://api.flashtrade.example.com` |

### 1.2 认证方式

除 `/api/auth/**` 外，所有端点须在请求头中携带 JWT Bearer Token：

```
Authorization: Bearer <access_token>
```

### 1.3 统一响应格式

**成功响应**

```json
{
  "success": true,
  "data": { ... },
  "timestamp": "2026-03-28T10:00:00Z"
}
```

**错误响应**

```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Token is invalid or expired"
  },
  "timestamp": "2026-03-28T10:00:00Z"
}
```

### 1.4 HTTP 状态码

| 状态码 | 含义 |
|--------|------|
| 200 | 请求成功 |
| 201 | 创建成功 |
| 400 | 请求参数错误 |
| 401 | 未认证或 Token 无效 |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 409 | 数据冲突 |
| 429 | 请求频率超限 |
| 500 | 服务器内部错误 |
| 503 | 外部服务不可用（券商 API 故障）|

---

## 2. 认证接口（Phase 3 实装）

### 2.1 用户登录

```
POST /api/auth/login
```

**请求体**

```json
{
  "username": "trader01",
  "password": "P@ssw0rd"
}
```

**响应（200 OK）**

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci...",
    "tokenType": "Bearer",
    "expiresIn": 3600
  },
  "timestamp": "2026-03-28T10:00:00Z"
}
```

**错误响应（401）**

```json
{
  "success": false,
  "error": {
    "code": "INVALID_CREDENTIALS",
    "message": "用户名或密码错误"
  }
}
```

---

### 2.2 刷新 Token

```
POST /api/auth/refresh
```

**请求体**

```json
{
  "refreshToken": "eyJhbGci..."
}
```

**响应（200 OK）**

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGci...",
    "tokenType": "Bearer",
    "expiresIn": 3600
  }
}
```

---

### 2.3 登出

```
POST /api/auth/logout
```

**请求头**：需要 `Authorization: Bearer <token>`

**响应（200 OK）**

```json
{
  "success": true,
  "data": {
    "message": "已成功登出"
  }
}
```

---

## 3. 持仓与订单接口（Phase 4 ✅ 已实装）

### 3.1 查询持仓列表

```
GET /api/portfolio/positions
```

**请求头**：`Authorization: Bearer <access_token>`

**响应（200 OK）**

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "accountId": "ACC001",
      "symbol": "AAPL",
      "quantity": 100.0000,
      "averageCost": 150.0000,
      "currentPrice": 175.0000,
      "marketValue": 17500.0000,
      "unrealizedPnl": 2500.0000,
      "brokerName": "schwab",
      "updatedAt": "2026-03-28T09:30:00"
    }
  ],
  "timestamp": "2026-03-28T10:00:00Z"
}
```

---

### 3.2 查询订单列表

```
GET /api/orders
```

**请求头**：`Authorization: Bearer <access_token>`

**查询参数**

| 参数 | 类型 | 必须 | 说明 |
|------|------|------|------|
| `page` | int | 否 | 页码（默认 0） |
| `size` | int | 否 | 每页数量（默认 20，最大 100）|
| `status` | string | 否 | 订单状态过滤（PENDING/FILLED/PARTIALLY_FILLED/CANCELLED/REJECTED）|

**响应（200 OK）**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "accountId": "ACC001",
        "symbol": "AAPL",
        "side": "BUY",
        "quantity": 100,
        "price": 150.00,
        "status": "FILLED",
        "createdAt": "2026-03-28T09:00:00Z",
        "filledAt": "2026-03-28T09:01:00Z"
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "currentPage": 0
  }
}
```

---

## 4. WebSocket 接口（Phase 4 ✅ 已实装）

### 4.1 连接

```
ws://localhost:8080/ws
```

使用 STOMP + SockJS 连接（`@EnableWebSocketMessageBroker`）。

### 4.2 订阅持仓更新

```
订阅 Topic：/topic/positions
```

服务器每 30 秒自动向此 Topic 广播全量持仓（可通过 `app.websocket.push-interval-ms` 配置间隔）。

**推送消息格式**

```json
{
  "type": "POSITION_UPDATE",
  "data": [
    {
      "symbol": "AAPL",
      "quantity": 100,
      "currentPrice": 175.00,
      "marketValue": 17500.00,
      "updatedAt": "2026-03-28T10:00:00Z"
    }
  ],
  "timestamp": "2026-03-28T10:00:00Z"
}
```

---

## 5. 系统接口

### 5.1 健康检查

```
GET /actuator/health
```

**响应（200 OK）**

```json
{
  "status": "UP"
}
```

### 5.2 API 文档

```
GET /swagger-ui.html   → Swagger UI 界面
GET /api-docs          → OpenAPI JSON 规格
```

---

## 6. 错误代码一览

| 错误代码 | HTTP 状态 | 说明 |
|----------|-----------|------|
| `INVALID_CREDENTIALS` | 401 | 用户名或密码错误 |
| `TOKEN_EXPIRED` | 401 | Access Token 已过期 |
| `TOKEN_INVALID` | 401 | Token 格式非法或被吊销 |
| `REFRESH_TOKEN_EXPIRED` | 401 | Refresh Token 已过期 |
| `ACCESS_DENIED` | 403 | 当前角色无权访问 |
| `RESOURCE_NOT_FOUND` | 404 | 请求资源不存在 |
| `VALIDATION_ERROR` | 400 | 请求参数校验失败 |
| `BROKER_UNAVAILABLE` | 503 | 外部券商 API 不可用 |
| `INTERNAL_ERROR` | 500 | 服务器内部错误 |

---

## 7. 修订历史

| 版本 | 日期 | 内容 | 阶段 |
|------|------|------|------|
| 1.0.0 | 2026-03-28 | 初版：端点规划、统一响应格式、错误代码表 | Phase 1 |
