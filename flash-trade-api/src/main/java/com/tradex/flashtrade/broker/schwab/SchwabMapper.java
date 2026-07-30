package com.tradex.flashtrade.broker.schwab;

import com.tradex.flashtrade.broker.dto.BrokerOrder;
import com.tradex.flashtrade.broker.dto.BrokerPortfolio;
import com.tradex.flashtrade.broker.dto.BrokerPosition;
import com.tradex.flashtrade.broker.dto.BrokerTrade;
import com.tradex.flashtrade.broker.schwab.dto.SchwabAccountPositions;
import com.tradex.flashtrade.broker.schwab.dto.SchwabOrderLeg;
import com.tradex.flashtrade.broker.schwab.dto.SchwabOrderResponse;
import com.tradex.flashtrade.broker.schwab.dto.SchwabPositionItem;
import com.tradex.flashtrade.broker.schwab.dto.SchwabTransaction;
import com.tradex.flashtrade.broker.schwab.dto.SchwabTransferItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * 嘉信证券数据映射器
 * <p>
 * 将 Schwab API 响应 DTO 转换为系统内部统一的 {@link BrokerPortfolio}、
 * {@link BrokerOrder}、{@link BrokerTrade} 格式。
 * </p>
 */
@Component
public class SchwabMapper {

    private static final Logger log = LoggerFactory.getLogger(SchwabMapper.class);

    private static final String BROKER_NAME = "SCHWAB";

    // -------------------------------------------------------------------------
    // Portfolio（持仓汇总）
    // -------------------------------------------------------------------------

