package com.tradex.flashtrade.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 每个请求设置唯一 {@code traceId} 到 MDC 的过滤器。
 * <p>
 * 仅接受符合白名单的 {@code X-Trace-Id} 请求头，否则自动生成 UUID。
 * 同时将 traceId 写入响应头 {@code X-Trace-Id}。
 * </p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 50)
public class RequestTracingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String traceId = TraceIdSupport.fromRequestHeader(
                request.getHeader(TraceIdSupport.HEADER_NAME));
        MDC.put(TraceIdSupport.MDC_KEY, traceId);
        response.setHeader(TraceIdSupport.HEADER_NAME, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TraceIdSupport.MDC_KEY);
        }
    }
}
