# FlashTradeAPI — API 规格书

**文档版本**：1.0.0  
**最后更新**：2026-11-20
**状态**：V1 响应契约已冻结并完成当前 API 包装迁移；完整回归与部署验收仍需继续
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

受保护端点须在请求头中携带 JWT Bearer Token；登录、Token 刷新和 Web CSRF 初始化端点按各接口说明开放：

```
Authorization: Bearer <access_token>
```

### 1.3 统一响应格式

**成功响应**

```json
{
  "code": "SUCCESS",
  "message": "请求成功",
  "data": { }
}
```

**错误响应**

```json
{
  "code": "UNAUTHORIZED",
  "message": "认证失败，请先登录",
  "data": {
    "fieldErrors": [],
    "traceId": "f3c1a8e18d6b4c29"
  }
}
```

成功和错误响应都使用 `code/message/data`。HTTP 状态码独立表达协议状态；成功业务码为 `SUCCESS`。错误 `data` 固定包含 `fieldErrors` 和 `traceId`，不得依赖旧 `success/data/error` 字段。

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
POST /api/v1/auth/login
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
  "code": "SUCCESS",
  "message": "请求成功",
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci...",
    "tokenType": "Bearer",
    "expiresIn": 3600
  }
}
```

**错误响应（401）**

```json
{
  "code": "INVALID_CREDENTIALS",
  "message": "用户名或密码错误",
  "data": {
    "fieldErrors": [],
    "traceId": "f3c1a8e18d6b4c29"
  }
}
```

---

### 2.2 刷新 Token

```
POST /api/v1/auth/refresh
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
  "code": "SUCCESS",
  "message": "请求成功",
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci...",
    "tokenType": "Bearer",
    "expiresIn": 3600
  }
}
```

成功刷新会同时返回新的 Access Token 和 Refresh Token，并立即撤销本次使用的旧 Refresh Token；客户端必须原子地保存新令牌对，不能继续使用旧 Refresh Token。

---

### 2.3 登出

```
POST /api/v1/auth/logout
```

**请求头**：需要 `Authorization: Bearer <token>`

**请求体（可选）**：客户端持有 Refresh Token 时可提交，服务端仅在其主体与当前 Access Token 一致且有效时撤销；不提供请求体仍兼容旧客户端。

```json
{
  "refreshToken": "<refresh-token>"
}
```

服务端将当前 Access Token 加入黑名单；Refresh Token 有效且同主体时也加入黑名单。Refresh Token 无效时仍撤销有效 Access Token；主体不匹配时拒绝请求，仍撤销当前 Access Token，但不撤销该 Refresh Token。

**响应（200 OK）**

```json
{
  "code": "SUCCESS",
  "message": "请求成功",
  "data": null
}
```

### 2.4 Web 管理端 Cookie 会话

Web 管理端使用独立 Cookie 会话接口；Flutter 等 JSON 客户端继续使用上述 `/api/v1/auth/login`、`/api/v1/auth/refresh` 和 `/api/v1/auth/logout` 契约。

#### 初始化 CSRF Cookie

```
GET /api/v1/auth/web/csrf
```

响应为统一 `code/message/data` 格式，`data` 为 `null`。服务端设置可由浏览器读取的 `XSRF-TOKEN` Cookie（`Path=/`、`SameSite=Strict`；生产环境为 `Secure`）。浏览器须在以下 Web POST 请求中同时携带该 Cookie，并将其值放入 `X-XSRF-TOKEN` 请求头。

#### Web 登录、刷新与登出

| 操作 | 请求 | 认证与 CSRF | 响应 |
|---|---|---|---|
| 登录 | `POST /api/v1/auth/web/login`，JSON 请求体为 `username`、`password` | CSRF Cookie + `X-XSRF-TOKEN` | `data` 仅包含 `accessToken`、`tokenType`、`expiresIn`；Refresh Token 只写入 Cookie |
| 刷新 | `POST /api/v1/auth/web/refresh`，无请求体 | 浏览器自动发送 Refresh Cookie；须同时携带 CSRF Cookie 和请求头 | 轮换 Refresh Cookie，JSON 仅返回新的 Access Token 等字段；缺失、过期或已撤销的 Refresh Token 返回 401 |
| 登出 | `POST /api/v1/auth/web/logout`，无请求体 | 当前 Access Token 的 Bearer 头、Refresh Cookie、CSRF Cookie 和请求头 | 撤销当前会话令牌并清除 Refresh Cookie |

Refresh Cookie 名称为 `FLASH_TRADE_WEB_REFRESH`，属性为 `HttpOnly`、`SameSite=Strict`、`Path=/api/v1/auth/web`，有效期由 Refresh Token 配置控制（当前默认 7 天）；生产环境必须设置 `Secure`。Access Token 由 Web 客户端仅保存在内存中，不写入 `localStorage`。Web 写请求的 401 不会被自动重放。

Web Cookie 会话按同源部署设计；若部署为跨源，必须显式配置精确的可信 CORS Origin，并维持凭证和 CSRF 校验。上述接口契约不代表真实数据库、Redis 或生产部署已完成端到端验收。

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
  "code": "SUCCESS",
  "message": "请求成功",
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
  ]
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
  "code": "SUCCESS",
  "message": "请求成功",
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
| `UNAUTHORIZED` | 401 | 未提供有效身份 |
| `INVALID_CREDENTIALS` | 401 | 用户名或密码错误 |
| `TOKEN_EXPIRED` | 401 | Access Token 已过期 |
| `TOKEN_INVALID` | 401 | Token 格式非法或被吊销 |
| `REFRESH_TOKEN_EXPIRED` | 401 | Refresh Token 已过期 |
| `ACCESS_DENIED` | 403 | 当前角色无权访问 |
| `ACCOUNT_RISK_BLOCKED` | 403 | 账户被风控禁止操作 |
| `RESOURCE_NOT_FOUND` | 404 | 请求资源不存在 |
| `NOT_FOUND` | 404 | 未找到资源 |
| `ACCOUNT_NOT_FOUND` | 404 | 指定账户不存在 |
| `INVALID_ARGUMENT` | 400 | 无效的请求参数 |
| `VALIDATION_ERROR` | 400 | 请求参数校验失败 |
| `INVALID_STATE` | 400 | 状态不允许的操作 |
| `STOCK_NOT_FOUND` | 400 | 股票代码不存在或无效 |
| `STOCK_NOT_WHITELISTED` | 400 | 股票代码未在白名单中 |
| `ORDER_RESULT_UNCONFIRMED` | 409 | 订单提交结果尚未确认 |
| `TOO_MANY_REQUESTS` | 429 | 请求频率过高 |
| `BROKER_UNAVAILABLE` | 503 | 外部券商 API 不可用 |
| `INTERNAL_ERROR` | 500 | 服务器内部错误 |

---

## 7. 修订历史

| 版本 | 日期 | 内容 | 阶段 |
|------|------|------|------|
| 1.0.0 | 2026-03-28 | 初版：端点规划、统一响应格式、错误代码表 | Phase 1 |
| 1.1.0 | 2026-11-20 | 冻结 `code/message/data` 响应及稳定业务码注册表 | Phase 1 |
