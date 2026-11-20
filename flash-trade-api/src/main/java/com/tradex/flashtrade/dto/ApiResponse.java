package com.tradex.flashtrade.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理端 API 响应包装类，与统一响应契约保持一致。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一API响应")
public class ApiResponse<T> {
    
    @Schema(description = "稳定业务代码", example = "SUCCESS")
    private String code;
    
    @Schema(description = "响应消息", example = "请求成功")
    private String message;
    
    @Schema(description = "响应数据")
    private T data;
    
    /**
     * 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .code("SUCCESS")
            .message("请求成功")
            .data(data)
            .build();
    }
    
    /**
     * 成功响应（仅消息）
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
            .code("SUCCESS")
            .message(message)
            .data(data)
            .build();
    }
}
