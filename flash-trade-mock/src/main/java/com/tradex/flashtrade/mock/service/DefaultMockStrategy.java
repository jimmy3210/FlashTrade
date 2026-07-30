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
public class DefaultMockStrategy implements BrokerMockStrategy {

    @Override
    public boolean supports(String brokerName) {
        return true;
    }

    @Override
    public List<MockPosition> getPositions(String accountId) {
        return List.of(new MockPosition("XYZ", 1, 100.00, 100.00));
    }

    @Override
    public List<MockOrderResponse> getOrders(String accountId) {
        return List.of(new MockOrderResponse("default-ord-001", "XYZ", "BUY", "LIMIT", 1, 100.00, "PENDING"));
    }

    @Override
    public List<MockTrade> getTrades(String accountId) {
        return List.of(new MockTrade("default-trd-001", "XYZ", "BUY", 1, 100.00, "2026-07-14T08:20:00"));
    }

    @Override
    public List<MockWatchlistItem> getWatchlist() {
        return List.of(new MockWatchlistItem("AAPL", "Apple Inc.", 193.26, 1.28));
    }

    @Override
    public MockOrderResponse placeOrder(String accountId, MockOrderRequest request) {
        return new MockOrderResponse("default-ord-" + UUID.randomUUID().toString().substring(0, 8),
                request.symbol(), request.side(), request.orderType(), request.quantity(), request.limitPrice(), "PENDING");
    }

    @Override
    public Map<String, Object> cancelOrder(String accountId, String orderId) {
        return Map.of("broker", "DEFAULT", "status", "CANCELLED", "orderId", orderId);
    }
}
