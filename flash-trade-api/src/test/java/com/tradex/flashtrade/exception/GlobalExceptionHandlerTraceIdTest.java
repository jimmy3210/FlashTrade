package com.tradex.flashtrade.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.tradex.flashtrade.dto.common.ApiResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletResponse;

class GlobalExceptionHandlerTraceIdTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void businessErrorUsesSameTraceIdInHeaderAndResponseBody() throws Exception {
        MDC.put("traceId", "request-trace-42");
        MockHttpServletResponse response = new MockHttpServletResponse();

        ApiResponse<ApiResponse.ErrorData> body = exceptionHandler
                .handleBusiness(new BusinessException(ErrorCode.INVALID_CREDENTIALS), response)
                .getBody();

        assertThat(response.getHeader("X-Trace-Id")).isEqualTo("request-trace-42");
        assertThat(body.code()).isEqualTo(ErrorCode.INVALID_CREDENTIALS.getCode());
        assertThat(body.data().traceId())
                .isEqualTo(response.getHeader("X-Trace-Id"));
    }

    @Test
    void businessErrorCreatesTraceIdWhenMdcIsMissing() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        ApiResponse<ApiResponse.ErrorData> body = exceptionHandler
                .handleBusiness(new BusinessException(ErrorCode.INVALID_CREDENTIALS), response)
                .getBody();

        String responseTraceId = response.getHeader("X-Trace-Id");
        assertThat(responseTraceId).matches("[0-9a-f]{16}");
        assertThat(body.data().traceId()).isEqualTo(responseTraceId);
    }
}
