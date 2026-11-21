package com.tradex.flashtrade.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tradex.flashtrade.entity.LoginLog;

@Repository
public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {

    @Query("""
        select log from LoginLog log
        where (:username is null or log.username = :username)
          and (:loginStatus is null or log.loginStatus = :loginStatus)
          and (:requestIp is null or log.requestIp = :requestIp)
          and (:startTime is null or log.loginTime >= :startTime)
          and (:endTime is null or log.loginTime <= :endTime)
        """)
    Page<LoginLog> search(
            @Param("username") String username,
            @Param("loginStatus") String loginStatus,
            @Param("requestIp") String requestIp,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);
    
    Page<LoginLog> findByUsername(String username, Pageable pageable);
    
    Page<LoginLog> findByLoginStatus(String loginStatus, Pageable pageable);
    
    Page<LoginLog> findByLoginTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    Page<LoginLog> findByRequestIp(String requestIp, Pageable pageable);
}
