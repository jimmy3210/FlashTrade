package com.tradex.flashtrade.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trades")
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "account_id", nullable = false, length = 50)
    private String accountId;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OrderSide side;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal price;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal commission = BigDecimal.ZERO;

    @Column(name = "broker_name", nullable = false, length = 50)
    private String brokerName;

    @Column(name = "external_trade_id", length = 100)
    private String externalTradeId;

    @Column(name = "traded_at", nullable = false)
    private LocalDateTime tradedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

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

    public BigDecimal getCommission() { return commission; }
    public void setCommission(BigDecimal commission) { this.commission = commission; }

    public String getBrokerName() { return brokerName; }
    public void setBrokerName(String brokerName) { this.brokerName = brokerName; }

    public String getExternalTradeId() { return externalTradeId; }
    public void setExternalTradeId(String externalTradeId) { this.externalTradeId = externalTradeId; }

    public LocalDateTime getTradedAt() { return tradedAt; }
    public void setTradedAt(LocalDateTime tradedAt) { this.tradedAt = tradedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
