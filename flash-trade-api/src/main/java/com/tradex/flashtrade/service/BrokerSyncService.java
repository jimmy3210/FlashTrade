package com.tradex.flashtrade.service;

import com.tradex.flashtrade.broker.dto.BrokerOrder;
import com.tradex.flashtrade.broker.dto.BrokerPortfolio;
import com.tradex.flashtrade.broker.dto.BrokerPosition;
import com.tradex.flashtrade.broker.dto.BrokerTrade;
import com.tradex.flashtrade.config.BrokerSyncProperties;
import com.tradex.flashtrade.config.BrokerSyncProperties.AccountMapping;
import com.tradex.flashtrade.entity.*;
import com.tradex.flashtrade.repository.OrderRepository;
import com.tradex.flashtrade.repository.PositionRepository;
import com.tradex.flashtrade.repository.TradeRepository;
import com.tradex.flashtrade.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 券商数据同步服务
 * <p>
 * 职责：将 BrokerService 获取的持仓、委托、成交数据<br>
 * 定期同步到本地 MySQL 数据库（positions / orders / trades 表）。
 * <p>
 * 配置示例（application-dev.yml）：
 * <pre>{@code
 * app:
 *   broker-sync:
 *     enabled: true
 *     interval-ms: 300000
 *     accounts:
 *       - userId: 1
 *         brokerName: SCHWAB
 *         accountId: dev-account-schwab-001
 * }</pre>
 */
@Service
public class BrokerSyncService {

    private static final Logger log = LoggerFactory.getLogger(BrokerSyncService.class);

    private final BrokerSyncProperties    properties;
    private final BrokerService           brokerService;
    private final UserRepository          userRepository;
    private final PositionRepository      positionRepository;
    private final OrderRepository         orderRepository;
    private final TradeRepository         tradeRepository;
    private final Executor                executor;

    public BrokerSyncService(BrokerSyncProperties properties,
                             BrokerService brokerService,
                             UserRepository userRepository,
                             PositionRepository positionRepository,
                             OrderRepository orderRepository,
                             TradeRepository tradeRepository,
                             @Qualifier("brokerTaskExecutor") Executor executor) {
        this.properties         = properties;
        this.brokerService      = brokerService;
        this.userRepository     = userRepository;
        this.positionRepository = positionRepository;
        this.orderRepository    = orderRepository;
        this.tradeRepository    = tradeRepository;
        this.executor           = executor;
    }

    /**
     * 定时同步所有已配置的券商账户。
     * 同步间隔由 {@code app.broker-sync.interval-ms} 控制（默认 5 分钟）。
     */
    @Scheduled(fixedDelayString = "${app.broker-sync.interval-ms:300000}")
    public void syncAll() {
        if (!properties.isEnabled()) {
            log.debug("BrokerSync is disabled, skipping.");
            return;
        }
        List<AccountMapping> accounts = properties.getAccounts();
        if (accounts.isEmpty()) {
            log.debug("No broker accounts configured for sync.");
            return;
        }
        log.info("Starting broker sync for {} account(s).", accounts.size());
        List<CompletableFuture<Void>> futures = accounts.stream()
                .map(mapping -> CompletableFuture.runAsync(() -> {
                    try {
                        syncAccount(mapping);
                    } catch (Exception e) {
                        log.error("Failed to sync broker={} accountId={}: {}",
                                mapping.getBrokerName(), mapping.getAccountId(), e.getMessage(), e);
                    }
                }, executor))
                .toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        log.info("Broker sync completed.");
    }

