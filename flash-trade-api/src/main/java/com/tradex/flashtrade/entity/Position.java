package com.tradex.flashtrade.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "positions",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_positions",
        columnNames = {"user_id", "account_id", "symbol", "broker_name"}
    )
)
public class Position {

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

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(name = "average_cost", nullable = false, precision = 15, scale = 4)
    private BigDecimal averageCost = BigDecimal.ZERO;

    @Column(name = "current_price", precision = 15, scale = 4)
    private BigDecimal currentPrice;

    @Column(name = "market_value", precision = 15, scale = 4)
    private BigDecimal marketValue;

    @Column(name = "unrealized_pnl", precision = 15, scale = 4)
    private BigDecimal unrealizedPnl;

    @Column(name = "broker_name", nullable = false, length = 50)
    private String brokerName;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
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

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getAverageCost() { return averageCost; }
    public void setAverageCost(BigDecimal averageCost) { this.averageCost = averageCost; }

    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }

    public BigDecimal getMarketValue() { return marketValue; }
    public void setMarketValue(BigDecimal marketValue) { this.marketValue = marketValue; }

    public BigDecimal getUnrealizedPnl() { return unrealizedPnl; }
    public void setUnrealizedPnl(BigDecimal unrealizedPnl) { this.unrealizedPnl = unrealizedPnl; }

    public String getBrokerName() { return brokerName; }
    public void setBrokerName(String brokerName) { this.brokerName = brokerName; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
