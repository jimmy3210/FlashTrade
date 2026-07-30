package com.tradex.flashtrade.dto.admin;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统事件日志 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "系统事件日志 DTO")
public class SystemEventLogDto {
    
    @Schema(description = "日志ID")
    private Long id;
    
    @Schema(description = "事件类型")
    private String eventType;
    
    @Schema(description = "严重级别: LOW/MEDIUM/HIGH/CRITICAL")
    private String severity;
    
    @Schema(description = "组件名称")
    private String component;
    
    @Schema(description = "事件消息")
    private String message;
    
    @Schema(description = "关联用户ID")
    private Long relatedUserId;
    
    @Schema(description = "关联订单ID")
    private Long relatedOrderId;
    
    @Schema(description = "关联资源描述")
    private String relatedResource;
    
    @Schema(description = "事件时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @Schema(description = "是否已解决")
    private Boolean resolved;
    
    @Schema(description = "解决时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime resolvedAt;
}
