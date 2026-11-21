# FlashTrade 后端 API 文档

**版本**: 1.2  
**日期**: 2026-07-26  
**状态**: 一对一绑定 + 交易端账户查询完整实现

**响应格式状态**：当前后端及 Flutter/Web 自有客户端已按 V1 `code/message/data` 契约迁移；`code` 使用稳定业务码，HTTP 状态独立表达，错误 `data` 包含 `fieldErrors` 和服务端生成/验证的 `traceId`。本文件中早期端点示例可能保留历史包装，实际响应以 API 规格书及运行代码为准。完整 API 回归和已部署调用方升级仍待验收。

---

## 📌 API 总览

### 基础信息
- **基础 URL**: `http://localhost:8080/quicktrade/api`
- **认证**: JWT Bearer Token (`Authorization: Bearer {token}`)
- **响应格式**: JSON
- **默认时区**: UTC (数据库 CURRENT_TIMESTAMP)

---

## 🔐 认证

### 获取 Token
```http
POST /auth/login
Content-Type: application/json

{
  "username": "trader01",
  "password": "password123"
}
```

**响应** (200 OK):
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "username": "trader01",
    "role": "ROLE_TRADER"
  }
}
```

**所有 API 调用均需在请求头中包含**:
```
Authorization: Bearer {token}
```

---

## 1️⃣ 交易操作 API

### 1.1 下单 / Place Order

```http
POST /v1/trade/order
Authorization: Bearer {token}
Content-Type: application/json

{
  "symbol": "AAPL",
  "side": "BUY",
  "quantity": 10,
  "limitPrice": 150.50,
  "clientOrderId": "order_20260726_001"
}
```

**Query 参数**:
- `accountId` (string, required): 目标交易账户 ID

**请求体**:
```typescript
{
  symbol: string;           // 股票代码（NASDAQ/NYSE）
  side: "BUY" | "SELL";     // 买入/卖出
  quantity: number;          // 数量
  limitPrice?: number;       // 限价（若为 0 则市价）
  clientOrderId?: string;    // 客户端订单 ID（用于幂等）
}
```

`clientOrderId` 可选，建议每个订单请求提供不超过 100 个字符的唯一 ID。服务端按用户、账户和券商保存该 ID；相同 ID 的成功重试返回已有委托，不会再次下单。若已有请求仍在处理或结果尚未确认，接口返回 HTTP 409（`ORDER_RESULT_UNCONFIRMED`）。客户端应先查询订单状态，不得自动生成新 ID 重放同一笔结果未知的订单。未提供 `clientOrderId` 时不提供请求幂等保证。

**响应** (200 OK):
```json
{
  "success": true,
  "data": {
    "externalOrderId": "123456789",
    "status": "SUBMITTED",
    "symbol": "AAPL",
    "side": "BUY",
    "quantity": 10,
    "limitPrice": 150.50,
    "filledQuantity": 0,
    "avgFillPrice": null,
    "createdAt": "2026-07-26T10:30:45Z"
  }
}
```

**错误响应** (400/401/500):
```json
{
  "success": false,
  "error": "用户未绑定券商账户",
  "errorCode": "RESOURCE_NOT_FOUND"
}
```

---

### 1.2 撤单 / Cancel Order

```http
DELETE /v1/trade/order/{externalOrderId}
Authorization: Bearer {token}

?accountId=123456789
```

**Path 参数**:
- `externalOrderId` (string, required): 券商侧委托 ID

**Query 参数**:
- `accountId` (string, required): 交易账户 ID

**响应** (200 OK):
```json
{
  "success": true,
  "data": null
}
```

---

### 1.3 查询委托状态 / Order Status

```http
GET /v1/trade/order/{externalOrderId}/status
Authorization: Bearer {token}

