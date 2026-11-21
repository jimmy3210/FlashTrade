package com.tradex.flashtrade.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradex.flashtrade.dto.auth.LoginRequest;
import com.tradex.flashtrade.dto.auth.LoginResponse;
import com.tradex.flashtrade.dto.auth.LogoutRequest;
import com.tradex.flashtrade.dto.auth.TokenRefreshRequest;
import com.tradex.flashtrade.dto.auth.AuthSessionResponse;
import com.tradex.flashtrade.exception.BusinessException;
import com.tradex.flashtrade.exception.ErrorCode;
import com.tradex.flashtrade.exception.GlobalExceptionHandler;
import com.tradex.flashtrade.config.SecurityConfig;
import com.tradex.flashtrade.service.AuthService;
import com.tradex.flashtrade.security.ClientIpResolver;
import com.tradex.flashtrade.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    value = AuthController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class},
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {SecurityConfig.class, JwtAuthenticationFilter.class}
    )
)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean(name = "appAuthService")
    private AuthService authService;
    @MockitoBean
    private ClientIpResolver clientIpResolver;

    @Test
    @DisplayName("当前会话接口返回服务端认证角色")
    void currentSession_returnsAuthorities() {
        User principal = new User(
                "root-operator",
                "unused",
                java.util.List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")));

        var response = new AuthController(authService, clientIpResolver).currentSession(principal);

        AuthSessionResponse session = response.getBody().data();
        org.assertj.core.api.Assertions.assertThat(session.username()).isEqualTo("root-operator");
        org.assertj.core.api.Assertions.assertThat(session.roles())
                .containsExactly("ROLE_SUPER_ADMIN");
    }

    @Test
    @DisplayName("当前会话接口无认证主体时返回未认证业务异常")
    void currentSession_missingPrincipal_returnsUnauthorized() {
        org.junit.jupiter.api.Assertions.assertThrows(
                BusinessException.class,
                () -> new AuthController(authService, clientIpResolver).currentSession(null));
    }

    // ============================================================
    // POST /api/auth/login
    // ============================================================

    @Test
    @DisplayName("POST /api/auth/login - 登录成功返回 200 和 Token")
    void login_success() throws Exception {
        LoginRequest request = new LoginRequest("trader01", "password123");
        LoginResponse response = LoginResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .expiresIn(3600)
                .build();

        when(authService.login(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").value(3600));
    }

    @Test
    @DisplayName("POST /api/auth/login - 凭证错误返回 401")
    void login_invalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest("trader01", "wrong-password");
        when(authService.login(any(), any()))
                .thenThrow(new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.data.fieldErrors").isArray())
                .andExpect(jsonPath("$.data.traceId").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/auth/login - 请求体校验失败返回 400")
    void login_validationError() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.data.fieldErrors[0].code").value("REQUIRED"))
                .andExpect(jsonPath("$.data.traceId").isNotEmpty());
    }

    // ============================================================
    // POST /api/auth/refresh
    // ============================================================

    @Test
    @DisplayName("POST /api/auth/refresh - 刷新成功返回新 Access Token")
    void refresh_success() throws Exception {
        TokenRefreshRequest request = new TokenRefreshRequest("valid-refresh-token");
        LoginResponse response = LoginResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .tokenType("Bearer")
                .expiresIn(3600)
                .build();

        when(authService.refreshToken(anyString())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("new-refresh-token"));
    }

    @Test
    @DisplayName("POST /api/auth/refresh - Token 已过期返回 401")
    void refresh_expiredToken() throws Exception {
        TokenRefreshRequest request = new TokenRefreshRequest("expired-refresh-token");
        when(authService.refreshToken(anyString()))
                .thenThrow(new BusinessException(ErrorCode.REFRESH_TOKEN_EXPIRED));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("REFRESH_TOKEN_EXPIRED"));
    }

    // ============================================================
    // POST /api/auth/logout
    // ============================================================

    @Test
    @DisplayName("POST /api/auth/logout - 不带 Refresh Token 仍兼容旧客户端")
    void logout_withoutRefreshToken_success() throws Exception {
        doNothing().when(authService).logout(anyString(), isNull());

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));

        verify(authService).logout("valid-token", null);
    }

    @Test
    @DisplayName("POST /api/auth/logout - 将可选 Refresh Token 交给服务层校验并撤销")
    void logout_withRefreshToken_success() throws Exception {
        String refreshToken = "refresh-token";
        doNothing().when(authService).logout(anyString(), anyString());

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LogoutRequest(refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));

        verify(authService).logout("valid-token", refreshToken);
    }
}
