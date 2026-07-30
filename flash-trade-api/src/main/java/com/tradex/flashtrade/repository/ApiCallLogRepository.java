package com.tradex.flashtrade.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tradex.flashtrade.entity.ApiCallLog;

@Repository
public interface ApiCallLogRepository extends JpaRepository<ApiCallLog, Long> {
    
    Page<ApiCallLog> findByUserId(Long userId, Pageable pageable);
    
    Page<ApiCallLog> findByEndpoint(String endpoint, Pageable pageable);
    
    Page<ApiCallLog> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    Page<ApiCallLog> findByIsCritical(Boolean isCritical, Pageable pageable);
    
    Page<ApiCallLog> findByErrorFlag(Boolean errorFlag, Pageable pageable);
}
