package com.tradex.flashtrade.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * 订单成交详情表 - 记录订单的分笔成交信息
 * 
 * 支持部分成交（Partial Fill）场景：
 * - 一个订单可能分多次成交
 * - 每笔成交记录价格、数量、时间、佣金等详细信息
 * 
 * 例如：100股订单 @ $50
 * - 第1笔：50股 @ $50.10，佣金 $1.00
 * - 第2笔：50股 @ $50.20，佣金 $1.00
 */
@Entity
@Table(
    name = "order_fills",
    indexes = {
        @Index(name = "idx_fill_order_id", columnList = "order_id"),
        @Index(name = "idx_fill_sequence", columnList = "order_id,fill_sequence"),
        @Index(name = "idx_fill_filled_at", columnList = "filled_at")
    },
    uniqueConstraints = @UniqueConstraint(
        name = "uk_fill_id",
        columnNames = {"order_id", "external_fill_id"}
    )
)
public class OrderFill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 订单 ID（本地数据库）
     */
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    /**
     * 成交 ID（来自 etrade）
     * 唯一标识一笔成交
     */
    @Column(name = "external_fill_id", nullable = false, length = 100)
    private String externalFillId;

    /**
     * 成交序列号
     * 同一订单的成交序列：1st fill = 1, 2nd fill = 2, ...
     */
    @Column(name = "fill_sequence", nullable = false)
    private Integer fillSequence;

    // ===== 成交数据 =====
    /**
     * 本笔成交的数量
     */
    @Column(name = "fill_quantity", nullable = false, precision = 15, scale = 4)
    private BigDecimal fillQuantity;

    /**
     * 本笔成交的价格
     */
    @Column(name = "fill_price", nullable = false, precision = 15, scale = 4)
    private BigDecimal fillPrice;

    /**
     * 本笔成交的佣金
     */
    @Column(name = "fill_commission", precision = 15, scale = 4)
    private BigDecimal fillCommission;

    /**
     * 本笔成交的金额（= 成交数量 × 成交价格）
     */
    @Column(name = "fill_value", precision = 15, scale = 4)
    private BigDecimal fillValue;

    /**
     * 成交时的市场价格（用于计算滑点分析）
     */
    @Column(name = "market_price", precision = 15, scale = 4)
    private BigDecimal marketPrice;

    // ===== 累积数据 =====
    /**
     * 累积成交数量（从第1笔到当前笔的总和）
     * 用于快速查看订单成交进度
     */
    @Column(name = "cumulative_quantity", nullable = false, precision = 15, scale = 4)
    private BigDecimal cumulativeQuantity;

    /**
     * 累积成交佣金（从第1笔到当前笔的总和）
     */
    @Column(name = "cumulative_commission", precision = 15, scale = 4)
    private BigDecimal cumulativeCommission;

    /**
     * 累积成交金额
     */
    @Column(name = "cumulative_value", precision = 15, scale = 4)
    private BigDecimal cumulativeValue;

    // ===== 时间戳 =====
    /**
     * 成交时间（来自 etrade）
     */
    @Column(name = "filled_at", nullable = false)
    private LocalDateTime filledAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ===== Business Logic Methods =====

    /**
     * 计算平均成交价格
     */
    public BigDecimal getAverageFillPrice() {
        if (fillQuantity == null || fillQuantity.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (fillValue == null) {
            return BigDecimal.ZERO;
        }
        return fillValue.divide(fillQuantity, 4, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 计算本笔成交的净价（成交价 - 佣金/数量）
     */
    public BigDecimal getNetFillPrice() {
        BigDecimal netPrice = getAverageFillPrice();
        if (fillCommission != null && fillQuantity != null && fillQuantity.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal commissionPerShare = fillCommission.divide(fillQuantity, 4, java.math.RoundingMode.HALF_UP);
            netPrice = netPrice.subtract(commissionPerShare);
        }
        return netPrice;
    }

    /**
     * 获取累积平均成交价格
     */
    public BigDecimal getCumulativeAveragePrice() {
        if (cumulativeQuantity == null || cumulativeQuantity.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (cumulativeValue == null) {
            return BigDecimal.ZERO;
        }
        return cumulativeValue.divide(cumulativeQuantity, 4, java.math.RoundingMode.HALF_UP);
    }

    // ===== Getters and Setters =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getExternalFillId() { return externalFillId; }
    public void setExternalFillId(String externalFillId) { this.externalFillId = externalFillId; }

    public Integer getFillSequence() { return fillSequence; }
    public void setFillSequence(Integer fillSequence) { this.fillSequence = fillSequence; }

    public BigDecimal getFillQuantity() { return fillQuantity; }
    public void setFillQuantity(BigDecimal fillQuantity) { this.fillQuantity = fillQuantity; }

    public BigDecimal getFillPrice() { return fillPrice; }
    public void setFillPrice(BigDecimal fillPrice) { this.fillPrice = fillPrice; }

    public BigDecimal getFillCommission() { return fillCommission; }
    public void setFillCommission(BigDecimal fillCommission) { this.fillCommission = fillCommission; }

    public BigDecimal getFillValue() { return fillValue; }
    public void setFillValue(BigDecimal fillValue) { this.fillValue = fillValue; }

    public BigDecimal getMarketPrice() { return marketPrice; }
    public void setMarketPrice(BigDecimal marketPrice) { this.marketPrice = marketPrice; }

    public BigDecimal getCumulativeQuantity() { return cumulativeQuantity; }
    public void setCumulativeQuantity(BigDecimal cumulativeQuantity) { this.cumulativeQuantity = cumulativeQuantity; }

    public BigDecimal getCumulativeCommission() { return cumulativeCommission; }
    public void setCumulativeCommission(BigDecimal cumulativeCommission) { this.cumulativeCommission = cumulativeCommission; }

    public BigDecimal getCumulativeValue() { return cumulativeValue; }
    public void setCumulativeValue(BigDecimal cumulativeValue) { this.cumulativeValue = cumulativeValue; }

    public LocalDateTime getFilledAt() { return filledAt; }
    public void setFilledAt(LocalDateTime filledAt) { this.filledAt = filledAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "OrderFill{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", fillSequence=" + fillSequence +
                ", fillQuantity=" + fillQuantity +
                ", fillPrice=" + fillPrice +
                ", filledAt=" + filledAt +
                '}';
    }
}
