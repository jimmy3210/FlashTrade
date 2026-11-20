package com.tradex.flashtrade.service;

import com.tradex.flashtrade.exception.BusinessException;
import com.tradex.flashtrade.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class RiskServiceTest {

    @Mock
    private JdbcTemplate jdbc;

    private RiskService riskService;

    @BeforeEach
    void setUp() {
        riskService = new RiskService(jdbc);
    }

    @Test
    void disableAccountRejectsUntilScopeAuthorizationIsImplemented() {
        assertRejected(() -> riskService.disableAccount("account", 1L), ErrorCode.ACCESS_DENIED);
        verifyNoInteractions(jdbc);
    }

    @Test
    void enableAccountRejectsUntilScopeAuthorizationIsImplemented() {
        assertRejected(() -> riskService.enableAccount("account", 1L), ErrorCode.ACCESS_DENIED);
        verifyNoInteractions(jdbc);
    }

    @Test
    void limitAccountRejectsUntilScopeAuthorizationIsImplemented() {
        assertRejected(() -> riskService.limitAccount("account", 1L), ErrorCode.ACCESS_DENIED);
        verifyNoInteractions(jdbc);
    }

    private void assertRejected(Runnable action, ErrorCode expectedCode) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(expectedCode);
    }
}
