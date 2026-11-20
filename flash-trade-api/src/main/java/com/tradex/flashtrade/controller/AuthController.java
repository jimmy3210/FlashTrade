package com.tradex.flashtrade.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CookieValue;

import com.tradex.flashtrade.dto.auth.LoginRequest;
import com.tradex.flashtrade.dto.auth.LoginResponse;
import com.tradex.flashtrade.dto.auth.AuthSessionResponse;
import com.tradex.flashtrade.dto.auth.LogoutRequest;
import com.tradex.flashtrade.dto.auth.TokenRefreshRequest;
import com.tradex.flashtrade.dto.auth.WebAuthResponse;
import com.tradex.flashtrade.dto.common.ApiResponse;
import com.tradex.flashtrade.exception.BusinessException;
import com.tradex.flashtrade.exception.ErrorCode;
import com.tradex.flashtrade.service.AuthService;
import com.tradex.flashtrade.security.ClientIpResolver;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import jakarta.validation.Valid;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "认证", description = "用户登录、Token 刷新与登出")
public class AuthController {

    private static final String WEB_REFRESH_COOKIE = "FLASH_TRADE_WEB_REFRESH";
    private static final String WEB_REFRESH_COOKIE_PATH = "/api/v1/auth/web";

    private final AuthService authService;
    private final ClientIpResolver clientIpResolver;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshTokenExpirationMs;

    @Value("${app.security.web-auth.refresh-cookie-secure:false}")
    private boolean refreshCookieSecure;

    public AuthController(
            @Qualifier("appAuthService") AuthService authService,
            ClientIpResolver clientIpResolver) {
        this.authService = authService;
        this.clientIpResolver = clientIpResolver;
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户名+密码登录，成功返回 JWT Access Token 和 Refresh Token")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        LoginResponse response = authService.login(request, clientIpResolver.resolve(httpRequest));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新 Token", description = "使用并撤销旧 Refresh Token，轮换返回新的 Access Token 和 Refresh Token")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(
            @Valid @RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.refreshToken(request.refreshToken())));
    }

    @GetMapping("/web/csrf")
    @Operation(summary = "获取管理端 CSRF Cookie")
    public ResponseEntity<ApiResponse<Void>> webCsrf(CsrfToken csrfToken) {
        csrfToken.getToken();
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/web/login")
    @Operation(summary = "管理端登录", description = "Refresh Token 仅通过 HttpOnly Cookie 返回")
    public ResponseEntity<ApiResponse<WebAuthResponse>> webLogin(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        LoginResponse tokens = authService.login(request, clientIpResolver.resolve(httpRequest));
        return webTokenResponse(tokens);
    }

    @PostMapping("/web/refresh")
    @Operation(summary = "管理端刷新会话", description = "从 HttpOnly Cookie 读取并轮换 Refresh Token")
    public ResponseEntity<ApiResponse<WebAuthResponse>> webRefresh(
            @CookieValue(name = WEB_REFRESH_COOKIE, required = false) String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }
        return webTokenResponse(authService.refreshToken(refreshToken));
    }

    @PostMapping("/web/logout")
    @Operation(summary = "管理端登出", description = "撤销当前 Access Token 和同主体 Refresh Token")
    public ResponseEntity<ApiResponse<Void>> webLogout(
            @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @CookieValue(name = WEB_REFRESH_COOKIE, required = false) String refreshToken) {
        String accessToken = StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")
                ? bearerToken.substring(7)
                : null;
        authService.logout(accessToken, refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie("", Duration.ZERO).toString())
                .body(ApiResponse.success(null));
    }

    private ResponseEntity<ApiResponse<WebAuthResponse>> webTokenResponse(LoginResponse tokens) {
        WebAuthResponse response = new WebAuthResponse(
                tokens.getAccessToken(),
                tokens.getTokenType(),
                tokens.getExpiresIn());
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshCookie(
                                tokens.getRefreshToken(),
                                Duration.ofMillis(refreshTokenExpirationMs)).toString())
                .body(ApiResponse.success(response));
    }

    private ResponseCookie refreshCookie(String value, Duration maxAge) {
        return ResponseCookie.from(WEB_REFRESH_COOKIE, value)
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite("Strict")
                .path(WEB_REFRESH_COOKIE_PATH)
                .maxAge(maxAge)
                .build();
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前会话身份", description = "返回当前已认证用户的角色，用于管理端界面权限展示")
    public ResponseEntity<ApiResponse<AuthSessionResponse>> currentSession(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .sorted()
                .toList();
        return ResponseEntity.ok(ApiResponse.success(
                new AuthSessionResponse(userDetails.getUsername(), roles)));
    }

    @PostMapping("/logout")
    @Operation(
        summary = "登出",
        description = "将当前 Access Token 及（可选）属于同一用户的 Refresh Token 加入黑名单",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @RequestBody(required = false) LogoutRequest request) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            authService.logout(
                    bearerToken.substring(7),
                    request == null ? null : request.refreshToken());
        }
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
