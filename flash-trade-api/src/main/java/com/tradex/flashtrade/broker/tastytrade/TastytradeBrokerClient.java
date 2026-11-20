package com.tradex.flashtrade.broker.tastytrade;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.tradex.flashtrade.broker.BrokerClient;
import com.tradex.flashtrade.config.BrokerMode;
import com.tradex.flashtrade.broker.dto.BrokerOrder;
import com.tradex.flashtrade.broker.dto.BrokerPortfolio;
import com.tradex.flashtrade.broker.dto.OrderRequest;
import com.tradex.flashtrade.broker.dto.PlaceOrderResult;
import com.tradex.flashtrade.broker.tastytrade.config.TastytradeProperties;
import com.tradex.flashtrade.exception.ErrorCode;
import com.tradex.flashtrade.exception.BusinessException;
import com.tradex.flashtrade.service.BrokerConfigService;

/**
 * Tastytrade BrokerClient 实现（支持 mock 和 real 两种模式）。
 */
@Component
public class TastytradeBrokerClient implements BrokerClient {

    private final TastytradeProperties props;
    private final TastytradeRestClient restClient;
    private final TastytradeMapper mapper;
    private final BrokerConfigService configService;
    private final Map<String, Map<String, BrokerOrder>> mockOrders = new ConcurrentHashMap<>();

    public TastytradeBrokerClient(TastytradeProperties props, TastytradeRestClient restClient, TastytradeMapper mapper,
                                  BrokerConfigService configService) {
        this.props = props;
        this.restClient = restClient;
        this.mapper = mapper;
        this.configService = configService;
    }

    @Override
    public String getBrokerName() { return "TASTYTRADE"; }

    @Override
    public BrokerPortfolio getPortfolio(String accountId) {
        if (isMockEnabled()) {
            return BrokerPortfolio.builder().accountId(accountId).brokerName("TASTYTRADE").positions(Collections.emptyList()).build();
        }
        var resp = restClient.getPositions(accountId);
        return mapper.toPortfolio(resp, accountId);
    }

    @Override
    public List<BrokerOrder> getOrders(String accountId) {
        if (isMockEnabled()) {
            Map<String, BrokerOrder> orders = mockOrders.get(accountId);
            if (orders == null) {
                return Collections.emptyList();
            }
            return orders.values().stream()
                    .sorted(Comparator.comparing(BrokerOrder::getCreatedAt).reversed())
                    .toList();
        }
        var resp = restClient.getOrders(accountId);
        return mapper.toOrders(resp);
    }

    @Override
    public java.util.List<com.tradex.flashtrade.broker.dto.BrokerTrade> getTrades(String accountId) {
        if (isMockEnabled()) return Collections.emptyList();
        return mapper.toTrades(restClient.getOrders(accountId));
    }

    @Override
    public java.util.List<com.tradex.flashtrade.broker.dto.BrokerWatchlistItem> getWatchlist(String accountId) {
        if (isMockEnabled()) return Collections.emptyList();
        var resp = restClient.getWatchlist(accountId);
        if (resp == null || resp.getData() == null) {
            return Collections.emptyList();
        }
        return resp.getData().stream()
                .map(item -> new com.tradex.flashtrade.broker.dto.BrokerWatchlistItem(
                        getBrokerName(), item.getName(), item.getSymbol()))
                .toList();
    }

    @Override
    public PlaceOrderResult placeOrder(String accountId, OrderRequest request) {
        if (isMockEnabled()) {
            String externalOrderId = "TT-MOCK-" + UUID.randomUUID();
            BrokerOrder order = BrokerOrder.builder()
                    .externalOrderId(externalOrderId)
                    .symbol(request.getSymbol())
                    .side(request.getSide().name())
                    .quantity(BigDecimal.valueOf(request.getQuantity()))
                    .price(request.getLimitPrice())
                    .status("PENDING")
                    .createdAt(LocalDateTime.now())
                    .build();
            mockOrdersFor(accountId).put(externalOrderId, order);
            return PlaceOrderResult.builder()
                    .success(true)
                    .externalOrderId(externalOrderId)
                    .message("Mock 下单已接受")
                    .build();
        }
        var resp = restClient.placeOrder(accountId, request);
        return mapper.toPlaceOrderResult(resp);
    }

    @Override
    public void cancelOrder(String accountId, String externalOrderId) {
        if (isMockEnabled()) {
            Map<String, BrokerOrder> orders = mockOrders.get(accountId);
            BrokerOrder order = orders == null ? null : orders.computeIfPresent(
                    externalOrderId, (id, current) -> copyWithStatus(current, "CANCELLED"));
            if (order == null) {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Mock 订单不存在");
            }
            return;
        }
        boolean ok = restClient.cancelOrder(accountId, externalOrderId);
        if (!ok) throw new RuntimeException("Tastytrade cancelOrder failed for " + externalOrderId);
    }

    @Override
    public BrokerOrder getOrderStatus(String accountId, String externalOrderId) {
        if (isMockEnabled()) {
            Map<String, BrokerOrder> orders = mockOrders.get(accountId);
            BrokerOrder order = orders == null ? null : orders.get(externalOrderId);
            if (order == null) {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Mock 订单不存在");
            }
            return order;
        }
        return BrokerClient.super.getOrderStatus(accountId, externalOrderId);
    }

    private Map<String, BrokerOrder> mockOrdersFor(String accountId) {
        return mockOrders.computeIfAbsent(accountId, ignored -> new ConcurrentHashMap<>());
    }

    private BrokerOrder copyWithStatus(BrokerOrder order, String status) {
        return BrokerOrder.builder()
                .externalOrderId(order.getExternalOrderId())
                .symbol(order.getSymbol())
                .side(order.getSide())
                .quantity(order.getQuantity())
                .price(order.getPrice())
                .status(status)
                .createdAt(order.getCreatedAt())
                .filledAt(order.getFilledAt())
                .build();
    }

    private boolean isMockEnabled() {
        return configService.getEffectiveMode(getBrokerName()) == BrokerMode.MOCK;
    }
}

