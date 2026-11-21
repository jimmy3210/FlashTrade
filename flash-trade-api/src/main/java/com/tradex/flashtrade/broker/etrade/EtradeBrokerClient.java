package com.tradex.flashtrade.broker.etrade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.tradex.flashtrade.broker.BrokerClient;
import com.tradex.flashtrade.config.BrokerMode;
import com.tradex.flashtrade.broker.dto.BrokerOrder;
import com.tradex.flashtrade.broker.dto.BrokerPortfolio;
import com.tradex.flashtrade.broker.dto.BrokerPosition;
import com.tradex.flashtrade.broker.dto.BrokerTrade;
import com.tradex.flashtrade.broker.dto.MarketData;
import com.tradex.flashtrade.broker.dto.OrderRequest;
import com.tradex.flashtrade.broker.dto.PlaceOrderResult;
import com.tradex.flashtrade.broker.etrade.config.EtradeProperties;
import com.tradex.flashtrade.service.BrokerConfigService;

/**
 * E*TRADE 券商客户端实现
 * <p>
 * Mock 模式返回固定演示数据；Real 模式通过 {@link EtradeRestClient} 调用
 * E*TRADE Trader API（OAuth 1.0a）。
 * </p>
 */
@Service
public class EtradeBrokerClient implements BrokerClient {

    private static final Logger log = LoggerFactory.getLogger(EtradeBrokerClient.class);

    /** 与 Position.broker_name 及 BrokerService 路由 Key 对应 */
    public static final String BROKER_NAME = "ETRADE";

    private final EtradeProperties   props;
    private final EtradeRestClient   restClient;
    private final EtradeMapper       mapper;
    private final BrokerConfigService configService;

    public EtradeBrokerClient(EtradeProperties props,
                              EtradeRestClient restClient,
                              EtradeMapper     mapper,
                              BrokerConfigService configService) {
        this.props         = props;
        this.restClient    = restClient;
        this.mapper        = mapper;
        this.configService = configService;
    }

    @Override
    public String getBrokerName() {
        return BROKER_NAME;
    }

    // -------------------------------------------------------------------------
    // Portfolio
    // -------------------------------------------------------------------------

    @Override
    public BrokerPortfolio getPortfolio(String accountId) {
        if (isMockEnabled()) {
            log.debug("E*TRADE Mock: getPortfolio accountId={}", accountId);
            return buildMockPortfolio(accountId);
        }
        log.info("E*TRADE: getPortfolio accountId={}", accountId);
        return mapper.toPortfolio(restClient.getPortfolio(accountId));
    }

    // -------------------------------------------------------------------------
    // Orders
    // -------------------------------------------------------------------------

    @Override
    public List<BrokerOrder> getOrders(String accountId) {
        if (isMockEnabled()) {
            log.debug("E*TRADE Mock: getOrders accountId={}", accountId);
            return buildMockOrders();
        }
        log.info("E*TRADE: getOrders accountId={}", accountId);
        return mapper.toOrders(restClient.getOrders(accountId));
    }

    // -------------------------------------------------------------------------
    // Trades
    // -------------------------------------------------------------------------

    @Override
    public List<BrokerTrade> getTrades(String accountId) {
        if (isMockEnabled()) {
            log.debug("E*TRADE Mock: getTrades accountId={}", accountId);
            return buildMockTrades();
        }
        log.info("E*TRADE: getTrades accountId={}", accountId);
        return mapper.toTrades(restClient.getTransactions(accountId));
    }

    // -------------------------------------------------------------------------
    // Mock 数据生成（开发 / 测试用）
    // -------------------------------------------------------------------------

    private BrokerPortfolio buildMockPortfolio(String accountId) {
        List<BrokerPosition> positions = List.of(
                BrokerPosition.builder()
                        .symbol("AAPL")
                        .quantity(BigDecimal.valueOf(80))
                        .averageCost(BigDecimal.valueOf(155.00))
                        .currentPrice(BigDecimal.valueOf(178.00))
                        .marketValue(BigDecimal.valueOf(14240.00))
                        .unrealizedPnl(BigDecimal.valueOf(1840.00))
                        .build(),
                BrokerPosition.builder()
                        .symbol("AMZN")
                        .quantity(BigDecimal.valueOf(30))
                        .averageCost(BigDecimal.valueOf(180.00))
                        .currentPrice(BigDecimal.valueOf(195.00))
                        .marketValue(BigDecimal.valueOf(5850.00))
                        .unrealizedPnl(BigDecimal.valueOf(450.00))
                        .build()
        );
        return BrokerPortfolio.builder()
                .accountId(accountId)
                .brokerName(BROKER_NAME)
                .totalMarketValue(BigDecimal.valueOf(20090.00))
                .totalUnrealizedPnl(BigDecimal.valueOf(2290.00))
                .positions(positions)
                .build();
    }

    private List<BrokerOrder> buildMockOrders() {
        return List.of(
                BrokerOrder.builder()
                        .externalOrderId("ETRADE-ORD-001")
                        .symbol("AAPL")
                        .side("BUY")
                        .quantity(BigDecimal.valueOf(80))
                        .price(BigDecimal.valueOf(155.00))
                        .status("FILLED")
                        .createdAt(LocalDateTime.now().minusDays(14))
                        .filledAt(LocalDateTime.now().minusDays(14))
                        .build(),
                BrokerOrder.builder()
                        .externalOrderId("ETRADE-ORD-002")
                        .symbol("AMZN")
                        .side("BUY")
                        .quantity(BigDecimal.valueOf(30))
                        .price(BigDecimal.valueOf(180.00))
                        .status("FILLED")
                        .createdAt(LocalDateTime.now().minusDays(7))
                        .filledAt(LocalDateTime.now().minusDays(7))
                        .build()
        );
    }

