package com.tradex.flashtrade.broker.schwab;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tradex.flashtrade.broker.dto.BrokerOrder;
import com.tradex.flashtrade.broker.dto.BrokerPortfolio;
import com.tradex.flashtrade.broker.dto.BrokerTrade;
import com.tradex.flashtrade.broker.schwab.config.SchwabProperties;
import com.tradex.flashtrade.config.BrokerMode;
import com.tradex.flashtrade.service.BrokerConfigService;

@ExtendWith(MockitoExtension.class)
@DisplayName("SchwabBrokerClient 单元测试")
class SchwabBrokerClientTest {

    @Mock
    private SchwabRestClient schwabRestClient;

    @Mock
    private SchwabMapper schwabMapper;

    @Mock
    private BrokerConfigService brokerConfigService;

    private SchwabBrokerClient client;

    @BeforeEach
    void setUp() {
        SchwabProperties props = new SchwabProperties();
        props.setClientId("test-client-id");
        props.setClientSecret("test-secret");
        client = new SchwabBrokerClient(props, schwabRestClient, schwabMapper, brokerConfigService);
    }

    @Test
    @DisplayName("getBrokerName() 返回 SCHWAB")
    void getBrokerName_returnsSchwab() {
        assertThat(client.getBrokerName()).isEqualTo("SCHWAB");
    }

    // -------------------------------------------------------------------------
    // Mock モード
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Mock 模式启用时不调用 RestClient，直接返回 Mock 数据")
    void getPortfolio_mockEnabled_returnsWithoutCallingRestClient() {
        SchwabProperties mockProps = new SchwabProperties();
        when(brokerConfigService.getEffectiveMode(anyString())).thenReturn(BrokerMode.MOCK);
        SchwabBrokerClient mockClient =
                new SchwabBrokerClient(mockProps, schwabRestClient, schwabMapper, brokerConfigService);

        BrokerPortfolio portfolio = mockClient.getPortfolio("account-001");

        assertThat(portfolio.getBrokerName()).isEqualTo("SCHWAB");
        assertThat(portfolio.getPositions()).isNotEmpty();
        verifyNoInteractions(schwabRestClient);
    }

    @Test
    @DisplayName("Mock 模式: getOrders 返回 MockOrders")
    void getOrders_mockEnabled_returnsMockOrders() {
        SchwabProperties mockProps = new SchwabProperties();
        when(brokerConfigService.getEffectiveMode(anyString())).thenReturn(BrokerMode.MOCK);
        SchwabBrokerClient mockClient =
                new SchwabBrokerClient(mockProps, schwabRestClient, schwabMapper, brokerConfigService);

        List<BrokerOrder> orders = mockClient.getOrders("account-001");

        assertThat(orders).isNotEmpty();
        assertThat(orders.get(0).getExternalOrderId()).startsWith("SCHWAB-ORD");
        verifyNoInteractions(schwabRestClient);
    }

    // -------------------------------------------------------------------------
    // Real モード（RestClient 経由）
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Real 模式: getPortfolio 会调用 RestClient 和 Mapper")
    void getPortfolio_realMode_callsRestClientAndMapper() {
        BrokerPortfolio expected = BrokerPortfolio.builder()
                .accountId("account-001")
                .brokerName("SCHWAB")
                .build();
        when(brokerConfigService.getEffectiveMode(anyString())).thenReturn(BrokerMode.SANDBOX);
        when(schwabRestClient.getPositions(anyString())).thenReturn(List.of());
        when(schwabMapper.toPortfolio(anyList(), anyString())).thenReturn(expected);

        BrokerPortfolio result = client.getPortfolio("account-001");

        assertThat(result.getBrokerName()).isEqualTo("SCHWAB");
        verify(schwabRestClient).getPositions("account-001");
        verify(schwabMapper).toPortfolio(anyList(), eq("account-001"));
    }

    @Test
    @DisplayName("Real 模式: getTrades 会调用 RestClient 和 Mapper")
    void getTrades_realMode_callsRestClientAndMapper() {
        when(brokerConfigService.getEffectiveMode(anyString())).thenReturn(BrokerMode.SANDBOX);
        when(schwabRestClient.getTransactions(anyString())).thenReturn(List.of());
        when(schwabMapper.toTrades(anyList())).thenReturn(List.of());

        List<BrokerTrade> result = client.getTrades("account-001");

        assertThat(result).isEmpty();
        verify(schwabRestClient).getTransactions("account-001");
        verify(schwabMapper).toTrades(anyList());
    }
}
