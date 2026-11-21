package com.tradex.flashtrade.dto.common;

import java.util.List;

public record ApiResponse<T>(String code, String message, T data) {

    public static <T> ApiResponse<T> success(T data) {
        return success("请求成功", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("SUCCESS", message, data);
    }

    public static ApiResponse<ErrorData> error(String code, String message, String traceId) {
        return error(code, message, List.of(), traceId);
    }

    public static ApiResponse<ErrorData> error(
            String code, String message, List<FieldError> fieldErrors, String traceId) {
        return new ApiResponse<>(code, message, new ErrorData(fieldErrors, traceId));
    }

    public record ErrorData(List<FieldError> fieldErrors, String traceId) {
        public ErrorData {
            fieldErrors = List.copyOf(fieldErrors);
        }
    }

    public record FieldError(String field, String code, String message) {}
}
