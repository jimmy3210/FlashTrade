package com.tradex.flashtrade.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单每日汇总 Entity
 * 用于快速查询日统计数据
 */
@Entity
@Table(name = "order_daily_summary", indexes = {
    @Index(name = "uk_user_date", columnList = "user_id,summary_date"),
    @Index(name = "idx_user_date", columnList = "user_id,summary_date"),
    @Index(name = "idx_date", columnList = "summary_date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDailySummary {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "summary_date", nullable = false)
    private LocalDate summaryDate;
    
    @Column(name = "total_orders")
    private Integer totalOrders = 0;
    
    @Column(name = "buy_orders")
    private Integer buyOrders = 0;
    
    @Column(name = "sell_orders")
    private Integer sellOrders = 0;
    
    @Column(name = "filled_orders")
    private Integer filledOrders = 0;
    
    @Column(name = "cancelled_orders")
    private Integer cancelledOrders = 0;
    
    @Column(name = "rejected_orders")
    private Integer rejectedOrders = 0;
    
    @Column(name = "total_buy_qty")
    private Integer totalBuyQty = 0;
    
    @Column(name = "total_sell_qty")
    private Integer totalSellQty = 0;
    
    @Column(name = "total_buy_amount")
    private BigDecimal totalBuyAmount;
    
    @Column(name = "total_sell_amount")
    private BigDecimal totalSellAmount;
    
    @Column(name = "total_commission")
    private BigDecimal totalCommission;
    
    @Column(name = "total_tax")
    private BigDecimal totalTax;
    
    @Column(name = "realized_pnl")
    private BigDecimal realizedPnl;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
