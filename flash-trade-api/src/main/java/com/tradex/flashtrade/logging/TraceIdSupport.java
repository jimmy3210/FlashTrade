package com.tradex.flashtrade.logging;

import java.util.UUID;
import java.util.regex.Pattern;

import org.slf4j.MDC;

import jakarta.servlet.http.HttpServletResponse;

public final class TraceIdSupport {

    public static final String HEADER_NAME = "X-Trace-Id";
    public static final String MDC_KEY = "traceId";

    private static final Pattern TRACE_ID_PATTERN = Pattern.compile("[A-Za-z0-9._-]{1,64}");

    private TraceIdSupport() {}

    public static String fromRequestHeader(String candidate) {
        if (candidate == null || !TRACE_ID_PATTERN.matcher(candidate).matches()) {
            return generateTraceId();
        }
        return candidate;
    }

    public static String ensureTraceId(HttpServletResponse response) {
        String traceId = MDC.get(MDC_KEY);
        if (traceId == null || !TRACE_ID_PATTERN.matcher(traceId).matches()) {
            traceId = generateTraceId();
        }
        response.setHeader(HEADER_NAME, traceId);
        return traceId;
    }

    private static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
