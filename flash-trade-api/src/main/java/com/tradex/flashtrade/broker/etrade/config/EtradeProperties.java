package com.tradex.flashtrade.broker.etrade.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * E*TRADE API 配置属性
 * <p>
 * 对应 {@code app.etrade.*} 配置项。<br>
 * E*TRADE 使用 OAuth 1.0a 进行签名认证。
 * 敏感字段（consumerKey / consumerSecret）通过环境变量注入。
 */
@Component
@RefreshScope
@ConfigurationProperties(prefix = "app.etrade")
public class EtradeProperties {

    /** OAuth 1.0a Consumer Key（来自 E*TRADE 开发者门户） */
    private String consumerKey;

    /** OAuth 1.0a Consumer Secret */
    private String consumerSecret;

    /** E*TRADE API 基础 URL（Sandbox / Production） */
    private String baseUrl = "https://api.etrade.com/v1";

    /** OAuth 1.0a 请求 Token URL */
    private String requestTokenUrl = "https://api.etrade.com/oauth/request_token";

    /** OAuth 1.0a Access Token URL */
    private String accessTokenUrl = "https://api.etrade.com/oauth/access_token";

    /** Redis 中存储 Access Token 的缓存键前缀 */
    private String tokenCacheKeyPrefix = "etrade:oauth:token";

    /** HTTP 连接超时（毫秒） */
    private int connectTimeoutMs = 5000;

    /** HTTP 读取超时（毫秒） */
    private int readTimeoutMs = 15000;

    // --- Getters & Setters ---

    public String getConsumerKey()                     { return consumerKey; }
    public void setConsumerKey(String v)               { this.consumerKey = v; }

    public String getConsumerSecret()                  { return consumerSecret; }
    public void setConsumerSecret(String v)            { this.consumerSecret = v; }

    public String getBaseUrl()                         { return baseUrl; }
    public void setBaseUrl(String v)                   { this.baseUrl = v; }

    public String getRequestTokenUrl()                 { return requestTokenUrl; }
    public void setRequestTokenUrl(String v)           { this.requestTokenUrl = v; }

    public String getAccessTokenUrl()                  { return accessTokenUrl; }
    public void setAccessTokenUrl(String v)            { this.accessTokenUrl = v; }

    public String getTokenCacheKeyPrefix()             { return tokenCacheKeyPrefix; }
    public void setTokenCacheKeyPrefix(String v)       { this.tokenCacheKeyPrefix = v; }

    public int getConnectTimeoutMs()                   { return connectTimeoutMs; }
    public void setConnectTimeoutMs(int v)             { this.connectTimeoutMs = v; }

    public int getReadTimeoutMs()                      { return readTimeoutMs; }
    public void setReadTimeoutMs(int v)                { this.readTimeoutMs = v; }

}
