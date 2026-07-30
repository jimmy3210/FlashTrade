package com.tradex.flashtrade.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tradex.flashtrade.entity.LogRetentionPolicy;

@Repository
public interface LogRetentionPolicyRepository extends JpaRepository<LogRetentionPolicy, Long> {
    
    LogRetentionPolicy findByLogType(String logType);
}
