package com.tradex.flashtrade.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * 会话 Token 表 - 统一支持多种认证方式（OAuth 2.0 / OAuth 1.0a / API Key / Session）
 * 
 * 设计原理：使用"统一表+类型字段"模式，而非多表分离
 * - 优势：简化代码（1个Entity vs 3个），易于维护和扩展
 * - 字段映射由 auth_type 字段区分：
 *   - BEARER (OAuth 2.0): token + refresh_token
 *   - OAUTH1A (etrade): token + token_secret
 *   - APIKEY: token（直接使用）
 *   - SESSION: token（session_token）
 */
@Entity
@Table(
    name = "session_tokens",
    indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_auth_type", columnList = "auth_type"),
        @Index(name = "idx_expires_at", columnList = "expires_at"),
        @Index(name = "idx_user_auth_type", columnList = "user_id,auth_type"),
        @Index(name = "idx_token_auth_type", columnList = "auth_type,expires_at")
    },
    uniqueConstraints = @UniqueConstraint(
        name = "uk_active_token",
        columnNames = {"user_id", "broker_account_id", "auth_type"}
    )
)
public class SessionToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "broker_id", nullable = false)
    private Long brokerId;

    @Column(name = "broker_account_id", nullable = false, length = 100)
    private String brokerAccountId;

    // ===== 认证方式标识 =====
    /**
     * 认证类型枚举：
     * - BEARER: OAuth 2.0（Schwab）
     * - OAUTH1A: OAuth 1.0a（etrade）
     * - APIKEY: API Key（Tastytrade）
     * - SESSION: Session Token（Tastyworks）
     */
    @Column(name = "auth_type", nullable = false, length = 20)
    private String authType;

    @Column(name = "token_type", length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'Bearer'")
    private String tokenType = "Bearer";

    // ===== 通用 Token 字段 =====
    /**
     * 主要 token（根据认证类型的具体含义）：
     * - OAuth 2.0: access_token
     * - OAuth 1.0a: oauth_token
     * - API Key: api_key
     * - Session: session_token
     */
    @Column(name = "token", nullable = false, columnDefinition = "TEXT")
    private String token;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // ===== OAuth 2.0 特定字段（Schwab） =====
    /**
     * OAuth 2.0 刷新令牌（用于获取新的 access_token）
     * NULL for OAuth 1.0a, API Key, Session types
     */
    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    /**
     * OAuth 2.0 授权范围
     * 例如：PlaceTrades, ReadAccount, ModifyOrders
     */
    @Column(name = "scope", length = 255)
    private String scope;

    // ===== OAuth 1.0a 特定字段（etrade） =====
    /**
     * OAuth 1.0a Token Secret
     * 用于生成 HMAC-SHA1 签名（与 consumer_secret 一起）
     * NULL for OAuth 2.0, API Key, Session types
     */
    @Column(name = "token_secret", length = 255)
    private String tokenSecret;

    /**
     * Consumer Key（冗余存储，便于查询和审计）
     * NULL for OAuth 2.0, API Key, Session types
     */
    @Column(name = "consumer_key", length = 255)
    private String consumerKey;

    /**
     * OAuth 1.0a 授权验证码（临时，授权流程中使用）
     * 获得 access_token 后可删除
     * NULL for OAuth 2.0, API Key, Session types
     */
    @Column(name = "oauth_verifier", length = 255)
    private String oauthVerifier;

    /**
     * OAuth 1.0a Token 过期时间（单独管理，因为其过期机制不同于 OAuth 2.0）
     * NULL for OAuth 2.0, API Key, Session types
     */
    @Column(name = "oauth_expires_at")
    private LocalDateTime oauthExpiresAt;

    // ===== 使用追踪 =====
    /**
     * 最后一次使用时间（用于监控和清理）
     */
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ===== Business Logic Methods =====

    /**
     * 检查 token 是否已过期
     * 根据 auth_type 使用不同的过期字段：
     * - OAuth 2.0: expires_at
     * - OAuth 1.0a: oauth_expires_at
     */
    public boolean isExpired() {
        LocalDateTime expirationTime = getEffectiveExpiresAt();
        return expirationTime != null && LocalDateTime.now().isAfter(expirationTime);
    }

    /**
     * 检查 token 是否即将过期（5分钟内）
     */
    public boolean isExpiringSoon() {
        LocalDateTime expirationTime = getEffectiveExpiresAt();
        if (expirationTime == null) {
            return false;
        }
        LocalDateTime soonThreshold = LocalDateTime.now().plusMinutes(5);
        return LocalDateTime.now().isBefore(expirationTime) 
            && expirationTime.isBefore(soonThreshold);
    }

    /**
     * 获取有效的过期时间
     * - OAuth 2.0: 使用 expires_at
     * - OAuth 1.0a: 使用 oauth_expires_at
     */
    private LocalDateTime getEffectiveExpiresAt() {
        if ("OAUTH1A".equals(authType)) {
            return oauthExpiresAt;
        }
        return expiresAt;
    }

    /**
     * 检查是否可用（未过期且有效）
     */
    public boolean isValid() {
        return !isExpired() && token != null && !token.isEmpty();
    }

    /**
     * 检查是否为 OAuth 2.0 token
     */
    public boolean isOAuth2() {
        return "BEARER".equals(authType);
    }

    /**
     * 检查是否为 OAuth 1.0a token
     */
    public boolean isOAuth1a() {
        return "OAUTH1A".equals(authType);
    }

    /**
     * 检查是否为 API Key
     */
    public boolean isApiKey() {
        return "APIKEY".equals(authType);
    }

    /**
     * 检查是否为 Session Token
     */
    public boolean isSession() {
        return "SESSION".equals(authType);
    }

    // ===== Getters and Setters =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getBrokerId() { return brokerId; }
    public void setBrokerId(Long brokerId) { this.brokerId = brokerId; }

    public String getBrokerAccountId() { return brokerAccountId; }
    public void setBrokerAccountId(String brokerAccountId) { this.brokerAccountId = brokerAccountId; }

    public String getAuthType() { return authType; }
    public void setAuthType(String authType) { this.authType = authType; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

    public String getTokenSecret() { return tokenSecret; }
    public void setTokenSecret(String tokenSecret) { this.tokenSecret = tokenSecret; }

    public String getConsumerKey() { return consumerKey; }
    public void setConsumerKey(String consumerKey) { this.consumerKey = consumerKey; }

    public String getOauthVerifier() { return oauthVerifier; }
    public void setOauthVerifier(String oauthVerifier) { this.oauthVerifier = oauthVerifier; }

    public LocalDateTime getOauthExpiresAt() { return oauthExpiresAt; }
    public void setOauthExpiresAt(LocalDateTime oauthExpiresAt) { this.oauthExpiresAt = oauthExpiresAt; }

    public LocalDateTime getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(LocalDateTime lastUsedAt) { this.lastUsedAt = lastUsedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "SessionToken{" +
                "id=" + id +
                ", userId=" + userId +
                ", brokerId=" + brokerId +
                ", brokerAccountId='" + brokerAccountId + '\'' +
                ", authType='" + authType + '\'' +
                ", expiresAt=" + expiresAt +
                ", createdAt=" + createdAt +
                '}';
    }
}
