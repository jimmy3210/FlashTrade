package com.tradex.flashtrade.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record TokenRefreshRequest(
        @NotBlank(message = "Refresh Token 不可为空")
        String refreshToken
) {}
