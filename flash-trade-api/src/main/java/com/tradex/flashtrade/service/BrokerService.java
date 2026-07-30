package com.tradex.flashtrade.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradex.flashtrade.broker.BrokerClient;
import com.tradex.flashtrade.broker.dto.BrokerOrder;
import com.tradex.flashtrade.broker.dto.BrokerPortfolio;
import com.tradex.flashtrade.broker.dto.BrokerTrade;
import com.tradex.flashtrade.broker.dto.BrokerWatchlistItem;
import com.tradex.flashtrade.exception.BusinessException;
import com.tradex.flashtrade.exception.ErrorCode;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 券商路由服务
 * <p>
 * 职责：
 * <ul>
 *   <li>按 brokerName 路由到对应的 {@link BrokerClient} 实现</li>
 *   <li>Redis 缓存（TTL 5分钟）减少外部调用次数</li>
 *   <li>Resilience4j {@code @Retry} 指数退避重试（最多3次）</li>
 *   <li>Resilience4j {@code @CircuitBreaker} 断路器保护，打开时降级为缓存数据</li>
 *   <li>{@code @Async} 异步调用，不阻塞业务线程</li>
 *   <li>Micrometer Timer 计测各券商 API 响应时间</li>
 * </ul>
 */
@Service
public class BrokerService {

    private static final Logger log = LoggerFactory.getLogger(BrokerService.class);

    private static final String CACHE_PREFIX_PORTFOLIO = "broker:portfolio:";
    private static final String CACHE_PREFIX_ORDERS    = "broker:orders:";
    private static final String CACHE_PREFIX_TRADES    = "broker:trades:";
    private static final String CACHE_PREFIX_WATCHLIST = "broker:watchlist:";
    private static final long   CACHE_TTL_MINUTES      = 5;

    private final Map<String, BrokerClient> brokerMap;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    public BrokerService(List<BrokerClient> brokerClients,
                         RedisTemplate<String, String> redisTemplate,
                         ObjectMapper objectMapper,
                         MeterRegistry meterRegistry) {
        this.brokerMap     = brokerClients.stream()
                .collect(Collectors.toMap(
                        BrokerClient::getBrokerName,
                        Function.identity()
                ));
        this.redisTemplate = redisTemplate;
        this.objectMapper  = objectMapper;
        this.meterRegistry = meterRegistry;
        log.info("已注册券商: {}", this.brokerMap.keySet());
    }

    // -------------------------------------------------------------------------
    // Portfolio（持仓汇总）
    // -------------------------------------------------------------------------