    private List<BrokerTrade> buildMockTrades() {
        return List.of(
                BrokerTrade.builder()
                        .externalTradeId("ETRADE-TRD-001")
                        .externalOrderId("ETRADE-ORD-001")
                        .symbol("AAPL")
                        .side("BUY")
                        .quantity(BigDecimal.valueOf(80))
                        .price(BigDecimal.valueOf(155.00))
                        .commission(BigDecimal.ZERO)
                        .tradedAt(LocalDateTime.now().minusDays(14))
                        .build(),
                BrokerTrade.builder()
                        .externalTradeId("ETRADE-TRD-002")
                        .externalOrderId("ETRADE-ORD-002")
                        .symbol("AMZN")
                        .side("BUY")
                        .quantity(BigDecimal.valueOf(30))
                        .price(BigDecimal.valueOf(180.00))
                        .commission(BigDecimal.ZERO)
                        .tradedAt(LocalDateTime.now().minusDays(7))
                        .build()
        );
    }

    @Override
    public Map<String, MarketData> getBatchMarketData(String accountId, List<String> symbols) {
        if (isMockEnabled()) {
            Map<String, MarketData> result = new HashMap<>();
            for (String symbol : symbols) {
                double base  = 100.0 + (Math.abs(symbol.hashCode()) % 400);
                double delta = (Math.random() - 0.5) * base * 0.01;
                java.math.BigDecimal price = java.math.BigDecimal.valueOf(base + delta)
                        .setScale(2, RoundingMode.HALF_UP);
                result.put(symbol, MarketData.builder()
                        .symbol(symbol).lastPrice(price)
                        .bidPrice(price.subtract(java.math.BigDecimal.valueOf(0.01)))
                        .askPrice(price.add(java.math.BigDecimal.valueOf(0.01)))
                        .build());
            }
            return result;
        }
        return BrokerClient.super.getBatchMarketData(accountId, symbols);
    }

    private boolean isMockEnabled() {
        return configService.getEffectiveMode(BROKER_NAME) == BrokerMode.MOCK;
    }

    @Override
    public PlaceOrderResult placeOrder(String accountId, OrderRequest request) {
        if (isMockEnabled()) {
            String externalId = "ETRADE-ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            log.info("Etrade Mock: placeOrder {} {} {} -> {}",
                    request.getSide(), request.getQuantity(), request.getSymbol(), externalId);
            return PlaceOrderResult.builder().success(true)
                    .externalOrderId(externalId).message("Mock 下单成功").build();
        }

        log.info("E*TRADE Real: preview order accountId={} symbol={} qty={}", accountId, request.getSymbol(), request.getQuantity());
        var previewResponse = restClient.previewOrder(accountId, request);
        if (previewResponse == null || previewResponse.getPreviewOrderResponse() == null) {
            return PlaceOrderResult.builder().success(false)
                    .message("E*TRADE 订单预览失败：空响应").build();
        }

        var previewOrder = previewResponse.getPreviewOrderResponse();
        if (previewOrder.getErrors() != null && !previewOrder.getErrors().isEmpty()) {
            String message = previewOrder.getErrors().stream()
                    .map(err -> err.getCode() + ": " + err.getDescription())
                    .collect(java.util.stream.Collectors.joining("; "));
            return PlaceOrderResult.builder().success(false)
                    .message("E*TRADE 订单预览失败：" + message).build();
        }
        if (previewOrder.getPreviewId() == null || previewOrder.getPreviewId().isBlank()) {
            return PlaceOrderResult.builder().success(false)
                    .message("E*TRADE 订单预览失败：previewId 缺失").build();
        }

        log.info("E*TRADE Real: place order accountId={} previewId={}", accountId, previewOrder.getPreviewId());
        var placeResponse = restClient.placeOrder(accountId, request, previewOrder.getPreviewId());
        if (placeResponse == null || placeResponse.getPlaceOrderResponse() == null) {
            return PlaceOrderResult.builder().success(false)
                    .message("E*TRADE 下单失败：空响应").build();
        }

        var placeOrder = placeResponse.getPlaceOrderResponse();
        if (placeOrder.getErrors() != null && !placeOrder.getErrors().isEmpty()) {
            String message = placeOrder.getErrors().stream()
                    .map(err -> err.getCode() + ": " + err.getDescription())
                    .collect(java.util.stream.Collectors.joining("; "));
            return PlaceOrderResult.builder().success(false)
                    .message("E*TRADE 下单失败：" + message).build();
        }

        return PlaceOrderResult.builder().success(true)
                .externalOrderId(placeOrder.getOrderId())
                .message("E*TRADE 下单成功").build();
    }

    @Override
    public void cancelOrder(String accountId, String externalOrderId) {
        if (isMockEnabled()) {
            log.info("Etrade Mock: cancelOrder {}", externalOrderId);
            return;
        }
        boolean ok = restClient.cancelOrder(accountId, externalOrderId);
        if (!ok) {
            throw new RuntimeException("E*TRADE cancelOrder failed: " + externalOrderId);
        }
    }

    @Override
    public BrokerOrder getOrderStatus(String accountId, String externalOrderId) {
        if (isMockEnabled()) {
            return BrokerOrder.builder().externalOrderId(externalOrderId)
                    .symbol("MOCK").side("BUY").quantity(BigDecimal.valueOf(80))
                    .price(BigDecimal.valueOf(155.00)).status("FILLED")
                    .createdAt(LocalDateTime.now().minusMinutes(5))
                    .filledAt(LocalDateTime.now()).build();
        }
        var resp = restClient.getOrderById(accountId, externalOrderId);
        var orders = mapper.toOrders(resp);
        return orders.isEmpty() ? null : orders.get(0);
    }
}
