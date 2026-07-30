package com.tradex.flashtrade.broker.schwab;

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
import com.tradex.flashtrade.broker.dto.BrokerOrder;
import com.tradex.flashtrade.broker.dto.BrokerPortfolio;
import com.tradex.flashtrade.broker.dto.BrokerPosition;
import com.tradex.flashtrade.broker.dto.BrokerTrade;
import com.tradex.flashtrade.broker.dto.MarketData;
import com.tradex.flashtrade.broker.dto.OrderRequest;
import com.tradex.flashtrade.broker.dto.PlaceOrderResult;
import com.tradex.flashtrade.broker.schwab.config.SchwabProperties;
import com.tradex.flashtrade.service.BrokerConfigService;

/**
 * 嘉信证券（Charles Schwab）{@link BrokerClient} 实现
 * <p>
 * 通过 {@link SchwabRestClient} 调用 Schwab Trader API v1，
 * 并使用 {@link SchwabMapper} 将响应数据转换为系统内部格式。
 * <br>
 * 当 {@code app.schwab.mock-enabled=true} 时，返回硬编码的 Mock 数据，
 * 无需真实 Schwab 账户即可在本地开发。
 * </p>
 */
@Service
public class SchwabBrokerClient implements BrokerClient {

    private static final Logger log = LoggerFactory.getLogger(SchwabBrokerClient.class);

    /** 与 Position.broker_name 及 BrokerService 路由 Key 对应 */
    public static final String BROKER_NAME = "SCHWAB";

    private final SchwabProperties    props;
    private final SchwabRestClient    restClient;
    private final SchwabMapper        mapper;
    private final BrokerConfigService configService;

