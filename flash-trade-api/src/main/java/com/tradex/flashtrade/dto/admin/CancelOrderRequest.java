package com.tradex.flashtrade.dto.admin;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 取消订单请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "取消订单请求")
public class CancelOrderRequest {
    
    @NotBlank(message = "取消原因不能为空")
    @Schema(description = "取消原因")
    private String reason;
}
