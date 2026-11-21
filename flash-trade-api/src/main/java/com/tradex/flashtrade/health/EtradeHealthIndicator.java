package com.tradex.flashtrade.health;

import com.tradex.flashtrade.broker.etrade.EtradeBrokerClient;
import com.tradex.flashtrade.service.BrokerConfigService;
import org.springframework.stereotype.Component;

/** E*TRADE API 连接状态的 HealthIndicator。 */
@Component("etradeHealth")
public class EtradeHealthIndicator extends AbstractBrokerHealthIndicator {

    public EtradeHealthIndicator(
            EtradeBrokerClient etradeBrokerClient,
            BrokerConfigService brokerConfigService) {
        super(etradeBrokerClient, "health-check-account", brokerConfigService);
    }
}
