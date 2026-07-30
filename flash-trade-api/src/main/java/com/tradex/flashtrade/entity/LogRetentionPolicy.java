package com.tradex.flashtrade.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 日志保留策略 Entity
 * 配置不同类型日志的保留时间
 */
@Entity
@Table(name = "log_retention_policy", uniqueConstraints = {
    @UniqueConstraint(name = "uk_log_type", columnNames = "log_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogRetentionPolicy {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "log_type", nullable = false, length = 50)
    private String logType;
    
    @Column(name = "retention_days")
    private Integer retentionDays;
    
    @Column(name = "archive_flag")
    private Boolean archiveFlag = true;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
