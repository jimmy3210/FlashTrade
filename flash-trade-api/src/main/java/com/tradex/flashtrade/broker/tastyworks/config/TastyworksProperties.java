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
 *   tastyworks:
 *     base-url: https://api.tastyworks.com
 *     username: ${TASTYWORKS_USERNAME}
 *     password: ${TASTYWORKS_PASSWORD}
 *     session-cache-key-prefix: tastyworks:session:token
 *     mock-enabled: false
 * </pre>
 */
@RefreshScope
@Component
@ConfigurationProperties(prefix = "app.tastyworks")
public class TastyworksProperties {

    /** Tastyworks REST API 根路径 */
    private String baseUrl = "https://api.tastyworks.com";

    /** 登录用户名 */
    private String username;

    /** 登录密码（通过 Docker Secret 注入） */
    private String password;

    /** Session Token 在 Redis 中的 Key 前缀 */
    private String sessionCacheKeyPrefix = "tastyworks:session:token";

    /** true = 返回 Mock 数据（开发/测试用），false = 调用真实 API */
    private boolean mockEnabled = false;

    public String getBaseUrl()                { return baseUrl; }
    public String getUsername()               { return username; }
    public String getPassword()               { return password; }
    public String getSessionCacheKeyPrefix()  { return sessionCacheKeyPrefix; }
    public boolean isMockEnabled()            { return mockEnabled; }

    public void setBaseUrl(String v)               { this.baseUrl               = v; }
    public void setUsername(String v)              { this.username              = v; }
    public void setPassword(String v)              { this.password              = v; }
    public void setSessionCacheKeyPrefix(String v) { this.sessionCacheKeyPrefix = v; }
    public void setMockEnabled(boolean v)          { this.mockEnabled           = v; }
}
