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
 * 系统事件日志 Entity
 * 记录系统错误、异常、告警事件
 */
@Entity
@Table(name = "system_event_log", indexes = {
    @Index(name = "idx_event_type", columnList = "event_type"),
    @Index(name = "idx_severity", columnList = "severity"),
    @Index(name = "idx_component", columnList = "component"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_resolved", columnList = "resolved"),
    @Index(name = "idx_severity_created", columnList = "severity,created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemEventLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;
    
    @Column(nullable = false, length = 20)
    private String severity;
    
    @Column(nullable = false, length = 100)
    private String component;
    
    @Column(nullable = false, length = 500)
    private String message;
    
    @Column(name = "stack_trace", columnDefinition = "LONGTEXT")
    private String stackTrace;
    
    @Column(name = "related_user_id")
    private Long relatedUserId;
    
    @Column(name = "related_order_id")
    private Long relatedOrderId;
    
    @Column(name = "related_resource", length = 255)
    private String relatedResource;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private Boolean resolved;
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    @Column(name = "resolved_by")
    private Long resolvedBy;
    
    @Column(name = "resolution_note", length = 500)
    private String resolutionNote;
}