?accountId=123456789
```

**Path 参数**:
- `externalOrderId` (string, required): 委托 ID

**Query 参数**:
- `accountId` (string, required): 交易账户 ID

**响应** (200 OK):
```json
{
  "success": true,
  "data": {
    "externalOrderId": "123456789",
    "status": "FILLED",
    "symbol": "AAPL",
    "side": "BUY",
    "quantity": 10,
    "filledQuantity": 10,
    "avgFillPrice": 150.45,
    "createdAt": "2026-07-26T10:30:45Z",
    "updatedAt": "2026-07-26T10:35:22Z"
  }
}
```

---

### 1.4 ⭐ 获取用户当前账户 / Current Account (NEW)

```http
GET /v1/trade/current-account
Authorization: Bearer {token}
```

**用途**: 客户端查询用户当前绑定的券商账户及其 API 环境配置

**响应** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": 1,
    "userId": 5,
    "username": "trader01",
    "brokerId": 2,
    "brokerCode": "ETRADE",
    "brokerName": "E*TRADE",
    "accountNo": "123456789",
    "accountType": "CASH",
    "status": "ACTIVE",
    "apiEnvironment": "LIVE",
    "devAccountId": 10,
    "currentBindingUsername": "trader01"
  }
}
```

**关键字段说明**:
- `apiEnvironment`: "LIVE" (生产环境) | "SANDBOX" (沙箱环境)
- `currentBindingUsername`: 当前使用该账户的用户名（通常与 username 相同）
- `status`: "ACTIVE" | "SUSPENDED" | "CLOSED"

**错误响应** (404):
```json
{
  "success": false,
  "error": "用户未绑定券商账户",
  "errorCode": "RESOURCE_NOT_FOUND"
}
```

---

## 2️⃣ 管理员 API

### 2.1 券商账户列表 / Account List

```http
GET /admin/broker-accounts
Authorization: Bearer {token}
```

**权限**: `ROLE_ADMIN`

**响应** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "userId": 5,
      "username": "trader01",
      "brokerId": 2,
      "brokerCode": "ETRADE",
      "brokerName": "E*TRADE",
      "accountNo": "123456789",
      "accountType": "CASH",
      "status": "ACTIVE",
      "apiEnvironment": "LIVE",
      "devAccountId": 10,
      "currentBindingUsername": "trader01"
    },
    {
      "id": 2,
      "userId": 5,
      "username": "trader01",
      "brokerId": 3,
      "brokerCode": "SCHWAB",
      "brokerName": "Charles Schwab",
      "accountNo": "987654321",
      "accountType": "MARGIN",
      "status": "ACTIVE",
      "apiEnvironment": "SANDBOX",
      "devAccountId": 11,
      "currentBindingUsername": null
    }
  ]
}
```

**列表特性**:
- 包含所有用户的所有券商账户
- `currentBindingUsername` 显示当前绑定用户（null 表示未绑定）
- 按 id 升序排列

---

### 2.2 创建券商账户 / Create Account

```http
POST /admin/broker-accounts
Authorization: Bearer {token}
Content-Type: application/json

{
  "userId": 5,
  "brokerName": "E*TRADE",
  "accountNo": "123456789",
  "accountType": "CASH",
  "status": "ACTIVE",
  "devAccountId": 10,
  "apiEnvironment": "LIVE"
}
```

**请求体**:
```typescript
{
  userId: number;                    // 用户 ID (required)
  brokerName: string;                // 券商名称 (required)
  accountNo: string;                 // 账户号 (required)
  accountType?: "CASH" | "MARGIN";   // 账户类型 (default: CASH)
  status?: "ACTIVE" | "SUSPENDED" | "CLOSED";  // 状态 (default: ACTIVE)
  devAccountId?: number;             // 开发者账号 ID (optional)
  apiEnvironment?: "LIVE" | "SANDBOX";  // API 环境 (default: LIVE)
}
```

**响应** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": 3,
    "userId": 5,
    "username": "trader01",
    "brokerId": 2,
    "brokerCode": "ETRADE",
    "brokerName": "E*TRADE",
    "accountNo": "123456789",
    "accountType": "CASH",
    "status": "ACTIVE",
    "apiEnvironment": "LIVE",
    "devAccountId": 10,
    "currentBindingUsername": null
  }
}
```

