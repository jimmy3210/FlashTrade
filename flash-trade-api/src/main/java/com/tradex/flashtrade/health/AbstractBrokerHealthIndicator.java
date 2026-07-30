package com.tradex.flashtrade.health;

import com.tradex.flashtrade.broker.BrokerClient;
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
    private final boolean mockEnabled;

    protected AbstractBrokerHealthIndicator(BrokerClient brokerClient,
                                            String testAccountId,
                                            boolean mockEnabled) {
        this.brokerClient  = brokerClient;
        this.testAccountId = testAccountId;
        this.mockEnabled   = mockEnabled;
    }

    @Override
    public Health health() {
        String brokerName = brokerClient.getBrokerName();
        if (mockEnabled) {
            return Health.up()
                    .withDetail("broker", brokerName)
                    .withDetail("mode", "mock")
                    .build();
        }
        try {
            brokerClient.getPortfolio(testAccountId);
            return Health.up()
                    .withDetail("broker", brokerName)
                    .withDetail("mode", "live")
                    .build();
        } catch (Exception e) {
            log.warn("Health check failed for broker={}: {}", brokerName, e.getMessage());
            return Health.down()
                    .withDetail("broker", brokerName)
                    .withDetail("mode", "live")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
