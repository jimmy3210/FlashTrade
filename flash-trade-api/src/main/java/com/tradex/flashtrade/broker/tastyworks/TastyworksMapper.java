package com.tradex.flashtrade.broker.tastyworks;

import com.tradex.flashtrade.broker.dto.BrokerOrder;
import com.tradex.flashtrade.broker.dto.BrokerPortfolio;
import com.tradex.flashtrade.broker.dto.BrokerPosition;
import com.tradex.flashtrade.broker.dto.BrokerTrade;
import com.tradex.flashtrade.broker.tastyworks.dto.TastyworksFill;
import com.tradex.flashtrade.broker.tastyworks.dto.TastyworksOrder;
import com.tradex.flashtrade.broker.tastyworks.dto.TastyworksOrderLeg;
import com.tradex.flashtrade.broker.tastyworks.dto.TastyworksOrdersResponse;
import com.tradex.flashtrade.broker.tastyworks.dto.TastyworksPosition;
import com.tradex.flashtrade.broker.tastyworks.dto.TastyworksPositionsResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Tastyworks DTO → 内部 Broker DTO 类型转换组件。
 */
@Component
public class TastyworksMapper {

    // -------------------------------------------------------------------------
    // Portfolio
    // -------------------------------------------------------------------------

    public BrokerPortfolio toPortfolio(TastyworksPositionsResponse response, String accountId) {
        if (response == null
                || response.getData() == null
                || response.getData().getItems() == null) {
            return BrokerPortfolio.builder()
                    .accountId(accountId)
                    .brokerName("TASTYWORKS")
                    .positions(Collections.emptyList())
                    .build();
        }

        List<BrokerPosition> positions = response.getData().getItems().stream()
                .filter(Objects::nonNull)
                .map(this::toPosition)
                .collect(Collectors.toList());

        BigDecimal totalMarketValue = positions.stream()
                .map(BrokerPosition::getMarketValue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return BrokerPortfolio.builder()
                .accountId(accountId)
                .brokerName("TASTYWORKS")
                .totalMarketValue(totalMarketValue)
                .positions(positions)
                .build();
    }

    private BrokerPosition toPosition(TastyworksPosition pos) {
        BigDecimal qty          = parseBigDecimal(pos.getQuantity());
        BigDecimal avgCost      = parseBigDecimal(pos.getAverageOpenPrice());
        BigDecimal closePrice   = parseBigDecimal(pos.getClosePrice());

        // 若持仓方向为 Short 则取反
        if ("Short".equalsIgnoreCase(pos.getQuantityDirection()) && qty != null) {
            qty = qty.negate();
        }

        BigDecimal marketValue = (qty != null && closePrice != null)
                ? qty.multiply(closePrice) : null;

        BigDecimal unrealizedPnl = (qty != null && avgCost != null && closePrice != null)
                ? closePrice.subtract(avgCost).multiply(qty) : null;

        return BrokerPosition.builder()
                .symbol(pos.getSymbol())
                .quantity(qty)
                .averageCost(avgCost)
                .currentPrice(closePrice)
                .marketValue(marketValue)
                .unrealizedPnl(unrealizedPnl)
                .build();
    }

    // -------------------------------------------------------------------------
    // Orders
    // -------------------------------------------------------------------------

    public List<BrokerOrder> toOrders(TastyworksOrdersResponse response) {
        if (response == null
                || response.getData() == null
                || response.getData().getItems() == null) {
            return Collections.emptyList();
        }

        return response.getData().getItems().stream()
                .filter(Objects::nonNull)
                .map(this::toOrder)
                .collect(Collectors.toList());
    }

    private BrokerOrder toOrder(TastyworksOrder order) {
        String symbol = order.getUnderlyingSymbol();
        String side   = extractSideFromLegs(order.getLegs());

        LocalDateTime createdAt = parseIso(order.getReceivedAt());

        return BrokerOrder.builder()
                .externalOrderId(order.getId() != null ? String.valueOf(order.getId()) : null)
                .symbol(symbol)
                .side(side)
                .quantity(parseBigDecimal(order.getSize()))
                .price(parseBigDecimal(order.getPrice()))
                .status(normalizeStatus(order.getStatus()))
                .createdAt(createdAt)
                .build();
    }

    /**
     * 从 legs[0].action 推断买卖方向。
     * Tastyworks action: "Buy to Open" / "Sell to Close" / "Sell to Open" / "Buy to Close"
     */
    private String extractSideFromLegs(List<TastyworksOrderLeg> legs) {
        if (legs == null || legs.isEmpty()) return null;
        String action = legs.get(0).getAction();
        if (action == null) return null;
        String lower = action.toLowerCase();
        return lower.startsWith("buy") ? "BUY" : "SELL";
    }

    private String normalizeStatus(String status) {
        if (status == null) return null;
        return switch (status) {
            case "Received"   -> "PENDING";
            case "Live"       -> "PENDING";
            case "Filled"     -> "FILLED";
            case "Cancelled"  -> "CANCELLED";
            case "Rejected"   -> "REJECTED";
            default           -> status.toUpperCase();
        };
    }

    // -------------------------------------------------------------------------
    // Trades（从 Order.legs[].fills[] 提取成交记录）
    // -------------------------------------------------------------------------

    public List<BrokerTrade> toTrades(TastyworksOrdersResponse response) {
        if (response == null
                || response.getData() == null
                || response.getData().getItems() == null) {
            return Collections.emptyList();
        }

        return response.getData().getItems().stream()
                .filter(Objects::nonNull)
                .filter(o -> "Filled".equals(o.getStatus()))
                .flatMap(o -> extractTrades(o).stream())
                .collect(Collectors.toList());
    }

    private List<BrokerTrade> extractTrades(TastyworksOrder order) {
        if (order.getLegs() == null) return Collections.emptyList();

        return order.getLegs().stream()
                .filter(Objects::nonNull)
                .filter(leg -> leg.getFills() != null)
                .flatMap(leg -> leg.getFills().stream()
                        .filter(Objects::nonNull)
                        .map(fill -> toTrade(order, leg, fill)))
                .collect(Collectors.toList());
    }

    private BrokerTrade toTrade(TastyworksOrder order, TastyworksOrderLeg leg, TastyworksFill fill) {
        String side = extractSideFromLegs(order.getLegs());

        return BrokerTrade.builder()
                .externalTradeId(fill.getFillId())
                .externalOrderId(order.getId() != null ? String.valueOf(order.getId()) : null)
                .symbol(leg.getSymbol() != null ? leg.getSymbol() : order.getUnderlyingSymbol())
                .side(side)
                .quantity(parseBigDecimal(fill.getQuantity()))
                .price(parseBigDecimal(fill.getFillPrice()))
                .tradedAt(parseIso(fill.getFilledAt()))
                .build();
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDateTime parseIso(String isoString) {
        if (isoString == null || isoString.isBlank()) return null;
        try {
            return OffsetDateTime.parse(isoString).toLocalDateTime();
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