**验证规则**:
- ✅ userId 必须存在于 users 表
- ✅ brokerName 必须匹配 t_broker 中的 broker_code 或 broker_name
- ✅ accountNo 在同一券商内不能重复
- ✅ devAccountId 必须属于该券商
- ✅ apiEnvironment 只能是 "LIVE" 或 "SANDBOX"
- ✅ accountType 只能是 "CASH" 或 "MARGIN"

---

### 2.3 更新券商账户 / Update Account

```http
PUT /admin/broker-accounts/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "userId": 5,
  "brokerName": "E*TRADE",
  "accountNo": "123456789",
  "accountType": "MARGIN",
  "status": "ACTIVE",
  "devAccountId": 10,
  "apiEnvironment": "SANDBOX"
}
```

**Path 参数**:
- `id` (number, required): 账户 ID

**请求体**: 同创建接口

**响应** (200 OK): 同创建接口

---

### 2.4 删除/停用账户 / Delete Account

```http
DELETE /admin/broker-accounts/{id}
Authorization: Bearer {token}
```

**Path 参数**:
- `id` (number, required): 账户 ID

**响应** (200 OK):
```json
{
  "success": true,
  "data": null
}
```

**说明**: 不是硬删除，而是将 status 标记为 "CLOSED"

---

### 2.5 ⭐ 查询用户当前账户 / User's Current Account (NEW)

```http
GET /admin/broker-accounts/user/{userId}/current
Authorization: Bearer {token}
```

**Path 参数**:
- `userId` (number, required): 用户 ID

**用途**: 管理员查询某个用户当前绑定的券商账户

**响应** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": 1,
    "userId": 5,
    "username": "trader01",
    "brokerId": 2,
    "brokerCode": "ETRADE",
    "brokerName": "E*TRADE",
    "accountNo": "123456789",
    "accountType": "CASH",
    "status": "ACTIVE",
    "apiEnvironment": "LIVE",
    "devAccountId": 10,
    "currentBindingUsername": "trader01"
  }
}
```

**错误响应** (404):
```json
{
  "success": false,
  "error": "用户未绑定券商账户: 5",
  "errorCode": "RESOURCE_NOT_FOUND"
}
```

---

### 2.6 ⭐ 为用户设置/切换账户 / Bind Account to User (NEW)

```http
POST /admin/broker-accounts/user/{userId}/bind/{accountId}
Authorization: Bearer {token}
```

**Path 参数**:
- `userId` (number, required): 用户 ID
- `accountId` (number, required): 要绑定的账户 ID

**请求体**: 无 (空 POST)

**用途**: 
1. 为用户首次绑定账户
2. 为用户切换到其他账户

**处理流程**:
1. 验证用户存在
2. 验证账户存在且状态为 ACTIVE
3. 检查该账户是否已被其他用户绑定
4. **自动解绑用户的旧账户** (设置 current_broker_account_id = NULL)
5. **绑定新账户** (设置 current_broker_account_id = {accountId})

**响应** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": 2,
    "userId": 5,
    "username": "trader01",
    "brokerId": 3,
    "brokerCode": "SCHWAB",
    "brokerName": "Charles Schwab",
    "accountNo": "987654321",
    "accountType": "MARGIN",
    "status": "ACTIVE",
    "apiEnvironment": "SANDBOX",
    "devAccountId": 11,
    "currentBindingUsername": "trader01"
  }
}
```

**错误响应** (400):
```json
{
  "success": false,
  "error": "该券商账户已被其他用户绑定",
  "errorCode": "INVALID_ARGUMENT"
}
```

**错误响应** (400):
```json
{
  "success": false,
  "error": "券商账户非活跃状态，无法绑定: CLOSED",
  "errorCode": "INVALID_ARGUMENT"
}
```

---

## 3️⃣ 用户管理 API

