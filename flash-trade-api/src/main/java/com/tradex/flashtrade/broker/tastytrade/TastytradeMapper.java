package com.tradex.flashtrade.broker.tastytrade;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.tradex.flashtrade.broker.dto.BrokerOrder;
import com.tradex.flashtrade.broker.dto.BrokerPortfolio;
import com.tradex.flashtrade.broker.dto.BrokerPosition;
import com.tradex.flashtrade.broker.dto.BrokerTrade;
import com.tradex.flashtrade.broker.dto.PlaceOrderResult;
import com.tradex.flashtrade.broker.tastytrade.dto.TastytradeOrder;
import com.tradex.flashtrade.broker.tastytrade.dto.TastytradeOrdersResponse;
import com.tradex.flashtrade.broker.tastytrade.dto.TastytradePosition;
import com.tradex.flashtrade.broker.tastytrade.dto.TastytradePositionsResponse;

@Component
public class TastytradeMapper {

    public BrokerPortfolio toPortfolio(TastytradePositionsResponse resp, String accountId) {
        if (resp == null || resp.getData() == null) {
            return BrokerPortfolio.builder()
                    .accountId(accountId)
                    .brokerName("TASTYTRADE")
                    .positions(Collections.emptyList())
                    .build();
        }

        List<BrokerPosition> positions = resp.getData().stream()
                .filter(Objects::nonNull)
                .map(this::toPosition)
                .collect(Collectors.toList());

        BigDecimal totalMarketValue = positions.stream()
                .map(BrokerPosition::getMarketValue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return BrokerPortfolio.builder()
                .accountId(accountId)
                .brokerName("TASTYTRADE")
                .totalMarketValue(totalMarketValue)
                .positions(positions)
                .build();
    }

    private BrokerPosition toPosition(TastytradePosition p) {
        BigDecimal qty = p.getQuantity();
        BigDecimal avg = p.getAverage_cost();
        BigDecimal last = p.getLast_price();
        BigDecimal marketValue = (qty != null && last != null) ? qty.multiply(last) : null;

        return BrokerPosition.builder()
                .symbol(p.getSymbol())
                .quantity(qty)
                .averageCost(avg)
                .currentPrice(last)
                .marketValue(marketValue)
                .unrealizedPnl((avg != null && last != null && qty != null) ? last.subtract(avg).multiply(qty) : null)
                .build();
    }

    public List<BrokerOrder> toOrders(TastytradeOrdersResponse resp) {
        if (resp == null || resp.getData() == null) return Collections.emptyList();
        return resp.getData().stream().filter(Objects::nonNull).map(this::toOrder).collect(Collectors.toList());
    }

    public PlaceOrderResult toPlaceOrderResult(TastytradeOrder o) {
        if (o == null) return PlaceOrderResult.builder().success(false).message("empty response").build();
        String status = normalizeStatus(o.getStatus());
        boolean success = "FILLED".equals(status) || "PENDING".equals(status) || "PARTIALLY_FILLED".equals(status);
        return PlaceOrderResult.builder()
            .success(success)
            .externalOrderId(o.getId())
            .message(status + (o.getStatus() != null && !o.getStatus().equalsIgnoreCase(status) ? "(" + o.getStatus() + ")" : ""))
            .build();
    }

    private BrokerOrder toOrder(TastytradeOrder o) {
        return BrokerOrder.builder()
                .externalOrderId(o.getId())
                .symbol(o.getSymbol())
                .side(o.getSide())
                .quantity(o.getQuantity())
                .price(o.getPrice())
                .status(o.getStatus())
                .createdAt(parseIso(o.getCreated_at()))
                .build();
    }

    private LocalDateTime parseIso(String iso) {
        if (iso == null || iso.isBlank()) return null;
        try {
            return java.time.OffsetDateTime.parse(iso).toLocalDateTime();
        } catch (Exception e) {
            return null;
        }
    }

    private String normalizeStatus(String tastyStatus) {
        if (tastyStatus == null) return "UNKNOWN";
        String s = tastyStatus.trim().toLowerCase();
        return switch (s) {
            case "filled", "completed" -> "FILLED";
            case "partially_filled", "partially-filled", "partially filled" -> "PARTIALLY_FILLED";
            case "cancelled", "canceled" -> "CANCELLED";
            case "submitted", "accepted", "pending" -> "PENDING";
            case "rejected", "failed" -> "REJECTED";
            default -> tastyStatus.toUpperCase();
        };
    }

    public List<BrokerTrade> toTrades(TastytradeOrdersResponse resp) {
        // Simplified: tastytrade orders mapping to trades is not implemented in detail here
        return Collections.emptyList();
    }
}
