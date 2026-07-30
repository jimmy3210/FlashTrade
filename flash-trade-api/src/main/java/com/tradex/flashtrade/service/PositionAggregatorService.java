package com.tradex.flashtrade.service;

import com.tradex.flashtrade.dto.portfolio.PositionView;
import com.tradex.flashtrade.entity.Order;
import com.tradex.flashtrade.entity.OrderSide;
import com.tradex.flashtrade.entity.OrderStatus;
import com.tradex.flashtrade.entity.Trade;
import com.tradex.flashtrade.repository.OrderRepository;
import com.tradex.flashtrade.repository.PositionRepository;
import com.tradex.flashtrade.repository.TradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 资产视图聚合引擎（PositionAggregator）
 * <p>
 * 根据 trades + orders + 实时行情，计算每个 (accountId, symbol) 的聚合资产视图。
 * 计算结果为纯内存计算，不持久化到数据库。
 *
 * <h3>计算规则</h3>
 * <ul>
 *   <li>netQuantity = Σ BUY成交量 - Σ SELL成交量</li>
 *   <li>avgCost = Σ(BUY价×BUY量) / Σ BUY量（全量加权平均）</li>
 *   <li>realizedPnl = Σ(SELL价×SELL量) - Σ SELL量 × avgCost</li>
 *   <li>unrealizedPnl = (lastPrice - avgCost) × netQuantity</li>
 *   <li>totalQuantity = Σ 所有成交量（BUY + SELL，用于手续费统计）</li>
 *   <li>pendingBuy / pendingSell 来自 OPEN 状态委托</li>
 *   <li>availableQuantity = max(0, netQuantity - pendingSell)</li>
 *   <li>隔夜持仓的浮动盈亏按当前市价实时重算，不累加历史</li>
 * </ul>
 */
@Service
public class PositionAggregatorService {

    private static final Logger log = LoggerFactory.getLogger(PositionAggregatorService.class);

    private static final List<OrderStatus> OPEN_STATUSES =
            List.of(OrderStatus.PENDING, OrderStatus.PARTIALLY_FILLED);

    private final TradeRepository    tradeRepository;
    private final OrderRepository    orderRepository;
    private final PositionRepository positionRepository;
    private final MarketDataService  marketDataService;

    public PositionAggregatorService(TradeRepository tradeRepository,
                                     OrderRepository orderRepository,
                                     PositionRepository positionRepository,
                                     MarketDataService marketDataService) {
        this.tradeRepository    = tradeRepository;
        this.orderRepository    = orderRepository;
        this.positionRepository = positionRepository;
        this.marketDataService  = marketDataService;
    }

    /**
     * 计算指定用户所有账户的资产视图。
     *
     * @param userId 用户 ID
     * @return symbol 维度的资产视图列表（已按 symbol 排序）
     */
    @Transactional(readOnly = true)
    public List<PositionView> aggregate(Long userId) {
        List<Trade> trades = tradeRepository.findByUserIdOrderByTradedAtDesc(userId);
        return buildViews(userId, null, trades);
    }

    /**
     * 计算指定用户、指定账户的资产视图。
     *
     * @param userId    用户 ID
     * @param accountId 账户 ID
     * @return symbol 维度的资产视图列表
     */
    @Transactional(readOnly = true)
    public List<PositionView> aggregateByAccount(Long userId, String accountId) {
        List<Trade> trades = tradeRepository.findByUserIdAndAccountIdOrderByTradedAtAsc(userId, accountId);
        return buildViews(userId, accountId, trades);
    }

    // -------------------------------------------------------------------------
    // 内部聚合逻辑
    // -------------------------------------------------------------------------

