package com.tradex.flashtrade.broker.schwab;

import com.tradex.flashtrade.broker.schwab.config.SchwabProperties;
import com.tradex.flashtrade.broker.schwab.dto.SchwabTokenResponse;
import com.tradex.flashtrade.exception.BrokerApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * 嘉信证券 OAuth2 Token 管理服务
 * <p>
 * Access Token 有效期约 30 分钟，本服务在 Redis 中缓存 Token，
 * 并在临近到期时（默认提前 120 秒）自动使用 Refresh Token 更新。
 * <br>
 * <b>注意</b>：初次获取 Access Token 需要通过授权码流程，
 * 授权码由外部（管理界面/运维脚本）通过 {@link #storeTokens} 写入。
 * </p>
 */
@Service
public class SchwabTokenService {

    private static final Logger log = LoggerFactory.getLogger(SchwabTokenService.class);

    private static final String SUFFIX_ACCESS  = ":access";
    private static final String SUFFIX_REFRESH = ":refresh";
    private static final String SUFFIX_EXPIRY  = ":expiry";

    private static final String BROKER_NAME = "SCHWAB";

    private final SchwabProperties props;
    private final RedisTemplate<String, String> redisTemplate;
    private final RestClient tokenRestClient;

    public SchwabTokenService(SchwabProperties props,
                              RedisTemplate<String, String> redisTemplate) {
        this.props         = props;
        this.redisTemplate = redisTemplate;
        this.tokenRestClient = RestClient.builder()
                .baseUrl(props.getTokenUrl())
                .build();
    }

    // -------------------------------------------------------------------------
    // 公开 API
    // -------------------------------------------------------------------------

    /**
     * 获取当前有效的 Access Token。
     * <p>若 Token 即将过期，则先自动刷新再返回。</p>
     *
     * @param accountId 账户 ID（用于 Redis key 隔离，支持多账户）
     * @return Bearer Access Token 字符串
     * @throws BrokerApiException 无法获取或刷新 Token 时抛出
     */
    public String getValidAccessToken(String accountId) {
        if (isTokenExpiringSoon(accountId)) {
            log.info("Schwab Access Token 即将到期，尝试刷新 AccountId={}", accountId);
            refreshAccessToken(accountId);
        }
        String token = redisTemplate.opsForValue().get(accessKey(accountId));
        if (token == null || token.isBlank()) {
            throw new BrokerApiException(BROKER_NAME, 401,
                    "未找到有效的 Schwab Access Token，请先完成 OAuth2 授权");
        }
        return token;
    }

    /**
     * 使用授权码（authorization_code）交换 Token 并写入 Redis。
     * <p>仅在首次授权时调用。</p>
     *
     * @param code      OAuth2 授权码
     * @param accountId 账户 ID
     */
    public void exchangeAuthorizationCode(String code, String accountId) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("redirect_uri", props.getRedirectUri());

        SchwabTokenResponse resp = callTokenEndpoint(form);
        storeTokens(accountId, resp);
        log.info("Schwab 授权码换取 Token 成功 AccountId={}", accountId);
    }

    /**
     * 手动写入 Token（适用于运维脚本初始化或测试环境）。
     *
     * @param accountId    账户 ID
     * @param accessToken  Access Token
     * @param refreshToken Refresh Token
     * @param expiresInSec Access Token 有效期（秒）
     */
    public void storeTokens(String accountId, String accessToken,
                            String refreshToken, int expiresInSec) {
        redisTemplate.opsForValue().set(accessKey(accountId), accessToken,
                expiresInSec, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(refreshKey(accountId), refreshToken,
                7, TimeUnit.DAYS);
        redisTemplate.opsForValue().set(expiryKey(accountId),
                String.valueOf(System.currentTimeMillis() / 1000 + expiresInSec),
                expiresInSec, TimeUnit.SECONDS);
        log.debug("Schwab Token 已写入 Redis AccountId={} expiresIn={}s", accountId, expiresInSec);
    }

    // -------------------------------------------------------------------------
    // 内部方法
    // -------------------------------------------------------------------------

    private boolean isTokenExpiringSoon(String accountId) {
        String expiryStr = redisTemplate.opsForValue().get(expiryKey(accountId));
        if (expiryStr == null) return true;
        long expiryEpoch = Long.parseLong(expiryStr);
        long nowEpoch    = System.currentTimeMillis() / 1000;
        return (expiryEpoch - nowEpoch) <= props.getTokenRefreshThresholdSeconds();
    }

    private void refreshAccessToken(String accountId) {
        String refreshToken = redisTemplate.opsForValue().get(refreshKey(accountId));
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BrokerApiException(BROKER_NAME, 401,
                    "Schwab Refresh Token 不存在，请重新授权");
        }
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", refreshToken);

        SchwabTokenResponse resp = callTokenEndpoint(form);
        storeTokens(accountId, resp);
        log.info("Schwab Access Token 刷新成功 AccountId={}", accountId);
    }

    private void storeTokens(String accountId, SchwabTokenResponse resp) {
        storeTokens(accountId, resp.getAccessToken(), resp.getRefreshToken(), resp.getExpiresIn());
    }

    private SchwabTokenResponse callTokenEndpoint(MultiValueMap<String, String> form) {
        String credentials = props.getClientId() + ":" + props.getClientSecret();
        String basicAuth   = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        try {
            return tokenRestClient.post()
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + basicAuth)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(SchwabTokenResponse.class);
        } catch (RestClientException ex) {
            log.error("Schwab Token 端点调用失败: {}", ex.getMessage());
            throw new BrokerApiException(BROKER_NAME, 503,
                    "Schwab Token 端点不可用: " + ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Redis Key 生成
    // -------------------------------------------------------------------------

    private String accessKey(String accountId) {
        return props.getTokenCacheKeyPrefix() + ":" + accountId + SUFFIX_ACCESS;
    }

    private String refreshKey(String accountId) {
        return props.getTokenCacheKeyPrefix() + ":" + accountId + SUFFIX_REFRESH;
    }

    private String expiryKey(String accountId) {
        return props.getTokenCacheKeyPrefix() + ":" + accountId + SUFFIX_EXPIRY;
    }
}
