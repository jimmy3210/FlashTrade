package com.tradex.flashtrade.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一 API 响应包装类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "统一API响应")
public class ApiResponse<T> {
    
    @Schema(description = "响应代码", example = "0")
    private Integer code;
    
    @Schema(description = "响应消息", example = "Success")
    private String message;
    
    @Schema(description = "响应数据")
    private T data;
    
    @Schema(description = "错误详情")
    private String error;
    
    /**
     * 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .code(0)
            .message("Success")
            .data(data)
            .build();
    }
    
    /**
     * 成功响应（仅消息）
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
            .code(0)
            .message(message)
            .data(data)
            .build();
    }
    
    /**
     * 失败响应
     */
    public static <T> ApiResponse<T> fail(Integer code, String message) {
        return ApiResponse.<T>builder()
            .code(code)
            .message(message)
            .build();
    }
    
    /**
     * 失败响应（带错误详情）
     */
    public static <T> ApiResponse<T> fail(Integer code, String message, String error) {
        return ApiResponse.<T>builder()
            .code(code)
            .message(message)
            .error(error)
            .build();
    }
}
