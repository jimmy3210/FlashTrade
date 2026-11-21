package com.tradex.flashtrade.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestTracingFilterTest {

    private final RequestTracingFilter filter = new RequestTracingFilter();

    @Test
    void acceptsSafeTraceIdAndReturnsItInResponseHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Trace-Id", "trace-id_123.abc");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) ->
                assertEquals("trace-id_123.abc", MDC.get("traceId")));

        assertEquals("trace-id_123.abc", response.getHeader("X-Trace-Id"));
        assertNull(MDC.get("traceId"));
    }

    @Test
    void replacesInvalidTraceIdWithGeneratedSafeValue() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Trace-Id", "trace-id\r\nInjected: value");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        String traceId = response.getHeader("X-Trace-Id");
        assertFalse(traceId.contains("\r"));
        assertFalse(traceId.contains("\n"));
        assertTrue(traceId.matches("[0-9a-f]{16}"));
        assertNull(MDC.get("traceId"));
    }
}
