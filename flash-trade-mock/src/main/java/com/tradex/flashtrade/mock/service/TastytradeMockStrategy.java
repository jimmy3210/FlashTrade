package com.tradex.flashtrade.mock.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.tradex.flashtrade.mock.dto.MockOrderRequest;
import com.tradex.flashtrade.mock.dto.MockOrderResponse;
import com.tradex.flashtrade.mock.dto.MockPosition;
import com.tradex.flashtrade.mock.dto.MockTrade;
import com.tradex.flashtrade.mock.dto.MockWatchlistItem;

@Component
public class TastytradeMockStrategy implements BrokerMockStrategy {

    @Override
    public boolean supports(String brokerName) {
        return "TASTYTRADE".equalsIgnoreCase(brokerName);
    }

    @Override
    public List<MockPosition> getPositions(String accountId) {
        return List.of(
                new MockPosition("SPY", 20, 510.20, 10204.00),
                new MockPosition("QQQ", 15, 390.50, 5857.50)
        );
    }

    @Override
    public List<MockOrderResponse> getOrders(String accountId) {
        return List.of(new MockOrderResponse("tt-ord-001", "SPY", "BUY", "MARKET", 20, null, "FILLED"));
    }

    @Override
    public List<MockTrade> getTrades(String accountId) {
        return List.of(new MockTrade("tt-trd-001", "SPY", "BUY", 20, 510.20, "2026-07-14T08:10:00"));
    }

    @Override
    public List<MockWatchlistItem> getWatchlist() {
        return List.of(
                new MockWatchlistItem("SPY", "SPDR S&P 500 ETF Trust", 510.20, 0.92),
                new MockWatchlistItem("QQQ", "Invesco QQQ Trust", 390.50, 1.14),
                new MockWatchlistItem("AAPL", "Apple Inc.", 195.30, 0.68)
        );
    }

    @Override
    public MockOrderResponse placeOrder(String accountId, MockOrderRequest request) {
        return new MockOrderResponse("tt-ord-" + UUID.randomUUID().toString().substring(0, 8),
                request.symbol(), request.side(), request.orderType(), request.quantity(), request.limitPrice(), "PENDING");
    }

    @Override
    public Map<String, Object> cancelOrder(String accountId, String orderId) {
        return Map.of("broker", "TASTYTRADE", "status", "CANCELLED", "orderId", orderId);
    }
}
