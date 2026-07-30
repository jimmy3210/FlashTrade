package com.tradex.flashtrade.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradex.flashtrade.dto.auth.LoginRequest;
import com.tradex.flashtrade.dto.auth.LoginResponse;
import com.tradex.flashtrade.dto.auth.TokenRefreshRequest;
import com.tradex.flashtrade.dto.common.ApiResponse;
import com.tradex.flashtrade.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "认证", description = "用户登录、Token 刷新与登出")
public class AuthController {

    private final AuthService authService;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户名+密码登录，成功返回 JWT Access Token 和 Refresh Token")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        String clientIp = resolveClientIp(httpRequest);
        try {
            LoginResponse resp = authService.login(request, clientIp);
            logger.info("Login success username={} ip={}", request.username(), clientIp);
            return ResponseEntity.ok(ApiResponse.success(resp));
        } catch (Exception ex) {
            String user = null;
            try { user = request.username(); } catch (Exception e) { /* ignore */ }
            logger.error("Login failed username={} ip={} error={}", user, clientIp, ex.toString(), ex);
            throw ex;
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新 Token", description = "使用 Refresh Token 换取新的 Access Token")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(
            @Valid @RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.refreshToken(request.refreshToken())));
    }

    @PostMapping("/logout")
    @Operation(
        summary = "登出",
        description = "将当前 Access Token 加入黑名单使其立即失效",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            authService.logout(bearerToken.substring(7));
        }
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /** 获取客户端真实 IP，优先读取 X-Forwarded-For / X-Real-IP 请求头 */
    private String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xff)) {
            return xff.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
