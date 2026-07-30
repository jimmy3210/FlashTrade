# Tastytrade OAuth2 / Token 说明

## 1. OAuth2 基础

tastytrade 采用 OAuth2 进行身份认证，所有 API 请求都需要使用 `access_token`。

## 2. 创建 OAuth 应用

开发者需要在 tastytrade 账户中创建 OAuth Application，获得：

- `client_id`
- `client_secret`

## 3. 生成 Grant

可以在 my.tastytrade.com 中创建个人 Grant，获得 `refresh_token`。

## 4. 获取 Access Token

### 接口

```http
POST https://api.tastyworks.com/oauth/token
```

Sandbox：

```http
POST https://api.cert.tastyworks.com/oauth/token
```

### 请求体

```text
grant_type=refresh_token
refresh_token=<refresh_token>
client_secret=<client_secret>
```

### 响应示例

```json
{
  "access_token": "eyJhbGciOi...",
  "token_type": "Bearer",
  "expires_in": 900,
  "refresh_token": "..."
}
```

## 5. 使用 Authorization Header

```http
Authorization: Bearer <access_token>
User-Agent: your-client/1.0
```

## 6. 注意事项

- Access Token 通常 15 分钟有效
- 失效后需要重新获取
- `client_secret` 需要妥善保管
- 建议先在 Sandbox 环境测试
