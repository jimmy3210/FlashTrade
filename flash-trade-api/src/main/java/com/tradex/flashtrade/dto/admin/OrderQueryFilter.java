package com.tradex.flashtrade.dto.admin;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单查询过滤条件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单查询过滤条件")
public class OrderQueryFilter {
    
    @Schema(description = "用户ID（可选，不指定则查所有）")
    private Long userId;
    
    @Schema(description = "订单状态（可选）")
    private String status;
    
    @Schema(description = "股票代码（可选）")
    private String symbol;
    
    @Schema(description = "买卖方向（可选）: BUY/SELL")
    private String side;
    
    @Schema(description = "风险标记（可选）: NORMAL/SUSPICIOUS/BLOCKED")
    private String riskFlag;
    
    @Schema(description = "开始日期（可选）")
    private LocalDate startDate;
    
    @Schema(description = "结束日期（可选）")
    private LocalDate endDate;
    
    @Schema(description = "排序字段: created_at/updated_at/quantity/price/id/symbol")
    private String sortBy = "created_at";
    
    @Schema(description = "排序顺序: asc/desc")
    private String sortOrder = "desc";
}
