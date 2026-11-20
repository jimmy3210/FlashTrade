package com.tradex.flashtrade.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradex.flashtrade.config.SecurityConfig;
import com.tradex.flashtrade.entity.Role;
import com.tradex.flashtrade.entity.User;
import com.tradex.flashtrade.exception.GlobalExceptionHandler;
import com.tradex.flashtrade.repository.UserRepository;
import com.tradex.flashtrade.security.ClientIpResolver;
import com.tradex.flashtrade.security.JwtAuthenticationFilter;
import com.tradex.flashtrade.security.JwtTokenProvider;
import com.tradex.flashtrade.security.UserDetailsServiceImpl;
import com.tradex.flashtrade.service.AuthService;
import com.tradex.flashtrade.service.LoginRateLimiterService;
import com.tradex.flashtrade.service.TokenBlacklistService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import jakarta.servlet.http.Cookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({
        GlobalExceptionHandler.class,
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        UserDetailsServiceImpl.class,
        AuthService.class,
        TokenBlacklistService.class,
        AuthSessionIntegrationTest.JwtTokenProviderTestConfiguration.class
})
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "app.jwt.secret=flashtrade-auth-integration-test-secret-32-bytes",
        "app.jwt.expiration-ms=3600000",
        "app.jwt.refresh-expiration-ms=604800000",
        "app.security.web-auth.refresh-cookie-secure=true"
})
class AuthSessionIntegrationTest {

    private static final String LOGIN_PATH = "/api/v1/auth/login";
    private static final String REFRESH_PATH = "/api/v1/auth/refresh";
    private static final String LOGOUT_PATH = "/api/v1/auth/logout";
    private static final String WEB_CSRF_PATH = "/api/v1/auth/web/csrf";
    private static final String WEB_LOGIN_PATH = "/api/v1/auth/web/login";
    private static final String WEB_REFRESH_PATH = "/api/v1/auth/web/refresh";
    private static final String WEB_LOGOUT_PATH = "/api/v1/auth/web/logout";
    private static final String REFRESH_COOKIE = "FLASH_TRADE_WEB_REFRESH";
    private static final String CSRF_COOKIE = "XSRF-TOKEN";
    private static final String USERNAME = "session-test-user";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private ClientIpResolver clientIpResolver;

    @MockitoBean
    private LoginRateLimiterService loginRateLimiter;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RedisTemplate<String, String> redisTemplate;

    @MockitoBean
    private ValueOperations<String, String> valueOperations;

    private final Set<String> redisKeys = ConcurrentHashMap.newKeySet();

