package com.tradex.flashtrade.broker.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 券商侧订单对象（统一数据格式）
 */
public class BrokerOrder {

    private String externalOrderId;
    private String symbol;
    private String side;        // BUY / SELL
    private BigDecimal quantity;
    private BigDecimal price;
    private String status;      // PENDING / FILLED / PARTIALLY_FILLED / CANCELLED / REJECTED
    private LocalDateTime createdAt;
    private LocalDateTime filledAt;

    public BrokerOrder() {}

    private BrokerOrder(Builder b) {
        this.externalOrderId = b.externalOrderId;
        this.symbol          = b.symbol;
        this.side            = b.side;
        this.quantity        = b.quantity;
        this.price           = b.price;
        this.status          = b.status;
        this.createdAt       = b.createdAt;
        this.filledAt        = b.filledAt;
    }

    public static Builder builder() { return new Builder(); }

    public String getExternalOrderId() { return externalOrderId; }
    public String getSymbol()          { return symbol; }
    public String getSide()            { return side; }
    public BigDecimal getQuantity()    { return quantity; }
    public BigDecimal getPrice()       { return price; }
    public String getStatus()          { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getFilledAt()  { return filledAt; }

    public static class Builder {
        private String externalOrderId;
        private String symbol;
        private String side;
        private BigDecimal quantity;
        private BigDecimal price;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime filledAt;

        public Builder externalOrderId(String v)    { this.externalOrderId = v; return this; }
        public Builder symbol(String v)             { this.symbol          = v; return this; }
        public Builder side(String v)               { this.side            = v; return this; }
        public Builder quantity(BigDecimal v)       { this.quantity        = v; return this; }
        public Builder price(BigDecimal v)          { this.price           = v; return this; }
        public Builder status(String v)             { this.status          = v; return this; }
        public Builder createdAt(LocalDateTime v)   { this.createdAt       = v; return this; }
        public Builder filledAt(LocalDateTime v)    { this.filledAt        = v; return this; }
        public BrokerOrder build()                  { return new BrokerOrder(this); }
    }
}
