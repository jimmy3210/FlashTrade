package com.tradex.flashtrade.service;

import com.tradex.flashtrade.exception.BusinessException;
import com.tradex.flashtrade.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 使用 Redis 管理登录尝试次数的限流服务。
 * <p>
 * Key: {@code login:fail:{ip}:{username}} — 失败次数计数器 (TTL=lockWindowSec)<br>
 * Key: {@code login:lock:{ip}:{username}} — 锁定标志 (TTL=lockDurationSec)
 * </p>
 */
@Service
public class LoginRateLimiterService {

    private static final Logger log = LoggerFactory.getLogger(LoginRateLimiterService.class);

    private static final String PREFIX_FAIL = "login:fail:";
    private static final String PREFIX_LOCK = "login:lock:";

    @Value("${app.rate-limit.login.max-attempts:5}")
    private int maxAttempts;

    @Value("${app.rate-limit.login.lock-window-seconds:300}")
    private long lockWindowSeconds;

    @Value("${app.rate-limit.login.lock-duration-seconds:900}")
    private long lockDurationSeconds;

    private final RedisTemplate<String, String> redisTemplate;

    public LoginRateLimiterService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 检查是否处于锁定状态，若已锁定则抛出 {@link BusinessException}。
     */
    public void checkLocked(String ip, String username) {
        String lockKey = lockKey(ip, username);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
            log.warn("Login locked: ip={} username={}", ip, username);
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS,
                    "登录尝试次数过多，请 " + (lockDurationSeconds / 60) + " 分钟后再试");
        }
    }

    /**
     * 记录登录失败次数，超过 maxAttempts 则锁定账户。
     */
    public void recordFailure(String ip, String username) {
        String failKey = failKey(ip, username);
        Long count = redisTemplate.opsForValue().increment(failKey);
        if (count != null && count == 1) {
            redisTemplate.expire(failKey, lockWindowSeconds, TimeUnit.SECONDS);
        }
        if (count != null && count >= maxAttempts) {
            String lockKey = lockKey(ip, username);
            redisTemplate.opsForValue().set(lockKey, "1", lockDurationSeconds, TimeUnit.SECONDS);
            log.warn("Login locked after {} failures: ip={} username={}", count, ip, username);
        }
    }

    /**
     * 登录成功时重置失败计数器。
     */
    public void resetOnSuccess(String ip, String username) {
        redisTemplate.delete(failKey(ip, username));
        redisTemplate.delete(lockKey(ip, username));
    }

    private String failKey(String ip, String username) {
        return PREFIX_FAIL + sanitize(ip) + ":" + sanitize(username);
    }

    private String lockKey(String ip, String username) {
        return PREFIX_LOCK + sanitize(ip) + ":" + sanitize(username);
    }

    private String sanitize(String value) {
        return value == null ? "unknown" : value.replaceAll("[^a-zA-Z0-9.\\-_@]", "_");
    }
}