    @CircuitBreaker(name = "broker", fallbackMethod = "fallbackPortfolio")
    @Retry(name = "broker")
    public BrokerPortfolio getPortfolio(String brokerName, String accountId) {
        return timedCall("portfolio", brokerName, () -> {
            String cacheKey = CACHE_PREFIX_PORTFOLIO + brokerName + ":" + accountId;
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("缓存命中 portfolio BrokerName={} AccountId={}", brokerName, accountId);
                BrokerPortfolio hit = deserialize(cached, BrokerPortfolio.class);
                if (hit != null) return hit;
            }
            BrokerClient client = resolveBroker(brokerName);
            BrokerPortfolio portfolio = client.getPortfolio(accountId);
            cacheValue(cacheKey, portfolio);
            return portfolio;
        });
    }

    /** 熔断器打开时的降级处理：优先返回缓存数据，无缓存则抛出 503 */
    public BrokerPortfolio fallbackPortfolio(String brokerName, String accountId, Throwable ex) {
        log.warn("Circuit open for portfolio broker={}: {}", brokerName, ex.getMessage());
        String cacheKey = CACHE_PREFIX_PORTFOLIO + brokerName + ":" + accountId;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            BrokerPortfolio data = deserialize(cached, BrokerPortfolio.class);
            if (data != null) return data;
        }
        throw new BusinessException(ErrorCode.BROKER_UNAVAILABLE,
                "券商 [" + brokerName + "] 暂时不可用，请稍后重试");
    }

    @Async("brokerTaskExecutor")
    public CompletableFuture<BrokerPortfolio> getPortfolioAsync(String brokerName, String accountId) {
        return CompletableFuture.completedFuture(getPortfolio(brokerName, accountId));
    }

    // -------------------------------------------------------------------------
    // Orders（委托列表）
    // -------------------------------------------------------------------------

    @CircuitBreaker(name = "broker", fallbackMethod = "fallbackOrders")
    @Retry(name = "broker")
    public List<BrokerOrder> getOrders(String brokerName, String accountId) {
        return timedCall("orders", brokerName, () -> {
            String cacheKey = CACHE_PREFIX_ORDERS + brokerName + ":" + accountId;
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("缓存命中 orders BrokerName={} AccountId={}", brokerName, accountId);
                List<BrokerOrder> hit = deserialize(cached, new TypeReference<List<BrokerOrder>>() {});
                if (hit != null) return hit;
            }
            BrokerClient client = resolveBroker(brokerName);
            List<BrokerOrder> orders = client.getOrders(accountId);
            cacheValue(cacheKey, orders);
            return orders;
        });
    }

    public List<BrokerOrder> fallbackOrders(String brokerName, String accountId, Throwable ex) {
        log.warn("Circuit open for orders broker={}: {}", brokerName, ex.getMessage());
        String cacheKey = CACHE_PREFIX_ORDERS + brokerName + ":" + accountId;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            List<BrokerOrder> data = deserialize(cached, new TypeReference<List<BrokerOrder>>() {});
            if (data != null) return data;
        }
        return Collections.emptyList();
    }

    @Async("brokerTaskExecutor")
    public CompletableFuture<List<BrokerOrder>> getOrdersAsync(String brokerName, String accountId) {
        return CompletableFuture.completedFuture(getOrders(brokerName, accountId));
    }

    // -------------------------------------------------------------------------
    // Trades（成交记录）
    // -------------------------------------------------------------------------

    @CircuitBreaker(name = "broker", fallbackMethod = "fallbackTrades")
    @Retry(name = "broker")
    public List<BrokerTrade> getTrades(String brokerName, String accountId) {
        return timedCall("trades", brokerName, () -> {
            String cacheKey = CACHE_PREFIX_TRADES + brokerName + ":" + accountId;
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("缓存命中 trades BrokerName={} AccountId={}", brokerName, accountId);
                List<BrokerTrade> hit = deserialize(cached, new TypeReference<List<BrokerTrade>>() {});
                if (hit != null) return hit;
            }
            BrokerClient client = resolveBroker(brokerName);
            List<BrokerTrade> trades = client.getTrades(accountId);
            cacheValue(cacheKey, trades);
            return trades;
        });
    }

    public List<BrokerTrade> fallbackTrades(String brokerName, String accountId, Throwable ex) {
        log.warn("Circuit open for trades broker={}: {}", brokerName, ex.getMessage());
        String cacheKey = CACHE_PREFIX_TRADES + brokerName + ":" + accountId;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            List<BrokerTrade> data = deserialize(cached, new TypeReference<List<BrokerTrade>>() {});
            if (data != null) return data;
        }
        return Collections.emptyList();
    }

    @CircuitBreaker(name = "broker", fallbackMethod = "fallbackWatchlist")
    @Retry(name = "broker")
    public List<BrokerWatchlistItem> getWatchlist(String brokerName, String accountId) {
        return timedCall("watchlist", brokerName, () -> {
            String cacheKey = CACHE_PREFIX_WATCHLIST + brokerName + ":" + accountId;
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("缓存命中 watchlist BrokerName={} AccountId={}", brokerName, accountId);
                List<BrokerWatchlistItem> hit = deserialize(cached, new TypeReference<List<BrokerWatchlistItem>>() {});
                if (hit != null) return hit;
            }
            BrokerClient client = resolveBroker(brokerName);
            List<BrokerWatchlistItem> watchlist = client.getWatchlist(accountId);
            cacheValue(cacheKey, watchlist);
            return watchlist;
        });
    }

    public List<BrokerWatchlistItem> fallbackWatchlist(String brokerName, String accountId, Throwable ex) {
        log.warn("Circuit open for watchlist broker={}: {}", brokerName, ex.getMessage());
        String cacheKey = CACHE_PREFIX_WATCHLIST + brokerName + ":" + accountId;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            List<BrokerWatchlistItem> data = deserialize(cached, new TypeReference<List<BrokerWatchlistItem>>() {});
            if (data != null) return data;
        }
        return Collections.emptyList();
    }

    @Async("brokerTaskExecutor")
    public CompletableFuture<List<BrokerWatchlistItem>> getWatchlistAsync(String brokerName, String accountId) {
        return CompletableFuture.completedFuture(getWatchlist(brokerName, accountId));
    }

    // -------------------------------------------------------------------------
    // 内部工具方法
    // -------------------------------------------------------------------------

    private BrokerClient resolveBroker(String brokerName) {
        BrokerClient client = brokerMap.get(brokerName);
        if (client == null) {
            throw new BusinessException(ErrorCode.BROKER_UNAVAILABLE,
                    "不支持的券商: " + brokerName);
        }
        return client;
    }

    /**
     * 使用 Micrometer Timer 计量 API 调用耗时的辅助方法。
     *
     * @param type       "portfolio" | "orders" | "trades"
     * @param brokerName 券商名称（作为 Tag 记录）
     * @param fn         实际执行的逻辑
     */
    private <T> T timedCall(String type, String brokerName, Supplier<T> fn) {
        return Timer.builder("broker.api.call")
                .tag("type", type)
                .tag("broker", brokerName)
                .register(meterRegistry)
                .record(fn::get);
    }

    private void cacheValue(String key, Object value) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            // 缓存写入失败不影响主业务
            log.warn("缓存写入失败 key={}: {}", key, e.getMessage());
        }
    }

    private <T> T deserialize(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.warn("缓存反序列化失败，将重新调用接口: {}", e.getMessage());
            return null;
        }
    }

    private <T> T deserialize(String json, TypeReference<T> typeRef) {
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (Exception e) {
            log.warn("缓存反序列化失败，将重新调用接口: {}", e.getMessage());
            return null;
        }
    }
}
