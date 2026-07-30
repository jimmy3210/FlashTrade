package com.tradex.flashtrade.fix.config;

import com.tradex.flashtrade.fix.adapter.Fix42MessageAdapter;
import com.tradex.flashtrade.fix.adapter.Fix44MessageAdapter;
import com.tradex.flashtrade.fix.adapter.Fix50MessageAdapter;
import com.tradex.flashtrade.fix.audit.FixAuditLogger;
import com.tradex.flashtrade.fix.core.FixApplicationHandler;
import com.tradex.flashtrade.fix.core.FixGatewayService;
import com.tradex.flashtrade.fix.core.FixSessionManager;
import com.tradex.flashtrade.fix.routing.FixMessageDispatcher;
import com.tradex.flashtrade.fix.routing.FixVersionRouter;
import com.tradex.flashtrade.fix.security.FixRateLimiter;
import com.tradex.flashtrade.fix.test.FixTestAcceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * FIX Gateway の Bean 登録設定
 * flashtrade.fix.enabled=true の場合のみ有効化
 */
@Configuration
@EnableConfigurationProperties(FixGatewayProperties.class)
@ConditionalOnProperty(prefix = "flashtrade.fix", name = "enabled", havingValue = "true", matchIfMissing = false)
public class FixGatewayAutoConfiguration {

    @Bean
    public Fix42MessageAdapter fix42MessageAdapter() {
        return new Fix42MessageAdapter();
    }

    @Bean
    public Fix44MessageAdapter fix44MessageAdapter() {
        return new Fix44MessageAdapter();
    }

    @Bean
    public Fix50MessageAdapter fix50MessageAdapter() {
        return new Fix50MessageAdapter();
    }

    @Bean
    public FixVersionRouter fixVersionRouter(
            Fix42MessageAdapter fix42,
            Fix44MessageAdapter fix44,
            Fix50MessageAdapter fix50
    ) {
        return new FixVersionRouter(fix42, fix44, fix50);
    }

    @Bean
    public FixAuditLogger fixAuditLogger() {
        return new FixAuditLogger();
    }

    @Bean
    public FixRateLimiter fixRateLimiter(FixGatewayProperties properties) {
        return new FixRateLimiter(properties.rateLimit().messagesPerSecond());
    }

    @Bean
    public FixMessageDispatcher fixMessageDispatcher() {
        return new FixMessageDispatcher();
    }

    @Bean
    public FixApplicationHandler fixApplicationHandler(
            FixVersionRouter versionRouter,
            FixMessageDispatcher dispatcher,
            FixAuditLogger auditLogger,
            FixRateLimiter rateLimiter,
            FixSessionManager sessionManager
    ) {
        return new FixApplicationHandler(versionRouter, dispatcher, auditLogger, rateLimiter, sessionManager);
    }

    @Bean
    public FixSessionManager fixSessionManager() {
        return new FixSessionManager();
    }

    @Bean
    public FixGatewayService fixGatewayService(
            FixGatewayProperties properties,
            FixApplicationHandler applicationHandler
    ) {
        return new FixGatewayService(properties, applicationHandler);
    }

    @Bean
    @ConditionalOnProperty(prefix = "flashtrade.fix.test-server", name = "enabled", havingValue = "true")
    public FixTestAcceptor fixTestAcceptor(FixGatewayProperties properties) {
        return new FixTestAcceptor(properties.testServer());
    }
}
