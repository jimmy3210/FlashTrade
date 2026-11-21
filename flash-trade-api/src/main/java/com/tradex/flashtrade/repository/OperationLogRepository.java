package com.tradex.flashtrade.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tradex.flashtrade.entity.OperationLog;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {

    @Query("""
        select log from OperationLog log
        where (:operatorId is null or log.operatorId = :operatorId)
          and (:operation is null or log.operation = :operation)
          and (:targetType is null or log.targetType = :targetType)
          and (:targetId is null or log.targetId = :targetId)
          and (:httpStatus is null or log.httpStatus = :httpStatus)
          and (:startTime is null or log.createdAt >= :startTime)
          and (:endTime is null or log.createdAt <= :endTime)
        """)
    Page<OperationLog> search(
            @Param("operatorId") Long operatorId,
            @Param("operation") String operation,
            @Param("targetType") String targetType,
            @Param("targetId") Long targetId,
            @Param("httpStatus") Integer httpStatus,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);
    
    Page<OperationLog> findByOperatorId(Long operatorId, Pageable pageable);
    
    Page<OperationLog> findByTargetTypeAndTargetId(String targetType, Long targetId, Pageable pageable);
    
    Page<OperationLog> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    List<OperationLog> findByOperation(String operation);
}