当前管理端使用 `/users`。列表和详情允许平台管理员，以及有明确公司 Scope 的 `ROLE_ADMIN` / `ROLE_COMPANY_ADMIN`；服务端按 `users.company_id` 过滤，跨公司详情按未找到处理，无公司 Scope 默认拒绝。创建、更新、停用和密码修改仅允许 `ROLE_SUPER_ADMIN`。

```http
GET /users
Authorization: Bearer {token}
```

### 3.1 用户详情

```http
GET /users/{id}
Authorization: Bearer {token}
```

### 3.2 创建 / 更新用户

```http
POST /users
PUT /users/{id}
Authorization: Bearer {token}
Content-Type: application/json
```

```json
{
  "username": "trader01",
  "accountUserId": "broker-account-reference",
  "brokerName": "SCHWAB",
  "companyId": 42,
  "password": "<initial-password>"
}
```

`companyId` 必须指向已启用公司；创建时必填。更新也必须提交归属公司，变更公司归属只允许平台管理员，并写入操作审计。用户始终以 `ROLE_TRADER` 创建；更新保留现有角色，不接受客户端角色字段。密码不会写入审计内容。

用户选择公司可调用 `GET /api/v1/admin/companies?page=0&size=100`，并按分页读取全部公司；仅已启用公司可用于创建或变更归属。

### 3.3 停用用户 / 重置密码

```http
POST /users/status
POST /users/{id}/password
Authorization: Bearer {token}
```

上述写操作仅允许 `ROLE_SUPER_ADMIN`。旧用户若 `company_id` 未完成可信映射，将不会出现在公司管理员的列表中。

### 3.3A 旧数据公司 Scope 映射

旧用户和旧券商账户若缺少可信公司归属，不自动获得任何公司 Scope。以下接口仅允许 `ROLE_SUPER_ADMIN`，并记录操作审计：

```http
GET /admin/company-mappings
POST /admin/company-mappings/users/{id}
POST /admin/company-mappings/accounts/{id}
POST /admin/company-mappings/batch
Authorization: Bearer {token}
```

映射旧用户：

```json
{"companyId":42}
```

映射旧券商账户所有者：

```json
{"userId":7}
```

目标用户必须已归属有效启用公司。批量接口逐项处理并返回成功或失败结果，不会因单项失败回滚其他成功项：

```json
{
  "users":[{"userId":7,"companyId":42}],
  "accounts":[{"accountId":9,"userId":7}]
}
```

### 3.4 用户响应字段

成功响应使用统一的 `code/message/data` 契约；`data` 为 `UserAdminDto`：

```typescript
interface UserAdminDto {
  userId: number;
  username: string;
  accountUserId: string | null;
  brokerName: string | null;
  companyId: number | null;
  companyName: string | null;
  role: string;
  deleteFlag: 0 | 1;
}
```

---

## 📊 数据模型参考

### BrokerAccountDto (券商账户数据传输对象)

```typescript
interface BrokerAccountDto {
  id: number;                              // 账户 ID
  userId: number;                          // 创建者用户 ID
  username: string;                        // 创建者用户名
  brokerId: number;                        // 券商 ID
  brokerCode: string;                      // 券商代码 (ETRADE/SCHWAB/TASTYTRADE/TASTYWORKS)
  brokerName: string;                      // 券商名称
  accountNo: string;                       // 账户号 (券商账户编号)
  accountType: "CASH" | "MARGIN";          // 账户类型
  status: "ACTIVE" | "SUSPENDED" | "CLOSED";  // 账户状态
  apiEnvironment: "LIVE" | "SANDBOX";      // API 环境
  devAccountId?: number;                   // 开发者账号 ID
  currentBindingUsername?: string;         // 当前使用者用户名 (null=未绑定)
}
```

### OrderRequest (下单请求)

```typescript
interface OrderRequest {
  symbol: string;              // 股票代码
  side: "BUY" | "SELL";        // 买入/卖出
  quantity: number;            // 数量
  limitPrice?: number;         // 限价 (0=市价)
  clientOrderId?: string;      // 客户端订单 ID (用于幂等)
}
```

