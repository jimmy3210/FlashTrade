package com.tradex.flashtrade.dto.auth;

public record WebAuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn) {
}
