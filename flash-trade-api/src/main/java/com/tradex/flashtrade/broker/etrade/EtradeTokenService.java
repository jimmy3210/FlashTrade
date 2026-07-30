package com.tradex.flashtrade.broker.etrade;

import com.tradex.flashtrade.broker.etrade.config.EtradeProperties;
import com.tradex.flashtrade.exception.BrokerApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * E*TRADE OAuth 1.0a Token 管理服务
 * <p>
 * E*TRADE 使用 OAuth 1.0a 认证方案：
 * <ol>
 *   <li>使用 consumerKey/consumerSecret 获取 Request Token</li>
 *   <li>用户在浏览器中完成授权，获得 verifier code</li>
 *   <li>使用 Request Token + verifier 换取 Access Token</li>
 * </ol>
 * Access Token 每天午夜（美东时间）自动失效，需重新授权。
 * <br>
 * 本服务通过 Redis 缓存 Access Token 及其 TTL。
 * </p>
 */
@Service
public class EtradeTokenService {

    private static final Logger log = LoggerFactory.getLogger(EtradeTokenService.class);

    private static final String SUFFIX_TOKEN  = ":access_token";
    private static final String SUFFIX_SECRET = ":token_secret";
    private static final String BROKER_NAME   = "ETRADE";

    /** E*TRADE Access Token 日内有效，TTL 设置 23 小时（留出换新余量） */
    private static final long TOKEN_TTL_HOURS = 23;

    private final EtradeProperties props;
    private final RedisTemplate<String, String> redisTemplate;

    public EtradeTokenService(EtradeProperties props,
                              RedisTemplate<String, String> redisTemplate) {
        this.props         = props;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取当前有效的 Access Token。
     *
     * @param accountId 账户标识（用于 Redis key 隔离）
     * @return OAuth 1.0a Access Token 字符串
     * @throws BrokerApiException Token 不存在或已过期
     */
    public String getAccessToken(String accountId) {
        String token = redisTemplate.opsForValue().get(tokenKey(accountId));
        if (token == null || token.isBlank()) {
            throw new BrokerApiException(BROKER_NAME, 401,
                    "E*TRADE Access Token 不存在，请先完成 OAuth 1.0a 授权");
        }
        return token;
    }

    /**
     * 获取当前有效的 Token Secret（OAuth 1.0a 签名所需）。
     *
     * @param accountId 账户标识
     * @return OAuth 1.0a Token Secret
     * @throws BrokerApiException Secret 不存在
     */
    public String getTokenSecret(String accountId) {
        String secret = redisTemplate.opsForValue().get(secretKey(accountId));
        if (secret == null || secret.isBlank()) {
            throw new BrokerApiException(BROKER_NAME, 401,
                    "E*TRADE Token Secret 不存在，请先完成 OAuth 1.0a 授权");
        }
        return secret;
    }

    /**
     * 手动写入 Access Token 和 Token Secret（授权完成后由运维或用户触发）。
     *
     * @param accountId   账户标识
     * @param accessToken OAuth 1.0a Access Token
     * @param tokenSecret OAuth 1.0a Token Secret
     */
    public void storeTokens(String accountId, String accessToken, String tokenSecret) {
        redisTemplate.opsForValue().set(tokenKey(accountId), accessToken,
                TOKEN_TTL_HOURS, TimeUnit.HOURS);
        redisTemplate.opsForValue().set(secretKey(accountId), tokenSecret,
                TOKEN_TTL_HOURS, TimeUnit.HOURS);
        log.info("E*TRADE Token 已写入 Redis AccountId={} TTL={}h", accountId, TOKEN_TTL_HOURS);
    }

    /** 检查 Token 是否存在 */
    public boolean hasValidToken(String accountId) {
        String token = redisTemplate.opsForValue().get(tokenKey(accountId));
        return token != null && !token.isBlank();
    }

    private String tokenKey(String accountId) {
        return props.getTokenCacheKeyPrefix() + ":" + accountId + SUFFIX_TOKEN;
    }

    private String secretKey(String accountId) {
        return props.getTokenCacheKeyPrefix() + ":" + accountId + SUFFIX_SECRET;
    }
}
