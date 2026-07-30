package com.tradex.flashtrade.broker.etrade;

import com.tradex.flashtrade.broker.dto.BrokerOrder;
import com.tradex.flashtrade.broker.dto.BrokerPortfolio;
import com.tradex.flashtrade.broker.dto.BrokerTrade;
import com.tradex.flashtrade.broker.etrade.dto.EtradeInstrument;
import com.tradex.flashtrade.broker.etrade.dto.EtradeOrder;
import com.tradex.flashtrade.broker.etrade.dto.EtradeOrderDetail;
import com.tradex.flashtrade.broker.etrade.dto.EtradeOrdersResponse;
import com.tradex.flashtrade.broker.etrade.dto.EtradePortfolioResponse;
import com.tradex.flashtrade.broker.etrade.dto.EtradePosition;
import com.tradex.flashtrade.broker.etrade.dto.EtradeTransactionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EtradeMapper 单元测试")
class EtradeMapperTest {

    private EtradeMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new EtradeMapper();
    }

    // -------------------------------------------------------------------------
    // Portfolio mapping
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("持仓响应正确映射为 BrokerPortfolio")
    void toPortfolio_validData_mapsCorrectly() {
        EtradeInstrument instrument = new EtradeInstrument();
        instrument.setSymbol("AAPL");

        EtradePosition pos = new EtradePosition();
        pos.setProduct(instrument);
        pos.setQuantity(80.0);
        pos.setCostPerShare(155.0);
        pos.setCurrentPrice(178.0);
        pos.setMarketValue(14240.0);
        pos.setTotalGain(1840.0);

        EtradePortfolioResponse.AccountPortfolio ap = new EtradePortfolioResponse.AccountPortfolio();
        ap.setPositions(List.of(pos));

        EtradePortfolioResponse.PortfolioResponse pr = new EtradePortfolioResponse.PortfolioResponse();
        pr.setAccountPortfolio(List.of(ap));

        EtradePortfolioResponse response = new EtradePortfolioResponse();
        response.setPortfolioResponse(pr);

        BrokerPortfolio result = mapper.toPortfolio(response);

        assertThat(result.getPositions()).hasSize(1);
        assertThat(result.getPositions().get(0).getSymbol()).isEqualTo("AAPL");
        assertThat(result.getPositions().get(0).getQuantity().doubleValue()).isEqualTo(80.0);
        assertThat(result.getPositions().get(0).getAverageCost().doubleValue()).isEqualTo(155.0);
        assertThat(result.getTotalMarketValue().doubleValue()).isEqualTo(14240.0);
    }

    @Test
    @DisplayName("null 响应应返回空 BrokerPortfolio")
    void toPortfolio_nullResponse_returnsEmptyPortfolio() {
        BrokerPortfolio result = mapper.toPortfolio(null);

        assertThat(result.getPositions()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // Orders mapping
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("订单响应正确映射为 BrokerOrder 列表")
    void toOrders_validData_mapsCorrectly() {
        EtradeInstrument instrument = new EtradeInstrument();
        instrument.setSymbol("MSFT");

        EtradeOrderDetail detail = new EtradeOrderDetail();
        detail.setProduct(instrument);
        detail.setOrderedQuantity(50.0);

        EtradeOrder order = new EtradeOrder();
        order.setOrderId(999L);
        order.setOrderAction("BUY");
        order.setPriceType("LIMIT");
        order.setLimitPrice(280.0);
        order.setOrderedQuantity(50.0);
        order.setStatus("OPEN");
        order.setPlacedTime(1700000000000L);
        order.setOrderDetails(List.of(detail));

        EtradeOrdersResponse.OrdersResponse ordersResp = new EtradeOrdersResponse.OrdersResponse();
        ordersResp.setOrders(List.of(order));

        EtradeOrdersResponse response = new EtradeOrdersResponse();
        response.setOrdersResponse(ordersResp);

        List<BrokerOrder> result = mapper.toOrders(response);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getExternalOrderId()).isEqualTo("999");
        assertThat(result.get(0).getSymbol()).isEqualTo("MSFT");
        assertThat(result.get(0).getSide()).isEqualTo("BUY");
        assertThat(result.get(0).getPrice().doubleValue()).isEqualTo(280.0);
    }

    @Test
    @DisplayName("BUY_TO_COVER 应正规化为 BUY")
    void toOrders_buyToCoverAction_normalizedToBuy() {
        EtradeOrder order = new EtradeOrder();
        order.setOrderId(1L);
        order.setOrderAction("BUY_TO_COVER");
        order.setOrderedQuantity(10.0);
        order.setStatus("FILLED");

        EtradeOrdersResponse.OrdersResponse ordersResp = new EtradeOrdersResponse.OrdersResponse();
        ordersResp.setOrders(List.of(order));

        EtradeOrdersResponse response = new EtradeOrdersResponse();
        response.setOrdersResponse(ordersResp);

        List<BrokerOrder> result = mapper.toOrders(response);

        assertThat(result.get(0).getSide()).isEqualTo("BUY");
    }

    @Test
    @DisplayName("SELL_SHORT 应正规化为 SELL")
    void toOrders_sellShortAction_normalizedToSell() {
        EtradeOrder order = new EtradeOrder();
        order.setOrderId(2L);
        order.setOrderAction("SELL_SHORT");
        order.setOrderedQuantity(5.0);
        order.setStatus("FILLED");

        EtradeOrdersResponse.OrdersResponse ordersResp = new EtradeOrdersResponse.OrdersResponse();
        ordersResp.setOrders(List.of(order));

        EtradeOrdersResponse response = new EtradeOrdersResponse();
        response.setOrdersResponse(ordersResp);

        List<BrokerOrder> result = mapper.toOrders(response);

        assertThat(result.get(0).getSide()).isEqualTo("SELL");
    }

    // -------------------------------------------------------------------------
    // Trades mapping
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("成交记录 BOUGHT/SOLD 正确映射，DIV 被过滤")
    void toTrades_filtersDivTransactions() {
        EtradeInstrument instrument = new EtradeInstrument();
        instrument.setSymbol("AAPL");

        EtradeTransactionResponse.BrokerageInfo brokerage = new EtradeTransactionResponse.BrokerageInfo();
        brokerage.setProduct(instrument);
        brokerage.setQuantity(80.0);
        brokerage.setPrice(155.0);

        EtradeTransactionResponse.EtradeTransaction bought = new EtradeTransactionResponse.EtradeTransaction();
        bought.setTransactionId(101L);
        bought.setTransactionType("BOUGHT");
        bought.setTransactionDate(1700000000000L);
        bought.setBrokerage(brokerage);

        EtradeTransactionResponse.EtradeTransaction div = new EtradeTransactionResponse.EtradeTransaction();
        div.setTransactionId(102L);
        div.setTransactionType("DIV");
        div.setTransactionDate(1700001000000L);

        EtradeTransactionResponse.TransactionListResponse listResp =
                new EtradeTransactionResponse.TransactionListResponse();
        listResp.setTransactions(List.of(bought, div));

        EtradeTransactionResponse response = new EtradeTransactionResponse();
        response.setTransactionListResponse(listResp);

        List<BrokerTrade> result = mapper.toTrades(response);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getExternalTradeId()).isEqualTo("101");
        assertThat(result.get(0).getSide()).isEqualTo("BUY");
        assertThat(result.get(0).getSymbol()).isEqualTo("AAPL");
        assertThat(result.get(0).getQuantity().doubleValue()).isEqualTo(80.0);
    }
}
