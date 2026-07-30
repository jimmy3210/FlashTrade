package com.tradex.flashtrade.health;

import com.tradex.flashtrade.broker.tastyworks.TastyworksBrokerClient;
import com.tradex.flashtrade.broker.tastyworks.config.TastyworksProperties;
import org.springframework.stereotype.Component;

/** Tastyworks API 连接状态的 HealthIndicator。 */
@Component("tastyworksHealth")
public class TastyworksHealthIndicator extends AbstractBrokerHealthIndicator {

    public TastyworksHealthIndicator(TastyworksBrokerClient tastyworksBrokerClient, TastyworksProperties props) {
        super(tastyworksBrokerClient, "health-check-account", props.isMockEnabled());
    }
}
