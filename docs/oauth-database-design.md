# OAuth 1.0a vs 2.0 数据库设计方案分析

## 1. 现状分析

### 项目中的认证类型

| 券商 | 认证类型 | 字段差异 | TokenService |
|------|---------|---------|--------------|
| **Schwab** | OAuth 2.0 | access_token + refresh_token | SchwabTokenService |
| **etrade** | OAuth 1.0a | oauth_token + oauth_token_secret | *(待实现)* |
| **Tastyworks** | Session (用户名/密码) | session_token | TastyworksTokenService |
| **Tastytrade** | API Key | N/A | TastytradeTokenService |

### OAuth 1.0a vs OAuth 2.0 的关键差异

| 维度 | OAuth 1.0a (etrade) | OAuth 2.0 (Schwab) |
|------|------------------|-----------------|
| **Token 数** | 2 个：`oauth_token` + `oauth_token_secret` | 2 个：`access_token` + `refresh_token` |
| **签名** | 所有请求需要 HMAC-SHA1 签名 | Bearer Token in Header |
| **刷新** | 无原生刷新机制，需重新授权 | 使用 refresh_token 刷新 |
| **Consumer Secret** | 需要 Consumer Secret（来自 app key） | 不需要（只用于换 token）|
| **临时验证码** | oauth_verifier（授权流程） | authorization_code（授权流程） |
| **URL 签名** | Request/Access Token URL 中包含参数 | 只在 body 中 |

---

## 2. 方案对比

### ❌ 方案 A：分离表（分别创建 session_tokens_oauth1a 和 session_tokens_oauth2）

**表结构**

```sql
-- OAuth 2.0 Token 表（Schwab 等）
CREATE TABLE session_tokens_oauth2 (
    id                  BIGINT,
    user_id             BIGINT,
    broker_account_id   BIGINT,
    access_token        TEXT NOT NULL,
    refresh_token       TEXT NOT NULL,
    expires_at          DATETIME(6),
    scope               VARCHAR(255),
    token_type          VARCHAR(20) DEFAULT 'Bearer',
    ...
);

-- OAuth 1.0a Token 表（etrade 等）
CREATE TABLE session_tokens_oauth1a (
    id                  BIGINT,
    user_id             BIGINT,
    broker_account_id   BIGINT,
    consumer_key        VARCHAR(255),
    consumer_secret     VARCHAR(255),
    oauth_token         VARCHAR(255) NOT NULL,
    oauth_token_secret  VARCHAR(255) NOT NULL,
    oauth_verifier      VARCHAR(255),
    oauth_expires_at    DATETIME(6),
    ...
);

-- API Key / Session Token 表
CREATE TABLE session_tokens_apikey (
    id                  BIGINT,
    user_id             BIGINT,
    broker_account_id   BIGINT,
    session_token       TEXT NOT NULL,
    expires_at          DATETIME(6),
    ...
);
```

**优点**
- ✅ 表结构清晰，字段紧凑（无大量 NULL）
- ✅ 查询性能高（字段少，索引简单）
- ✅ 每个表只关心特定认证类型的字段
- ✅ 未来扩展新认证类型时容易添加

**缺点**
- ❌ 代码复杂（需要 3 个 Entity、3 个 Repository）
- ❌ 业务逻辑需要判断 broker_id，然后查询对应的表
- ❌ 公共查询困难（如查询"用户的所有 token"需要 UNION）
- ❌ 数据库迁移复杂（会有冗余表）
- ❌ 未来如果有多个同类型，又要分表

---

### ✅ 方案 B：统一表 + 类型字段（推荐）

**表结构**

```sql
CREATE TABLE session_tokens (
    id                      BIGINT,
    user_id                 BIGINT,
    broker_id               BIGINT,
    broker_account_id       BIGINT,
    
    -- 类型标识
    auth_type               VARCHAR(20) NOT NULL COMMENT 'OAUTH2 / OAUTH1A / APIKEY / SESSION',
    token_type              VARCHAR(20) DEFAULT 'Bearer',
    
    -- 通用字段（所有类型都有）
    token                   TEXT NOT NULL COMMENT '主要 token',
    expires_at              DATETIME(6),
    
    -- OAuth 2.0 特定字段（Schwab）
    refresh_token           TEXT NULL,
    scope                   VARCHAR(255) NULL,
    
    -- OAuth 1.0a 特定字段（etrade）
    token_secret            VARCHAR(255) NULL COMMENT 'OAuth 1.0a Token Secret',
    consumer_key            VARCHAR(255) NULL COMMENT 'Consumer Key（冗余）',
    verifier                VARCHAR(255) NULL COMMENT 'OAuth 授权验证码',
    
    -- 时间戳
    created_at              DATETIME(6),
    updated_at              DATETIME(6),
    last_used_at            DATETIME(6),
    
    PRIMARY KEY (id),
    UNIQUE KEY uq_active_token (user_id, broker_id, broker_account_id, auth_type),
    INDEX idx_user_id (user_id),
    INDEX idx_expires_at (expires_at)
);
```

