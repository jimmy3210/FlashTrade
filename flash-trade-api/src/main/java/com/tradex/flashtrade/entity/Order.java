package com.tradex.flashtrade.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "account_id", nullable = false, length = 50)
    private String accountId;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OrderSide side;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal quantity;

    @Column(precision = 15, scale = 4)
    private BigDecimal price;

    /**
     * 订单类型 - 支持复杂订单
     * MARKET: 市价单（默认）
     * LIMIT: 限价单
     * STOP: 止损单
     * STOP_LIMIT: 止损限价单
     */
    @Column(name = "order_type", length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'MARKET'")
    private String orderType = "MARKET";

    /**
     * 订单时间类型
     * DAY: 当日有效（默认）
     * GTC: 有效至取消
     * IOC: 立即或取消
     * FOK: 全部或取消
     */
    @Column(name = "time_in_force", length = 10, columnDefinition = "VARCHAR(10) DEFAULT 'DAY'")
    private String timeInForce = "DAY";

    /**
     * 止损价 - 用于 STOP 和 STOP_LIMIT 订单
     * NULL for MARKET 和 LIMIT 订单
     */
    @Column(name = "stop_price", precision = 15, scale = 4)
    private BigDecimal stopPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "broker_name", nullable = false, length = 50)
    private String brokerName;

    @Column(name = "external_order_id", length = 100)
    private String externalOrderId;

    @Column(name = "preview_id", length = 100)
    private String previewId;

    @Column(name = "broker_account_id")
    private Long brokerAccountId;

    @Column(name = "limit_price", precision = 15, scale = 2)
    private BigDecimal limitPrice;

    @Column(name = "filled_quantity", precision = 15, scale = 4)
    private BigDecimal filledQuantity;

    @Column(name = "average_price", precision = 15, scale = 2)
    private BigDecimal averagePrice;

    @Column(name = "total_value", precision = 15, scale = 2)
    private BigDecimal totalValue;

    @Column(name = "commission", precision = 15, scale = 2)
    private BigDecimal commission;

    @Column(name = "admin_note", length = 500)
    private String adminNote;

    @Column(name = "risk_flag", length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'NORMAL'")
    private String riskFlag = "NORMAL";

    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "admin_action_time")
    private LocalDateTime adminActionTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "filled_at")
    private LocalDateTime filledAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public OrderSide getSide() { return side; }
    public void setSide(OrderSide side) { this.side = side; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }

    public String getTimeInForce() { return timeInForce; }
    public void setTimeInForce(String timeInForce) { this.timeInForce = timeInForce; }

    public BigDecimal getStopPrice() { return stopPrice; }
    public void setStopPrice(BigDecimal stopPrice) { this.stopPrice = stopPrice; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public String getBrokerName() { return brokerName; }
    public void setBrokerName(String brokerName) { this.brokerName = brokerName; }

    public String getExternalOrderId() { return externalOrderId; }
    public void setExternalOrderId(String externalOrderId) { this.externalOrderId = externalOrderId; }

    public String getPreviewId() { return previewId; }
    public void setPreviewId(String previewId) { this.previewId = previewId; }

    public Long getBrokerAccountId() { return brokerAccountId; }
    public void setBrokerAccountId(Long brokerAccountId) { this.brokerAccountId = brokerAccountId; }

    public BigDecimal getLimitPrice() { return limitPrice; }
    public void setLimitPrice(BigDecimal limitPrice) { this.limitPrice = limitPrice; }

    public BigDecimal getFilledQuantity() { return filledQuantity; }
    public void setFilledQuantity(BigDecimal filledQuantity) { this.filledQuantity = filledQuantity; }

    public BigDecimal getAveragePrice() { return averagePrice; }
    public void setAveragePrice(BigDecimal averagePrice) { this.averagePrice = averagePrice; }

    public BigDecimal getTotalValue() { return totalValue; }
    public void setTotalValue(BigDecimal totalValue) { this.totalValue = totalValue; }

    public BigDecimal getCommission() { return commission; }
    public void setCommission(BigDecimal commission) { this.commission = commission; }

    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }

    public String getRiskFlag() { return riskFlag; }
    public void setRiskFlag(String riskFlag) { this.riskFlag = riskFlag; }

    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }

    public LocalDateTime getAdminActionTime() { return adminActionTime; }
    public void setAdminActionTime(LocalDateTime adminActionTime) { this.adminActionTime = adminActionTime; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getFilledAt() { return filledAt; }
    public void setFilledAt(LocalDateTime filledAt) { this.filledAt = filledAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
