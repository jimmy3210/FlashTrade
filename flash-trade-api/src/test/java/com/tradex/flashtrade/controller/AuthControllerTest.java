package com.tradex.flashtrade.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradex.flashtrade.dto.auth.LoginRequest;
import com.tradex.flashtrade.dto.auth.LoginResponse;
import com.tradex.flashtrade.dto.auth.TokenRefreshRequest;
import com.tradex.flashtrade.exception.BusinessException;
import com.tradex.flashtrade.exception.ErrorCode;
import com.tradex.flashtrade.exception.GlobalExceptionHandler;
import com.tradex.flashtrade.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    value = AuthController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

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
                .andExpect(jsonPath("$.success").value(true))
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
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("POST /api/auth/login - 请求体校验失败返回 400")
    void login_validationError() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
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
                .tokenType("Bearer")
                .expiresIn(3600)
                .build();

        when(authService.refreshToken(anyString())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));
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
                .andExpect(jsonPath("$.error.code").value("REFRESH_TOKEN_EXPIRED"));
    }

    // ============================================================
    // POST /api/auth/logout
    // ============================================================

    @Test
    @DisplayName("POST /api/auth/logout - 登出成功返回 200")
    void logout_success() throws Exception {
        doNothing().when(authService).logout(anyString());

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(authService).logout("valid-token");
    }
}
