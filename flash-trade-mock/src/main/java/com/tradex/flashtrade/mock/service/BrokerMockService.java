package com.tradex.flashtrade.mock.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.tradex.flashtrade.mock.dto.MockOrderRequest;
import com.tradex.flashtrade.mock.dto.MockOrderResponse;
import com.tradex.flashtrade.mock.dto.MockPosition;
import com.tradex.flashtrade.mock.dto.MockTrade;
import com.tradex.flashtrade.mock.dto.MockWatchlistItem;

@Service
public class BrokerMockService {

    private final List<BrokerMockStrategy> strategies;

    public BrokerMockService(List<BrokerMockStrategy> strategies) {
        this.strategies = strategies;
    }

    public List<MockPosition> getPositions(String brokerName, String accountId) {
        return strategyFor(brokerName).getPositions(accountId);
    }

    public List<MockOrderResponse> getOrders(String brokerName, String accountId) {
        return strategyFor(brokerName).getOrders(accountId);
    }

    public List<MockTrade> getTrades(String brokerName, String accountId) {
        return strategyFor(brokerName).getTrades(accountId);
    }

    public List<MockWatchlistItem> getWatchlist(String brokerName) {
        return strategyFor(brokerName).getWatchlist();
    }

    public MockOrderResponse placeOrder(String brokerName, String accountId, MockOrderRequest request) {
        return strategyFor(brokerName).placeOrder(accountId, request);
    }

    public Map<String, Object> cancelOrder(String brokerName, String accountId, String orderId) {
        return strategyFor(brokerName).cancelOrder(accountId, orderId);
    }

    private BrokerMockStrategy strategyFor(String brokerName) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(brokerName))
                .findFirst()
                .orElseGet(() -> strategies.stream()
                        .filter(strategy -> strategy.supports("DEFAULT"))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("No mock strategy found for broker=" + brokerName)));
    }
}
