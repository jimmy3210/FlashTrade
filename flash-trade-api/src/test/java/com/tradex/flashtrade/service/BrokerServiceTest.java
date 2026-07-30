package com.tradex.flashtrade.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradex.flashtrade.broker.BrokerClient;
import com.tradex.flashtrade.broker.dto.BrokerOrder;
import com.tradex.flashtrade.broker.dto.BrokerPortfolio;
import com.tradex.flashtrade.exception.BrokerApiException;
import com.tradex.flashtrade.exception.BusinessException;
import com.tradex.flashtrade.exception.ErrorCode;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@ExtendWith(MockitoExtension.class)
@DisplayName("BrokerService 单元测试")
class BrokerServiceTest {

    @Mock
    private BrokerClient mockBrokerClient;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    private BrokerService brokerService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String BROKER_NAME = "SCHWAB";
    private static final String ACCOUNT_ID  = "account-001";

    @BeforeEach
    void setUp() {
        when(mockBrokerClient.getBrokerName()).thenReturn(BROKER_NAME);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        brokerService = new BrokerService(List.of(mockBrokerClient), redisTemplate, objectMapper,
                new SimpleMeterRegistry());
    }

    // -------------------------------------------------------------------------
    // getPortfolio — 缓存命中
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("缓存命中时直接返回，不调用 BrokerClient")
    void getPortfolio_cacheHit_returnsCachedValue() throws Exception {
        BrokerPortfolio expected = BrokerPortfolio.builder()
                .accountId(ACCOUNT_ID)
                .brokerName(BROKER_NAME)
                .totalMarketValue(BigDecimal.valueOf(100_000.0))
                .build();
        String json = objectMapper.writeValueAsString(expected);
        when(valueOps.get(anyString())).thenReturn(json);

        BrokerPortfolio result = brokerService.getPortfolio(BROKER_NAME, ACCOUNT_ID);

        assertThat(result.getAccountId()).isEqualTo(ACCOUNT_ID);
        verify(mockBrokerClient, never()).getPortfolio(any());
    }

    // -------------------------------------------------------------------------
    // getPortfolio — 缓存未命中
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("缓存未命中时调用 BrokerClient 并写入缓存")
    void getPortfolio_cacheMiss_callsBrokerAndCaches() {
        BrokerPortfolio portfolio = BrokerPortfolio.builder()
                .accountId(ACCOUNT_ID)
                .brokerName(BROKER_NAME)
                .totalMarketValue(BigDecimal.valueOf(50_000.0))
                .build();
        when(valueOps.get(anyString())).thenReturn(null);
        when(mockBrokerClient.getPortfolio(ACCOUNT_ID)).thenReturn(portfolio);

        BrokerPortfolio result = brokerService.getPortfolio(BROKER_NAME, ACCOUNT_ID);

        assertThat(result.getTotalMarketValue()).isEqualByComparingTo(BigDecimal.valueOf(50_000.0));
        verify(mockBrokerClient).getPortfolio(ACCOUNT_ID);
        verify(valueOps).set(anyString(), anyString(), eq(5L), eq(TimeUnit.MINUTES));
    }

    // -------------------------------------------------------------------------
    // getOrders — 缓存未命中
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getOrders — 缓存未命中时调用券商并返回委托列表")
    void getOrders_cacheMiss_callsBrokerAndReturnsOrders() {
        List<BrokerOrder> orders = List.of(
                BrokerOrder.builder().externalOrderId("ORD-001").symbol("AAPL").build()
        );
        when(valueOps.get(anyString())).thenReturn(null);
        when(mockBrokerClient.getOrders(ACCOUNT_ID)).thenReturn(orders);

        List<BrokerOrder> result = brokerService.getOrders(BROKER_NAME, ACCOUNT_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getExternalOrderId()).isEqualTo("ORD-001");
        verify(valueOps).set(anyString(), anyString(), eq(5L), eq(TimeUnit.MINUTES));
    }

    // -------------------------------------------------------------------------
    // 未注册券商
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("不支持的券商名称应抛出 BROKER_UNAVAILABLE")
    void getPortfolio_unknownBroker_throwsBusinessException() {
        when(valueOps.get(anyString())).thenReturn(null);

        assertThatThrownBy(() -> brokerService.getPortfolio("UNKNOWN_BROKER", ACCOUNT_ID))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.BROKER_UNAVAILABLE));
    }

    // -------------------------------------------------------------------------
    // 发生 BrokerApiException 时
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("BrokerApiException 发生时应抛出（等待 @Recover 兜底）")
    void getPortfolio_brokerApiException_propagates() {
        when(valueOps.get(anyString())).thenReturn(null);
        when(mockBrokerClient.getPortfolio(ACCOUNT_ID))
                .thenThrow(new BrokerApiException(BROKER_NAME, 503, "Service Unavailable"));

        // @Recover 经由 Spring AOP ，单元测试中 BrokerApiException 会直接传播
        assertThatThrownBy(() -> brokerService.getPortfolio(BROKER_NAME, ACCOUNT_ID))
                .isInstanceOf(BrokerApiException.class)
                .satisfies(ex -> assertThat(((BrokerApiException) ex).getBrokerName())
                        .isEqualTo(BROKER_NAME));
    }
}
