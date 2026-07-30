package com.tradex.flashtrade.broker.tastyworks;

import com.tradex.flashtrade.broker.dto.BrokerOrder;
import com.tradex.flashtrade.broker.dto.BrokerPortfolio;
import com.tradex.flashtrade.broker.dto.BrokerTrade;
import com.tradex.flashtrade.broker.tastyworks.dto.TastyworksFill;
import com.tradex.flashtrade.broker.tastyworks.dto.TastyworksOrder;
import com.tradex.flashtrade.broker.tastyworks.dto.TastyworksOrderLeg;
import com.tradex.flashtrade.broker.tastyworks.dto.TastyworksOrdersResponse;
import com.tradex.flashtrade.broker.tastyworks.dto.TastyworksPosition;
import com.tradex.flashtrade.broker.tastyworks.dto.TastyworksPositionsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TastyworksMapper 单元测试")
class TastyworksMapperTest {

    private TastyworksMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TastyworksMapper();
    }

    // -------------------------------------------------------------------------
    // Portfolio mapping
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("持仓响应正确映射为 BrokerPortfolio")
    void toPortfolio_validData_mapsCorrectly() {
        TastyworksPosition pos = new TastyworksPosition();
        pos.setSymbol("SPY");
        pos.setQuantity("50");
        pos.setQuantityDirection("Long");
        pos.setAverageOpenPrice("420.00");
        pos.setClosePrice("445.00");

        TastyworksPositionsResponse.PositionsData data = new TastyworksPositionsResponse.PositionsData();
        data.setItems(List.of(pos));

        TastyworksPositionsResponse response = new TastyworksPositionsResponse();
        response.setData(data);

        BrokerPortfolio result = mapper.toPortfolio(response, "account-tw-001");

        assertThat(result.getAccountId()).isEqualTo("account-tw-001");
        assertThat(result.getBrokerName()).isEqualTo("TASTYWORKS");
        assertThat(result.getPositions()).hasSize(1);
        assertThat(result.getPositions().get(0).getSymbol()).isEqualTo("SPY");
        assertThat(result.getPositions().get(0).getQuantity().doubleValue()).isEqualTo(50.0);
        assertThat(result.getPositions().get(0).getAverageCost().doubleValue()).isEqualTo(420.00);
    }

    @Test
    @DisplayName("Short 持仓 quantity 应为负数")
    void toPortfolio_shortPosition_negativeQuantity() {
        TastyworksPosition pos = new TastyworksPosition();
        pos.setSymbol("QQQ");
        pos.setQuantity("20");
        pos.setQuantityDirection("Short");
        pos.setAverageOpenPrice("350.00");
        pos.setClosePrice("368.00");

        TastyworksPositionsResponse.PositionsData data = new TastyworksPositionsResponse.PositionsData();
        data.setItems(List.of(pos));

        TastyworksPositionsResponse response = new TastyworksPositionsResponse();
        response.setData(data);

        BrokerPortfolio result = mapper.toPortfolio(response, "account-tw-002");

        assertThat(result.getPositions().get(0).getQuantity().doubleValue()).isEqualTo(-20.0);
    }

    @Test
    @DisplayName("null 响应应返回空 BrokerPortfolio")
    void toPortfolio_nullResponse_returnsEmptyPortfolio() {
        BrokerPortfolio result = mapper.toPortfolio(null, "account-tw-999");

        assertThat(result.getPositions()).isEmpty();
        assertThat(result.getAccountId()).isEqualTo("account-tw-999");
    }

    // -------------------------------------------------------------------------
    // Orders mapping
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("订单响应正确映射为 BrokerOrder 列表")
    void toOrders_validData_mapsCorrectly() {
        TastyworksOrderLeg leg = new TastyworksOrderLeg();
        leg.setSymbol("SPY");
        leg.setAction("Buy to Open");
        leg.setQuantity("50");

        TastyworksOrder order = new TastyworksOrder();
        order.setId(777L);
        order.setUnderlyingSymbol("SPY");
        order.setOrderType("Limit");
        order.setPrice("420.00");
        order.setSize("50");
        order.setStatus("Filled");
        order.setReceivedAt("2024-01-20T10:00:00Z");
        order.setLegs(List.of(leg));

        TastyworksOrdersResponse.OrdersData data = new TastyworksOrdersResponse.OrdersData();
        data.setItems(List.of(order));

        TastyworksOrdersResponse response = new TastyworksOrdersResponse();
        response.setData(data);

        List<BrokerOrder> result = mapper.toOrders(response);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getExternalOrderId()).isEqualTo("777");
        assertThat(result.get(0).getSymbol()).isEqualTo("SPY");
        assertThat(result.get(0).getSide()).isEqualTo("BUY");
        assertThat(result.get(0).getStatus()).isEqualTo("FILLED");
        assertThat(result.get(0).getPrice().doubleValue()).isEqualTo(420.00);
    }

    // -------------------------------------------------------------------------
    // Trades mapping
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Filled 订单的 fills 应正确映射为 BrokerTrade 列表")
    void toTrades_filledOrderWithFills_mapsCorrectly() {
        TastyworksFill fill = new TastyworksFill();
        fill.setFillId("FILL-001");
        fill.setQuantity("50");
        fill.setFillPrice("421.50");
        fill.setFilledAt("2024-01-20T10:00:05Z");

        TastyworksOrderLeg leg = new TastyworksOrderLeg();
        leg.setSymbol("SPY");
        leg.setAction("Buy to Open");
        leg.setQuantity("50");
        leg.setFills(List.of(fill));

        TastyworksOrder order = new TastyworksOrder();
        order.setId(777L);
        order.setUnderlyingSymbol("SPY");
        order.setStatus("Filled");
        order.setLegs(List.of(leg));

        TastyworksOrdersResponse.OrdersData data = new TastyworksOrdersResponse.OrdersData();
        data.setItems(List.of(order));

        TastyworksOrdersResponse response = new TastyworksOrdersResponse();
        response.setData(data);

        List<BrokerTrade> result = mapper.toTrades(response);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getExternalTradeId()).isEqualTo("FILL-001");
        assertThat(result.get(0).getSymbol()).isEqualTo("SPY");
        assertThat(result.get(0).getSide()).isEqualTo("BUY");
        assertThat(result.get(0).getPrice().doubleValue()).isEqualTo(421.50);
        assertThat(result.get(0).getTradedAt()).isNotNull();
    }
}
