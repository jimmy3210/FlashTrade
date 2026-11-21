package com.tradex.flashtrade.broker.schwab.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * 嘉信证券（Charles Schwab）API 配置属性
 * <p>
 * 对应 application.yml 中 {@code app.schwab.*} 配置项。
 * {@code @RefreshScope} 允许调用 {@code POST /actuator/refresh} 后
 * 无需重启应用即可动态加载更新后的连接配置。
 * 敏感字段（clientId / clientSecret）通过环境变量注入，禁止硬编码。
 */
@Component
@RefreshScope
@ConfigurationProperties(prefix = "app.schwab")
public class SchwabProperties {

    /** OAuth2 客户端 ID（来自 developer.schwab.com 控制台） */
    private String clientId;

    /** OAuth2 客户端密钥 */
    private String clientSecret;

    /** OAuth2 回调地址（需与控制台注册地址一致） */
    private String redirectUri = "https://127.0.0.1";

    /** Schwab Trader API 基础 URL */
    private String baseUrl = "https://api.schwabapi.com/trader/v1";

    /** OAuth2 Token 端点 URL */
    private String tokenUrl = "https://api.schwabapi.com/v1/oauth/token";

    /**
     * Redis 中存储 Access Token 的缓存键前缀。
     * 实际键：{tokenCacheKeyPrefix}:{accountId}
     */
    private String tokenCacheKeyPrefix = "schwab:oauth:token";

    /** Access Token 提前刷新阈值（秒），在过期前此值秒数开始刷新 */
    private int tokenRefreshThresholdSeconds = 120;

    /** HTTP 连接超时（毫秒） */
    private int connectTimeoutMs = 5000;

    /** HTTP 读取超时（毫秒） */
    private int readTimeoutMs = 15000;

    // --- Getters & Setters ---

    public String getClientId()                        { return clientId; }
    public void setClientId(String v)                  { this.clientId = v; }

    public String getClientSecret()                    { return clientSecret; }
    public void setClientSecret(String v)              { this.clientSecret = v; }

    public String getRedirectUri()                     { return redirectUri; }
    public void setRedirectUri(String v)               { this.redirectUri = v; }

    public String getBaseUrl()                         { return baseUrl; }
    public void setBaseUrl(String v)                   { this.baseUrl = v; }

    public String getTokenUrl()                        { return tokenUrl; }
    public void setTokenUrl(String v)                  { this.tokenUrl = v; }

    public String getTokenCacheKeyPrefix()             { return tokenCacheKeyPrefix; }
    public void setTokenCacheKeyPrefix(String v)       { this.tokenCacheKeyPrefix = v; }

    public int getTokenRefreshThresholdSeconds()       { return tokenRefreshThresholdSeconds; }
    public void setTokenRefreshThresholdSeconds(int v) { this.tokenRefreshThresholdSeconds = v; }

    public int getConnectTimeoutMs()                   { return connectTimeoutMs; }
    public void setConnectTimeoutMs(int v)             { this.connectTimeoutMs = v; }

    public int getReadTimeoutMs()                      { return readTimeoutMs; }
    public void setReadTimeoutMs(int v)                { this.readTimeoutMs = v; }

}
