package com.tradex.flashtrade.dto.common;

import java.time.Instant;

public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final ErrorBody error;
    private final Instant timestamp;

    private ApiResponse(boolean success, T data, ErrorBody error) {
        this.success = success;
        this.data = data;
        this.error = error;
        this.timestamp = Instant.now();
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, null, new ErrorBody(code, message, null));
    }

    public static <T> ApiResponse<T> error(String code, String message, String traceId) {
        return new ApiResponse<>(false, null, new ErrorBody(code, message, traceId));
    }

    public boolean isSuccess() { return success; }
    public T getData() { return data; }
    public ErrorBody getError() { return error; }
    public Instant getTimestamp() { return timestamp; }

    public record ErrorBody(String code, String message, String traceId) {}
}
