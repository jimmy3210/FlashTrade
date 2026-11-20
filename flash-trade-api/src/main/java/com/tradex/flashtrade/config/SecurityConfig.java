package com.tradex.flashtrade.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradex.flashtrade.dto.common.ApiResponse;
import com.tradex.flashtrade.exception.ErrorCode;
import com.tradex.flashtrade.logging.TraceIdSupport;
import com.tradex.flashtrade.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Set;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final Set<String> WEB_AUTH_CSRF_PATHS = Set.of(
            "/api/v1/auth/web/login",
            "/api/v1/auth/web/refresh",
            "/api/v1/auth/web/logout");

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private List<String> allowedOrigins;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, ObjectMapper objectMapper) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            CookieCsrfTokenRepository cookieCsrfTokenRepository) throws Exception {
        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(cookieCsrfTokenRepository)
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                .requireCsrfProtectionMatcher(this::requiresWebAuthCsrf))
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/refresh")
                    .permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/auth/web/csrf")
                    .permitAll()
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/v1/auth/web/login",
                    "/api/v1/auth/web/refresh")
                    .permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/web/logout")
                    .authenticated()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/logout")
                    .authenticated()
                .requestMatchers(HttpMethod.GET, "/api/v1/auth/me")
                    .authenticated()
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/api-docs/**",
                    "/actuator/health",
                    "/actuator/info"
                ).permitAll()
                .requestMatchers("/actuator/refresh", "/actuator/bus-refresh")
                    .hasRole("SUPER_ADMIN")
                .requestMatchers("/api/v1/admin/companies", "/api/v1/admin/companies/**")
                    .hasAnyRole("ADMIN", "COMPANY_ADMIN", "SUPER_ADMIN")
                .requestMatchers(
                    HttpMethod.GET,
                    "/users",
                    "/users/**",
                    "/admin/broker-accounts",
                    "/admin/broker-accounts/**")
                    .hasAnyRole("ADMIN", "COMPANY_ADMIN", "SUPER_ADMIN")
                .requestMatchers("/api/v1/admin/**")
                    .hasRole("SUPER_ADMIN")
                .requestMatchers(
                    "/users",
                    "/users/**",
                    "/admin",
                    "/admin/**",
                    "/positions",
                    "/positions/**")
                    .hasRole("SUPER_ADMIN")
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, e) ->
                        writeSecurityError(response, ErrorCode.UNAUTHORIZED))
                .accessDeniedHandler((request, response, e) ->
                        writeSecurityError(response, ErrorCode.ACCESS_DENIED))
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CookieCsrfTokenRepository cookieCsrfTokenRepository(
            @Value("${app.security.web-auth.refresh-cookie-secure:false}") boolean cookieSecure) {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookiePath("/");
        repository.setCookieCustomizer(cookie -> cookie
                .secure(cookieSecure)
                .sameSite("Strict"));
        return repository;
    }

    private boolean requiresWebAuthCsrf(jakarta.servlet.http.HttpServletRequest request) {
        return "POST".equals(request.getMethod())
                && WEB_AUTH_CSRF_PATHS.contains(request.getRequestURI());
    }

    void writeSecurityError(HttpServletResponse response, ErrorCode errorCode) throws java.io.IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(errorCode.getHttpStatus().value());
        objectMapper.writeValue(
                response.getOutputStream(),
                ApiResponse.error(
                        errorCode.getCode(),
                        errorCode.getMessage(),
                        TraceIdSupport.ensureTraceId(response)));
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Trace-Id",
                "X-Idempotency-Key",
                "X-XSRF-TOKEN"));
        config.setExposedHeaders(List.of("X-Trace-Id"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
