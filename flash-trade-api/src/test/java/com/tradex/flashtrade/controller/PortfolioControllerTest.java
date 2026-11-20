package com.tradex.flashtrade.controller;

import com.tradex.flashtrade.dto.portfolio.OrderResponse;
import com.tradex.flashtrade.dto.portfolio.PositionResponse;
import com.tradex.flashtrade.entity.OrderSide;
import com.tradex.flashtrade.entity.OrderStatus;
import com.tradex.flashtrade.exception.GlobalExceptionHandler;
import com.tradex.flashtrade.config.SecurityConfig;
import com.tradex.flashtrade.security.JwtTokenProvider;
import com.tradex.flashtrade.security.UserDetailsServiceImpl;
import com.tradex.flashtrade.service.PositionAggregatorService;
import com.tradex.flashtrade.service.PortfolioService;
import com.tradex.flashtrade.service.TokenBlacklistService;
import com.tradex.flashtrade.service.UserLookupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    value = PortfolioController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
@WithMockUser(username = "trader01")
class PortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PortfolioService portfolioService;
    @MockitoBean
    private PositionAggregatorService positionAggregatorService;
    @MockitoBean
    private UserLookupService userLookupService;
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;
    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;
    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    // ============================================================
    // GET /api/portfolio/positions
    // ============================================================

    @Test
    @DisplayName("GET /api/portfolio/positions - 成功返回 200 和持仓列表")
    void getPositions_success() throws Exception {
        PositionResponse pos = PositionResponse.builder()
                .id(1L)
                .accountId("ACC001")
                .symbol("AAPL")
                .quantity(new BigDecimal("100.0000"))
                .averageCost(new BigDecimal("150.0000"))
                .currentPrice(new BigDecimal("175.0000"))
                .marketValue(new BigDecimal("17500.0000"))
                .unrealizedPnl(new BigDecimal("2500.0000"))
                .brokerName("schwab")
                .updatedAt(LocalDateTime.now())
                .build();

        when(portfolioService.getPositions(anyString(), isNull(), isNull())).thenReturn(List.of(pos));

        mockMvc.perform(get("/api/v1/portfolio/positions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].symbol").value("AAPL"))
                .andExpect(jsonPath("$.data[0].quantity").value(100.0000));
    }

    @Test
    @DisplayName("GET /api/portfolio/positions - 无持仓时返回空数组")
    void getPositions_emptyList() throws Exception {
        when(portfolioService.getPositions(anyString(), isNull(), isNull())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/portfolio/positions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    // ============================================================
    // GET /api/orders
    // ============================================================

    @Test
    @DisplayName("GET /api/orders - 成功返回 200 和订单分页")
    void getOrders_success() throws Exception {
        OrderResponse order = OrderResponse.builder()
                .id(1L)
                .accountId("ACC001")
                .symbol("AAPL")
                .side(OrderSide.BUY)
                .quantity(new BigDecimal("100.0000"))
                .price(new BigDecimal("150.0000"))
                .status(OrderStatus.FILLED)
                .brokerName("schwab")
                .createdAt(LocalDateTime.now())
                .build();

        when(portfolioService.getOrders(any(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(order)));

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content[0].symbol").value("AAPL"))
                .andExpect(jsonPath("$.data.content[0].status").value("FILLED"));
    }

    @Test
    @DisplayName("GET /api/orders - 带 status 参数时过滤订单")
    void getOrders_withStatus() throws Exception {
        when(portfolioService.getOrders(any(), eq(OrderStatus.PENDING), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/orders").param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty());

        verify(portfolioService).getOrders(any(), eq(OrderStatus.PENDING), any(Pageable.class));
    }
}
