package com.tradex.flashtrade.health;

import com.tradex.flashtrade.broker.schwab.SchwabBrokerClient;
import com.tradex.flashtrade.broker.schwab.config.SchwabProperties;
import org.springframework.stereotype.Component;

/** Schwab API 连接状态的 HealthIndicator。 */
@Component("schwabHealth")
public class SchwabHealthIndicator extends AbstractBrokerHealthIndicator {

    public SchwabHealthIndicator(SchwabBrokerClient schwabBrokerClient, SchwabProperties props) {
        super(schwabBrokerClient, "health-check-account", props.isMockEnabled());
    }
}
