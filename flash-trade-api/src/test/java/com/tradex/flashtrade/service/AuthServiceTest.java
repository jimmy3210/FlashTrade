package com.tradex.flashtrade.service;

import com.tradex.flashtrade.dto.auth.LoginRequest;
import com.tradex.flashtrade.dto.auth.LoginResponse;
import com.tradex.flashtrade.entity.Role;
import com.tradex.flashtrade.entity.User;
import com.tradex.flashtrade.exception.BusinessException;
import com.tradex.flashtrade.exception.ErrorCode;
import com.tradex.flashtrade.repository.UserRepository;
import com.tradex.flashtrade.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager  authenticationManager;
    @Mock private JwtTokenProvider        jwtTokenProvider;
    @Mock private TokenBlacklistService   tokenBlacklistService;
    @Mock private UserRepository          userRepository;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("trader01");
        testUser.setPasswordHash("$2a$10$hashed");
        testUser.setRole(Role.ROLE_TRADER);
        testUser.setEnabled(true);
    }

    // ============================================================
    // login
    // ============================================================

    @Test
    @DisplayName("登录成功：返回 Access Token + Refresh Token")
    void login_success() {
        LoginRequest request = new LoginRequest("trader01", "password123");

        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken("trader01", null));
        when(jwtTokenProvider.generateAccessToken("trader01")).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken("trader01")).thenReturn("refresh-token");
        when(jwtTokenProvider.getExpirationMs()).thenReturn(3_600_000L);

        LoginResponse response = authService.login(request, "127.0.0.1");

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(3600L);
    }

    @Test
    @DisplayName("登录失败：凭证错误时抛出 INVALID_CREDENTIALS")
    void login_invalidCredentials() {
        LoginRequest request = new LoginRequest("trader01", "wrong-pw");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request, "127.0.0.1"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e ->
                    assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_CREDENTIALS));
    }

    // ============================================================
    // refreshToken
    // ============================================================

    @Test
    @DisplayName("刷新 Token 成功：返回新的 Access Token")
    void refreshToken_success() {
        String rt = "valid-refresh-token";
        when(jwtTokenProvider.validateToken(rt)).thenReturn(true);
        when(tokenBlacklistService.isBlacklisted(rt)).thenReturn(false);
        when(jwtTokenProvider.getUsernameFromToken(rt)).thenReturn("trader01");
        when(userRepository.findByUsername("trader01")).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateAccessToken("trader01")).thenReturn("new-access-token");
        when(jwtTokenProvider.getExpirationMs()).thenReturn(3_600_000L);

        LoginResponse response = authService.refreshToken(rt);

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isNull();
    }

    @Test
    @DisplayName("刷新 Token 失败：Token 验证失败时抛出 REFRESH_TOKEN_EXPIRED")
    void refreshToken_invalidToken() {
        when(jwtTokenProvider.validateToken("bad-token")).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken("bad-token"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e ->
                    assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.REFRESH_TOKEN_EXPIRED));
    }

    @Test
    @DisplayName("刷新 Token 失败：Token 已被吊销时抛出 TOKEN_INVALID")
    void refreshToken_blacklisted() {
        String rt = "revoked-token";
        when(jwtTokenProvider.validateToken(rt)).thenReturn(true);
        when(tokenBlacklistService.isBlacklisted(rt)).thenReturn(true);

        assertThatThrownBy(() -> authService.refreshToken(rt))
                .isInstanceOf(BusinessException.class)
                .satisfies(e ->
                    assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.TOKEN_INVALID));
    }

    // ============================================================
    // logout
    // ============================================================

    @Test
    @DisplayName("登出：有效 Token 被加入 Redis 黑名单")
    void logout_validToken_blacklisted() {
        String token = "valid-access-token";
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getExpirationMs()).thenReturn(3_600_000L);

        authService.logout(token);

        verify(tokenBlacklistService).blacklist(token, 3_600_000L);
    }

    @Test
    @DisplayName("登出：无效 Token 不加入黑名单")
    void logout_invalidToken_noOp() {
        when(jwtTokenProvider.validateToken("invalid")).thenReturn(false);

        authService.logout("invalid");

        verifyNoInteractions(tokenBlacklistService);
    }
}
