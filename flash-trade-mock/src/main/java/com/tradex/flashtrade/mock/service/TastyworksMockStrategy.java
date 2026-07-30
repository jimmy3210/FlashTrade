package com.tradex.flashtrade.mock.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.tradex.flashtrade.mock.dto.MockOrderRequest;
import com.tradex.flashtrade.mock.dto.MockOrderResponse;
import com.tradex.flashtrade.mock.dto.MockPosition;
import com.tradex.flashtrade.mock.dto.MockTrade;

@Component
public class TastyworksMockStrategy implements BrokerMockStrategy {

    @Override
    public boolean supports(String brokerName) {
        return "TASTYWORKS".equalsIgnoreCase(brokerName);
    }

    @Override
    public List<MockPosition> getPositions(String accountId) {
        return List.of(
                new MockPosition("TSLA", 10, 248.00, 2480.00),
                new MockPosition("PLTR", 40, 19.80, 792.00)
        );
    }

    @Override
    public List<MockOrderResponse> getOrders(String accountId) {
        return List.of(new MockOrderResponse("tw-ord-001", "TSLA", "SELL", "LIMIT", 10, 248.00, "CANCELLED"));
    }

    @Override
    public List<MockTrade> getTrades(String accountId) {
        return List.of(new MockTrade("tw-trd-001", "TSLA", "SELL", 10, 248.00, "2026-07-14T08:15:00"));
    }

    @Override
    public MockOrderResponse placeOrder(String accountId, MockOrderRequest request) {
        return new MockOrderResponse("tw-ord-" + UUID.randomUUID().toString().substring(0, 8),
                request.symbol(), request.side(), request.orderType(), request.quantity(), request.limitPrice(), "PENDING");
    }

    @Override
    public Map<String, Object> cancelOrder(String accountId, String orderId) {
        return Map.of("broker", "TASTYWORKS", "status", "CANCELLED", "orderId", orderId);
    }
}
