package com.tradex.flashtrade.dto.admin;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 操作日志 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "操作日志 DTO")
public class OperationLogDto {
    
    @Schema(description = "日志ID")
    private Long id;
    
    @Schema(description = "操作员ID")
    private Long operatorId;
    
    @Schema(description = "操作员用户名")
    private String operatorUsername;
    
    @Schema(description = "操作类型")
    private String operation;
    
    @Schema(description = "目标资源类型")
    private String targetType;
    
    @Schema(description = "目标资源ID")
    private Long targetId;
    
    @Schema(description = "目标资源名称")
    private String targetName;
    
    @Schema(description = "修改摘要")
    private String changeSummary;
    
    @Schema(description = "请求IP")
    private String requestIp;
    
    @Schema(description = "HTTP方法")
    private String requestMethod;
    
    @Schema(description = "HTTP响应码")
    private Integer httpStatus;
    
    @Schema(description = "操作时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
