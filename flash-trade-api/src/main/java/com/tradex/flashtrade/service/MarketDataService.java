package com.tradex.flashtrade.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradex.flashtrade.broker.dto.MarketData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;

/**
 * 行情缓存服务
 * <p>
 * 职责：将行情数据写入 Redis（Key: {@code market:{symbol}}，TTL 5s），
 * 并提供按 symbol 查询最新价格的方法供 PositionAggregatorService 使用。
 */
@Service
public class MarketDataService {

    private static final Logger log = LoggerFactory.getLogger(MarketDataService.class);
    private static final String KEY_PREFIX = "market:";
    private static final Duration TTL = Duration.ofSeconds(5);

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public MarketDataService(RedisTemplate<String, String> redisTemplate,
                             ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper  = objectMapper;
    }

    /** 写入行情缓存 */
    public void put(MarketData data) {
        if (data == null || data.getSymbol() == null) return;
        try {
            String key   = KEY_PREFIX + data.getSymbol();
            String value = objectMapper.writeValueAsString(data);
            redisTemplate.opsForValue().set(key, value, TTL);
        } catch (Exception e) {
            log.warn("写入行情缓存失败 symbol={}: {}", data.getSymbol(), e.getMessage());
        }
    }

    /** 读取行情缓存（返回 Optional，不抛异常） */
    public Optional<MarketData> get(String symbol) {
        try {
            String value = redisTemplate.opsForValue().get(KEY_PREFIX + symbol);
            if (value == null) return Optional.empty();
            return Optional.of(objectMapper.readValue(value, MarketData.class));
        } catch (Exception e) {
            log.warn("读取行情缓存失败 symbol={}: {}", symbol, e.getMessage());
            return Optional.empty();
        }
    }

    /** 获取最新价格，不存在时返回 null */
    public BigDecimal getLastPrice(String symbol) {
        return get(symbol).map(MarketData::getLastPrice).orElse(null);
    }
}
