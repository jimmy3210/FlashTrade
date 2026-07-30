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
 * API 调用日志 Entity
 * 记录关键 API 调用（交易、查询等）
 */
@Entity
@Table(name = "api_call_log", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_endpoint", columnList = "endpoint"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_is_critical", columnList = "is_critical"),
    @Index(name = "idx_error_flag", columnList = "error_flag"),
    @Index(name = "idx_response_code", columnList = "response_code")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiCallLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(length = 100)
    private String username;
    
    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod;
    
    @Column(nullable = false, length = 255)
    private String endpoint;
    
    @Column(name = "request_params", length = 1000)
    private String requestParams;
    
    @Column(name = "request_body", columnDefinition = "JSON")
    private String requestBody;
    
    @Column(name = "response_code")
    private Integer responseCode;
    
    @Column(name = "response_time_ms")
    private Integer responseTimeMs;
    
    @Column(name = "request_ip", length = 45)
    private String requestIp;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "is_critical")
    private Boolean isCritical;
    
    @Column(name = "error_flag")
    private Boolean errorFlag;
}
