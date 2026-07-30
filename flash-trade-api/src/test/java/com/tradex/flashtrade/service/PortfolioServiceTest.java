package com.tradex.flashtrade.service;

import com.tradex.flashtrade.dto.portfolio.OrderResponse;
import com.tradex.flashtrade.dto.portfolio.PositionResponse;
import com.tradex.flashtrade.entity.*;
import com.tradex.flashtrade.repository.OrderRepository;
import com.tradex.flashtrade.repository.PositionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock private PositionRepository positionRepository;
    @Mock private OrderRepository    orderRepository;
    @Mock private UserLookupService  userLookupService;

    @InjectMocks
    private PortfolioService portfolioService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("trader01");
        testUser.setRole(Role.ROLE_TRADER);
        testUser.setEnabled(true);
    }

    // ============================================================
    // getPositions
    // ============================================================

    @Test
    @DisplayName("getPositions: 返回该用户的持仓列表")
    void getPositions_returnsList() {
        Position pos = buildPosition();
        when(userLookupService.getUserIdByUsername("trader01")).thenReturn(1L);
        when(positionRepository.findByUserIdOrderBySymbolAsc(1L)).thenReturn(List.of(pos));

        List<PositionResponse> result = portfolioService.getPositions("trader01");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSymbol()).isEqualTo("AAPL");
        assertThat(result.get(0).getQuantity()).isEqualByComparingTo("100.0000");
    }

    @Test
    @DisplayName("getPositions: 持仓为空时返回空列表")
    void getPositions_emptyList() {
        when(userLookupService.getUserIdByUsername("trader01")).thenReturn(1L);
        when(positionRepository.findByUserIdOrderBySymbolAsc(1L)).thenReturn(List.of());

        assertThat(portfolioService.getPositions("trader01")).isEmpty();
    }

    // ============================================================
    // getOrders
    // ============================================================

    @Test
    @DisplayName("getOrders: 不指定状态时返回全部订单（分页）")
    void getOrders_noStatus_returnsAllOrders() {
        Order order = buildOrder();
        PageRequest pageable = PageRequest.of(0, 20);
        when(userLookupService.getUserIdByUsername("trader01")).thenReturn(1L);
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(order)));

        Page<OrderResponse> result = portfolioService.getOrders("trader01", null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getSymbol()).isEqualTo("AAPL");
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(OrderStatus.FILLED);
    }

    @Test
    @DisplayName("getOrders: 指定 PENDING 状态时调用带状态过滤的查询")
    void getOrders_withStatus_filtersCorrectly() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(userLookupService.getUserIdByUsername("trader01")).thenReturn(1L);
        when(orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(1L, OrderStatus.PENDING, pageable))
                .thenReturn(Page.empty());

        portfolioService.getOrders("trader01", OrderStatus.PENDING, pageable);

        verify(orderRepository, never()).findByUserIdOrderByCreatedAtDesc(any(), any());
        verify(orderRepository).findByUserIdAndStatusOrderByCreatedAtDesc(1L, OrderStatus.PENDING, pageable);
    }

    // ---- helpers ----

    private Position buildPosition() {
        Position p = new Position();
        p.setId(1L);
        p.setUser(testUser);
        p.setAccountId("ACC001");
        p.setSymbol("AAPL");
        p.setQuantity(new BigDecimal("100.0000"));
        p.setAverageCost(new BigDecimal("150.0000"));
        p.setCurrentPrice(new BigDecimal("175.0000"));
        p.setMarketValue(new BigDecimal("17500.0000"));
        p.setUnrealizedPnl(new BigDecimal("2500.0000"));
        p.setBrokerName("schwab");
        return p;
    }

    private Order buildOrder() {
        Order o = new Order();
        o.setId(1L);
        o.setUser(testUser);
        o.setAccountId("ACC001");
        o.setSymbol("AAPL");
        o.setSide(OrderSide.BUY);
        o.setQuantity(new BigDecimal("100.0000"));
        o.setPrice(new BigDecimal("150.0000"));
        o.setStatus(OrderStatus.FILLED);
        o.setBrokerName("schwab");
        return o;
    }
}
