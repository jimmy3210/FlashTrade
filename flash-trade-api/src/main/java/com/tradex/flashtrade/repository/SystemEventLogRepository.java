package com.tradex.flashtrade.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tradex.flashtrade.entity.SystemEventLog;

@Repository
public interface SystemEventLogRepository extends JpaRepository<SystemEventLog, Long> {
    
    Page<SystemEventLog> findByEventType(String eventType, Pageable pageable);
    
    Page<SystemEventLog> findBySeverity(String severity, Pageable pageable);
    
    Page<SystemEventLog> findByComponent(String component, Pageable pageable);
    
    Page<SystemEventLog> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    Page<SystemEventLog> findByResolved(Boolean resolved, Pageable pageable);
}
