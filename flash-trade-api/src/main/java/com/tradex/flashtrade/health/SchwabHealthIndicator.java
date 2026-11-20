package com.tradex.flashtrade.health;

import com.tradex.flashtrade.broker.schwab.SchwabBrokerClient;
import com.tradex.flashtrade.service.BrokerConfigService;
import org.springframework.stereotype.Component;

/** Schwab API 连接状态的 HealthIndicator。 */
@Component("schwabHealth")
public class SchwabHealthIndicator extends AbstractBrokerHealthIndicator {

    public SchwabHealthIndicator(
            SchwabBrokerClient schwabBrokerClient,
            BrokerConfigService brokerConfigService) {
        super(schwabBrokerClient, "health-check-account", brokerConfigService);
    }
}