**优点**
- ✅ 简洁：只需 1 个 Entity、1 个 Repository
- ✅ 业务逻辑简单：根据 `auth_type` 字段分支即可
- ✅ 公共查询容易：直接查询 session_tokens 表
- ✅ 扩展性强：新增认证类型只需加字段，不需加表
- ✅ 维护性好：所有 token 在一个地方管理
- ✅ 与现有设计兼容（之前的 session_tokens 就是这个思路）

**缺点**
- ⚠️ 表宽（18+ 列），某些字段会是 NULL（但这是可接受的）
- ⚠️ 需要在代码中处理字段的选择性使用

---

### 🟡 方案 C：混合方案（使用 JSON 字段存储认证相关数据）

**表结构**

```sql
CREATE TABLE session_tokens (
    id                  BIGINT,
    user_id             BIGINT,
    broker_account_id   BIGINT,
    auth_type           VARCHAR(20) NOT NULL,
    
    -- 通用字段
    primary_token       TEXT NOT NULL,
    expires_at          DATETIME(6),
    
    -- 灵活存储：所有认证类型的额外信息
    auth_details        JSON NOT NULL COMMENT '{"refresh_token": "...", "consumer_secret": "...", ...}',
    
    created_at          DATETIME(6),
    ...
);

-- 示例数据
-- OAuth 2.0:
-- auth_details = {"refresh_token": "abc123", "scope": "..."}

-- OAuth 1.0a:
-- auth_details = {"token_secret": "xyz789", "consumer_key": "key1", "consumer_secret": "secret1"}

-- Session:
-- auth_details = {"username": "user@example.com"}
```

**优点**
- ✅ 表结构非常清晰（只 5 列）
- ✅ 完全灵活：不同认证类型的字段随意组合
- ✅ 未来任意认证类型都能支持，无需修改表

**缺点**
- ❌ 字段不可直接索引（无法高效查询 `refresh_token` 之类）
- ❌ SQL 查询复杂（需要使用 JSON 函数）
- ❌ ORM 映射困难（需要自定义 serializer）
- ❌ 数据一致性难以保证

---

## 3. 推荐方案：**方案 B（统一表 + 类型字段）**

### 原因

1. **现有设计已经在用这个思路**  
   V12 迁移脚本中的 `session_tokens` 表就包含了 `auth_type` 字段的雏形。

2. **平衡了简洁性和扩展性**  
   - 代码实现简单：只需一套 Entity/Repository/Service
   - 业务逻辑清晰：`if (token.getAuthType() == OAUTH1A)`
   - 扩展容易：新认证类型加字段即可

3. **性能可接受**  
   - NULL 字段不占用磁盘空间（InnoDB 特性）
   - 查询性能不会显著下降（索引是 auth_type + user_id）
   - 某些字段可以动态索引（例如仅为 oauth1a 的记录索引 token_secret）

4. **维护性更强**  
   - 审计日志、数据清理、权限管理都更简单
   - 团队协作更容易（不用理解多个表的差异）

---

## 4. 具体实施方案

### 方案 B 的 SQL 改进（相比之前的 V16）

```sql
-- 更新 V16 的 session_tokens 表设计
ALTER TABLE session_tokens
ADD COLUMN auth_type VARCHAR(20) NOT NULL DEFAULT 'BEARER' 
    COMMENT 'OAUTH2 / OAUTH1A / APIKEY / SESSION' AFTER token_type;

-- OAuth 2.0 特定字段
ALTER TABLE session_tokens
ADD COLUMN refresh_token TEXT NULL COMMENT 'OAuth 2.0 Refresh Token'
ADD COLUMN scope VARCHAR(255) NULL COMMENT 'OAuth 2.0 Scope';

-- 现有的 OAuth 1.0a 字段保留（已在 V16 中添加）
-- token_secret, consumer_key, verifier 等

-- 添加对应类型的索引
ALTER TABLE session_tokens
ADD INDEX idx_token_auth_type (auth_type, expires_at)
ADD INDEX idx_user_auth_type (user_id, auth_type);
```

### 方案 B 的 Entity 设计

