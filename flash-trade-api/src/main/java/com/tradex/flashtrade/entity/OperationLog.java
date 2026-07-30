package com.tradex.flashtrade.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 操作审计日志 Entity
 * 记录所有管理员操作（创建用户、修改账户、取消订单等）
 */
@Entity
@Table(name = "operation_log", indexes = {
    @Index(name = "idx_operator_id", columnList = "operator_id"),
    @Index(name = "idx_target_type_id", columnList = "target_type,target_id"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_operation", columnList = "operation")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "operator_id")
    private Long operatorId;
    
    @Column(name = "operator_username", length = 100)
    private String operatorUsername;
    
    @Column(nullable = false, length = 100)
    private String operation;
    
    @Column(name = "target_type", nullable = false, length = 50)
    private String targetType;
    
    @Column(name = "target_id")
    private Long targetId;
    
    @Column(name = "target_name", length = 255)
    private String targetName;
    
    @Column(name = "old_value", columnDefinition = "JSON")
    private String oldValue;
    
    @Column(name = "new_value", columnDefinition = "JSON")
    private String newValue;
    
    @Column(name = "change_summary", length = 500)
    private String changeSummary;
    
    @Column(name = "request_ip", length = 45)
    private String requestIp;
    
    @Column(name = "request_path", length = 255)
    private String requestPath;
    
    @Column(name = "request_method", length = 10)
    private String requestMethod;
    
    @Column(name = "http_status")
    private Integer httpStatus;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(name = "device_info", length = 500)
    private String deviceInfo;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