    /**
     * 将 Schwab 持仓响应列表映射为内部 {@link BrokerPortfolio}。
     *
     * @param accountPositionsList Schwab API 返回的持仓列表
     * @param accountId            账户 ID
     * @return 内部持仓汇总对象
     */
    public BrokerPortfolio toPortfolio(List<SchwabAccountPositions> accountPositionsList,
                                       String accountId) {
        if (accountPositionsList == null || accountPositionsList.isEmpty()) {
            return BrokerPortfolio.builder()
                    .accountId(accountId)
                    .brokerName(BROKER_NAME)
                    .totalMarketValue(BigDecimal.ZERO)
                    .totalUnrealizedPnl(BigDecimal.ZERO)
                    .positions(Collections.emptyList())
                    .build();
        }

        // Schwab 返回的列表中，第一个元素即对应该 accountId 的持仓
        SchwabAccountPositions.SecuritiesAccount account =
                accountPositionsList.get(0).getSecuritiesAccount();

        List<SchwabPositionItem> rawPositions =
                account != null && account.getPositions() != null
                        ? account.getPositions()
                        : Collections.emptyList();

        List<BrokerPosition> positions = rawPositions.stream()
                .filter(p -> p.getInstrument() != null)
                .map(this::toPosition)
                .collect(Collectors.toList());

        BigDecimal totalMarketValue = positions.stream()
                .map(BrokerPosition::getMarketValue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalUnrealizedPnl = positions.stream()
                .map(BrokerPosition::getUnrealizedPnl)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return BrokerPortfolio.builder()
                .accountId(accountId)
                .brokerName(BROKER_NAME)
                .totalMarketValue(totalMarketValue)
                .totalUnrealizedPnl(totalUnrealizedPnl)
                .positions(positions)
                .build();
    }

    private BrokerPosition toPosition(SchwabPositionItem item) {
        // 多头为正数量，空头为负数量
        double longQty  = item.getLongQuantity()  != null ? item.getLongQuantity()  : 0.0;
        double shortQty = item.getShortQuantity() != null ? item.getShortQuantity() : 0.0;
        double netQty   = longQty - shortQty;

        double unrealizedPnl = item.getLongOpenProfitLoss() != null
                ? item.getLongOpenProfitLoss() : 0.0;

        return BrokerPosition.builder()
                .symbol(item.getInstrument().getSymbol())
                .quantity(BigDecimal.valueOf(netQty))
                .averageCost(toBd(item.getAveragePrice()))
                .currentPrice(item.getInstrument().getNetChange() != null
                        ? toBd(item.getInstrument().getNetChange()) : null)
                .marketValue(toBd(item.getMarketValue()))
                .unrealizedPnl(BigDecimal.valueOf(unrealizedPnl))
                .build();
    }

    // -------------------------------------------------------------------------
    // Orders（委托列表）
    // -------------------------------------------------------------------------

    /**
     * 将 Schwab 订单列表映射为内部 {@link BrokerOrder} 列表。
     */
    public List<BrokerOrder> toOrders(List<SchwabOrderResponse> schwabOrders) {
        if (schwabOrders == null) return Collections.emptyList();
        return schwabOrders.stream()
                .map(this::toOrder)
                .collect(Collectors.toList());
    }

    private BrokerOrder toOrder(SchwabOrderResponse src) {
        // 从第一条 orderLeg 中提取 symbol 和委托方向
        String symbol    = null;
        String side      = null;
        if (src.getOrderLegCollection() != null && !src.getOrderLegCollection().isEmpty()) {
            SchwabOrderLeg leg = src.getOrderLegCollection().get(0);
            side = normalizeInstruction(leg.getInstruction());
            if (leg.getInstrument() != null) {
                symbol = leg.getInstrument().getSymbol();
            }
        }

        return BrokerOrder.builder()
                .externalOrderId(src.getOrderId() != null
                        ? String.valueOf(src.getOrderId()) : null)
                .symbol(symbol)
                .side(side)
                .quantity(toBd(src.getQuantity()))
                .price(toBd(src.getPrice()))
                .status(normalizeOrderStatus(src.getStatus()))
                .createdAt(parseDateTime(src.getEnteredTime()))
                .filledAt(parseDateTime(src.getCloseTime()))
                .build();
    }

    // -------------------------------------------------------------------------
    // Trades（成交记录）
    // -------------------------------------------------------------------------

    /**
     * 将 Schwab 成交记录列表映射为内部 {@link BrokerTrade} 列表。
     */
    public List<BrokerTrade> toTrades(List<SchwabTransaction> transactions) {
        if (transactions == null) return Collections.emptyList();
        return transactions.stream()
                .filter(t -> "TRADE".equalsIgnoreCase(t.getType()))
                .map(this::toTrade)
                .collect(Collectors.toList());
    }

    private BrokerTrade toTrade(SchwabTransaction src) {
        // 从 transferItems 中提取股票资产的成交信息（跳过手续费等 feeType）
        String symbol   = null;
        String side     = null;
        Double quantity = null;
        Double price    = null;
        Double commission = 0.0;

        if (src.getTransferItems() != null) {
            for (SchwabTransferItem item : src.getTransferItems()) {
                if (item.getFeeType() != null) {
                    // 手续费项目
                    if (item.getCost() != null) commission += Math.abs(item.getCost());
                    continue;
                }
                if (item.getInstrument() != null && item.getInstrument().getSymbol() != null) {
                    symbol   = item.getInstrument().getSymbol();
                    quantity = item.getAmount() != null ? Math.abs(item.getAmount()) : null;
                    price    = item.getPrice();
                    // amount 正=买入，负=卖出
                    side = (item.getAmount() != null && item.getAmount() > 0) ? "BUY" : "SELL";
                }
            }
        }

        return BrokerTrade.builder()
                .externalTradeId(src.getActivityId() != null
                        ? String.valueOf(src.getActivityId()) : null)
                .externalOrderId(src.getOrderId() != null
                        ? String.valueOf(src.getOrderId()) : null)
                .symbol(symbol)
                .side(side)
                .quantity(toBd(quantity))
                .price(toBd(price))
                .commission(BigDecimal.valueOf(commission))
                .tradedAt(parseDateTime(src.getTradeDate()))
                .build();
    }

    // -------------------------------------------------------------------------
    // 内部辅助方法
    // -------------------------------------------------------------------------

    /** Schwab instruction → BUY/SELL 正規化 */
    private String normalizeInstruction(String instruction) {
        if (instruction == null) return null;
        return switch (instruction.toUpperCase()) {
            case "BUY", "BUY_TO_COVER" -> "BUY";
            case "SELL", "SELL_SHORT"   -> "SELL";
            default -> instruction;
        };
    }

    /** Schwab status → 内部 OrderStatus 正规化 */
    private String normalizeOrderStatus(String status) {
        if (status == null) return null;
        return switch (status.toUpperCase()) {
            case "FILLED"              -> "FILLED";
            case "CANCELED", "EXPIRED" -> "CANCELLED";
            case "REJECTED"            -> "REJECTED";
            case "WORKING", "QUEUED",
                 "ACCEPTED"            -> "PENDING";
            default                    -> status;
        };
    }

    /** Double → BigDecimal（null 安全） */
    private BigDecimal toBd(Double value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }

    /** ISO-8601 字符串 → LocalDateTime（解析失败则返回 null） */
    private LocalDateTime parseDateTime(String isoString) {
        if (isoString == null || isoString.isBlank()) return null;
        try {
            return OffsetDateTime.parse(isoString).toLocalDateTime();
        } catch (DateTimeParseException ex) {
            log.debug("日期解析失败 value='{}': {}", isoString, ex.getMessage());
            return null;
        }
    }
}
