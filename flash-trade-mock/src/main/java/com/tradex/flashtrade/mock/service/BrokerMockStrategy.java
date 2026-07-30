package com.tradex.flashtrade.mock.service;

import java.util.List;
import java.util.Map;

import com.tradex.flashtrade.mock.dto.MockOrderRequest;
import com.tradex.flashtrade.mock.dto.MockOrderResponse;
import com.tradex.flashtrade.mock.dto.MockPosition;
import com.tradex.flashtrade.mock.dto.MockTrade;
import com.tradex.flashtrade.mock.dto.MockWatchlistItem;

public interface BrokerMockStrategy {

    boolean supports(String brokerName);

    List<MockPosition> getPositions(String accountId);

    List<MockOrderResponse> getOrders(String accountId);

    List<MockTrade> getTrades(String accountId);

    default List<MockWatchlistItem> getWatchlist() {
        return List.of();
    }

    MockOrderResponse placeOrder(String accountId, MockOrderRequest request);

    Map<String, Object> cancelOrder(String accountId, String orderId);
}
