package com.tradex.flashtrade.controller;

import com.tradex.flashtrade.broker.dto.OrderRequest;
import com.tradex.flashtrade.broker.dto.PlaceOrderResult;
import com.tradex.flashtrade.exception.BusinessException;
import com.tradex.flashtrade.exception.ErrorCode;
import com.tradex.flashtrade.service.BrokerAccountService;
import com.tradex.flashtrade.service.TradeExecutionService;
import com.tradex.flashtrade.service.UserLookupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeControllerTest {

    private static final String USERNAME = "trader";
    private static final String ACCOUNT_ID = "account-1";

    @Mock
    private TradeExecutionService tradeExecutionService;
    @Mock
    private BrokerAccountService brokerAccountService;
    @Mock
    private UserLookupService userLookupService;

    private TradeController tradeController;

    @BeforeEach
    void setUp() {
        tradeController = new TradeController(
                tradeExecutionService, brokerAccountService, userLookupService);
    }

    @Test
    void unconfirmedOrderResultIsReturnedAsAnErrorRatherThanSuccessEnvelope() {
        var user = User.withUsername(USERNAME).password("unused").roles("TRADER").build();
        OrderRequest request = new OrderRequest();
        when(tradeExecutionService.placeOrder(USERNAME, ACCOUNT_ID, request))
                .thenReturn(PlaceOrderResult.builder()
                        .success(false)
                        .message("processing")
                        .build());

        assertThatThrownBy(() -> tradeController.placeOrder(user, ACCOUNT_ID, request))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.ORDER_RESULT_UNCONFIRMED);

        verify(tradeExecutionService).placeOrder(USERNAME, ACCOUNT_ID, request);
    }
}
