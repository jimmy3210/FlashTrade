package com.tradex.flashtrade.broker.tastyworks;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tradex.flashtrade.broker.dto.BrokerOrder;
import com.tradex.flashtrade.broker.dto.BrokerPortfolio;
import com.tradex.flashtrade.broker.dto.BrokerTrade;
import com.tradex.flashtrade.broker.tastyworks.config.TastyworksProperties;
import com.tradex.flashtrade.broker.tastyworks.dto.TastyworksOrdersResponse;
import com.tradex.flashtrade.broker.tastyworks.dto.TastyworksPositionsResponse;
import com.tradex.flashtrade.service.BrokerConfigService;

@ExtendWith(MockitoExtension.class)
@DisplayName("TastyworksBrokerClient 单元测试")
class TastyworksBrokerClientTest {

    @Mock
    private TastyworksProperties tastyworksProps;

    @Mock
    private TastyworksRestClient tastyworksRestClient;

    @Mock
    private TastyworksMapper tastyworksMapper;

    @Mock
    private BrokerConfigService brokerConfigService;

    private TastyworksBrokerClient client;

    @BeforeEach
    void setUp() {
        client = new TastyworksBrokerClient(tastyworksProps, tastyworksRestClient, tastyworksMapper, brokerConfigService);
    }

    @Test
    @DisplayName("getBrokerName 返回 TASTYWORKS")
    void getBrokerName_returnsTastyworks() {
        assertThat(client.getBrokerName()).isEqualTo("TASTYWORKS");
    }

    @Test
    @DisplayName("Mock 模式: getPortfolio 不调用 RestClient，直接返回演示数据")
    void getPortfolio_mockEnabled_returnsMockPortfolio() {
        when(tastyworksProps.isMockEnabled()).thenReturn(true);
        when(brokerConfigService.isMockEnabled(anyString(), anyBoolean()))
                .thenAnswer(invocation -> invocation.getArgument(1, Boolean.class));

        BrokerPortfolio result = client.getPortfolio("account-tw-001");

        assertThat(result.getBrokerName()).isEqualTo("TASTYWORKS");
        assertThat(result.getPositions()).isNotEmpty();
        verifyNoInteractions(tastyworksRestClient);
        verifyNoInteractions(tastyworksMapper);
    }

    @Test
    @DisplayName("Mock 模式: getOrders 不调用 RestClient，直接返回演示数据")
    void getOrders_mockEnabled_returnsMockOrders() {
        when(tastyworksProps.isMockEnabled()).thenReturn(true);
        when(brokerConfigService.isMockEnabled(anyString(), anyBoolean()))
                .thenAnswer(invocation -> invocation.getArgument(1, Boolean.class));

        List<BrokerOrder> result = client.getOrders("account-tw-001");

        assertThat(result).isNotEmpty();
        verifyNoInteractions(tastyworksRestClient);
    }

    @Test
    @DisplayName("Real 模式: getPortfolio 会调用 RestClient 和 Mapper")
    void getPortfolio_realMode_callsRestClientAndMapper() {
        BrokerPortfolio expected = BrokerPortfolio.builder()
                .accountId("account-tw-001")
                .brokerName("TASTYWORKS")
                .build();
        when(tastyworksProps.isMockEnabled()).thenReturn(false);
        when(brokerConfigService.isMockEnabled(anyString(), anyBoolean()))
                .thenAnswer(invocation -> invocation.getArgument(1, Boolean.class));
        when(tastyworksRestClient.getPositions(anyString()))
                .thenReturn(new TastyworksPositionsResponse());
        when(tastyworksMapper.toPortfolio(any(TastyworksPositionsResponse.class), anyString()))
                .thenReturn(expected);

        BrokerPortfolio result = client.getPortfolio("account-tw-001");

        assertThat(result.getBrokerName()).isEqualTo("TASTYWORKS");
        verify(tastyworksRestClient).getPositions("account-tw-001");
        verify(tastyworksMapper).toPortfolio(any(TastyworksPositionsResponse.class),
                                             anyString());
    }

    @Test
    @DisplayName("Real 模式: getTrades 会调用 RestClient 和 Mapper")
    void getTrades_realMode_callsRestClientAndMapper() {
        when(tastyworksProps.isMockEnabled()).thenReturn(false);
        when(brokerConfigService.isMockEnabled(anyString(), anyBoolean()))
                .thenAnswer(invocation -> invocation.getArgument(1, Boolean.class));
        when(tastyworksRestClient.getOrders(anyString()))
                .thenReturn(new TastyworksOrdersResponse());
        when(tastyworksMapper.toTrades(any(TastyworksOrdersResponse.class)))
                .thenReturn(List.of());

        List<BrokerTrade> result = client.getTrades("account-tw-001");

        assertThat(result).isEmpty();
        verify(tastyworksRestClient).getOrders("account-tw-001");
        verify(tastyworksMapper).toTrades(any(TastyworksOrdersResponse.class));
    }
}
