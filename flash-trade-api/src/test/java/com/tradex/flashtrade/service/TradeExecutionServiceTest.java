package com.tradex.flashtrade.service;

import com.tradex.flashtrade.broker.BrokerClient;
import com.tradex.flashtrade.broker.dto.BrokerOrder;
import com.tradex.flashtrade.broker.dto.OrderRequest;
import com.tradex.flashtrade.broker.dto.PlaceOrderResult;
import com.tradex.flashtrade.config.BrokerSyncProperties;
import com.tradex.flashtrade.config.BrokerSyncProperties.AccountMapping;
import com.tradex.flashtrade.entity.Order;
import com.tradex.flashtrade.entity.Role;
import com.tradex.flashtrade.entity.User;
import com.tradex.flashtrade.exception.BusinessException;
import com.tradex.flashtrade.exception.ErrorCode;
import com.tradex.flashtrade.repository.OrderRepository;
import com.tradex.flashtrade.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeExecutionServiceTest {

    private static final String ACCOUNT_ID = "account-1";
    private static final String OWNER_USERNAME = "trader-1";
    private static final String OTHER_USERNAME = "trader-2";

    @Mock private BrokerClient broker;
    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private RiskService riskService;

    private TradeExecutionService tradeExecutionService;
    private User owner;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setUsername(OWNER_USERNAME);
        owner.setRole(Role.ROLE_TRADER);
        owner.setEnabled(true);

        AccountMapping mapping = new AccountMapping();
        mapping.setAccountId(ACCOUNT_ID);
        mapping.setUserId(owner.getId());
        mapping.setBrokerName("TEST");

        BrokerSyncProperties properties = new BrokerSyncProperties();
        properties.setAccounts(List.of(mapping));

        when(broker.getBrokerName()).thenReturn("TEST");
        tradeExecutionService = new TradeExecutionService(
                List.of(broker), properties, orderRepository, userRepository, redisTemplate, riskService);
    }

    @Test
    void placeOrderRejectsAccountOwnedByAnotherUserBeforeRiskOrBrokerCalls() {
        when(userRepository.findByUsername(OTHER_USERNAME)).thenReturn(Optional.of(otherUser()));

        assertAccessDenied(() -> tradeExecutionService.placeOrder(
                OTHER_USERNAME, ACCOUNT_ID, new OrderRequest()));

        verifyNoInteractions(riskService, redisTemplate, orderRepository);
        verify(broker, never()).placeOrder(any(), any());
    }

    @Test
    void cancelOrderRejectsAccountOwnedByAnotherUserBeforeBrokerCall() {
        when(userRepository.findByUsername(OTHER_USERNAME)).thenReturn(Optional.of(otherUser()));

        assertAccessDenied(() -> tradeExecutionService.cancelOrder(
                OTHER_USERNAME, ACCOUNT_ID, "external-order-1"));

        verifyNoInteractions(orderRepository);
        verify(broker, never()).cancelOrder(any(), any());
    }

    @Test
    void getOrderStatusRejectsAccountOwnedByAnotherUserBeforeBrokerCall() {
        when(userRepository.findByUsername(OTHER_USERNAME)).thenReturn(Optional.of(otherUser()));

        assertAccessDenied(() -> tradeExecutionService.getOrderStatus(
                OTHER_USERNAME, ACCOUNT_ID, "external-order-1"));

        verify(broker, never()).getOrderStatus(any(), any());
    }

    @Test
    void getOrderStatusAllowsAccountOwner() {
        BrokerOrder expected = new BrokerOrder();
        when(userRepository.findByUsername(OWNER_USERNAME)).thenReturn(Optional.of(owner));
        when(broker.getOrderStatus(ACCOUNT_ID, "external-order-1")).thenReturn(expected);

        BrokerOrder actual = tradeExecutionService.getOrderStatus(
                OWNER_USERNAME, ACCOUNT_ID, "external-order-1");

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void cancelOrderAllowsAccountOwner() {
        when(userRepository.findByUsername(OWNER_USERNAME)).thenReturn(Optional.of(owner));
        when(orderRepository.findByExternalOrderIdAndBrokerName("external-order-1", "TEST"))
                .thenReturn(Optional.empty());

        tradeExecutionService.cancelOrder(OWNER_USERNAME, ACCOUNT_ID, "external-order-1");

        verify(broker).cancelOrder(ACCOUNT_ID, "external-order-1");
    }

    @Test
    void placeOrderAllowsAccountOwner() {
        OrderRequest request = new OrderRequest();
        request.setSymbol("AAPL");
        request.setClientOrderId(null);
        PlaceOrderResult result = PlaceOrderResult.builder()
                .success(true)
                .externalOrderId("external-order-1")
                .build();
        when(userRepository.findByUsername(OWNER_USERNAME)).thenReturn(Optional.of(owner));
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(riskService.isAccountBlocked(ACCOUNT_ID)).thenReturn(false);
        when(broker.placeOrder(ACCOUNT_ID, request)).thenReturn(result);

        PlaceOrderResult actual = tradeExecutionService.placeOrder(OWNER_USERNAME, ACCOUNT_ID, request);

        assertThat(actual).isSameAs(result);
        verify(orderRepository).save(any());
    }

    @Test
    void repeatedClientOrderIdReturnsPersistedOrderWithoutCallingBrokerAgain() {
        String clientOrderId = "client-order-1";
        OrderRequest request = orderRequest(clientOrderId);
        Order persistedOrder = new Order();
        persistedOrder.setExternalOrderId("external-order-1");

        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(values);
        when(values.setIfAbsent(anyString(), eq("SUBMITTING"), eq(Duration.ofHours(24))))
                .thenReturn(true);
        when(orderRepository.findByUserIdAndAccountIdAndBrokerNameAndClientOrderId(
                owner.getId(), ACCOUNT_ID, "TEST", clientOrderId))
                .thenReturn(Optional.empty(), Optional.of(persistedOrder));
        when(userRepository.findByUsername(OWNER_USERNAME)).thenReturn(Optional.of(owner));
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(riskService.isAccountBlocked(ACCOUNT_ID)).thenReturn(false);
        when(broker.placeOrder(ACCOUNT_ID, request)).thenReturn(PlaceOrderResult.builder()
                .success(true)
                .externalOrderId("external-order-1")
                .build());

        PlaceOrderResult firstResult = tradeExecutionService.placeOrder(
                OWNER_USERNAME, ACCOUNT_ID, request);
        PlaceOrderResult retryResult = tradeExecutionService.placeOrder(
                OWNER_USERNAME, ACCOUNT_ID, request);

        assertThat(firstResult.isSuccess()).isTrue();
        assertThat(retryResult.isSuccess()).isTrue();
        assertThat(retryResult.getExternalOrderId()).isEqualTo("external-order-1");
        ArgumentCaptor<Order> savedOrder = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(savedOrder.capture());
        assertThat(savedOrder.getValue().getClientOrderId()).isEqualTo(clientOrderId);
        verify(broker, times(1)).placeOrder(ACCOUNT_ID, request);
    }

    @Test
    void duplicateWhileSubmissionIsUnconfirmedDoesNotCallBrokerAgain() {
        String clientOrderId = "client-order-pending";
        OrderRequest request = orderRequest(clientOrderId);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(values);
        when(values.setIfAbsent(anyString(), eq("SUBMITTING"), eq(Duration.ofHours(24))))
                .thenReturn(false);
        when(orderRepository.findByUserIdAndAccountIdAndBrokerNameAndClientOrderId(
                owner.getId(), ACCOUNT_ID, "TEST", clientOrderId))
                .thenReturn(Optional.empty(), Optional.empty());
        when(userRepository.findByUsername(OWNER_USERNAME)).thenReturn(Optional.of(owner));
        when(riskService.isAccountBlocked(ACCOUNT_ID)).thenReturn(false);

        PlaceOrderResult result = tradeExecutionService.placeOrder(
                OWNER_USERNAME, ACCOUNT_ID, request);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("结果尚未确认");
        verify(broker, never()).placeOrder(any(), any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void sameClientOrderIdOnDifferentAccountsUsesIndependentIdempotencyKeys() {
        String secondAccountId = "account-2";
        AccountMapping secondMapping = new AccountMapping();
        secondMapping.setAccountId(secondAccountId);
        secondMapping.setUserId(owner.getId());
        secondMapping.setBrokerName("TEST");
        BrokerSyncProperties properties = new BrokerSyncProperties();
        properties.setAccounts(List.of(accountMapping(ACCOUNT_ID), secondMapping));
        tradeExecutionService = new TradeExecutionService(
                List.of(broker), properties, orderRepository, userRepository, redisTemplate, riskService);

        String clientOrderId = "client-order-shared";
        OrderRequest firstRequest = orderRequest(clientOrderId);
        OrderRequest secondRequest = orderRequest(clientOrderId);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(values);
        when(values.setIfAbsent(anyString(), eq("SUBMITTING"), eq(Duration.ofHours(24))))
                .thenReturn(true, true);
        when(orderRepository.findByUserIdAndAccountIdAndBrokerNameAndClientOrderId(
                owner.getId(), ACCOUNT_ID, "TEST", clientOrderId)).thenReturn(Optional.empty());
        when(orderRepository.findByUserIdAndAccountIdAndBrokerNameAndClientOrderId(
                owner.getId(), secondAccountId, "TEST", clientOrderId)).thenReturn(Optional.empty());
        when(userRepository.findByUsername(OWNER_USERNAME)).thenReturn(Optional.of(owner));
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(riskService.isAccountBlocked(ACCOUNT_ID)).thenReturn(false);
        when(riskService.isAccountBlocked(secondAccountId)).thenReturn(false);
        when(broker.placeOrder(eq(ACCOUNT_ID), any())).thenReturn(PlaceOrderResult.builder()
                .success(true).externalOrderId("external-order-1").build());
        when(broker.placeOrder(eq(secondAccountId), any())).thenReturn(PlaceOrderResult.builder()
                .success(true).externalOrderId("external-order-2").build());

        tradeExecutionService.placeOrder(OWNER_USERNAME, ACCOUNT_ID, firstRequest);
        tradeExecutionService.placeOrder(OWNER_USERNAME, secondAccountId, secondRequest);

        ArgumentCaptor<String> keys = ArgumentCaptor.forClass(String.class);
        verify(values, times(2)).setIfAbsent(keys.capture(), eq("SUBMITTING"), eq(Duration.ofHours(24)));
        assertThat(keys.getAllValues()).doesNotHaveDuplicates();
        verify(broker).placeOrder(ACCOUNT_ID, firstRequest);
        verify(broker).placeOrder(secondAccountId, secondRequest);
    }

    @Test
    void cannotPlaceOrderWhenIdempotencyReservationResultIsUnknown() {
        OrderRequest request = orderRequest("client-order-unknown");
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(values);
        when(values.setIfAbsent(anyString(), eq("SUBMITTING"), eq(Duration.ofHours(24))))
                .thenReturn(null);
        when(orderRepository.findByUserIdAndAccountIdAndBrokerNameAndClientOrderId(
                owner.getId(), ACCOUNT_ID, "TEST", request.getClientOrderId()))
                .thenReturn(Optional.empty());
        when(userRepository.findByUsername(OWNER_USERNAME)).thenReturn(Optional.of(owner));
        when(riskService.isAccountBlocked(ACCOUNT_ID)).thenReturn(false);

        assertThatThrownBy(() -> tradeExecutionService.placeOrder(
                OWNER_USERNAME, ACCOUNT_ID, request))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.BROKER_UNAVAILABLE);

        verify(broker, never()).placeOrder(any(), any());
    }

    private User otherUser() {
        User user = new User();
        user.setId(2L);
        user.setUsername(OTHER_USERNAME);
        user.setRole(Role.ROLE_TRADER);
        user.setEnabled(true);
        return user;
    }

    private OrderRequest orderRequest(String clientOrderId) {
        OrderRequest request = new OrderRequest();
        request.setSymbol("AAPL");
        request.setClientOrderId(clientOrderId);
        return request;
    }

    private AccountMapping accountMapping(String accountId) {
        AccountMapping mapping = new AccountMapping();
        mapping.setAccountId(accountId);
        mapping.setUserId(owner.getId());
        mapping.setBrokerName("TEST");
        return mapping;
    }

    private void assertAccessDenied(ThrowingCallable action) {
        assertThatThrownBy(action)
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.ACCESS_DENIED);
    }
}
