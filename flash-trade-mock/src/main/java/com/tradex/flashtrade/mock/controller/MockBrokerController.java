package com.tradex.flashtrade.mock.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradex.flashtrade.mock.dto.MockOrderRequest;
import com.tradex.flashtrade.mock.dto.MockOrderResponse;
import com.tradex.flashtrade.mock.dto.MockPosition;
import com.tradex.flashtrade.mock.dto.MockTrade;
import com.tradex.flashtrade.mock.dto.MockWatchlistItem;
import com.tradex.flashtrade.mock.service.BrokerMockService;

@RestController
@RequestMapping("/mock")
public class MockBrokerController {

    private final BrokerMockService brokerMockService;

    public MockBrokerController(BrokerMockService brokerMockService) {
        this.brokerMockService = brokerMockService;
    }

    @GetMapping({"/accounts/{accountId}/positions", "/{brokerName}/accounts/{accountId}/positions"})
    public ResponseEntity<List<MockPosition>> positions(@PathVariable(required = false) String brokerName,
                                                       @PathVariable String accountId) {
        String effectiveBroker = brokerName == null ? "DEFAULT" : brokerName.toUpperCase();
        return ResponseEntity.ok(brokerMockService.getPositions(effectiveBroker, accountId));
    }

    @GetMapping({"/accounts/{accountId}/orders", "/{brokerName}/accounts/{accountId}/orders"})
    public ResponseEntity<List<MockOrderResponse>> orders(@PathVariable(required = false) String brokerName,
                                                         @PathVariable String accountId) {
        String effectiveBroker = brokerName == null ? "DEFAULT" : brokerName.toUpperCase();
        return ResponseEntity.ok(brokerMockService.getOrders(effectiveBroker, accountId));
    }

    @GetMapping({"/accounts/{accountId}/trades", "/{brokerName}/accounts/{accountId}/trades"})
    public ResponseEntity<List<MockTrade>> trades(@PathVariable(required = false) String brokerName,
                                                  @PathVariable String accountId) {
        String effectiveBroker = brokerName == null ? "DEFAULT" : brokerName.toUpperCase();
        return ResponseEntity.ok(brokerMockService.getTrades(effectiveBroker, accountId));
    }

    @GetMapping({"/watchlist", "/{brokerName}/watchlist"})
    public ResponseEntity<List<MockWatchlistItem>> watchlist(@PathVariable(required = false) String brokerName) {
        String effectiveBroker = brokerName == null ? "DEFAULT" : brokerName.toUpperCase();
        return ResponseEntity.ok(brokerMockService.getWatchlist(effectiveBroker));
    }

    @PostMapping({"/accounts/{accountId}/orders", "/{brokerName}/accounts/{accountId}/orders"})
    public ResponseEntity<MockOrderResponse> placeOrder(@PathVariable(required = false) String brokerName,
                                                        @PathVariable String accountId,
                                                        @RequestBody MockOrderRequest request) {
        String effectiveBroker = brokerName == null ? "DEFAULT" : brokerName.toUpperCase();
        return ResponseEntity.ok(brokerMockService.placeOrder(effectiveBroker, accountId, request));
    }

    @PostMapping({"/accounts/{accountId}/orders/{orderId}/cancel", "/{brokerName}/accounts/{accountId}/orders/{orderId}/cancel"})
    public ResponseEntity<Map<String, Object>> cancelOrder(@PathVariable(required = false) String brokerName,
                                                           @PathVariable String accountId,
                                                           @PathVariable String orderId) {
        String effectiveBroker = brokerName == null ? "DEFAULT" : brokerName.toUpperCase();
        return ResponseEntity.ok(brokerMockService.cancelOrder(effectiveBroker, accountId, orderId));
    }
}
