package com.tradex.flashtrade.repository;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tradex.flashtrade.entity.OrderDailySummary;

@Repository
public interface OrderDailySummaryRepository extends JpaRepository<OrderDailySummary, Long> {
    
    Page<OrderDailySummary> findByUserId(Long userId, Pageable pageable);
    
    OrderDailySummary findByUserIdAndSummaryDate(Long userId, LocalDate summaryDate);
    
    Page<OrderDailySummary> findByUserIdAndSummaryDateBetween(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable);
}
