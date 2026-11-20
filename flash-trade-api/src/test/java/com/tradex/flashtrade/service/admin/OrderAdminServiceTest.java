package com.tradex.flashtrade.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import com.tradex.flashtrade.audit.AuditLogger;
import com.tradex.flashtrade.dto.admin.CancelOrderRequest;
import com.tradex.flashtrade.dto.admin.OrderQueryFilter;
import com.tradex.flashtrade.entity.Order;
import com.tradex.flashtrade.exception.BusinessException;
import com.tradex.flashtrade.exception.ErrorCode;
import com.tradex.flashtrade.repository.OrderRepository;
import com.tradex.flashtrade.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class OrderAdminServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogger auditLogger;

    private OrderAdminService service;

    @BeforeEach
    void setUp() {
        service = new OrderAdminService();
        ReflectionTestUtils.setField(service, "orderRepository", orderRepository);
        ReflectionTestUtils.setField(service, "userRepository", userRepository);
        ReflectionTestUtils.setField(service, "auditLogger", auditLogger);
    }

    @Test
    void queryOrdersMapsAllowedSortAndDirectionToEntityProperty() {
        when(orderRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        service.queryOrders(1, 20, OrderQueryFilter.builder()
                .sortBy("created_at")
                .sortOrder("asc")
                .build());

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(orderRepository).findAll(any(Specification.class), pageable.capture());
        assertThat(pageable.getValue().getSort().getOrderFor("createdAt").getDirection())
                .isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void queryOrdersFallsBackToCreatedAtForUnsupportedSort() {
        when(orderRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        service.queryOrders(1, 20, OrderQueryFilter.builder()
                .sortBy("accountId desc")
                .sortOrder("desc")
                .build());

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(orderRepository).findAll(any(Specification.class), pageable.capture());
        assertThat(pageable.getValue().getSort().getOrderFor("createdAt").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void cancelOrderRejectsUntilBrokerSideResultCanBeVerified() {
        assertThatThrownBy(() -> service.cancelOrder(1L, new CancelOrderRequest(), 2L))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_STATE);

        verifyNoInteractions(orderRepository, auditLogger);
    }
}
