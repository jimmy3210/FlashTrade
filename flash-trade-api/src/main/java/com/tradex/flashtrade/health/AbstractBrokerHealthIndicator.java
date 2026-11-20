package com.tradex.flashtrade.health;

import com.tradex.flashtrade.broker.BrokerClient;
import com.tradex.flashtrade.config.BrokerMode;
import com.tradex.flashtrade.service.BrokerConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

/**
 * 向 Actuator 健康检查公开各券商连接状态的基类。
 * <p>
 * 在 {@code GET /actuator/health} 响应中以 {@code components.{brokerName}} 形式展示。
 * Mock 模式下始终返回 UP 状态。
 * </p>
 */
public abstract class AbstractBrokerHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(AbstractBrokerHealthIndicator.class);

    private final BrokerClient brokerClient;
    private final String testAccountId;
    private final BrokerConfigService brokerConfigService;

    protected AbstractBrokerHealthIndicator(BrokerClient brokerClient,
                                            String testAccountId,
                                            BrokerConfigService brokerConfigService) {
        this.brokerClient  = brokerClient;
        this.testAccountId = testAccountId;
        this.brokerConfigService = brokerConfigService;
    }

    @Override
    public Health health() {
        String brokerName = brokerClient.getBrokerName();
        String modeName = "unavailable";
        try {
            BrokerMode mode = brokerConfigService.getEffectiveMode(brokerName);
            modeName = mode.name().toLowerCase();
            if (mode == BrokerMode.MOCK) {
                return Health.up()
                        .withDetail("broker", brokerName)
                        .withDetail("mode", modeName)
                        .build();
            }
            brokerClient.getPortfolio(testAccountId);
            return Health.up()
                    .withDetail("broker", brokerName)
                    .withDetail("mode", modeName)
                    .build();
        } catch (RuntimeException e) {
            log.warn("Health check failed for broker={} mode={} errorType={}",
                    brokerName, modeName, e.getClass().getSimpleName());
            return Health.down()
                    .withDetail("broker", brokerName)
                    .withDetail("mode", modeName)
                    .withDetail("error", "券商模式或连接健康检查失败")
                    .build();
        }
    }
}
