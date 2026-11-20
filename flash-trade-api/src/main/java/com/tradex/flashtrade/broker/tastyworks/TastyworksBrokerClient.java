package com.tradex.flashtrade.broker.tastyworks;

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
import com.tradex.flashtrade.broker.tastyworks.config.TastyworksProperties;
import com.tradex.flashtrade.service.BrokerConfigService;

/**
 * Tastyworks 券商客户端实现
 * <p>
 * Mock 模式返回固定演示数据；Real 模式通过 {@link TastyworksRestClient} 调用
 * Tastyworks REST API（Session Token 认证）。
 * </p>
 */
@Service
public class TastyworksBrokerClient implements BrokerClient {

    private static final Logger log = LoggerFactory.getLogger(TastyworksBrokerClient.class);

    /** 与 Position.broker_name 及 BrokerService 路由 Key 对应 */
    public static final String BROKER_NAME = "TASTYWORKS";

    private final TastyworksProperties  props;
    private final TastyworksRestClient  restClient;
    private final TastyworksMapper      mapper;
    private final BrokerConfigService   configService;

    public TastyworksBrokerClient(TastyworksProperties props,
                                  TastyworksRestClient restClient,
                                  TastyworksMapper     mapper,
                                  BrokerConfigService  configService) {
        this.props      = props;
        this.restClient = restClient;
        this.mapper     = mapper;
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
            log.debug("Tastyworks Mock: getPortfolio accountId={}", accountId);
            return buildMockPortfolio(accountId);
        }
        log.info("Tastyworks: getPortfolio accountId={}", accountId);
        return mapper.toPortfolio(restClient.getPositions(accountId), accountId);
    }

    // -------------------------------------------------------------------------
    // Orders
    // -------------------------------------------------------------------------

    @Override
    public List<BrokerOrder> getOrders(String accountId) {
        if (isMockEnabled()) {
            log.debug("Tastyworks Mock: getOrders accountId={}", accountId);
            return buildMockOrders();
        }
        log.info("Tastyworks: getOrders accountId={}", accountId);
        return mapper.toOrders(restClient.getOrders(accountId));
    }

    // -------------------------------------------------------------------------
    // Trades
    // -------------------------------------------------------------------------

    @Override
    public List<BrokerTrade> getTrades(String accountId) {
        if (isMockEnabled()) {
            log.debug("Tastyworks Mock: getTrades accountId={}", accountId);
            return buildMockTrades();
        }
        log.info("Tastyworks: getTrades accountId={}", accountId);
        return mapper.toTrades(restClient.getOrders(accountId));
    }

    // -------------------------------------------------------------------------
    // Mock 数据生成（开发 / 测试用）
    // -------------------------------------------------------------------------

    private BrokerPortfolio buildMockPortfolio(String accountId) {
        List<BrokerPosition> positions = List.of(
                BrokerPosition.builder()
                        .symbol("SPY")
                        .quantity(BigDecimal.valueOf(50))
                        .averageCost(BigDecimal.valueOf(420.00))
                        .currentPrice(BigDecimal.valueOf(445.00))
                        .marketValue(BigDecimal.valueOf(22250.00))
                        .unrealizedPnl(BigDecimal.valueOf(1250.00))
                        .build(),
                BrokerPosition.builder()
                        .symbol("QQQ")
                        .quantity(BigDecimal.valueOf(40))
                        .averageCost(BigDecimal.valueOf(350.00))
                        .currentPrice(BigDecimal.valueOf(368.00))
                        .marketValue(BigDecimal.valueOf(14720.00))
                        .unrealizedPnl(BigDecimal.valueOf(720.00))
                        .build()
        );
        return BrokerPortfolio.builder()
                .accountId(accountId)
                .brokerName(BROKER_NAME)
                .totalMarketValue(BigDecimal.valueOf(36970.00))
                .totalUnrealizedPnl(BigDecimal.valueOf(1970.00))
                .positions(positions)
                .build();
    }

    private List<BrokerOrder> buildMockOrders() {
        return List.of(
                BrokerOrder.builder()
                        .externalOrderId("TW-ORD-001")
                        .symbol("SPY")
                        .side("BUY")
                        .quantity(BigDecimal.valueOf(50))
                        .price(BigDecimal.valueOf(420.00))
                        .status("FILLED")
                        .createdAt(LocalDateTime.now().minusDays(20))
                        .filledAt(LocalDateTime.now().minusDays(20))
                        .build(),
                BrokerOrder.builder()
                        .externalOrderId("TW-ORD-002")
                        .symbol("QQQ")
                        .side("BUY")
                        .quantity(BigDecimal.valueOf(40))
                        .price(BigDecimal.valueOf(350.00))
                        .status("FILLED")
                        .createdAt(LocalDateTime.now().minusDays(10))
                        .filledAt(LocalDateTime.now().minusDays(10))
                        .build()
        );
    }

    private List<BrokerTrade> buildMockTrades() {
        return List.of(
                BrokerTrade.builder()
                        .externalTradeId("TW-TRD-001")
                        .externalOrderId("TW-ORD-001")
                        .symbol("SPY")
                        .side("BUY")
                        .quantity(BigDecimal.valueOf(50))
                        .price(BigDecimal.valueOf(420.00))
                        .commission(BigDecimal.ZERO)
                        .tradedAt(LocalDateTime.now().minusDays(20))
                        .build(),
                BrokerTrade.builder()
                        .externalTradeId("TW-TRD-002")
                        .externalOrderId("TW-ORD-002")
                        .symbol("QQQ")
                        .side("BUY")
                        .quantity(BigDecimal.valueOf(40))
                        .price(BigDecimal.valueOf(350.00))
                        .commission(BigDecimal.ZERO)
                        .tradedAt(LocalDateTime.now().minusDays(10))
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
            String externalId = "TW-ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            log.info("Tastyworks Mock: placeOrder {} {} {} -> {}",
                    request.getSide(), request.getQuantity(), request.getSymbol(), externalId);
            return PlaceOrderResult.builder().success(true)
                    .externalOrderId(externalId).message("Mock 下单成功").build();
        }
        return BrokerClient.super.placeOrder(accountId, request);
    }

    @Override
    public void cancelOrder(String accountId, String externalOrderId) {
        if (isMockEnabled()) {
            log.info("Tastyworks Mock: cancelOrder {}", externalOrderId);
            return;
        }
        BrokerClient.super.cancelOrder(accountId, externalOrderId);
    }

    @Override
    public BrokerOrder getOrderStatus(String accountId, String externalOrderId) {
        if (isMockEnabled()) {
            return BrokerOrder.builder().externalOrderId(externalOrderId)
                    .symbol("MOCK").side("BUY").quantity(BigDecimal.valueOf(50))
                    .price(BigDecimal.valueOf(420.00)).status("FILLED")
                    .createdAt(LocalDateTime.now().minusMinutes(5))
                    .filledAt(LocalDateTime.now()).build();
        }
        return BrokerClient.super.getOrderStatus(accountId, externalOrderId);
    }
}
