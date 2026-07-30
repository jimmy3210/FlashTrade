package com.tradex.flashtrade.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tradex.flashtrade.dto.ApiResponse;
import com.tradex.flashtrade.dto.admin.ApiCallLogDto;
import com.tradex.flashtrade.dto.admin.LoginLogDto;
import com.tradex.flashtrade.dto.admin.OperationLogDto;
import com.tradex.flashtrade.dto.admin.SystemEventLogDto;
import com.tradex.flashtrade.entity.ApiCallLog;
import com.tradex.flashtrade.entity.LoginLog;
import com.tradex.flashtrade.entity.OperationLog;
import com.tradex.flashtrade.entity.SystemEventLog;
import com.tradex.flashtrade.repository.ApiCallLogRepository;
import com.tradex.flashtrade.repository.LoginLogRepository;
import com.tradex.flashtrade.repository.OperationLogRepository;
import com.tradex.flashtrade.repository.SystemEventLogRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * 审计日志查询 Admin API Controller
 * 提供操作日志、登录日志、API调用日志、系统事件日志的查询功能
 */
@RestController
@RequestMapping("/admin/audit")
@Tag(name = "审计日志", description = "审计和监控日志查询接口")
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {
    
    @Autowired private OperationLogRepository operationLogRepository;
    @Autowired private LoginLogRepository loginLogRepository;
    @Autowired private ApiCallLogRepository apiCallLogRepository;
    @Autowired private SystemEventLogRepository systemEventLogRepository;
    
    /**
     * 查询操作日志
     */
    @GetMapping("/operations")
    @Operation(summary = "查询操作日志", description = "查询管理员操作的审计日志")
    public ResponseEntity<ApiResponse<Page<OperationLogDto>>> queryOperationLogs(
            @Parameter(description = "页码，从 1 开始", example = "1")
            @RequestParam(defaultValue = "1") int page,
            
            @Parameter(description = "每页数量", example = "20")
            @RequestParam(defaultValue = "20") int pageSize,
            
            @Parameter(description = "操作员ID（可选）")
            @RequestParam(required = false) Long operatorId,
            
            @Parameter(description = "操作类型（可选）")
            @RequestParam(required = false) String operation,
            
            @Parameter(description = "目标资源类型（可选）")
            @RequestParam(required = false) String targetType) {
        
        log.info("📋 Querying operation logs: page={}, operatorId={}, operation={}", 
                page, operatorId, operation);
        
        Page<OperationLog> logs;
        if (operatorId != null) {
            logs = operationLogRepository.findByOperatorId(operatorId, 
                    PageRequest.of(page - 1, pageSize));
        } else {
            logs = operationLogRepository.findAll(PageRequest.of(page - 1, pageSize));
        }
        
        return ResponseEntity.ok(ApiResponse.success(
            logs.map(this::convertToOperationLogDto)
        ));
    }
    
    /**
     * 查询登录日志
     */
    @GetMapping("/logins")
    @Operation(summary = "查询登录日志", description = "查询用户登录审计日志")
    public ResponseEntity<ApiResponse<Page<LoginLogDto>>> queryLoginLogs(
            @Parameter(description = "页码，从 1 开始", example = "1")
            @RequestParam(defaultValue = "1") int page,
            
            @Parameter(description = "每页数量", example = "20")
            @RequestParam(defaultValue = "20") int pageSize,
            
            @Parameter(description = "用户名（可选）")
            @RequestParam(required = false) String username,
            
            @Parameter(description = "登录状态（可选）: SUCCESS/FAILED")
            @RequestParam(required = false) String loginStatus,
            
            @Parameter(description = "请求IP（可选）")
            @RequestParam(required = false) String requestIp) {
        
        log.info("📋 Querying login logs: page={}, username={}, loginStatus={}", 
                page, username, loginStatus);
        
        Page<LoginLog> logs;
        if (username != null && !username.isEmpty()) {
            logs = loginLogRepository.findByUsername(username, 
                    PageRequest.of(page - 1, pageSize));
        } else if (loginStatus != null && !loginStatus.isEmpty()) {
            logs = loginLogRepository.findByLoginStatus(loginStatus, 
                    PageRequest.of(page - 1, pageSize));
        } else if (requestIp != null && !requestIp.isEmpty()) {
            logs = loginLogRepository.findByRequestIp(requestIp, 
                    PageRequest.of(page - 1, pageSize));
        } else {
            logs = loginLogRepository.findAll(PageRequest.of(page - 1, pageSize));
        }
        
        return ResponseEntity.ok(ApiResponse.success(
            logs.map(this::convertToLoginLogDto)
        ));
    }
    
    /**
     * 查询 API 调用日志
     */
    @GetMapping("/api-calls")
    @Operation(summary = "查询 API 调用日志", description = "查询系统 API 调用审计日志")
    public ResponseEntity<ApiResponse<Page<ApiCallLogDto>>> queryApiCallLogs(
            @Parameter(description = "页码，从 1 开始", example = "1")
            @RequestParam(defaultValue = "1") int page,
            
            @Parameter(description = "每页数量", example = "20")
            @RequestParam(defaultValue = "20") int pageSize,
            
            @Parameter(description = "用户ID（可选）")
            @RequestParam(required = false) Long userId,
            
            @Parameter(description = "API端点（可选）")
            @RequestParam(required = false) String endpoint,
            
            @Parameter(description = "仅查询关键操作（可选）")
            @RequestParam(required = false) Boolean isCritical,
            
            @Parameter(description = "仅查询错误的调用（可选）")
            @RequestParam(required = false) Boolean errorFlag) {
        
        log.info("📋 Querying API call logs: page={}, userId={}, endpoint={}", 
                page, userId, endpoint);
        
        Page<ApiCallLog> logs;
        if (userId != null) {
            logs = apiCallLogRepository.findByUserId(userId, 
                    PageRequest.of(page - 1, pageSize));
        } else if (isCritical != null && isCritical) {
            logs = apiCallLogRepository.findByIsCritical(true, 
                    PageRequest.of(page - 1, pageSize));
        } else if (errorFlag != null && errorFlag) {
            logs = apiCallLogRepository.findByErrorFlag(true, 
                    PageRequest.of(page - 1, pageSize));
        } else {
            logs = apiCallLogRepository.findAll(PageRequest.of(page - 1, pageSize));
        }
        
        return ResponseEntity.ok(ApiResponse.success(
            logs.map(this::convertToApiCallLogDto)
        ));
    }
    
    /**
     * 查询系统事件日志
     */
    @GetMapping("/events")
    @Operation(summary = "查询系统事件日志", description = "查询系统事件和告警日志")
    public ResponseEntity<ApiResponse<Page<SystemEventLogDto>>> querySystemEventLogs(
            @Parameter(description = "页码，从 1 开始", example = "1")
            @RequestParam(defaultValue = "1") int page,
            
            @Parameter(description = "每页数量", example = "20")
            @RequestParam(defaultValue = "20") int pageSize,
            
            @Parameter(description = "事件类型（可选）")
            @RequestParam(required = false) String eventType,
            
            @Parameter(description = "严重级别（可选）: LOW/MEDIUM/HIGH/CRITICAL")
            @RequestParam(required = false) String severity,
            
            @Parameter(description = "组件名称（可选）")
            @RequestParam(required = false) String component,
            
            @Parameter(description = "仅查询未解决的事件（可选）")
            @RequestParam(required = false) Boolean unresolved) {
        
        log.info("📋 Querying system event logs: page={}, severity={}, component={}", 
                page, severity, component);
        
        Page<SystemEventLog> logs;
        if (severity != null && !severity.isEmpty()) {
            logs = systemEventLogRepository.findBySeverity(severity, 
                    PageRequest.of(page - 1, pageSize));
        } else if (unresolved != null && unresolved) {
            logs = systemEventLogRepository.findByResolved(false, 
                    PageRequest.of(page - 1, pageSize));
        } else {
            logs = systemEventLogRepository.findAll(PageRequest.of(page - 1, pageSize));
        }
        
        return ResponseEntity.ok(ApiResponse.success(
            logs.map(this::convertToSystemEventLogDto)
        ));
    }
    
    // 转换方法
    
    private OperationLogDto convertToOperationLogDto(OperationLog log) {
        return OperationLogDto.builder()
            .id(log.getId())
            .operatorId(log.getOperatorId())
            .operatorUsername(log.getOperatorUsername())
            .operation(log.getOperation())
            .targetType(log.getTargetType())
            .targetId(log.getTargetId())
            .targetName(log.getTargetName())
            .changeSummary(log.getChangeSummary())
            .requestIp(log.getRequestIp())
            .requestMethod(log.getRequestMethod())
            .httpStatus(log.getHttpStatus())
            .createdAt(log.getCreatedAt())
            .build();
    }
    
    private LoginLogDto convertToLoginLogDto(LoginLog log) {
        return LoginLogDto.builder()
            .id(log.getId())
            .username(log.getUsername())
            .userId(log.getUserId())
            .loginStatus(log.getLoginStatus())
            .failedReason(log.getFailedReason())
            .requestIp(log.getRequestIp())
            .loginMethod(log.getLoginMethod())
            .loginTime(log.getLoginTime())
            .build();
    }
    
    private ApiCallLogDto convertToApiCallLogDto(ApiCallLog log) {
        return ApiCallLogDto.builder()
            .id(log.getId())
            .userId(log.getUserId())
            .username(log.getUsername())
            .httpMethod(log.getHttpMethod())
            .endpoint(log.getEndpoint())
            .responseCode(log.getResponseCode())
            .responseTimeMs(log.getResponseTimeMs())
            .requestIp(log.getRequestIp())
            .isCritical(log.getIsCritical())
            .errorFlag(log.getErrorFlag())
            .createdAt(log.getCreatedAt())
            .build();
    }
    
    private SystemEventLogDto convertToSystemEventLogDto(SystemEventLog log) {
        return SystemEventLogDto.builder()
            .id(log.getId())
            .eventType(log.getEventType())
            .severity(log.getSeverity())
            .component(log.getComponent())
            .message(log.getMessage())
            .relatedUserId(log.getRelatedUserId())
            .relatedOrderId(log.getRelatedOrderId())
            .relatedResource(log.getRelatedResource())
            .createdAt(log.getCreatedAt())
            .resolved(log.getResolved())
            .resolvedAt(log.getResolvedAt())
            .build();
    }
}
