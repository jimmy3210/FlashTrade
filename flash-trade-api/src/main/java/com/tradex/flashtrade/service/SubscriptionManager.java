package com.tradex.flashtrade.service;

import com.tradex.flashtrade.exception.BusinessException;
import com.tradex.flashtrade.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 订阅管理器
 * <p>
 * 核心职责：
 * <ul>
 *   <li>跟踪哪些 session 订阅了哪些 symbol 的行情</li>
 *   <li>同一 symbol 无论多少 session 订阅，行情只请求一次（共享调度槽）</li>
 *   <li>最后一个 session 取消订阅或断开后，自动释放该 symbol 的调度槽</li>
 * </ul>
 *
 * <p>订阅目标格式：
 * <ul>
 *   <li>行情订阅：STOMP destination = {@code /topic/market/{symbol}}（如 /topic/market/AAPL）</li>
 *   <li>资产视图：STOMP destination = {@code /topic/portfolio}（全量推送，无需单独管理）</li>
 * </ul>
 */
@Service
public class SubscriptionManager {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionManager.class);

    /** 行情 topic 前缀 */
    static final String MARKET_TOPIC_PREFIX = "/topic/market/";

    /** symbol → 订阅该 symbol 的 session 集合 */
    private final ConcurrentHashMap<String, Set<String>> symbolToSessions = new ConcurrentHashMap<>();

    /** sessionId → 该 session 订阅的 symbol 集合 */
    private final ConcurrentHashMap<String, Set<String>> sessionToSymbols = new ConcurrentHashMap<>();

    private final MarketSchedulerService marketScheduler;
    private final StockService stockService;

    public SubscriptionManager(MarketSchedulerService marketScheduler, StockService stockService) {
        this.marketScheduler = marketScheduler;
        this.stockService    = stockService;
    }

    // -------------------------------------------------------------------------
    // 手动订阅 / 取消订阅（供 REST API 或测试调用）
    // -------------------------------------------------------------------------

    /**
     * 订阅指定 symbol 的行情。
     *
     * @param sessionId WebSocket session ID
     * @param symbol    股票代码（自动大写）
     */
    public void subscribe(String sessionId, String symbol) {
        String s = symbol.toUpperCase();

        // 新 symbol の場合: 容量チェック
        if (!symbolToSessions.containsKey(s) && marketScheduler.isAtCapacity()) {
            log.warn("行情账户池已满，拒绝订阅 symbol [{}] session=[{}]", s, sessionId);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "系统行情容量已满，请稍后重试");
        }

        // ホワイトリスト検証（自動登録含む）
        stockService.validateAndRegister(s);

        symbolToSessions.computeIfAbsent(s, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
        sessionToSymbols.computeIfAbsent(sessionId, k -> ConcurrentHashMap.newKeySet()).add(s);

        // 若为首个订阅者，为 symbol 分配行情调度槽并设为高优先级
        if (symbolToSessions.get(s).size() == 1) {
            marketScheduler.assignSymbol(s);
            marketScheduler.setHighPriority(s);
            log.info("symbol [{}] 首次订阅，已分配行情调度槽并设为高优先级（session={}）", s, sessionId);
        }
    }

    /**
     * 取消订阅指定 symbol。
     *
     * @param sessionId WebSocket session ID
     * @param symbol    股票代码
     */
    public void unsubscribe(String sessionId, String symbol) {
        String s = symbol.toUpperCase();
        Set<String> sessions = symbolToSessions.get(s);
        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                symbolToSessions.remove(s);
                marketScheduler.releaseSymbol(s);
                marketScheduler.clearHighPriority(s);
                log.info("symbol [{}] 无订阅者，已释放行情调度槽并清除高优先级", s);
            }
        }
        Set<String> symbols = sessionToSymbols.get(sessionId);
        if (symbols != null) symbols.remove(s);
    }

    /**
     * 清理指定 session 的所有订阅（断开连接时调用）。
     */
    public void cleanupSession(String sessionId) {
        Set<String> symbols = sessionToSymbols.remove(sessionId);
        if (symbols == null || symbols.isEmpty()) return;
        for (String sym : symbols) {
            Set<String> sessions = symbolToSessions.get(sym);
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    symbolToSessions.remove(sym);
                    marketScheduler.releaseSymbol(sym);
                    marketScheduler.clearHighPriority(sym);
                    log.info("symbol [{}] 无订阅者（session {} 断开），已释放行情调度槽并清除高优先级", sym, sessionId);
                }
            }
        }
        log.debug("session [{}] 断开，已清理 {} 个行情订阅", sessionId, symbols.size());
    }

    // -------------------------------------------------------------------------
    // Spring WebSocket 事件监听（自动管理订阅生命周期）
    // -------------------------------------------------------------------------

    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();
        String sessionId   = accessor.getSessionId();
        if (destination == null || sessionId == null) return;

        if (destination.startsWith(MARKET_TOPIC_PREFIX)) {
            String symbol = destination.substring(MARKET_TOPIC_PREFIX.length());
            if (!symbol.isBlank()) {
                subscribe(sessionId, symbol);
            }
        }
    }

    @EventListener
    public void handleUnsubscribe(SessionUnsubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();
        String sessionId   = accessor.getSessionId();
        if (destination == null || sessionId == null) return;

        if (destination.startsWith(MARKET_TOPIC_PREFIX)) {
            String symbol = destination.substring(MARKET_TOPIC_PREFIX.length());
            if (!symbol.isBlank()) {
                unsubscribe(sessionId, symbol);
            }
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        if (sessionId != null) {
            cleanupSession(sessionId);
        }
    }

    // -------------------------------------------------------------------------
    // 查询方法
    // -------------------------------------------------------------------------

    /** 返回订阅了指定 symbol 的 session 数量 */
    public int getSubscriberCount(String symbol) {
        Set<String> sessions = symbolToSessions.get(symbol.toUpperCase());
        return sessions == null ? 0 : sessions.size();
    }

    /** 返回当前已订阅的所有 symbol（只读快照） */
    public Set<String> getSubscribedSymbols() {
        return Collections.unmodifiableSet(symbolToSessions.keySet());
    }
}
