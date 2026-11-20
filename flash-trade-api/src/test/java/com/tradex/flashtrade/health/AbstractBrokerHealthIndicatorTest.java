package com.tradex.flashtrade.health;

import com.tradex.flashtrade.broker.BrokerClient;
import com.tradex.flashtrade.broker.dto.BrokerPortfolio;
import com.tradex.flashtrade.config.BrokerMode;
import com.tradex.flashtrade.service.BrokerConfigService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AbstractBrokerHealthIndicatorTest {

    @Test
    void mockHealthCheckDoesNotCallBrokerClient() {
        BrokerClient brokerClient = mock(BrokerClient.class);
        BrokerConfigService configService = mock(BrokerConfigService.class);
        when(brokerClient.getBrokerName()).thenReturn("ETRADE");
        when(configService.getEffectiveMode("ETRADE")).thenReturn(BrokerMode.MOCK);

        TestHealthIndicator indicator = new TestHealthIndicator(brokerClient, configService);

        assertThat(indicator.health().getStatus()).isEqualTo(Status.UP);
        verify(brokerClient, never()).getPortfolio("health-check-account");
    }

    @Test
    void sandboxHealthCheckReportsConfiguredMode() {
        BrokerClient brokerClient = mock(BrokerClient.class);
        BrokerConfigService configService = mock(BrokerConfigService.class);
        when(brokerClient.getBrokerName()).thenReturn("SCHWAB");
        when(configService.getEffectiveMode("SCHWAB")).thenReturn(BrokerMode.SANDBOX);
        when(brokerClient.getPortfolio("health-check-account")).thenReturn(BrokerPortfolio.builder().build());

        TestHealthIndicator indicator = new TestHealthIndicator(brokerClient, configService);

        assertThat(indicator.health().getDetails()).containsEntry("mode", "sandbox");
        verify(brokerClient).getPortfolio("health-check-account");
    }

    @Test
    void configurationFailureDoesNotCallBrokerClient() {
        BrokerClient brokerClient = mock(BrokerClient.class);
        BrokerConfigService configService = mock(BrokerConfigService.class);
        when(brokerClient.getBrokerName()).thenReturn("ETRADE");
        when(configService.getEffectiveMode("ETRADE")).thenThrow(new IllegalStateException());

        TestHealthIndicator indicator = new TestHealthIndicator(brokerClient, configService);

        assertThat(indicator.health().getStatus()).isEqualTo(Status.DOWN);
        verify(brokerClient, never()).getPortfolio("health-check-account");
    }

    private static final class TestHealthIndicator extends AbstractBrokerHealthIndicator {
        private TestHealthIndicator(BrokerClient brokerClient, BrokerConfigService configService) {
            super(brokerClient, "health-check-account", configService);
        }
    }
}