```java
@Entity
@Table(name = "session_tokens")
public class SessionToken {
    // 基础字段
    private Long id;
    private Long userId;
    private Long brokerId;
    private Long brokerAccountId;
    
    @Enumerated(EnumType.STRING)
    private AuthType authType;  // OAUTH2 / OAUTH1A / APIKEY / SESSION
    
    @Enumerated(EnumType.STRING)
    private String tokenType;  // BEARER / OAUTH1A
    
    // 通用字段
    @Lob
    private String token;
    private LocalDateTime expiresAt;
    
    // === OAuth 2.0 (Schwab) ===
    @Lob
    private String refreshToken;
    private String scope;
    
    // === OAuth 1.0a (etrade) ===
    private String tokenSecret;
    private String consumerKey;
    private String verifier;
    private LocalDateTime oauthExpiresAt;
    
    // 时间戳
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastUsedAt;
}

// 枚举
public enum AuthType {
    OAUTH2,   // Schwab 等
    OAUTH1A,  // etrade
    APIKEY,   // Tastytrade
    SESSION   // Tastyworks
}
```

### 方案 B 的业务逻辑

```java
@Service
public class TokenService {
    
    public String getValidToken(Long brokerId, Long accountId) {
        SessionToken token = sessionTokenRepository.findActiveToken(accountId);
        
        if (token == null) {
            throw new TokenNotFoundException();
        }
        
        // 根据类型处理
        switch (token.getAuthType()) {
            case OAUTH2:
                return handleOAuth2Token(token);
            case OAUTH1A:
                return handleOAuth1aToken(token);
            case APIKEY:
                return token.getToken();
            case SESSION:
                return token.getToken();
            default:
                throw new UnsupportedAuthTypeException();
        }
    }
    
    private String handleOAuth2Token(SessionToken token) {
        if (token.isExpired()) {
            refreshOAuth2Token(token);  // 使用 refresh_token
        }
        return token.getToken();
    }
    
    private String handleOAuth1aToken(SessionToken token) {
        if (token.isExpired()) {
            throw new TokenExpiredException("OAuth 1.0a 需要重新授权");
        }
        // OAuth 1.0a 的签名在调用时处理，不在这里处理
        return token.getToken();
    }
}
```

---

## 5. 对比总结表

| 方案 | 表数 | Entity | NULL字段 | 查询性能 | 代码复杂度 | 扩展性 | 推荐度 |
|------|-----|--------|---------|---------|-----------|--------|--------|
| **A（分表）** | 3 | 3 | 0 | 高 | 高 | 中等 | ⚠️ 不推荐 |
| **B（统一+类型）** | 1 | 1 | 中等 | 中等 | 低 | 高 | ⭐⭐⭐⭐⭐ |
| **C（JSON）** | 1 | 1 | 0 | 低 | 高 | 很高 | ⚠️ 暂不考虑 |

---

## 6. 最终建议

### ✅ 采用方案 B（统一表 + 类型字段）

**理由**：
1. 与项目现有设计一致
2. 代码和业务逻辑简洁清晰
3. 支持当前和未来的各种认证类型
4. 维护和扩展成本最低

### 📝 实施步骤

1. 保留现有 V16 迁移脚本（已经是方案 B）
2. **补充字段**：
   - 添加 `auth_type` 字段（已在 V16 中）
   - 添加 `refresh_token` 和 `scope`（OAuth 2.0）
   - OAuth 1.0a 字段已完备

3. **更新 Entity**：
   - 添加对应的字段
   - 创建 `AuthType` 枚举

4. **创建 Service 层**：
   - 统一的 `TokenService` 根据 `auth_type` 分支
   - 各个 TokenService 仍然保留（SchwabTokenService、EtradeTokenService 等）

---

## 7. 后续维护

### 定期清理过期 Token

```sql
-- 每天凌晨 2 点清理过期 token
DELETE FROM session_tokens 
WHERE expires_at IS NOT NULL AND expires_at < NOW() 
AND auth_type IN ('OAUTH2', 'APIKEY');
```

### Token 更新检查

```sql
-- 监控 token 即将过期的记录
SELECT * FROM session_tokens
WHERE expires_at IS NOT NULL 
AND expires_at < DATE_ADD(NOW(), INTERVAL 5 MINUTE)
AND auth_type IN ('OAUTH2', 'APIKEY')
ORDER BY expires_at;
```

---

## 结论

**不需要为 OAuth 1.0a 和 2.0 创建不同的表**。

使用单一的 `session_tokens` 表配合 `auth_type` 字段来区分认证类型，是**最优方案**：
- ✅ 简洁高效
- ✅ 易于维护和扩展
- ✅ 与项目设计一致
- ✅ 代码复杂度最低

现有的 V16 迁移脚本已经遵循了这个设计，只需在上面稍作调整（添加 OAuth 2.0 字段）即可完全支持所有认证类型。
