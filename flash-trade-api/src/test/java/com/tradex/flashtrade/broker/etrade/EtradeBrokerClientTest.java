package com.tradex.flashtrade.broker.etrade;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tradex.flashtrade.broker.dto.BrokerOrder;
import com.tradex.flashtrade.broker.dto.BrokerPortfolio;
import com.tradex.flashtrade.broker.dto.BrokerTrade;
import com.tradex.flashtrade.broker.etrade.config.EtradeProperties;
import com.tradex.flashtrade.broker.etrade.dto.EtradePortfolioResponse;
import com.tradex.flashtrade.broker.etrade.dto.EtradeTransactionResponse;
import com.tradex.flashtrade.config.BrokerMode;
import com.tradex.flashtrade.service.BrokerConfigService;

@ExtendWith(MockitoExtension.class)
@DisplayName("EtradeBrokerClient 单元测试")
class EtradeBrokerClientTest {

    @Mock
    private EtradeProperties etradeProps;

    @Mock
    private EtradeRestClient etradeRestClient;

    @Mock
    private EtradeMapper etradeMapper;

    @Mock
    private BrokerConfigService brokerConfigService;

    private EtradeBrokerClient client;

    @BeforeEach
    void setUp() {
        client = new EtradeBrokerClient(etradeProps, etradeRestClient, etradeMapper, brokerConfigService);
    }

    @Test
    @DisplayName("getBrokerName 返回 ETRADE")
    void getBrokerName_returnsEtrade() {
        assertThat(client.getBrokerName()).isEqualTo("ETRADE");
    }

    @Test
    @DisplayName("Mock 模式: getPortfolio 不调用 RestClient，直接返回演示数据")
    void getPortfolio_mockEnabled_returnsMockPortfolio() {
        when(brokerConfigService.getEffectiveMode(anyString())).thenReturn(BrokerMode.MOCK);

        BrokerPortfolio result = client.getPortfolio("account-001");

        assertThat(result.getBrokerName()).isEqualTo("ETRADE");
        assertThat(result.getPositions()).isNotEmpty();
        verifyNoInteractions(etradeRestClient);
        verifyNoInteractions(etradeMapper);
    }

    @Test
    @DisplayName("Mock 模式: getOrders 不调用 RestClient，直接返回演示数据")
    void getOrders_mockEnabled_returnsMockOrders() {
        when(brokerConfigService.getEffectiveMode(anyString())).thenReturn(BrokerMode.MOCK);

        List<BrokerOrder> result = client.getOrders("account-001");

        assertThat(result).isNotEmpty();
        verifyNoInteractions(etradeRestClient);
    }

    @Test
    @DisplayName("Real 模式: getPortfolio 会调用 RestClient 和 Mapper")
    void getPortfolio_realMode_callsRestClientAndMapper() {
        BrokerPortfolio expected = BrokerPortfolio.builder()
                .accountId("account-001")
                .brokerName("ETRADE")
                .build();
        when(brokerConfigService.getEffectiveMode(anyString())).thenReturn(BrokerMode.SANDBOX);
        when(etradeRestClient.getPortfolio(anyString()))
                .thenReturn(new EtradePortfolioResponse());
        when(etradeMapper.toPortfolio(any(EtradePortfolioResponse.class))).thenReturn(expected);

        BrokerPortfolio result = client.getPortfolio("account-001");

        assertThat(result.getBrokerName()).isEqualTo("ETRADE");
        verify(etradeRestClient).getPortfolio("account-001");
        verify(etradeMapper).toPortfolio(any(EtradePortfolioResponse.class));
    }

    @Test
    @DisplayName("Real 模式: getTrades 会调用 RestClient 和 Mapper")
    void getTrades_realMode_callsRestClientAndMapper() {
        when(brokerConfigService.getEffectiveMode(anyString())).thenReturn(BrokerMode.SANDBOX);
        when(etradeRestClient.getTransactions(anyString()))
                .thenReturn(new EtradeTransactionResponse());
        when(etradeMapper.toTrades(any(EtradeTransactionResponse.class)))
                .thenReturn(List.of());

        List<BrokerTrade> result = client.getTrades("account-001");

        assertThat(result).isEmpty();
        verify(etradeRestClient).getTransactions("account-001");
        verify(etradeMapper).toTrades(any(EtradeTransactionResponse.class));
    }
}
