package com.tradex.flashtrade.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单管理 Admin DTO
 * 用于管理端查询订单、查看订单详情
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单管理 DTO")
public class OrderAdminDto {
    
    @Schema(description = "订单ID")
    private Long id;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "用户名")
    private String username;
    
    @Schema(description = "券商账户ID")
    private Long brokerAccountId;
    
    @Schema(description = "券商名称")
    private String brokerName;
    
    @Schema(description = "账户号")
    private String accountNo;
    
    @Schema(description = "股票代码")
    private String symbol;
    
    @Schema(description = "买卖方向: BUY/SELL")
    private String side;
    
    @Schema(description = "委托数量")
    private Integer quantity;
    
    @Schema(description = "订单类型: MARKET/LIMIT/STOP/STOP_LIMIT")
    private String orderType;
    
    @Schema(description = "限价")
    private BigDecimal limitPrice;
    
    @Schema(description = "订单状态: PENDING/FILLED/PARTIALLY_FILLED/CANCELLED/REJECTED")
    private String status;
    
    @Schema(description = "已成交数量")
    private Integer filledQuantity;
    
    @Schema(description = "平均成交价")
    private BigDecimal averagePrice;
    
    @Schema(description = "订单总价值")
    private BigDecimal totalValue;
    
    @Schema(description = "佣金")
    private BigDecimal commission;
    
    @Schema(description = "券商订单ID")
    private String externalOrderId;
    
    @Schema(description = "预览ID")
    private String previewId;
    
    @Schema(description = "风险标记: NORMAL/SUSPICIOUS/BLOCKED/CANCELLED_BY_ADMIN")
    private String riskFlag;
    
    @Schema(description = "管理员备注")
    private String adminNote;
    
    @Schema(description = "操作管理员ID")
    private Long adminId;
    
    @Schema(description = "管理员操作时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime adminActionTime;
    
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    @Schema(description = "订单时间线")
    private List<OrderTimelineEvent> timeline;
}
