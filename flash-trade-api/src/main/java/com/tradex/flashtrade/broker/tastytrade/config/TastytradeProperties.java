package com.tradex.flashtrade.broker.tastytrade.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * Tastytrade 连接配置
 */
@RefreshScope
@Component
@ConfigurationProperties(prefix = "app.tastytrade")
public class TastytradeProperties {

    /** REST API 根路径 */
    private String baseUrl = "https://api.tastytrade.com";

    /** API Key / 用户凭证（可选） */
    private String apiKey;

    /** 是否启用 Mock 模式（开发） */
    private boolean mockEnabled = true;

    public String getBaseUrl() { return baseUrl; }
    public String getApiKey() { return apiKey; }
    public boolean isMockEnabled() { return mockEnabled; }

    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public void setMockEnabled(boolean mockEnabled) { this.mockEnabled = mockEnabled; }
}
