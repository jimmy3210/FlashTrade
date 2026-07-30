package com.tradex.flashtrade.dto.admin;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API 调用日志 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "API 调用日志 DTO")
public class ApiCallLogDto {
    
    @Schema(description = "日志ID")
    private Long id;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "用户名")
    private String username;
    
    @Schema(description = "HTTP方法")
    private String httpMethod;
    
    @Schema(description = "API端点")
    private String endpoint;
    
    @Schema(description = "HTTP响应码")
    private Integer responseCode;
    
    @Schema(description = "响应时间（毫秒）")
    private Integer responseTimeMs;
    
    @Schema(description = "请求IP")
    private String requestIp;
    
    @Schema(description = "是否是关键操作")
    private Boolean isCritical;
    
    @Schema(description = "是否出错")
    private Boolean errorFlag;
    
    @Schema(description = "请求时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
