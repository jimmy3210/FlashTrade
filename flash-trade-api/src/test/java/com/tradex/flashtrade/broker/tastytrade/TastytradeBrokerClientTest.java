package com.tradex.flashtrade.broker.tastytrade;

import com.tradex.flashtrade.broker.dto.BrokerOrder;
import com.tradex.flashtrade.broker.dto.OrderRequest;
import com.tradex.flashtrade.config.BrokerMode;
import com.tradex.flashtrade.entity.OrderSide;
import com.tradex.flashtrade.exception.BusinessException;
import com.tradex.flashtrade.exception.ErrorCode;
import com.tradex.flashtrade.broker.tastytrade.config.TastytradeProperties;
import com.tradex.flashtrade.service.BrokerConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TastytradeBrokerClientTest {

    @Mock
    private TastytradeProperties properties;
    @Mock
    private TastytradeRestClient restClient;
    @Mock
    private TastytradeMapper mapper;
    @Mock
    private BrokerConfigService configService;

    private TastytradeBrokerClient client;

    @BeforeEach
    void setUp() {
        client = new TastytradeBrokerClient(properties, restClient, mapper, configService);
        when(configService.getEffectiveMode(anyString())).thenReturn(BrokerMode.MOCK);
    }

    @Test
    void mockOrderCanBeListedQueriedAndCancelledWithoutExternalCalls() {
        OrderRequest request = new OrderRequest();
        request.setSymbol("AAPL");
        request.setSide(OrderSide.BUY);
        request.setOrderType(OrderRequest.OrderType.LIMIT);
        request.setQuantity(3);
        request.setLimitPrice(new BigDecimal("185.50"));

        var result = client.placeOrder("account-1", request);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getExternalOrderId()).startsWith("TT-MOCK-");
        List<BrokerOrder> listedOrders = client.getOrders("account-1");
        assertThat(listedOrders).hasSize(1);
        assertThat(listedOrders.get(0).getExternalOrderId()).isEqualTo(result.getExternalOrderId());
        assertThat(listedOrders.get(0).getStatus()).isEqualTo("PENDING");
        assertThat(listedOrders.get(0).getQuantity()).isEqualByComparingTo("3");
        assertThat(client.getOrderStatus("account-1", result.getExternalOrderId()).getStatus())
                .isEqualTo("PENDING");

        client.cancelOrder("account-1", result.getExternalOrderId());

        assertThat(client.getOrderStatus("account-1", result.getExternalOrderId()).getStatus())
                .isEqualTo("CANCELLED");
        assertThat(client.getOrders("account-2")).isEmpty();
        verifyNoInteractions(restClient, mapper);
    }

    @Test
    void mockCancelAndStatusRejectUnknownOrders() {
        assertThatThrownBy(() -> client.cancelOrder("account-1", "unknown-order"))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
        assertThatThrownBy(() -> client.getOrderStatus("account-1", "unknown-order"))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);

        verifyNoInteractions(restClient, mapper);
    }
}