    public SchwabBrokerClient(SchwabProperties props,
                              SchwabRestClient restClient,
                              SchwabMapper     mapper,
                              BrokerConfigService configService) {
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
            log.debug("Schwab Mock: getPortfolio accountId={}", accountId);
            return buildMockPortfolio(accountId);
        }
        log.info("Schwab: getPortfolio accountId={}", accountId);
        return mapper.toPortfolio(restClient.getPositions(accountId), accountId);
    }

    // -------------------------------------------------------------------------
    // Orders
    // -------------------------------------------------------------------------

    @Override
    public List<BrokerOrder> getOrders(String accountId) {
        if (isMockEnabled()) {
            log.debug("Schwab Mock: getOrders accountId={}", accountId);
            return buildMockOrders();
        }
        log.info("Schwab: getOrders accountId={}", accountId);
        return mapper.toOrders(restClient.getOrders(accountId));
    }

    // -------------------------------------------------------------------------
    // Trades
    // -------------------------------------------------------------------------

    @Override
    public List<BrokerTrade> getTrades(String accountId) {
        if (isMockEnabled()) {
            log.debug("Schwab Mock: getTrades accountId={}", accountId);
            return buildMockTrades();
        }
        log.info("Schwab: getTrades accountId={}", accountId);
        return mapper.toTrades(restClient.getTransactions(accountId));
    }

    // -------------------------------------------------------------------------
    // Mock 数据生成（开发 / 测试用）
    // -------------------------------------------------------------------------

    private BrokerPortfolio buildMockPortfolio(String accountId) {
        List<BrokerPosition> positions = List.of(
                BrokerPosition.builder()
                        .symbol("AAPL")
                        .quantity(BigDecimal.valueOf(100))
                        .averageCost(BigDecimal.valueOf(150.25))
                        .currentPrice(BigDecimal.valueOf(175.50))
                        .marketValue(BigDecimal.valueOf(17550.00))
                        .unrealizedPnl(BigDecimal.valueOf(2525.00))
                        .build(),
                BrokerPosition.builder()
                        .symbol("MSFT")
                        .quantity(BigDecimal.valueOf(50))
                        .averageCost(BigDecimal.valueOf(280.00))
                        .currentPrice(BigDecimal.valueOf(310.00))
                        .marketValue(BigDecimal.valueOf(15500.00))
                        .unrealizedPnl(BigDecimal.valueOf(1500.00))
                        .build()
        );
        return BrokerPortfolio.builder()
                .accountId(accountId)
                .brokerName(BROKER_NAME)
                .totalMarketValue(BigDecimal.valueOf(33050.00))
                .totalUnrealizedPnl(BigDecimal.valueOf(4025.00))
                .positions(positions)
                .build();
    }

    private List<BrokerOrder> buildMockOrders() {
        return List.of(
                BrokerOrder.builder()
                        .externalOrderId("SCHWAB-ORD-001")
                        .symbol("AAPL")
                        .side("BUY")
                        .quantity(BigDecimal.valueOf(100))
                        .price(BigDecimal.valueOf(150.25))
                        .status("FILLED")
                        .createdAt(LocalDateTime.now().minusDays(10))
                        .filledAt(LocalDateTime.now().minusDays(10))
                        .build(),
                BrokerOrder.builder()
                        .externalOrderId("SCHWAB-ORD-002")
                        .symbol("MSFT")
                        .side("BUY")
                        .quantity(BigDecimal.valueOf(50))
                        .price(BigDecimal.valueOf(280.00))
                        .status("FILLED")
                        .createdAt(LocalDateTime.now().minusDays(5))
                        .filledAt(LocalDateTime.now().minusDays(5))
                        .build()
        );
    }

    private List<BrokerTrade> buildMockTrades() {
        return List.of(
                BrokerTrade.builder()
                        .externalTradeId("SCHWAB-TRD-001")
                        .externalOrderId("SCHWAB-ORD-001")
                        .symbol("AAPL")
                        .side("BUY")
                        .quantity(BigDecimal.valueOf(100))
                        .price(BigDecimal.valueOf(150.25))
                        .commission(BigDecimal.ZERO)
                        .tradedAt(LocalDateTime.now().minusDays(10))
                        .build(),
                BrokerTrade.builder()
                        .externalTradeId("SCHWAB-TRD-002")
                        .externalOrderId("SCHWAB-ORD-002")
                        .symbol("MSFT")
                        .side("BUY")
                        .quantity(BigDecimal.valueOf(50))
                        .price(BigDecimal.valueOf(280.00))
                        .commission(BigDecimal.ZERO)
                        .tradedAt(LocalDateTime.now().minusDays(5))
                        .build()
        );
    }

    // -------------------------------------------------------------------------
    // 行情（Mock）
    // -------------------------------------------------------------------------

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
                        .symbol(symbol)
                        .lastPrice(price)
                        .bidPrice(price.subtract(java.math.BigDecimal.valueOf(0.01)))
                        .askPrice(price.add(java.math.BigDecimal.valueOf(0.01)))
                        .build());
            }
            return result;
        }
        return BrokerClient.super.getBatchMarketData(accountId, symbols);
    }

    private boolean isMockEnabled() {
        return configService.isMockEnabled(BROKER_NAME, props.isMockEnabled());
    }

    // -------------------------------------------------------------------------
    // 交易操作（Mock）
    // -------------------------------------------------------------------------

    @Override
    public PlaceOrderResult placeOrder(String accountId, OrderRequest request) {
        if (isMockEnabled()) {
            String externalId = "SCHWAB-ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            log.info("Schwab Mock: placeOrder {} {} {} @ {} -> {}",
                    request.getSide(), request.getQuantity(), request.getSymbol(),
                    request.getLimitPrice(), externalId);
            return PlaceOrderResult.builder()
                    .success(true)
                    .externalOrderId(externalId)
                    .message("Mock 下单成功")
                    .build();
        }
        return BrokerClient.super.placeOrder(accountId, request);
    }

    @Override
    public void cancelOrder(String accountId, String externalOrderId) {
        if (isMockEnabled()) {
            log.info("Schwab Mock: cancelOrder accountId={} orderId={}", accountId, externalOrderId);
            return;
        }
        BrokerClient.super.cancelOrder(accountId, externalOrderId);
    }

    @Override
    public BrokerOrder getOrderStatus(String accountId, String externalOrderId) {
        if (isMockEnabled()) {
            return BrokerOrder.builder()
                    .externalOrderId(externalOrderId)
                    .symbol("MOCK")
                    .side("BUY")
                    .quantity(BigDecimal.valueOf(100))
                    .price(BigDecimal.valueOf(150.00))
                    .status("FILLED")
                    .createdAt(LocalDateTime.now().minusMinutes(5))
                    .filledAt(LocalDateTime.now())
                    .build();
        }
        return BrokerClient.super.getOrderStatus(accountId, externalOrderId);
    }
}
