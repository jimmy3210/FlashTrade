# Tastytrade Sandbox Environment

## 1. 基本地址

- Sandbox REST API: https://api.cert.tastyworks.com
- Sandbox websocket / account streamer: streamer.cert.tastyworks.com

> 注意：Sandbox 与 Production 完全隔离，使用不同的账号、OAuth 应用和凭证。

## 2. Sandbox 订单行为

Sandbox 的订单不会进入真实市场；它们只在测试系统内执行。

- Market 订单：始终以 $1 成交。
- Limit 订单：
  - 当价格小于 $3 时，立即成交。
  - 当价格大于 $3 时，订单会进入 Live 状态，但不会成交。

这意味着 Sandbox 中的成交规则是模拟逻辑，而不是实时市场撮合。

## 3. Sandbox 可用性与限制

### 3.1 支持的功能

- OAuth2 认证
- 客户与账户管理
- 余额与持仓查询
- 订单提交、查询、撤单、改单
- 复杂订单提交与校验
- 市场行情查询（HTTP API）
- 订单 Dry Run

### 3.2 受限或未支持的功能

- Net liquidating value history
- Market metrics
- 实时 Market Data
  - 仅支持延迟行情流，真实市场数据不在 Sandbox 中提供

## 4. Sandbox 页面账户信息

Sandbox 页面通常会展示当前测试客户和账户的管理面板，主要内容包括：

- Sandbox customer 信息
- 当前账户号
- 账户类型（如 Individual）
- Margin 类型（Cash / Margin）
- 现金余额
- 授权客户与权限等级

### 4.1 创建和管理账户

- Sandbox 页面支持直接创建测试账户。
- 创建时可以设置初始资金，默认值一般为 $100000。
- 已有账户可查看：账户编号、账户类型、Margin 类型、现金余额。
- 可以对测试账户执行“删除账户”和“添加资金”等操作。

### 4.2 账户权限与授权

- Sandbox 客户可授权当前 OAuth 应用访问其账户。
- 页面里会显示授权客户名称、权限等级（例如 Owner）。

## 5. OAuth2 认证与 Grant 管理

Sandbox 页面提供 OAuth2 应用管理和 Grant 生成功能。

### 5.1 OAuth 应用创建

创建 OAuth 应用时需填写：

- 应用名称
- Redirect URI
- Scopes（例如 `read,trade,openid`）

### 5.2 Client ID / Client Secret

- 创建应用后会返回 Client ID 和 Client Secret。
- Client Secret 只会显示一次，务必妥善保存。
- 如果丢失 Client Secret，需要重新生成新的 secret。

### 5.3 Refresh Token

- 创建 Grant 后会生成 Refresh Token。
- Refresh Token 在 Sandbox 中长期有效，通常不会过期。
- Sandbox 的 refresh_token 与生产环境不通用。

### 5.4 Sandbox OAuth2 流程

1. 在 Sandbox 页面创建 OAuth 应用。
2. 生成 Grant，并获取 refresh_token。
3. 使用 refresh_token 与 client_secret 交换 access_token。
4. 后续请求使用 `Authorization: Bearer <access_token>`。

## 6. Sandbox API 认证字段说明

### 6.1 Access Token

- 通过 `POST https://api.cert.tastyworks.com/oauth/token` 获取。
- 请求类型：`application/x-www-form-urlencoded`
- 需要参数：
  - `grant_type=refresh_token`
  - `refresh_token=<refresh_token>`
  - `client_secret=<client_secret>`

### 6.2 Response 示例

```json
{
  "access_token": "eyJhbGciOi...",
  "token_type": "Bearer",
  "expires_in": 900,
  "refresh_token": "..."
}
```

### 6.3 Header 使用

- `Authorization: Bearer <access_token>`
- `User-Agent: your-client/1.0`
- `Content-Type: application/json`

## 7. Sandbox 使用建议

- 不要把 Sandbox client_secret、refresh_token 等敏感信息写入源码。
- 生产环境和 Sandbox 环境的数据、账户和 OAuth 凭证互不通用。
- Sandbox 的成交规则是模拟规则，不等同于真实市场行为。
- 若要验证功能流程，可优先测试：
  - `GET /accounts/{account_number}/trading-status`
  - `GET /accounts/{account_number}/balances`
  - `GET /accounts/{account_number}/positions`

## 8. 推荐流程

1. 登录 Sandbox 账户并创建测试 customer。
2. 创建 OAuth 应用并记录 Client ID / Client Secret。
3. 生成 Grant 获取 Refresh Token。
4. 通过 Sandbox token 接口获取 Access Token。
5. 使用 Sandbox API 进行订单、持仓、余额等测试。
6. 需要切换到生产环境时，务必替换为 Production URL 和生产 OAuth 凭证。



---

Account Number:
5WK71121
Account Type:
Individual
Margin Type:
Cash
Cash Balance:
$50000.0
Delete
Add Funds
Authorized Customers
Name: lin jimmy
Authority Level: Owner


---

OAuth2 Application:
Name: trailer Sandbox OAuth2 App
Scopes: read,trade,openid
Client ID: 803c4248-355d-438d-91f5-168aedcc6dde
Redirect URIs: https://www.trailer.com
⚠️ Make sure to copy and store the Client Secret somewhere safe. The Client Secret cannot be viewed again. If you lose it, you will need to regenerate it!
Client Secret: e5a10c867139cbe0da4f8efaaeda4e21f363b4f2

---
✓ OAuth Grant Created Successfully!
Refresh Token: 

eyJhbGciOiJFZERTQSIsInR5cCI6InJ0K2p3dCIsImtpZCI6InJfQWVKWW0zMmJxeWJKdzBQT0xKZlg4V3U4b19lc09vZlJvWXhodC1COUkiLCJqa3UiOiJodHRwOi8vc2FuZGJveC1uZ2lueC5zZXJ2aWNlLmNvbnN1bDo4MDgwL29hdXRoL2p3a3MifQ.eyJpc3MiOiJodHRwczovL2FwaS5zYW5kYm94LnRhc3R5d29ya3MuY29tIiwic3ViIjoiVTY0ODI2MWY0LTk1MzMtNDU2MS1hZTkxLTg2NWFlNGMwZTdlNyIsImlhdCI6MTc4NDExNDIyMSwiYXVkIjoiODAzYzQyNDgtMzU1ZC00MzhkLTkxZjUtMTY4YWVkY2M2ZGRlIiwiZ3JhbnRfaWQiOiJHZGRjNDkxZWUtYmZmZi00NDA5LTk0NTAtNzVlYTM2MTIzMDg3Iiwic2NvcGUiOiJyZWFkIHRyYWRlIG9wZW5pZCJ9.copVqtiqaNchPiBspRh89t5vRlhoLQuylCpsThZXSdtu9Gv6gm8-J4ssHRfdgSagGQdPTN80fVMstWAFE4rYCQ