package com.tradex.flashtrade.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tradex.flashtrade.entity.ApiCallLog;

@Repository
public interface ApiCallLogRepository extends JpaRepository<ApiCallLog, Long> {

    @Query("""
        select log from ApiCallLog log
        where (:userId is null or log.userId = :userId)
          and (:endpoint is null or log.endpoint = :endpoint)
          and (:responseCode is null or log.responseCode = :responseCode)
          and (:isCritical is null or log.isCritical = :isCritical)
          and (:errorFlag is null or log.errorFlag = :errorFlag)
          and (:startTime is null or log.createdAt >= :startTime)
          and (:endTime is null or log.createdAt <= :endTime)
        """)
    Page<ApiCallLog> search(
            @Param("userId") Long userId,
            @Param("endpoint") String endpoint,
            @Param("responseCode") Integer responseCode,
            @Param("isCritical") Boolean isCritical,
            @Param("errorFlag") Boolean errorFlag,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);
    
    Page<ApiCallLog> findByUserId(Long userId, Pageable pageable);
    
    Page<ApiCallLog> findByEndpoint(String endpoint, Pageable pageable);
    
    Page<ApiCallLog> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    Page<ApiCallLog> findByIsCritical(Boolean isCritical, Pageable pageable);
    
    Page<ApiCallLog> findByErrorFlag(Boolean errorFlag, Pageable pageable);
}
