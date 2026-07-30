package com.tradex.flashtrade.broker.tastytrade;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.tradex.flashtrade.broker.tastytrade.config.TastytradeProperties;
import com.tradex.flashtrade.exception.BrokerApiException;

/**
 * Tastytrade Token 服务：目前支持直接使用 API Key（如果提供），
 * 否则抛出异常。后续可扩展为 OAuth2 / 登录流程。
 */
@Component
public class TastytradeTokenService {

    private static final Logger log = LoggerFactory.getLogger(TastytradeTokenService.class);
    private static final String BROKER = "TASTYTRADE";
    private static final long TOKEN_TTL_HOURS = 24;

    private final TastytradeProperties props;
    private final RedisTemplate<String, String> redisTemplate;

    public TastytradeTokenService(TastytradeProperties props, RedisTemplate<String, String> redisTemplate) {
        this.props = props;
        this.redisTemplate = redisTemplate;
    }

    public String getSessionToken() {
        // 优先从缓存读取
        String key = tokenKey();
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null && !cached.isBlank()) {
            return cached;
        }
        return refreshToken();
    }

    public String refreshToken() {
        if (props.getApiKey() != null && !props.getApiKey().isBlank()) {
            String token = "Bearer " + props.getApiKey();
            redisTemplate.opsForValue().set(tokenKey(), token, Duration.ofHours(TOKEN_TTL_HOURS));
            log.info("Tastytrade: 使用 API Key 生成 Bearer token，缓存 {}h", TOKEN_TTL_HOURS);
            return token;
        }
        log.error("Tastytrade: 未配置 API Key，无法获取 Token");
        throw new BrokerApiException(BROKER, 401, "Tastytrade API Key 未配置");
    }

    public void clearToken() {
        redisTemplate.delete(tokenKey());
        log.debug("Tastytrade: Token 已清除");
    }

    private String tokenKey() { return props.getApiKey() != null ? props.getApiKey() : "tastytrade:token:default"; }
}
