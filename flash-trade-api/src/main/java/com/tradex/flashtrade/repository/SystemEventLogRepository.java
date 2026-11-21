package com.tradex.flashtrade.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tradex.flashtrade.entity.SystemEventLog;

@Repository
public interface SystemEventLogRepository extends JpaRepository<SystemEventLog, Long> {

    @Query("""
        select log from SystemEventLog log
        where (:eventType is null or log.eventType = :eventType)
          and (:severity is null or log.severity = :severity)
          and (:component is null or log.component = :component)
          and (:unresolved is null or log.resolved = false)
          and (:startTime is null or log.createdAt >= :startTime)
          and (:endTime is null or log.createdAt <= :endTime)
        """)
    Page<SystemEventLog> search(
            @Param("eventType") String eventType,
            @Param("severity") String severity,
            @Param("component") String component,
            @Param("unresolved") Boolean unresolved,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);
    
    Page<SystemEventLog> findByEventType(String eventType, Pageable pageable);
    
    Page<SystemEventLog> findBySeverity(String severity, Pageable pageable);
    
    Page<SystemEventLog> findByComponent(String component, Pageable pageable);
    
    Page<SystemEventLog> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    Page<SystemEventLog> findByResolved(Boolean resolved, Pageable pageable);
}