    @Transactional
    public void syncAccount(AccountMapping mapping) {
        User user = userRepository.findById(mapping.getUserId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Sync config error: userId=" + mapping.getUserId() + " not found"));

        log.debug("Syncing broker={} accountId={} userId={}",
                mapping.getBrokerName(), mapping.getAccountId(), mapping.getUserId());

        syncPositions(user, mapping);
        syncOrders(user, mapping);
        syncTrades(user, mapping);
    }

    // -------------------------------------------------------------------------
    //  持仓同步
    // -------------------------------------------------------------------------

    private void syncPositions(User user, AccountMapping mapping) {
        BrokerPortfolio portfolio;
        try {
            portfolio = brokerService.getPortfolio(mapping.getBrokerName(), mapping.getAccountId());
        } catch (Exception e) {
            log.warn("Cannot fetch portfolio from broker={}: {}", mapping.getBrokerName(), e.getMessage());
            return;
        }

        for (BrokerPosition bp : portfolio.getPositions()) {
            Position pos = positionRepository
                    .findByUserIdAndAccountIdAndSymbolAndBrokerName(
                            user.getId(), mapping.getAccountId(), bp.getSymbol(), mapping.getBrokerName())
                    .orElse(new Position());

            pos.setUser(user);
            pos.setAccountId(mapping.getAccountId());
            pos.setSymbol(bp.getSymbol());
            pos.setQuantity(bp.getQuantity());
            pos.setAverageCost(bp.getAverageCost());
            pos.setCurrentPrice(bp.getCurrentPrice());
            pos.setMarketValue(bp.getMarketValue());
            pos.setUnrealizedPnl(bp.getUnrealizedPnl());
            pos.setBrokerName(mapping.getBrokerName());

            positionRepository.save(pos);
        }
        log.debug("Synced {} position(s) for broker={}", portfolio.getPositions().size(), mapping.getBrokerName());
    }

    // -------------------------------------------------------------------------
    //  委托同步
    // -------------------------------------------------------------------------

    private void syncOrders(User user, AccountMapping mapping) {
        List<BrokerOrder> brokerOrders;
        try {
            brokerOrders = brokerService.getOrders(mapping.getBrokerName(), mapping.getAccountId());
        } catch (Exception e) {
            log.warn("Cannot fetch orders from broker={}: {}", mapping.getBrokerName(), e.getMessage());
            return;
        }

        for (BrokerOrder bo : brokerOrders) {
            Order order = (bo.getExternalOrderId() != null)
                    ? orderRepository.findByExternalOrderIdAndBrokerName(
                            bo.getExternalOrderId(), mapping.getBrokerName())
                        .orElse(new Order())
                    : new Order();

            order.setUser(user);
            order.setAccountId(mapping.getAccountId());
            order.setSymbol(bo.getSymbol());
            order.setSide(OrderSide.valueOf(bo.getSide()));
            order.setQuantity(bo.getQuantity());
            order.setPrice(bo.getPrice());
            order.setStatus(OrderStatus.valueOf(bo.getStatus()));
            order.setBrokerName(mapping.getBrokerName());
            order.setExternalOrderId(bo.getExternalOrderId());
            order.setFilledAt(bo.getFilledAt());

            orderRepository.save(order);
        }
        log.debug("Synced {} order(s) for broker={}", brokerOrders.size(), mapping.getBrokerName());
    }

    // -------------------------------------------------------------------------
    //  成交同步
    // -------------------------------------------------------------------------

    private void syncTrades(User user, AccountMapping mapping) {
        List<BrokerTrade> brokerTrades;
        try {
            brokerTrades = brokerService.getTrades(mapping.getBrokerName(), mapping.getAccountId());
        } catch (Exception e) {
            log.warn("Cannot fetch trades from broker={}: {}", mapping.getBrokerName(), e.getMessage());
            return;
        }

        for (BrokerTrade bt : brokerTrades) {
            Trade trade = (bt.getExternalTradeId() != null)
                    ? tradeRepository.findByExternalTradeIdAndBrokerName(
                            bt.getExternalTradeId(), mapping.getBrokerName())
                        .orElse(new Trade())
                    : new Trade();

            trade.setUser(user);
            trade.setAccountId(mapping.getAccountId());
            trade.setSymbol(bt.getSymbol());
            trade.setSide(OrderSide.valueOf(bt.getSide()));
            trade.setQuantity(bt.getQuantity());
            trade.setPrice(bt.getPrice());
            trade.setCommission(bt.getCommission());
            trade.setBrokerName(mapping.getBrokerName());
            trade.setExternalTradeId(bt.getExternalTradeId());
            trade.setTradedAt(bt.getTradedAt() != null ? bt.getTradedAt() : LocalDateTime.now());

            // 若存在对应委托则关联
            if (bt.getExternalOrderId() != null) {
                orderRepository.findByExternalOrderIdAndBrokerName(
                        bt.getExternalOrderId(), mapping.getBrokerName())
                        .ifPresent(trade::setOrder);
            }

            tradeRepository.save(trade);
        }
        log.debug("Synced {} trade(s) for broker={}", brokerTrades.size(), mapping.getBrokerName());
    }
}
