package com.tradex.flashtrade.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tradex.flashtrade.entity.OperationLog;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {
    
    Page<OperationLog> findByOperatorId(Long operatorId, Pageable pageable);
    
    Page<OperationLog> findByTargetTypeAndTargetId(String targetType, Long targetId, Pageable pageable);
    
    Page<OperationLog> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    List<OperationLog> findByOperation(String operation);
}
