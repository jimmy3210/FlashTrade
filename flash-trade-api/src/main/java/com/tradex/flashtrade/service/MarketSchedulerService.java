package com.tradex.flashtrade.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.tradex.flashtrade.broker.BrokerClient;
import com.tradex.flashtrade.broker.dto.MarketData;
import com.tradex.flashtrade.config.BrokerSyncProperties;

/**
 * 行情调度器（Market Data Scheduler）
 * <p>
 * 核心设计：行情账户池（Market Account Pool）
 * <ul>
 *   <li>每个券商账户同时最多请求 {@code maxSymbolsPerAccount}（默认 30）只股票的行情</li>
 *   <li>新增 symbol 时，选择当前负载最小的账户（最少优先策略）</li>
 *   <li>最后一个订阅者取消后，自动释放 symbol 的调度槽</li>
 *   <li>每秒轮询一次，将行情写入 Redis，并触发 WebSocket 推送</li>
 * </ul>
 *
 * <p>当券商未实现 {@code getBatchMarketData}（即 Mock 模式）时，
 * 以 positions 表的当前价格生成模拟行情（±0.5% 随机波动）。
 */
@Service
public class MarketSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(MarketSchedulerService.class);

    /** 单账户最多调度的 symbol 数量（默认 30，可从 t_broker_api_limit 覆盖）*/
    private static final int DEFAULT_MAX_SYMBOLS = 30;

    // ---- 账户池数据结构 ----

    /** symbol → 被分配到的行情账户 */
    private final ConcurrentHashMap<String, MarketAccount> symbolToAccount = new ConcurrentHashMap<>();

    /** 所有可用行情账户（从 BrokerSyncProperties 初始化） */
    private final List<MarketAccount> accountPool = new ArrayList<>();

    /**
     * 高优先级 symbol 集合（TODO-11）：
     * 被加入该集合的 symbol 每秒刷新；未加入的 symbol 每 3 秒刷新一次。
     */
    private final Set<String> highPrioritySymbols = ConcurrentHashMap.newKeySet();

    /** 用于低优先级节流：每 3 个 tick 执行一次低优先级拉取 */
    private final AtomicInteger tickCount = new AtomicInteger(0);

    // ---- 依赖 ----

    private final Map<String, BrokerClient> brokerMap;
    private final MarketDataService         marketDataService;
    private final MarketPushService         marketPushService;
    private final org.springframework.jdbc.core.JdbcTemplate jdbc;

    public MarketSchedulerService(List<BrokerClient> brokerClients,
                                  BrokerSyncProperties syncProperties,
                                  MarketDataService marketDataService,
                                  MarketPushService marketPushService,
                                  org.springframework.jdbc.core.JdbcTemplate jdbc) {
        this.marketDataService  = marketDataService;
        this.marketPushService  = marketPushService;
        this.jdbc               = jdbc;
        this.brokerMap = brokerClients.stream()
                .collect(Collectors.toMap(BrokerClient::getBrokerName, Function.identity()));

        // 将 BrokerSyncProperties 中配置的账户初始化为行情账户池
        // TODO-09: 从 t_broker_api_limit 读取各券商的 maxSymbolsPerRequest
        for (BrokerSyncProperties.AccountMapping mapping : syncProperties.getAccounts()) {
            int maxSymbols = loadMaxSymbols(mapping.getBrokerName());
            MarketAccount acc = new MarketAccount(
                    mapping.getBrokerName(),
                    mapping.getAccountId(),
                    maxSymbols
            );
            accountPool.add(acc);
        }
        log.info("行情账户池初始化完成，共 {} 个账户，总容量 {} 个 symbol",
                accountPool.size(), accountPool.stream().mapToInt(a -> a.maxSymbols).sum());
    }

    /**
     * 从 t_broker_api_limit 读取行情批量请求上限（TODO-09）。
     * 若数据库中无对应记录或查询失败，回退到默认值 30。
     */
    private int loadMaxSymbols(String brokerName) {
        try {
            String sql = "SELECT max_symbols_per_request FROM t_broker_api_limit "
                       + "WHERE broker_id = ? AND api_name = 'GET_BATCH_MARKET_DATA'";
            Integer val = jdbc.queryForObject(sql, Integer.class, brokerName);
            if (val != null && val > 0) {
                log.debug("从 DB 读取 [{}] 行情批量上限: {}", brokerName, val);
                return val;
            }
        } catch (Exception e) {
            log.debug("读取 [{}] 行情批量上限失败（将使用默认值 {}）: {}",
                    brokerName, DEFAULT_MAX_SYMBOLS, e.getMessage());
        }
        return DEFAULT_MAX_SYMBOLS;
    }

    // -------------------------------------------------------------------------
    // Symbol 分配 / 释放
    // -------------------------------------------------------------------------

    /**
     * 为 symbol 分配行情账户。
     * 若 symbol 已被分配则跳过（幂等）；
     * 若账户池已满则打印警告（不抛异常，降级为无行情）。
     */
    public synchronized void assignSymbol(String symbol) {
        if (symbolToAccount.containsKey(symbol)) return;

        // 负载最小优先
        accountPool.stream()
                .min(Comparator.comparingInt(a -> a.assignedSymbols.size()))
                .filter(a -> a.assignedSymbols.size() < a.maxSymbols)
                .ifPresentOrElse(
                        acc -> {
                            acc.assignedSymbols.add(symbol);
                            symbolToAccount.put(symbol, acc);
                            log.debug("symbol [{}] 分配至行情账户 [{}/{}]",
                                    symbol, acc.brokerName, acc.accountId);
                        },
                        () -> log.warn("行情账户池已满，无法为 symbol [{}] 分配调度槽，当前总容量 {}",
                                symbol, accountPool.size() * DEFAULT_MAX_SYMBOLS)
                );
    }

    /**
     * 释放 symbol 的行情调度槽（无订阅者时调用）。
     */
    public synchronized void releaseSymbol(String symbol) {
        MarketAccount acc = symbolToAccount.remove(symbol);
        if (acc != null) {
            acc.assignedSymbols.remove(symbol);
            log.debug("symbol [{}] 已从行情账户 [{}/{}] 释放", symbol, acc.brokerName, acc.accountId);
        }
    }

    /** 返回当前已分配调度的 symbol 集合（只读快照） */
    public Set<String> getScheduledSymbols() {
        return Collections.unmodifiableSet(symbolToAccount.keySet());
    }

    /**
     * 将 symbol 标记为高优先级（1 秒刷新）。
     * 典型调用时机：前端将某 symbol 设为当前活跃。（TODO-11）
     */
    public void setHighPriority(String symbol) {
        highPrioritySymbols.add(symbol);
        log.debug("symbol [{}] 设为高优先级（1 秒刷新）", symbol);
    }

    /**
     * 取消 symbol 的高优先级标记，降为低优先级（3 秒刷新）。
     */
    public void clearHighPriority(String symbol) {
        highPrioritySymbols.remove(symbol);
        log.debug("symbol [{}] 降为低优先级（3 秒刷新）", symbol);
    }

    /** 是否处于容量满载状态 */
    public boolean isAtCapacity() {
        int total = accountPool.stream().mapToInt(a -> a.maxSymbols).sum();
        return symbolToAccount.size() >= total;
    }

    // -------------------------------------------------------------------------
    // 定时行情轮询（1 秒间隔）
    // -------------------------------------------------------------------------

    /**
     * 行情轮询（TODO-11 优先级调度）
     * <ul>
     *   <li>每次调用（1 秒间隔）：拉取高优先级 symbol 的行情</li>
     *   <li>每 3 次调用（约 3 秒）：额外拉取低优先级 symbol 的行情</li>
     * </ul>
     */
    @Scheduled(fixedRate = 1000)
    public void fetchMarketData() {
        if (accountPool.isEmpty() || symbolToAccount.isEmpty()) return;

        int tick = tickCount.incrementAndGet();
        boolean fetchLowPriority = (tick % 3 == 0);

        for (MarketAccount acc : accountPool) {
            List<String> symbols = acc.assignedSymbols.stream()
                    .filter(s -> highPrioritySymbols.contains(s) || fetchLowPriority)
                    .collect(Collectors.toList());
            if (symbols.isEmpty()) continue;

            Map<String, MarketData> result = fetchFromBroker(acc, symbols);
            for (Map.Entry<String, MarketData> entry : result.entrySet()) {
                marketDataService.put(entry.getValue());
                // 同步推送到已订阅的 WebSocket 客户端
                marketPushService.pushMarketData(entry.getValue());
            }
        }
    }

    // -------------------------------------------------------------------------
    // 内部工具
    // -------------------------------------------------------------------------

    private Map<String, MarketData> fetchFromBroker(MarketAccount acc, List<String> symbols) {
        BrokerClient client = brokerMap.get(acc.brokerName);
        if (client == null) return Map.of();

        try {
            return client.getBatchMarketData(acc.accountId, symbols);
        } catch (UnsupportedOperationException e) {
            // 券商未实现真实行情接口时，使用模拟行情
            return buildMockMarketData(symbols);
        } catch (Exception e) {
            log.warn("行情拉取失败 broker={} account={}: {}", acc.brokerName, acc.accountId, e.getMessage());
            return Map.of();
        }
    }

    /**
     * Mock 行情数据：对每个 symbol 生成 ±0.5% 随机波动的价格。
     * 初始基准价格使用固定参考值（100.00），
     * 实际应从 positions 表获取 currentPrice 作为基准。
     */
    private Map<String, MarketData> buildMockMarketData(List<String> symbols) {
        Map<String, MarketData> result = new HashMap<>();
        for (String symbol : symbols) {
            // 模拟价格：100 + 0~5 的随机浮动（种子来自 symbol hashCode 保证同 symbol 稳定）
            double base  = 100.0 + (Math.abs(symbol.hashCode()) % 400);
            double delta = (Math.random() - 0.5) * base * 0.01; // ±0.5%
            BigDecimal price = BigDecimal.valueOf(base + delta)
                    .setScale(2, RoundingMode.HALF_UP);

            result.put(symbol, MarketData.builder()
                    .symbol(symbol)
                    .lastPrice(price)
                    .bidPrice(price.subtract(BigDecimal.valueOf(0.01)))
                    .askPrice(price.add(BigDecimal.valueOf(0.01)))
                    .timestamp(System.currentTimeMillis())
                    .build());
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // 内部数据结构
    // -------------------------------------------------------------------------

    /** 行情账户：持有账户信息和已分配的 symbol 集合 */
    private static class MarketAccount {
        final String      brokerName;
        final String      accountId;
        final int         maxSymbols;
        final Set<String> assignedSymbols = ConcurrentHashMap.newKeySet();

        MarketAccount(String brokerName, String accountId, int maxSymbols) {
            this.brokerName = brokerName;
            this.accountId  = accountId;
            this.maxSymbols = maxSymbols;
        }
    }
}