### PlaceOrderResult (下单结果)

```typescript
interface PlaceOrderResult {
  externalOrderId: string;     // 券商侧委托 ID
  status: string;              // 委托状态
  symbol: string;
  side: "BUY" | "SELL";
  quantity: number;
  filledQuantity: number;      // 已成交数量
  avgFillPrice?: number;       // 平均成交价
  createdAt: string;           // 创建时间 (ISO8601)
  updatedAt?: string;          // 更新时间 (ISO8601)
}
```

### BrokerOrder (委托详情)

```typescript
interface BrokerOrder {
  externalOrderId: string;
  status: string;              // PENDING | PARTIALLY_FILLED | FILLED | CANCELED | REJECTED
  symbol: string;
  side: "BUY" | "SELL";
  quantity: number;
  filledQuantity: number;
  avgFillPrice?: number;
  createdAt: string;
  updatedAt?: string;
}
```

---

## ⚙️ 技术细节

### 事务保证

**账户绑定操作** (`POST /admin/broker-accounts/user/{userId}/bind/{accountId}`) 使用 `@Transactional`:
```
START TRANSACTION;
  UPDATE users SET current_broker_account_id = NULL WHERE id = ?;  -- 解绑旧账户
  UPDATE users SET current_broker_account_id = ? WHERE id = ?;     -- 绑定新账户
COMMIT;
```

**失败时**自动回滚，保证一致性。

### 数据库约束

```sql
ALTER TABLE users
    ADD CONSTRAINT fk_users_current_broker_account
        FOREIGN KEY (current_broker_account_id)
        REFERENCES t_broker_account(id) ON DELETE SET NULL;
```

- 若账户被删除，自动清除用户的绑定关系
- FK 约束保证 current_broker_account_id 只能指向存在的账户

### 查询优化

**列表查询包含 3 个 LEFT JOIN** 以一次性获取完整信息:
```sql
SELECT a.*, u.username, b.*, u_bind.username AS current_binding_username
FROM t_broker_account a
LEFT JOIN users u ON a.user_id = u.id                     -- 创建者
LEFT JOIN t_broker b ON a.broker_id = b.id                -- 券商信息
LEFT JOIN users u_bind ON a.id = u_bind.current_broker_account_id  -- 使用者
```

---

## 🔍 常见错误码

| 错误码 | HTTP | 说明 | 示例 |
|--------|------|------|------|
| RESOURCE_NOT_FOUND | 404 | 资源不存在 | 用户/账户 ID 不存在 |
| INVALID_ARGUMENT | 400 | 参数不合法 | 环境值不是 LIVE/SANDBOX |
| UNAUTHORIZED | 401 | 认证失败 | Token 无效或过期 |
| FORBIDDEN | 403 | 权限不足 | 非 ADMIN 用户访问管理接口 |
| CONFLICT | 409 | 业务冲突 | 账户已被他人绑定 |
| INTERNAL_ERROR | 500 | 服务器错误 | 数据库操作失败 |

---

## 📋 API 版本历史

### v1.2 (2026-07-26) - 一对一绑定新增
- ✅ `GET /v1/trade/current-account` - 客户端查询当前账户
- ✅ `GET /admin/broker-accounts/user/{userId}/current` - 查询用户绑定账户
- ✅ `POST /admin/broker-accounts/user/{userId}/bind/{accountId}` - 为用户绑定/切换账户
- ✅ BrokerAccountDto 新增 `currentBindingUsername` 字段

### v1.1 (2026-07-24) - API 环境支持
- ✅ 所有账户 CRUD 端点支持 `apiEnvironment` (LIVE/SANDBOX)
- ✅ 数据库 V17 迁移

### v1.0 (2026-07-20) - 初始版本
- ✅ 基础券商账户 CRUD
- ✅ 下单、撤单、委托查询

---

**API 文档完成日期**: 2026-07-26  
**最后更新**: 一对一绑定系统完整实现
