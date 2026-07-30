package com.tradex.flashtrade.dto.admin;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标记订单请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "标记订单请求")
public class MarkOrderRequest {
    
    @NotBlank(message = "风险标记不能为空")
    @Schema(description = "风险标记: NORMAL/SUSPICIOUS/BLOCKED/CANCELLED_BY_ADMIN")
    private String riskFlag;
    
    @Schema(description = "管理员备注")
    private String adminNote;
}
