package com.tradex.flashtrade.dto.admin;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单时间线事件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单时间线事件")
public class OrderTimelineEvent {
    
    @Schema(description = "事件时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    @Schema(description = "事件类型: ORDER_CREATED/ORDER_SUBMITTED/ORDER_FILLED/ORDER_CANCELLED等")
    private String event;
    
    @Schema(description = "事件描述")
    private String description;
}
