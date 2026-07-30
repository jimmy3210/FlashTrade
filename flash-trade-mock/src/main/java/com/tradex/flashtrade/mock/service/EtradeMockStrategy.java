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
public class EtradeMockStrategy implements BrokerMockStrategy {

    @Override
    public boolean supports(String brokerName) {
        return "ETRADE".equalsIgnoreCase(brokerName);
    }

    @Override
    public List<MockPosition> getPositions(String accountId) {
        return List.of(
                new MockPosition("AAPL", 80, 178.25, 14260.00),
                new MockPosition("AMZN", 30, 195.00, 5850.00)
        );
    }

    @Override
    public List<MockOrderResponse> getOrders(String accountId) {
        return List.of(new MockOrderResponse("etrade-ord-001", "AAPL", "BUY", "LIMIT", 80, 178.25, "FILLED"));
    }

    @Override
    public List<MockTrade> getTrades(String accountId) {
        return List.of(new MockTrade("etrade-trd-001", "AAPL", "BUY", 80, 178.25, "2026-07-14T08:00:00"));
    }

    @Override
    public List<MockWatchlistItem> getWatchlist() {
        return List.of(new MockWatchlistItem("AAPL", "Apple Inc.", 193.26, 1.28),
                new MockWatchlistItem("AMZN", "Amazon.com", 188.90, 0.74));
    }

    @Override
    public MockOrderResponse placeOrder(String accountId, MockOrderRequest request) {
        return new MockOrderResponse("etrade-ord-" + UUID.randomUUID().toString().substring(0, 8),
                request.symbol(), request.side(), request.orderType(), request.quantity(), request.limitPrice(), "PENDING");
    }

    @Override
    public Map<String, Object> cancelOrder(String accountId, String orderId) {
        return Map.of("broker", "ETRADE", "status", "CANCELLED", "orderId", orderId);
    }
}
