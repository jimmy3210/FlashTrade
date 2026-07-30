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
public class SchwabMockStrategy implements BrokerMockStrategy {

    @Override
    public boolean supports(String brokerName) {
        return "SCHWAB".equalsIgnoreCase(brokerName);
    }

    @Override
    public List<MockPosition> getPositions(String accountId) {
        return List.of(
                new MockPosition("MSFT", 50, 310.10, 15505.00),
                new MockPosition("NVDA", 25, 120.00, 3000.00)
        );
    }

    @Override
    public List<MockOrderResponse> getOrders(String accountId) {
        return List.of(new MockOrderResponse("schwab-ord-001", "MSFT", "BUY", "LIMIT", 50, 310.10, "PENDING"));
    }

    @Override
    public List<MockTrade> getTrades(String accountId) {
        return List.of(new MockTrade("schwab-trd-001", "MSFT", "BUY", 50, 310.10, "2026-07-14T08:05:00"));
    }

    @Override
    public List<MockWatchlistItem> getWatchlist() {
        return List.of(new MockWatchlistItem("MSFT", "Microsoft", 418.22, 1.36),
                new MockWatchlistItem("NVDA", "NVIDIA", 124.84, 2.19));
    }

    @Override
    public MockOrderResponse placeOrder(String accountId, MockOrderRequest request) {
        return new MockOrderResponse("schwab-ord-" + UUID.randomUUID().toString().substring(0, 8),
                request.symbol(), request.side(), request.orderType(), request.quantity(), request.limitPrice(), "PENDING");
    }

    @Override
    public Map<String, Object> cancelOrder(String accountId, String orderId) {
        return Map.of("broker", "SCHWAB", "status", "CANCELLED", "orderId", orderId);
    }
}