    @BeforeEach
    void setUp() {
        redisKeys.clear();

        User user = new User();
        user.setId(100L);
        user.setUsername(USERNAME);
        user.setPasswordHash("test-password-hash");
        user.setRole(Role.ROLE_TRADER);
        user.setEnabled(true);
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken(
                        USERNAME,
                        null,
                        List.of(new SimpleGrantedAuthority(Role.ROLE_TRADER.name()))));
        when(clientIpResolver.resolve(any(HttpServletRequest.class))).thenReturn("192.0.2.10");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(
                anyString(), eq("1"), anyLong(), eq(TimeUnit.SECONDS)))
                .thenReturn(Boolean.TRUE);
        when(redisTemplate.hasKey(anyString()))
                .thenAnswer(invocation -> redisKeys.contains(invocation.getArgument(0)));
        when(redisTemplate.delete(anyString())).thenAnswer(invocation -> {
            redisKeys.remove(invocation.getArgument(0));
            return Boolean.TRUE;
        });
        doAnswer(invocation -> {
            redisKeys.add(invocation.getArgument(0));
            return null;
        }).when(valueOperations).set(
                anyString(), eq("1"), anyLong(), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    void loginRefreshRejectsOldRefreshTokenAndLogoutRevokesSession() throws Exception {
        mockMvc.perform(post(LOGOUT_PATH))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        MvcResult loginResult = mockMvc.perform(post(LOGIN_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"session-test-user","password":"test-password"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andReturn();

        String accessToken = tokenFrom(loginResult, "accessToken");
        String refreshToken = tokenFrom(loginResult, "refreshToken");
        assertThat(jwtTokenProvider.validateToken(accessToken)).isTrue();
        assertThat(jwtTokenProvider.validateToken(refreshToken)).isTrue();
        assertThat(jwtTokenProvider.getUsernameFromToken(refreshToken)).isEqualTo(USERNAME);

        MvcResult refreshResult = mockMvc.perform(post(REFRESH_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                java.util.Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andReturn();

        String rotatedAccessToken = tokenFrom(refreshResult, "accessToken");
        String rotatedRefreshToken = tokenFrom(refreshResult, "refreshToken");
        assertThat(rotatedAccessToken).isNotEqualTo(accessToken);
        assertThat(rotatedRefreshToken).isNotEqualTo(refreshToken);
        assertThat(tokenBlacklistService.isBlacklisted(refreshToken)).isTrue();

        mockMvc.perform(post(REFRESH_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                java.util.Map.of("refreshToken", refreshToken))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("TOKEN_INVALID"));

        mockMvc.perform(post(LOGOUT_PATH)
                        .header("Authorization", "Bearer " + rotatedAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                java.util.Map.of("refreshToken", rotatedRefreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));

        assertThat(tokenBlacklistService.isBlacklisted(rotatedAccessToken)).isTrue();
        assertThat(tokenBlacklistService.isBlacklisted(rotatedRefreshToken)).isTrue();
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + rotatedAccessToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void refreshRejectsExpiredRefreshToken() throws Exception {
        String expiredRefreshToken = Jwts.builder()
                .subject(USERNAME)
                .issuedAt(new Date(System.currentTimeMillis() - 10_000))
                .expiration(new Date(System.currentTimeMillis() - 1_000))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();

        mockMvc.perform(post(REFRESH_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                java.util.Map.of("refreshToken", expiredRefreshToken))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("REFRESH_TOKEN_EXPIRED"));
    }

    @Test
    void webSessionUsesHttpOnlyRefreshCookieAndRequiresCsrf() throws Exception {
        MvcResult csrfResult = mockMvc.perform(get(WEB_CSRF_PATH))
                .andExpect(status().isOk())
                .andReturn();
        Cookie csrfResponseCookie = csrfResult.getResponse().getCookie(CSRF_COOKIE);
        assertThat(csrfResponseCookie).isNotNull();
        assertThat(csrfResponseCookie.getSecure()).isTrue();
        assertThat(csrfResponseCookie.getAttribute("SameSite")).isEqualTo("Strict");
        String csrfToken = cookieValue(csrfResult, CSRF_COOKIE);
        Cookie csrfCookie = new Cookie(CSRF_COOKIE, csrfToken);

        mockMvc.perform(post(WEB_LOGIN_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"session-test-user","password":"test-password"}
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(post(WEB_LOGIN_PATH)
                        .cookie(csrfCookie)
                        .header("X-XSRF-TOKEN", csrfToken + "-invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"session-test-user","password":"test-password"}
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(post(WEB_REFRESH_PATH)
                        .cookie(csrfCookie)
                        .header("X-XSRF-TOKEN", csrfToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("REFRESH_TOKEN_EXPIRED"));

        MvcResult loginResult = mockMvc.perform(post(WEB_LOGIN_PATH)
                        .cookie(csrfCookie)
                        .header("X-XSRF-TOKEN", csrfToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"session-test-user","password":"test-password"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.refreshToken").doesNotExist())
                .andReturn();

        String accessToken = tokenFrom(loginResult, "accessToken");
        String refreshToken = cookieValue(loginResult, REFRESH_COOKIE);
        String refreshSetCookie = setCookieHeader(loginResult, REFRESH_COOKIE);
        assertThat(jwtTokenProvider.validateToken(refreshToken)).isTrue();
        assertThat(refreshSetCookie)
                .contains("Secure", "HttpOnly", "SameSite=Strict", "Path=/api/v1/auth/web");

        MvcResult refreshResult = mockMvc.perform(post(WEB_REFRESH_PATH)
                        .cookie(csrfCookie, new Cookie(REFRESH_COOKIE, refreshToken))
                        .header("X-XSRF-TOKEN", csrfToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.refreshToken").doesNotExist())
                .andReturn();

        String rotatedAccessToken = tokenFrom(refreshResult, "accessToken");
        String rotatedRefreshToken = cookieValue(refreshResult, REFRESH_COOKIE);
        assertThat(rotatedRefreshToken).isNotEqualTo(refreshToken);
        assertThat(tokenBlacklistService.isBlacklisted(refreshToken)).isTrue();

        mockMvc.perform(post(WEB_REFRESH_PATH)
                        .cookie(csrfCookie, new Cookie(REFRESH_COOKIE, refreshToken))
                        .header("X-XSRF-TOKEN", csrfToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("TOKEN_INVALID"));

        MvcResult logoutResult = mockMvc.perform(post(WEB_LOGOUT_PATH)
                        .cookie(csrfCookie, new Cookie(REFRESH_COOKIE, rotatedRefreshToken))
                        .header("X-XSRF-TOKEN", csrfToken)
                        .header("Authorization", "Bearer " + rotatedAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andReturn();

        assertThat(tokenBlacklistService.isBlacklisted(rotatedAccessToken)).isTrue();
        assertThat(tokenBlacklistService.isBlacklisted(rotatedRefreshToken)).isTrue();
        assertThat(setCookieHeader(logoutResult, REFRESH_COOKIE)).contains("Max-Age=0");
    }

    private String tokenFrom(MvcResult result, String tokenName) throws Exception {
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.path("data").path(tokenName).asText();
    }

    private String cookieValue(MvcResult result, String cookieName) {
        String cookie = setCookieHeader(result, cookieName);
        return cookie.substring(cookieName.length() + 1, cookie.indexOf(';') < 0
                ? cookie.length()
                : cookie.indexOf(';'));
    }

    private String setCookieHeader(MvcResult result, String cookieName) {
        return result.getResponse().getHeaders(HttpHeaders.SET_COOKIE).stream()
                .filter(cookie -> cookie.startsWith(cookieName + "="))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing Set-Cookie for " + cookieName));
    }

    @TestConfiguration
    static class JwtTokenProviderTestConfiguration {

        @Bean
        TestJwtTokenProvider jwtTokenProvider(
                @Value("${app.jwt.secret}") String jwtSecret,
                @Value("${app.jwt.expiration-ms}") long jwtExpirationMs,
                @Value("${app.jwt.refresh-expiration-ms}") long refreshExpirationMs) {
            return new TestJwtTokenProvider(jwtSecret, jwtExpirationMs, refreshExpirationMs);
        }
    }

    private static class TestJwtTokenProvider extends JwtTokenProvider {

        TestJwtTokenProvider(
                String jwtSecret,
                long jwtExpirationMs,
                long refreshExpirationMs) {
            ReflectionTestUtils.setField(this, "jwtSecret", jwtSecret);
            ReflectionTestUtils.setField(this, "jwtExpirationMs", jwtExpirationMs);
            ReflectionTestUtils.setField(this, "refreshExpirationMs", refreshExpirationMs);
        }
    }
}
