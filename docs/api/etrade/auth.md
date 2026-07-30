# E*TRADE OAuth 1.0a 认证说明

## 1. OAuth 1.0a 基础

E*TRADE 采用 **OAuth 1.0a** 进行身份认证（注意不是 OAuth2），所有 API 请求都需要使用 OAuth 签名。

## 2. 获取 API 凭证

开发者需要在 E*TRADE Developer 平台申请 OAuth 应用，获得：

- **Consumer Key**（公钥）
- **Consumer Secret**（私钥）

申请地址：[https://developer.etrade.com/home](https://developer.etrade.com/home)

### 环境区分

- **Sandbox**：用于开发和测试，可免费申请
  - 认证服务器：`https://etwssandbox.etrade.com/oauth`
  - API 服务器：`https://apisb.etrade.com`
  
- **Production**：生产环境
  - 认证服务器：`https://us.etrade.com/e/t/etws/authorize`
  - API 服务器：`https://api.etrade.com`

## 3. OAuth 1.0a 认证流程

### 3.1 第一步：获取 Request Token

```http
GET https://etwssandbox.etrade.com/oauth/request_token
?oauth_consumer_key=YOUR_CONSUMER_KEY
&oauth_signature_method=HMAC-SHA1
&oauth_signature=SIGNATURE
&oauth_timestamp=TIMESTAMP
&oauth_nonce=NONCE
&oauth_version=1.0
```

**响应示例**（URL 编码格式）：

```
oauth_token=TOKEN_VALUE
&oauth_token_secret=SECRET_VALUE
&oauth_expires_in=600
```

### 3.2 第二步：用户授权

使用第一步获得的 `oauth_token`，打开授权页面让用户登录：

```
https://etwssandbox.etrade.com/oauth/authorize?oauth_token=TOKEN_VALUE
```

用户登录并同意授权后，E*TRADE 会重定向到回调 URL（配置在开发者后台），并携带 `oauth_token` 和 `oauth_verifier`。

### 3.3 第三步：获取 Access Token

使用第二步的 `oauth_verifier` 交换 Access Token：

```http
GET https://etwssandbox.etrade.com/oauth/renew_access_token
?oauth_consumer_key=YOUR_CONSUMER_KEY
&oauth_token=TOKEN_VALUE
&oauth_signature_method=HMAC-SHA1
&oauth_signature=SIGNATURE
&oauth_timestamp=TIMESTAMP
&oauth_nonce=NONCE
&oauth_version=1.0
&oauth_verifier=VERIFIER_VALUE
```

**响应示例**：

```
oauth_token=ACCESS_TOKEN
&oauth_token_secret=ACCESS_TOKEN_SECRET
&oauth_expires_in=1800
```

## 4. OAuth 签名生成

### 4.1 签名算法

- 方法：**HMAC-SHA1**
- 签名基础字符串包括：HTTP 方法、URL、所有参数（按字母顺序排序）

### 4.2 关键参数

- `oauth_consumer_key`：Consumer Key
- `oauth_token`：Access Token（API 调用时必需）
- `oauth_signature_method`：固定为 `HMAC-SHA1`
- `oauth_signature`：HMAC-SHA1 签名值
- `oauth_timestamp`：当前时间戳（秒）
- `oauth_nonce`：随机字符串，每次请求不同
- `oauth_version`：固定为 `1.0`

### 4.3 签名密钥

签名密钥为：`CONSUMER_SECRET&TOKEN_SECRET`

例如：
```
my_consumer_secret&my_access_token_secret
```

如果还未获得 Access Token，则为：
```
my_consumer_secret&
```

## 5. 使用 Authorization Header

所有 API 请求都需要在请求头中添加 OAuth 签名。

### 示例

```http
GET /v1/accounts/list HTTP/1.1
Host: apisb.etrade.com
Authorization: OAuth oauth_consumer_key="YOUR_CONSUMER_KEY",
  oauth_token="ACCESS_TOKEN",
  oauth_signature_method="HMAC-SHA1",
  oauth_signature="SIGNATURE_VALUE",
  oauth_timestamp="1234567890",
  oauth_nonce="ABC123",
  oauth_version="1.0"
User-Agent: FlashTrade-etrade-client/1.0
Accept: application/json
```

## 6. Token 管理

### 6.1 Token 有效期

- **Request Token**：10 分钟（`oauth_expires_in=600`）
- **Access Token**：30 分钟（`oauth_expires_in=1800`）

### 6.2 Token 刷新

Access Token 过期后，无法直接刷新，需要：

1. **在线用户流**：重新执行 OAuth 流程（重定向用户授权）
2. **离线流程**（如果支持）：使用预先获取的 Refresh Token（具体机制查看官方文档）

### 6.3 错误处理

当 Access Token 过期或无效时，API 会返回：

```
HTTP 401 Unauthorized
OAuth oauth_problem="token_expired"
```

此时需要重新获取 Access Token 或刷新认证。

## 7. 安全建议

- ✅ `Consumer Secret` 和 `Token Secret` 必须妥善保管，不能暴露在客户端
- ✅ 建议先在 Sandbox 环境完整测试认证和 API 调用流程
- ✅ 每次请求生成新的 `oauth_nonce`，避免重放攻击
- ✅ 使用 HTTPS 传输所有认证相关数据
- ✅ 定期检查 Token 有效期，提前刷新

## 8. 官方文档参考

- 官方 API 文档：[https://apisb.etrade.com/docs/api/account/api-account-v1.html](https://apisb.etrade.com/docs/api/account/api-account-v1.html)
- OAuth 1.0 规范：[https://oauth.net/core/1.0a/](https://oauth.net/core/1.0a/)
- E*TRADE 开发者社区：[https://developer.etrade.com](https://developer.etrade.com)
