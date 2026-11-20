package com.tradex.flashtrade.exception;

import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.tradex.flashtrade.dto.common.ApiResponse;
import com.tradex.flashtrade.logging.TraceIdSupport;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<ApiResponse.ErrorData>> handleBusiness(
            BusinessException ex, HttpServletResponse response) {
        return ResponseEntity
                .status(ex.getErrorCode().getHttpStatus())
                .body(ApiResponse.error(
                        ex.getErrorCode().getCode(), ex.getMessage(), currentTraceId(response)));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<ApiResponse.ErrorData>> handleAccessDenied(
            AccessDeniedException ex, HttpServletResponse response) {
        return ResponseEntity
                .status(ErrorCode.ACCESS_DENIED.getHttpStatus())
                .body(ApiResponse.error(
                        ErrorCode.ACCESS_DENIED.getCode(),
                        ErrorCode.ACCESS_DENIED.getMessage(),
                        currentTraceId(response)));
    }

    @ExceptionHandler(BrokerApiException.class)
    public ResponseEntity<ApiResponse<ApiResponse.ErrorData>> handleBrokerApi(
            BrokerApiException ex, HttpServletResponse response) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .header("Retry-After", "30")
                .body(ApiResponse.error(
                        ErrorCode.BROKER_UNAVAILABLE.getCode(),
                        "券商 [" + ex.getBrokerName() + "] 服务暂时不可用，请稍后重试",
                        currentTraceId(response)));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ApiResponse.ErrorData>> handleValidation(
            MethodArgumentNotValidException ex, HttpServletResponse response) {
        List<ApiResponse.FieldError> fieldErrors = ex.getBindingResult().getAllErrors().stream()
                .sorted((left, right) -> {
                    String leftField = left instanceof org.springframework.validation.FieldError fieldError
                            ? fieldError.getField() : "_global";
                    String rightField = right instanceof org.springframework.validation.FieldError fieldError
                            ? fieldError.getField() : "_global";
                    int fieldComparison = leftField.compareTo(rightField);
                    if (fieldComparison != 0) {
                        return fieldComparison;
                    }
                    int leftPriority = "NotBlank".equals(left.getCode()) ? 0 : 1;
                    int rightPriority = "NotBlank".equals(right.getCode()) ? 0 : 1;
                    return Integer.compare(leftPriority, rightPriority);
                })
                .map(error -> new ApiResponse.FieldError(
                        error instanceof org.springframework.validation.FieldError fieldError
                                ? fieldError.getField()
                                : "_global",
                        validationCode(error.getCode()),
                        error.getDefaultMessage() == null
                                ? "无效的字段值"
                                : error.getDefaultMessage()))
                .toList();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        ErrorCode.VALIDATION_ERROR.getCode(),
                        ErrorCode.VALIDATION_ERROR.getMessage(),
                        fieldErrors,
                        currentTraceId(response)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ApiResponse.ErrorData>> handleGeneral(
            Exception ex, HttpServletResponse response) {
        log.error("Unhandled exception", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        "INTERNAL_ERROR", "服务器内部错误，请稍后重试", currentTraceId(response)));
    }

    private String validationCode(String constraintCode) {
        if (constraintCode == null) {
            return "INVALID_VALUE";
        }
        return switch (constraintCode) {
            case "NotNull", "NotBlank", "NotEmpty" -> "REQUIRED";
            case "Email", "Pattern", "URL", "Url" -> "INVALID_FORMAT";
            case "Min", "Max", "DecimalMin", "DecimalMax", "Positive", "PositiveOrZero",
                    "Negative", "NegativeOrZero", "Size" -> "OUT_OF_RANGE";
            default -> "INVALID_VALUE";
        };
    }

    private String currentTraceId(HttpServletResponse response) {
        return TraceIdSupport.ensureTraceId(response);
    }
}
