package com.tradex.flashtrade.service;

import com.tradex.flashtrade.dto.auth.LoginRequest;
import com.tradex.flashtrade.dto.auth.LoginResponse;
import com.tradex.flashtrade.entity.User;
import com.tradex.flashtrade.exception.BusinessException;
import com.tradex.flashtrade.exception.ErrorCode;
import com.tradex.flashtrade.repository.UserRepository;
import com.tradex.flashtrade.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    /** Refresh Token 并发刷新防重放分布式锁的 TTL（秒） */
    private static final long REFRESH_LOCK_TTL_SECONDS = 3;
    private static final String REFRESH_LOCK_PREFIX = "auth:refresh:lock:";

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final LoginRateLimiterService loginRateLimiter;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider,
                       TokenBlacklistService tokenBlacklistService,
                       LoginRateLimiterService loginRateLimiter,
                       UserRepository userRepository,
                       RedisTemplate<String, String> redisTemplate) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider      = jwtTokenProvider;
        this.tokenBlacklistService = tokenBlacklistService;
        this.loginRateLimiter      = loginRateLimiter;
        this.userRepository        = userRepository;
        this.redisTemplate         = redisTemplate;
    }

    public LoginResponse login(LoginRequest request, String clientIp) {
        String username = request.username();
        String passwordLength = request.password() == null ? "null" : String.valueOf(request.password().length());

        log.debug("Login attempt received username={} ip={} passwordLength={}", username, clientIp, passwordLength);

        loginRateLimiter.checkLocked(clientIp, username);
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.password())
            );
        } catch (AuthenticationException e) {
            loginRateLimiter.recordFailure(clientIp, username);
            log.warn("Login authentication failed username={} ip={} reason={} exception={}",
                    username, clientIp, e.getClass().getSimpleName(), e.getMessage());
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        loginRateLimiter.resetOnSuccess(clientIp, username);
        log.info("Login success username={} ip={}", username, clientIp);

        String accessToken  = jwtTokenProvider.generateAccessToken(request.username());
        String refreshToken = jwtTokenProvider.generateRefreshToken(request.username());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationMs() / 1000)
                .build();
    }

    public LoginResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }
        if (tokenBlacklistService.isBlacklisted(refreshToken)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        // Redis SETNX 分布式锁 — 防止同一 Refresh Token 被并发刷新
        String lockKey = REFRESH_LOCK_PREFIX + refreshToken.hashCode();
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", REFRESH_LOCK_TTL_SECONDS, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(locked)) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, "Token 刷新操作正在进行中，请稍后重试");
        }

        try {
            String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
            userRepository.findByUsername(username)
                    .filter(User::isEnabled)
                    .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

            // 将旧 Refresh Token 立即列入黑名单废除
            tokenBlacklistService.blacklist(refreshToken, jwtTokenProvider.getRefreshExpirationMs());

            String newAccessToken = jwtTokenProvider.generateAccessToken(username);
            return LoginResponse.builder()
                    .accessToken(newAccessToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenProvider.getExpirationMs() / 1000)
                    .build();
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    public void logout(String token) {
        if (jwtTokenProvider.validateToken(token)) {
            tokenBlacklistService.blacklist(token, jwtTokenProvider.getExpirationMs());
        }
    }
}
