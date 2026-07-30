package com.tradex.flashtrade.service;

import com.tradex.flashtrade.dto.portfolio.PositionPushMessage;
import com.tradex.flashtrade.dto.portfolio.PositionResponse;
import com.tradex.flashtrade.dto.portfolio.PositionView;
import com.tradex.flashtrade.repository.PositionRepository;
import jakarta.annotation.PreDestroy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class PositionPushService {

    private static final String POSITION_TOPIC        = "/topic/positions";
    private static final String PORTFOLIO_TOPIC        = "/topic/portfolio";
    private static final long   DEBOUNCE_DELAY_MS     = 500;

    private final SimpMessagingTemplate                        messagingTemplate;
    private final PositionRepository                           positionRepository;
    private final PortfolioService                             portfolioService;
    private final PositionAggregatorService                    aggregatorService;
    private final ScheduledExecutorService                     debounceExecutor;
    private final ConcurrentHashMap<Long, ScheduledFuture<?>> pendingPush;

    public PositionPushService(SimpMessagingTemplate messagingTemplate,
                                PositionRepository positionRepository,
                                PortfolioService portfolioService,
                                PositionAggregatorService aggregatorService) {
        this.messagingTemplate  = messagingTemplate;
        this.positionRepository = positionRepository;
        this.portfolioService   = portfolioService;
        this.aggregatorService  = aggregatorService;
        this.debounceExecutor   = Executors.newSingleThreadScheduledExecutor(
                r -> new Thread(r, "position-debounce"));
        this.pendingPush        = new ConcurrentHashMap<>();
    }

    /**
     * 每 30 秒将所有持仓广播到 /topic/positions
     */
    @Scheduled(fixedDelayString = "${app.websocket.push-interval-ms:30000}")
    @Transactional(readOnly = true)
    public void broadcastAllPositions() {
        List<PositionResponse> positions = positionRepository.findAll()
                .stream()
                .map(portfolioService::toPositionResponse)
                .toList();

        if (!positions.isEmpty()) {
            messagingTemplate.convertAndSend(POSITION_TOPIC, new PositionPushMessage(positions));
        }
    }

    /**
     * 触发指定用户的持仓更新推送（防抖 500ms）。
     * 连续调用时仅在最后一次调用 500ms 后发送一次。
     */
    public void pushPositionsForUser(Long userId) {
        ScheduledFuture<?> existing = pendingPush.remove(userId);
        if (existing != null) {
            existing.cancel(false);
        }
        ScheduledFuture<?> future = debounceExecutor.schedule(() -> {
            pendingPush.remove(userId);
            doPush(userId);
        }, DEBOUNCE_DELAY_MS, TimeUnit.MILLISECONDS);
        pendingPush.put(userId, future);
    }

    @Transactional(readOnly = true)
    public void doPush(Long userId) {
        List<PositionResponse> positions = positionRepository
                .findByUserIdOrderBySymbolAsc(userId)
                .stream()
                .map(portfolioService::toPositionResponse)
                .toList();

        messagingTemplate.convertAndSend(POSITION_TOPIC, new PositionPushMessage(positions));

        // 同时推送聚合资产视图（新增）
        List<PositionView> views = aggregatorService.aggregate(userId);
        if (!views.isEmpty()) {
            messagingTemplate.convertAndSend(PORTFOLIO_TOPIC, views);
        }
    }

    @PreDestroy
    public void shutdown() {
        debounceExecutor.shutdownNow();
    }
}
