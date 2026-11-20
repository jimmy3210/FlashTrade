package com.tradex.flashtrade.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 生成 / 验证组件
 * <p>
 * 通过 {@code @RefreshScope}，执行 {@code POST /actuator/refresh} 时
 * {@code app.jwt.secret} 等配置可被动态重载。
 * </p>
 */
@Component
@RefreshScope
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret == null ? new byte[0] : jwtSecret.getBytes(StandardCharsets.UTF_8);
        // Ensure at least 256 bits (32 bytes) for HMAC-SHA algorithms as required by RFC 7518
        if (keyBytes.length < 32) {
            logger.warn("JWT secret is shorter than 32 bytes ({} bytes). Deriving a 256-bit key using SHA-256.", keyBytes.length);
            try {
                MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                keyBytes = sha256.digest(keyBytes);
            } catch (NoSuchAlgorithmException e) {
                // Should never happen; fallback to original bytes which will produce a clear error from the JWT library
                logger.error("SHA-256 MessageDigest not available, using raw JWT secret bytes (may be insecure)", e);
            }
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(String username) {
        return buildToken(username, jwtExpirationMs);
    }

    public String generateRefreshToken(String username) {
        return buildToken(username, refreshExpirationMs);
    }

    private String buildToken(String subject, long expirationMs) {
        Date now = new Date();
        return Jwts.builder()
                .subject(subject)
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getExpirationMs() {
        return jwtExpirationMs;
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
