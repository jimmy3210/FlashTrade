package com.tradex.flashtrade.health;

import com.tradex.flashtrade.broker.tastyworks.TastyworksBrokerClient;
import com.tradex.flashtrade.service.BrokerConfigService;
import org.springframework.stereotype.Component;

/** Tastyworks API 连接状态的 HealthIndicator。 */
@Component("tastyworksHealth")
public class TastyworksHealthIndicator extends AbstractBrokerHealthIndicator {

    public TastyworksHealthIndicator(
            TastyworksBrokerClient tastyworksBrokerClient,
            BrokerConfigService brokerConfigService) {
        super(tastyworksBrokerClient, "health-check-account", brokerConfigService);
    }
}
