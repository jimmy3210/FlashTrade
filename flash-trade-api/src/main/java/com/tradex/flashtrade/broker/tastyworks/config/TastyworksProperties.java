package com.tradex.flashtrade.broker.tastyworks.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * Tastyworks 连接配置
 * <p>
 * 通过 Spring Cloud Config 动态刷新。
 * </p>
 *
 * <pre>
 * app:
 *   tastytrade:
 *     base-url: https://api.cert.tastyworks.com
 *     username: ${TASTYTRADE_USERNAME}
 *     password: ${TASTYTRADE_PASSWORD}
 *     session-cache-key-prefix: tastytrade:session:token
 * </pre>
 */
@RefreshScope
@Component
@ConfigurationProperties(prefix = "app.tastytrade")
public class TastyworksProperties {

    /** Tastytrade REST API 根路径（官方 URL 可能仍保留 tastyworks legacy 域名） */
    private String baseUrl = "https://api.cert.tastyworks.com";

    /** 登录用户名 */
    private String username;

    /** 登录密码（通过 Docker Secret 注入） */
    private String password;

    /** Session Token 在 Redis 中的 Key 前缀 */
    private String sessionCacheKeyPrefix = "tastyworks:session:token";

    public String getBaseUrl()                { return baseUrl; }
    public String getUsername()               { return username; }
    public String getPassword()               { return password; }
    public String getSessionCacheKeyPrefix()  { return sessionCacheKeyPrefix; }

    public void setBaseUrl(String v)               { this.baseUrl               = v; }
    public void setUsername(String v)              { this.username              = v; }
    public void setPassword(String v)              { this.password              = v; }
    public void setSessionCacheKeyPrefix(String v) { this.sessionCacheKeyPrefix = v; }
}
