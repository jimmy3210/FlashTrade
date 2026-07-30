package com.tradex.flashtrade.logging;

import java.util.regex.Pattern;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.CompositeConverter;

/**
 * Logback 自定义转换器。
 * 将日志消息中的敏感字段（password / token / secret / apiKey 等）替换为 [MASKED]。
 */
public class MaskingConverter extends CompositeConverter<ILoggingEvent> {

    private static final Pattern SENSITIVE_PATTERN = Pattern.compile(
            "(?i)(\"?(password|passwd|secret|apiKey|api_key|token|access_token|refresh_token|authorization)\"?\\s*[:=]\\s*)" +
            "(\"[^\"]*\"|\\S+)",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    protected String transform(ILoggingEvent event, String in) {
        String message = in != null ? in : event.getFormattedMessage();
        return SENSITIVE_PATTERN.matcher(message).replaceAll("$1[MASKED]");
    }
}
