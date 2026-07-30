package com.tradex.flashtrade.broker.schwab;

import com.tradex.flashtrade.broker.dto.BrokerOrder;
import com.tradex.flashtrade.broker.dto.BrokerPortfolio;
import com.tradex.flashtrade.broker.dto.BrokerTrade;
import com.tradex.flashtrade.broker.schwab.dto.SchwabAccountPositions;
import com.tradex.flashtrade.broker.schwab.dto.SchwabInstrument;
import com.tradex.flashtrade.broker.schwab.dto.SchwabOrderLeg;
import com.tradex.flashtrade.broker.schwab.dto.SchwabOrderResponse;
import com.tradex.flashtrade.broker.schwab.dto.SchwabPositionItem;
import com.tradex.flashtrade.broker.schwab.dto.SchwabTransaction;
import com.tradex.flashtrade.broker.schwab.dto.SchwabTransferItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SchwabMapper 单元测试")
class SchwabMapperTest {

    private SchwabMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SchwabMapper();
    }

    // -------------------------------------------------------------------------
    // Portfolio mapping
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("持仓响应正确映射为 BrokerPortfolio")
    void toPortfolio_validData_mapsCorrectly() {
        SchwabInstrument instrument = new SchwabInstrument();
        instrument.setSymbol("AAPL");
        instrument.setNetChange(1.25);

        SchwabPositionItem posItem = new SchwabPositionItem();
        posItem.setLongQuantity(100.0);
        posItem.setShortQuantity(0.0);
        posItem.setAveragePrice(150.25);
        posItem.setMarketValue(17550.0);
        posItem.setLongOpenProfitLoss(2525.0);
        posItem.setInstrument(instrument);

        SchwabAccountPositions.SecuritiesAccount secAccount =
                new SchwabAccountPositions.SecuritiesAccount();
        secAccount.setAccountNumber("ACC-001");
        secAccount.setPositions(List.of(posItem));

        SchwabAccountPositions accountPositions = new SchwabAccountPositions();
        accountPositions.setSecuritiesAccount(secAccount);

        BrokerPortfolio result = mapper.toPortfolio(List.of(accountPositions), "ACC-001");

        assertThat(result.getBrokerName()).isEqualTo("SCHWAB");
        assertThat(result.getAccountId()).isEqualTo("ACC-001");
        assertThat(result.getPositions()).hasSize(1);
        assertThat(result.getPositions().get(0).getSymbol()).isEqualTo("AAPL");
        assertThat(result.getPositions().get(0).getQuantity().doubleValue()).isEqualTo(100.0);
        assertThat(result.getTotalMarketValue().doubleValue()).isEqualTo(17550.0);
        assertThat(result.getTotalUnrealizedPnl().doubleValue()).isEqualTo(2525.0);
    }

    @Test
    @DisplayName("空的持仓响应应返回零值 BrokerPortfolio")
    void toPortfolio_emptyList_returnsEmptyPortfolio() {
        BrokerPortfolio result = mapper.toPortfolio(List.of(), "ACC-999");

        assertThat(result.getPositions()).isEmpty();
        assertThat(result.getTotalMarketValue().doubleValue()).isEqualTo(0.0);
    }

    // -------------------------------------------------------------------------
    // Orders mapping
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("订单响应正确映射为 BrokerOrder 列表")
    void toOrders_validData_mapsCorrectly() {
        SchwabInstrument instrument = new SchwabInstrument();
        instrument.setSymbol("MSFT");

        SchwabOrderLeg leg = new SchwabOrderLeg();
        leg.setInstruction("BUY");
        leg.setQuantity(50.0);
        leg.setInstrument(instrument);

        SchwabOrderResponse order = new SchwabOrderResponse();
        order.setOrderId(123456L);
        order.setStatus("FILLED");
        order.setQuantity(50.0);
        order.setPrice(280.0);
        order.setEnteredTime("2024-01-15T10:30:00+00:00");
        order.setCloseTime("2024-01-15T10:30:05+00:00");
        order.setOrderLegCollection(List.of(leg));

        List<BrokerOrder> result = mapper.toOrders(List.of(order));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getExternalOrderId()).isEqualTo("123456");
        assertThat(result.get(0).getSymbol()).isEqualTo("MSFT");
        assertThat(result.get(0).getSide()).isEqualTo("BUY");
        assertThat(result.get(0).getStatus()).isEqualTo("FILLED");
    }

    @Test
    @DisplayName("SELL_SHORT 指令应正规化为 SELL")
    void toOrders_sellShortInstruction_normalizedToSell() {
        SchwabInstrument instrument = new SchwabInstrument();
        instrument.setSymbol("NVDA");

        SchwabOrderLeg leg = new SchwabOrderLeg();
        leg.setInstruction("SELL_SHORT");
        leg.setQuantity(10.0);
        leg.setInstrument(instrument);

        SchwabOrderResponse order = new SchwabOrderResponse();
        order.setOrderId(789L);
        order.setStatus("FILLED");
        order.setQuantity(10.0);
        order.setOrderLegCollection(List.of(leg));

        List<BrokerOrder> result = mapper.toOrders(List.of(order));

        assertThat(result.get(0).getSide()).isEqualTo("SELL");
    }

    // -------------------------------------------------------------------------
    // Trades mapping
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("成交记录正确映射为 BrokerTrade 列表")
    void toTrades_validData_mapsCorrectly() {
        SchwabInstrument instrument = new SchwabInstrument();
        instrument.setSymbol("AAPL");

        SchwabTransferItem item = new SchwabTransferItem();
        item.setInstrument(instrument);
        item.setAmount(100.0);   // 正数 = BUY
        item.setPrice(150.25);
        item.setCost(15025.0);

        SchwabTransaction transaction = new SchwabTransaction();
        transaction.setActivityId(987654L);
        transaction.setOrderId(123456L);
        transaction.setType("TRADE");
        transaction.setTradeDate("2024-01-15T10:30:05+00:00");
        transaction.setNetAmount(-15025.0);
        transaction.setTransferItems(List.of(item));

        List<BrokerTrade> result = mapper.toTrades(List.of(transaction));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getExternalTradeId()).isEqualTo("987654");
        assertThat(result.get(0).getExternalOrderId()).isEqualTo("123456");
        assertThat(result.get(0).getSymbol()).isEqualTo("AAPL");
        assertThat(result.get(0).getSide()).isEqualTo("BUY");
        assertThat(result.get(0).getQuantity().doubleValue()).isEqualTo(100.0);
        assertThat(result.get(0).getPrice().doubleValue()).isEqualTo(150.25);
    }

    @Test
    @DisplayName("非 TRADE 类型的成交记录应被过滤掉")
    void toTrades_nonTradeType_isFiltered() {
        SchwabTransaction dividendTx = new SchwabTransaction();
        dividendTx.setActivityId(111L);
        dividendTx.setType("DIVIDEND_OR_INTEREST");

        List<BrokerTrade> result = mapper.toTrades(List.of(dividendTx));

        assertThat(result).isEmpty();
    }
}
