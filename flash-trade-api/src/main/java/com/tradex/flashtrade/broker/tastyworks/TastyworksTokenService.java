package com.tradex.flashtrade.broker.tastyworks;

import com.tradex.flashtrade.broker.tastyworks.config.TastyworksProperties;
import com.tradex.flashtrade.broker.tastyworks.dto.TastyworksSessionResponse;
import com.tradex.flashtrade.exception.BrokerApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.Map;

/**
 * Tastyworks Session Token 管理器
 * <p>
 * 通过用户名/密码登录 POST /sessions 获取 session-token，
 * 存入 Redis（TTL 24h）。
 * </p>
 */
@Component
public class TastyworksTokenService {

    private static final Logger log              = LoggerFactory.getLogger(TastyworksTokenService.class);
    private static final String BROKER_NAME      = "TASTYWORKS";
    private static final long   TOKEN_TTL_HOURS  = 24;

    private final TastyworksProperties             props;
    private final RedisTemplate<String, String>    redisTemplate;
    private final RestClient                        loginClient;

    public TastyworksTokenService(TastyworksProperties props,
                                  RedisTemplate<String, String> redisTemplate) {
        this.props         = props;
        this.redisTemplate = redisTemplate;
        this.loginClient   = RestClient.builder()
                .baseUrl(props.getBaseUrl())
                .build();
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * 获取有效 Session Token。缓存命中时直接返回；否则调用登录接口刷新。
     *
     * @return session-token 字符串
     */
    public String getSessionToken() {
        String cached = redisTemplate.opsForValue().get(tokenKey());
        if (cached != null && !cached.isBlank()) {
            log.debug("Tastyworks: Session Token 缓存命中");
            return cached;
        }
        return refreshToken();
    }

    /**
     * 强制重新登录并刷新 Redis 缓存。
     *
     * @return 新 session-token
     */
    public String refreshToken() {
        log.info("Tastyworks: 登录刷新 Session Token, username={}", props.getUsername());
        try {
            TastyworksSessionResponse response = loginClient.post()
                    .uri("/sessions")
                    .header("Content-Type", "application/json")
                    .body(Map.of(
                            "login",    props.getUsername(),
                            "password", props.getPassword()
                    ))
                    .retrieve()
                    .body(TastyworksSessionResponse.class);

            if (response == null
                    || response.getData() == null
                    || response.getData().getSessionToken() == null) {
                throw new BrokerApiException(BROKER_NAME, 500, "Tastyworks 登录响应中缺少 session-token");
            }

            String token = response.getData().getSessionToken();
            redisTemplate.opsForValue().set(tokenKey(), token, Duration.ofHours(TOKEN_TTL_HOURS));
            log.info("Tastyworks: Session Token 刷新成功，TTL={}h", TOKEN_TTL_HOURS);
            return token;

        } catch (RestClientResponseException ex) {
            log.error("Tastyworks: 登录失败 status={}", ex.getStatusCode().value(), ex);
            throw new BrokerApiException(BROKER_NAME, ex.getStatusCode().value(),
                    "Tastyworks 登录失败: " + ex.getMessage());
        }
    }

    /** 判断 Redis 中是否存在有效 Token */
    public boolean hasValidToken() {
        return Boolean.TRUE.equals(redisTemplate.hasKey(tokenKey()));
    }

    /** 清除 Redis 中的 Token（退出登录） */
    public void clearToken() {
        redisTemplate.delete(tokenKey());
        log.debug("Tastyworks: Session Token 已清除");
    }

    // -------------------------------------------------------------------------
    // Private
    // -------------------------------------------------------------------------

    private String tokenKey() {
        return props.getSessionCacheKeyPrefix();
    }
}
