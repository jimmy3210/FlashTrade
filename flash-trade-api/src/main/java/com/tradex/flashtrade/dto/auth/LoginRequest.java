package com.tradex.flashtrade.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "用户名不可为空")
        @Size(max = 50, message = "用户名最多 50 个字符")
        String username,

        @NotBlank(message = "密码不可为空")
        @Size(min = 6, max = 100, message = "密码长度须在 6~100 字符之间")
        String password
) {}