    private List<PositionView> buildViews(Long userId, String filterAccountId, List<Trade> trades) {
        if (trades.isEmpty()) return List.of();

        // 按 (accountId, symbol) 分组
        Map<String, List<Trade>> grouped = trades.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getAccountId() + "|" + t.getSymbol(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<PositionView> result = new ArrayList<>();

        for (Map.Entry<String, List<Trade>> entry : grouped.entrySet()) {
            String   key       = entry.getKey();
            String[] parts     = key.split("\\|", 2);
            String   accountId = parts[0];
            String   symbol    = parts[1];

            List<Trade> symbolTrades = entry.getValue();

            // 计算基础聚合数据
            BigDecimal totalBuyAmount  = BigDecimal.ZERO;
            BigDecimal totalBuyQty     = BigDecimal.ZERO;
            BigDecimal totalSellAmount = BigDecimal.ZERO;
            BigDecimal totalSellQty    = BigDecimal.ZERO;
            int        totalQuantity   = 0;

            for (Trade t : symbolTrades) {
                BigDecimal qty = t.getQuantity().abs();
                totalQuantity += qty.intValue();

                if (t.getSide() == OrderSide.BUY) {
                    totalBuyQty    = totalBuyQty.add(qty);
                    totalBuyAmount = totalBuyAmount.add(qty.multiply(t.getPrice()));
                } else {
                    totalSellQty    = totalSellQty.add(qty);
                    totalSellAmount = totalSellAmount.add(qty.multiply(t.getPrice()));
                }
            }

            // avgCost = Σ(BUY价×BUY量) / Σ BUY量
            BigDecimal avgCost = totalBuyQty.compareTo(BigDecimal.ZERO) > 0
                    ? totalBuyAmount.divide(totalBuyQty, 4, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            // netQuantity = BUY量 - SELL量
            int netQuantity = totalBuyQty.subtract(totalSellQty).intValue();

            // realizedPnl = Σ SELL成交额 - Σ SELL量 × avgCost
            BigDecimal realizedPnl = totalSellAmount.subtract(
                    totalSellQty.multiply(avgCost));

            // 获取未成交委托
            int pendingBuy  = 0;
            int pendingSell = 0;
            try {
                List<Order> openOrders = orderRepository
                        .findByUserIdAndAccountIdAndStatusInOrderByCreatedAtDesc(
                                userId, accountId, OPEN_STATUSES);
                for (Order o : openOrders) {
                    if (!symbol.equals(o.getSymbol())) continue;
                    int qty = o.getQuantity().intValue();
                    if (o.getSide() == OrderSide.BUY)  pendingBuy  += qty;
                    else                               pendingSell += qty;
                }
            } catch (Exception e) {
                log.warn("查询未成交委托失败 userId={} accountId={} symbol={}: {}",
                         userId, accountId, symbol, e.getMessage());
            }

            int availableQuantity = Math.max(0, netQuantity - pendingSell);

            // 获取最新市场价：优先 Redis 行情，fallback 到 positions 表 currentPrice
            BigDecimal lastPrice = marketDataService.getLastPrice(symbol);
            if (lastPrice == null || lastPrice.compareTo(BigDecimal.ZERO) == 0) {
                lastPrice = positionRepository
                        .findByUserIdAndAccountIdAndSymbolOrderBySymbolAsc(userId, accountId, symbol)
                        .stream()
                        .findFirst()
                        .map(p -> p.getCurrentPrice())
                        .filter(p -> p != null && p.compareTo(BigDecimal.ZERO) > 0)
                        .orElse(avgCost);  // 最终 fallback：用成本价
            }

            // unrealizedPnl = (lastPrice - avgCost) × netQuantity（不累加历史）
            BigDecimal unrealizedPnl = BigDecimal.ZERO;
            if (netQuantity > 0 && lastPrice.compareTo(BigDecimal.ZERO) > 0) {
                unrealizedPnl = lastPrice.subtract(avgCost)
                        .multiply(BigDecimal.valueOf(netQuantity));
            }

            BigDecimal totalPnl   = realizedPnl.add(unrealizedPnl);
            BigDecimal marketValue = lastPrice.multiply(BigDecimal.valueOf(Math.max(0, netQuantity)));
            BigDecimal costValue   = avgCost.multiply(BigDecimal.valueOf(Math.max(0, netQuantity)));

            // 获取 brokerName（从任意一笔成交记录取）
            String brokerName = symbolTrades.isEmpty() ? "" : symbolTrades.get(0).getBrokerName();

            PositionView view = new PositionView();
            view.setAccountId(accountId);
            view.setBrokerName(brokerName);
            view.setSymbol(symbol);
            view.setNetQuantity(netQuantity);
            view.setPendingBuy(pendingBuy);
            view.setPendingSell(pendingSell);
            view.setAvailableQuantity(availableQuantity);
            view.setTotalQuantity(totalQuantity);
            view.setAvgCost(avgCost.setScale(4, RoundingMode.HALF_UP));
            view.setRealizedPnl(realizedPnl.setScale(4, RoundingMode.HALF_UP));
            view.setUnrealizedPnl(unrealizedPnl.setScale(4, RoundingMode.HALF_UP));
            view.setTotalPnl(totalPnl.setScale(4, RoundingMode.HALF_UP));
            view.setLastPrice(lastPrice.setScale(4, RoundingMode.HALF_UP));
            view.setMarketValue(marketValue.setScale(4, RoundingMode.HALF_UP));
            view.setCostValue(costValue.setScale(4, RoundingMode.HALF_UP));
            view.setUpdateTime(System.currentTimeMillis());

            result.add(view);
        }

        result.sort((a, b) -> a.getSymbol().compareTo(b.getSymbol()));
        return result;
    }
}
