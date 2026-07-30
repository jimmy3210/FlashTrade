package com.tradex.flashtrade.audit;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.tradex.flashtrade.entity.ApiCallLog;
import com.tradex.flashtrade.entity.LoginLog;
import com.tradex.flashtrade.entity.OperationLog;
import com.tradex.flashtrade.entity.SystemEventLog;
import com.tradex.flashtrade.repository.ApiCallLogRepository;
import com.tradex.flashtrade.repository.LoginLogRepository;
import com.tradex.flashtrade.repository.OperationLogRepository;
import com.tradex.flashtrade.repository.SystemEventLogRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * 审计日志工具类
 * 负责记录操作日志、登录日志、API调用日志、系统事件日志
 */
@Component
@Slf4j
public class AuditLogger {
    
    @Autowired private OperationLogRepository operationLogRepository;
    @Autowired private LoginLogRepository loginLogRepository;
    @Autowired private ApiCallLogRepository apiCallLogRepository;
    @Autowired private SystemEventLogRepository systemEventLogRepository;
    
    /**
     * 记录操作日志
     */
    public void logOperation(Long operatorId, String operatorUsername, String operation,
                            String targetType, Long targetId, String targetName,
                            Object oldValue, Object newValue, String changeSummary,
                            String requestIp, String requestPath, String requestMethod,
                            Integer httpStatus, String userAgent) {
        try {
                OperationLog operationLog = OperationLog.builder()
                .operatorId(operatorId)
                .operatorUsername(operatorUsername)
                .operation(operation)
                .targetType(targetType)
                .targetId(targetId)
                .targetName(targetName)
                .oldValue(oldValue != null ? JSON.toJSONString(oldValue) : null)
                .newValue(newValue != null ? JSON.toJSONString(newValue) : null)
                .changeSummary(changeSummary)
                .requestIp(requestIp)
                .requestPath(requestPath)
                .requestMethod(requestMethod)
                .httpStatus(httpStatus)
                .userAgent(userAgent)
                .createdAt(LocalDateTime.now())
                .build();
            
                operationLogRepository.save(operationLog);
                log.info("✅ Operation logged: op={}, target={}:{}, operator={}, ip={}", 
                    operation, targetType, targetId, operatorUsername, requestIp);
            
        } catch (Exception e) {
            log.error("❌ Failed to log operation: op={}, error={}", operation, e.getMessage(), e);
        }
    }
    
    /**
     * 记录登录日志
     */
    public void logLogin(String username, Long userId, String loginStatus, 
                        String failedReason, String requestIp, String userAgent,
                        String loginMethod, String sessionId) {
        try {
            LoginLog loginLog = LoginLog.builder()
                .username(username)
                .userId(userId)
                .loginStatus(loginStatus)
                .failedReason(failedReason)
                .requestIp(requestIp)
                .userAgent(userAgent)
                .loginMethod(loginMethod)
                .sessionId(sessionId)
                .loginTime(LocalDateTime.now())
                .build();
            
            loginLogRepository.save(loginLog);
            
            if ("FAILED".equals(loginStatus)) {
                log.warn("⚠️  Login failed: username={}, reason={}, ip={}", 
                        username, failedReason, requestIp);
            } else {
                log.info("✅ Login successful: username={}, ip={}", username, requestIp);
            }
            
        } catch (Exception e) {
            log.error("❌ Failed to log login", e);
        }
    }
    
    /**
     * 记录 API 调用日志
     */
    public void logApiCall(Long userId, String username, String httpMethod, 
                          String endpoint, String requestParams, Object requestBody,
                          Integer responseCode, Long responseTimeMs,
                          String requestIp, String userAgent, boolean isCritical) {
        try {
            // 异步记录，避免影响响应时间
            new Thread(() -> {
                try {
                    ApiCallLog apiCallLog = ApiCallLog.builder()
                        .userId(userId)
                        .username(username)
                        .httpMethod(httpMethod)
                        .endpoint(endpoint)
                        .requestParams(requestParams)
                        .requestBody(requestBody != null ? JSON.toJSONString(requestBody) : null)
                        .responseCode(responseCode)
                        .responseTimeMs(responseTimeMs.intValue())
                        .requestIp(requestIp)
                        .userAgent(userAgent)
                        .isCritical(isCritical)
                        .errorFlag(responseCode >= 400)
                        .createdAt(LocalDateTime.now())
                        .build();
                    
                    apiCallLogRepository.save(apiCallLog);
                    
                    if (isCritical) {
                        log.info("⭐ Critical API called: {} {} by user={}, responseTime={}ms, code={}",
                                httpMethod, endpoint, username, responseTimeMs, responseCode);
                    }
                } catch (Exception e) {
                    log.error("❌ Failed to log API call", e);
                }
            }).start();
            
        } catch (Exception e) {
            log.error("❌ Failed to log API call", e);
        }
    }
    
    /**
     * 记录系统事件日志
     */
    public void logSystemEvent(String eventType, String severity, String component,
                              String message, String stackTrace, Long relatedUserId,
                              Long relatedOrderId, String relatedResource) {
        try {
            SystemEventLog systemEventLog = SystemEventLog.builder()
                .eventType(eventType)
                .severity(severity)
                .component(component)
                .message(message)
                .stackTrace(stackTrace)
                .relatedUserId(relatedUserId)
                .relatedOrderId(relatedOrderId)
                .relatedResource(relatedResource)
                .resolved(false)
                .createdAt(LocalDateTime.now())
                .build();
            
            systemEventLogRepository.save(systemEventLog);
            
            // 根据严重级别输出不同日志
            if ("CRITICAL".equals(severity) || "HIGH".equals(severity)) {
                log.error("🚨 CRITICAL System event [{} - {}]: {} | Component: {} | Resource: {}",
                        eventType, severity, message, component, relatedResource);
            } else if ("WARN".equals(severity)) {
                log.warn("⚠️  System event [{} - {}]: {} | Component: {} | Resource: {}",
                        eventType, severity, message, component, relatedResource);
            } else {
                log.info("ℹ️  System event [{} - {}]: {} | Component: {}",
                        eventType, severity, message, component);
            }
            
        } catch (Exception e) {
            log.error("❌ Failed to log system event", e);
        }
    }
}
