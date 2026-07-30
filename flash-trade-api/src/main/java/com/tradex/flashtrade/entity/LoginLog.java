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
 * 登录审计日志 Entity
 * 记录所有登录事件（成功、失败、IP、时间等）
 */
@Entity
@Table(name = "login_log", indexes = {
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_login_status", columnList = "login_status"),
    @Index(name = "idx_login_time", columnList = "login_time"),
    @Index(name = "idx_request_ip", columnList = "request_ip")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String username;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "login_status", nullable = false, length = 20)
    private String loginStatus;
    
    @Column(name = "failed_reason", length = 200)
    private String failedReason;
    
    @Column(name = "request_ip", length = 45)
    private String requestIp;
    
    @Column(name = "request_path", length = 255)
    private String requestPath;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(name = "device_info", length = 500)
    private String deviceInfo;
    
    @Column(name = "login_method", length = 50)
    private String loginMethod;
    
    @Column(name = "login_time", nullable = false)
    private LocalDateTime loginTime;
    
    @Column(name = "session_id", length = 100)
    private String sessionId;
}
