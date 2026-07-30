package com.tradex.flashtrade.broker.etrade;

import com.tradex.flashtrade.broker.dto.BrokerOrder;
import com.tradex.flashtrade.broker.dto.BrokerPortfolio;
import com.tradex.flashtrade.broker.dto.BrokerPosition;
import com.tradex.flashtrade.broker.dto.BrokerTrade;
import com.tradex.flashtrade.broker.etrade.dto.EtradeOrder;
import com.tradex.flashtrade.broker.etrade.dto.EtradeOrdersResponse;
import com.tradex.flashtrade.broker.etrade.dto.EtradePortfolioResponse;
import com.tradex.flashtrade.broker.etrade.dto.EtradePortfolioResponse.AccountPortfolio;
import com.tradex.flashtrade.broker.etrade.dto.EtradePosition;
import com.tradex.flashtrade.broker.etrade.dto.EtradeTransactionResponse;
import com.tradex.flashtrade.broker.etrade.dto.EtradeTransactionResponse.EtradeTransaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * E*TRADE DTO → 内部 Broker DTO 类型转换组件。
 */
@Component
public class EtradeMapper {

    /** 计入成交统计的 E*TRADE 交易类型 */
    private static final Set<String> TRADE_TYPES = Set.of("BOUGHT", "SOLD");

    // -------------------------------------------------------------------------
    // Portfolio
    // -------------------------------------------------------------------------

    public BrokerPortfolio toPortfolio(EtradePortfolioResponse response) {
        if (response == null
                || response.getPortfolioResponse() == null
                || response.getPortfolioResponse().getAccountPortfolio() == null) {
            return BrokerPortfolio.builder().positions(Collections.emptyList()).build();
        }

        List<BrokerPosition> positions = response.getPortfolioResponse()
                .getAccountPortfolio().stream()
                .filter(Objects::nonNull)
                .flatMap(ap -> safePositions(ap).stream())
                .map(this::toPosition)
                .collect(Collectors.toList());

        BigDecimal totalMarketValue = positions.stream()
                .map(BrokerPosition::getMarketValue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return BrokerPortfolio.builder()
                .totalMarketValue(totalMarketValue)
                .positions(positions)
                .build();
    }

    private List<EtradePosition> safePositions(AccountPortfolio ap) {
        return ap.getPositions() == null ? Collections.emptyList() : ap.getPositions();
    }

    private BrokerPosition toPosition(EtradePosition pos) {
        String symbol = (pos.getProduct() != null) ? pos.getProduct().getSymbol() : null;
        return BrokerPosition.builder()
                .symbol(symbol)
                .quantity(pos.getQuantity() != null ? BigDecimal.valueOf(pos.getQuantity()) : null)
                .averageCost(pos.getCostPerShare() != null ? BigDecimal.valueOf(pos.getCostPerShare()) : null)
                .currentPrice(pos.getCurrentPrice() != null ? BigDecimal.valueOf(pos.getCurrentPrice()) : null)
                .marketValue(pos.getMarketValue() != null ? BigDecimal.valueOf(pos.getMarketValue()) : null)
                .unrealizedPnl(pos.getTotalGain() != null ? BigDecimal.valueOf(pos.getTotalGain()) : null)
                .build();
    }

    // -------------------------------------------------------------------------
    // Orders
    // -------------------------------------------------------------------------

    public List<BrokerOrder> toOrders(EtradeOrdersResponse response) {
        if (response == null
                || response.getOrdersResponse() == null
                || response.getOrdersResponse().getOrders() == null) {
            return Collections.emptyList();
        }

        return response.getOrdersResponse().getOrders().stream()
                .filter(Objects::nonNull)
                .map(this::toOrder)
                .collect(Collectors.toList());
    }

    private BrokerOrder toOrder(EtradeOrder order) {
        return BrokerOrder.builder()
                .externalOrderId(order.getOrderId() != null ? String.valueOf(order.getOrderId()) : null)
                .symbol(extractSymbol(order))
                .side(normalizeAction(order.getOrderAction()))
                .quantity(order.getOrderedQuantity() != null ? BigDecimal.valueOf(order.getOrderedQuantity()) : null)
                .price(order.getLimitPrice() != null ? BigDecimal.valueOf(order.getLimitPrice()) : null)
                .status(order.getStatus())
                .createdAt(epochMillisToUtc(order.getPlacedTime()))
                .filledAt(epochMillisToUtc(order.getExecutedTime()))
                .build();
    }

    private String extractSymbol(EtradeOrder order) {
        if (order.getOrderDetails() == null || order.getOrderDetails().isEmpty()) {
            return null;
        }
        var detail = order.getOrderDetails().get(0);
        return (detail.getProduct() != null) ? detail.getProduct().getSymbol() : null;
    }

    /**
     * 将 E*TRADE 特有动作标志转换为内部表示。
     * BUY_TO_COVER → BUY, SELL_SHORT → SELL
     */
    private String normalizeAction(String action) {
        if (action == null) return null;
        return switch (action) {
            case "BUY_TO_COVER" -> "BUY";
            case "SELL_SHORT"   -> "SELL";
            default             -> action;
        };
    }

    // -------------------------------------------------------------------------
    // Trades
    // -------------------------------------------------------------------------

    public List<BrokerTrade> toTrades(EtradeTransactionResponse response) {
        if (response == null
                || response.getTransactionListResponse() == null
                || response.getTransactionListResponse().getTransactions() == null) {
            return Collections.emptyList();
        }

        return response.getTransactionListResponse().getTransactions().stream()
                .filter(Objects::nonNull)
                .filter(tx -> TRADE_TYPES.contains(tx.getTransactionType()))
                .map(this::toTrade)
                .collect(Collectors.toList());
    }

    private BrokerTrade toTrade(EtradeTransaction tx) {
        String symbol  = null;
        Double qty     = null;
        Double price   = null;

        if (tx.getBrokerage() != null) {
            var info = tx.getBrokerage();
            symbol = (info.getProduct() != null) ? info.getProduct().getSymbol() : null;
            qty    = info.getQuantity();
            price  = info.getPrice();
        }

        String side = "BOUGHT".equals(tx.getTransactionType()) ? "BUY" : "SELL";

        return BrokerTrade.builder()
                .externalTradeId(tx.getTransactionId() != null ? String.valueOf(tx.getTransactionId()) : null)
                .symbol(symbol)
                .side(side)
                .quantity(qty != null ? BigDecimal.valueOf(qty) : null)
                .price(price != null ? BigDecimal.valueOf(price) : null)
                .tradedAt(epochMillisToUtc(tx.getTransactionDate()))
                .build();
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    private LocalDateTime epochMillisToUtc(Long epochMillis) {
        if (epochMillis == null) return null;
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC);
    }
}
