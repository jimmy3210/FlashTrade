package com.tradex.flashtrade.dto.admin;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录日志 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录日志 DTO")
public class LoginLogDto {
    
    @Schema(description = "日志ID")
    private Long id;
    
    @Schema(description = "用户名")
    private String username;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "登录状态: SUCCESS/FAILED")
    private String loginStatus;
    
    @Schema(description = "失败原因")
    private String failedReason;
    
    @Schema(description = "请求IP")
    private String requestIp;
    
    @Schema(description = "登录方式")
    private String loginMethod;
    
    @Schema(description = "登录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime;
}
