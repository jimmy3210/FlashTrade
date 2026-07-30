package com.tradex.flashtrade.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tradex.flashtrade.entity.LoginLog;

@Repository
public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {
    
    Page<LoginLog> findByUsername(String username, Pageable pageable);
    
    Page<LoginLog> findByLoginStatus(String loginStatus, Pageable pageable);
    
    Page<LoginLog> findByLoginTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    Page<LoginLog> findByRequestIp(String requestIp, Pageable pageable);
}
