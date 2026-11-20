package com.tradex.flashtrade.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradex.flashtrade.exception.ErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletResponse;

class SecurityConfigTraceIdTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SecurityConfig securityConfig = new SecurityConfig(null, objectMapper);

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @ParameterizedTest
    @EnumSource(value = ErrorCode.class, names = {"UNAUTHORIZED", "ACCESS_DENIED"})
    void securityErrorsReturnTheSameTraceIdInHeaderAndBody(ErrorCode errorCode) throws Exception {
        MDC.put("traceId", "safe-trace-id_123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        securityConfig.writeSecurityError(response, errorCode);

        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertThat(response.getStatus()).isEqualTo(errorCode.getHttpStatus().value());
        assertThat(body.path("code").asText()).isEqualTo(errorCode.getCode());
        assertThat(response.getHeader("X-Trace-Id")).isEqualTo("safe-trace-id_123");
        assertThat(body.path("data").path("traceId").asText())
                .isEqualTo(response.getHeader("X-Trace-Id"));
    }

    @ParameterizedTest
    @EnumSource(value = ErrorCode.class, names = {"UNAUTHORIZED", "ACCESS_DENIED"})
    void securityErrorsGenerateTraceIdWhenMdcIsMissing(ErrorCode errorCode) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        securityConfig.writeSecurityError(response, errorCode);

        JsonNode body = objectMapper.readTree(response.getContentAsString());
        String responseTraceId = response.getHeader("X-Trace-Id");
        assertThat(responseTraceId).matches("[0-9a-f]{16}");
        assertThat(body.path("data").path("traceId").asText()).isEqualTo(responseTraceId);
    }
}
