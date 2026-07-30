package com.tradex.flashtrade.broker.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 券商侧成交记录（统一数据格式）
 */
public class BrokerTrade {

    private String externalTradeId;
    private String externalOrderId;
    private String symbol;
    private String side;          // BUY / SELL
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal commission;
    private LocalDateTime tradedAt;

    public BrokerTrade() {}

    private BrokerTrade(Builder b) {
        this.externalTradeId  = b.externalTradeId;
        this.externalOrderId  = b.externalOrderId;
        this.symbol           = b.symbol;
        this.side             = b.side;
        this.quantity         = b.quantity;
        this.price            = b.price;
        this.commission       = b.commission;
        this.tradedAt         = b.tradedAt;
    }

    public static Builder builder() { return new Builder(); }

    public String getExternalTradeId()  { return externalTradeId; }
    public String getExternalOrderId()  { return externalOrderId; }
    public String getSymbol()           { return symbol; }
    public String getSide()             { return side; }
    public BigDecimal getQuantity()     { return quantity; }
    public BigDecimal getPrice()        { return price; }
    public BigDecimal getCommission()   { return commission; }
    public LocalDateTime getTradedAt()  { return tradedAt; }

    public static class Builder {
        private String externalTradeId;
        private String externalOrderId;
        private String symbol;
        private String side;
        private BigDecimal quantity;
        private BigDecimal price;
        private BigDecimal commission;
        private LocalDateTime tradedAt;

        public Builder externalTradeId(String v)   { this.externalTradeId = v; return this; }
        public Builder externalOrderId(String v)   { this.externalOrderId = v; return this; }
        public Builder symbol(String v)            { this.symbol          = v; return this; }
        public Builder side(String v)              { this.side            = v; return this; }
        public Builder quantity(BigDecimal v)      { this.quantity        = v; return this; }
        public Builder price(BigDecimal v)         { this.price           = v; return this; }
        public Builder commission(BigDecimal v)    { this.commission      = v; return this; }
        public Builder tradedAt(LocalDateTime v)   { this.tradedAt        = v; return this; }
        public BrokerTrade build()                 { return new BrokerTrade(this); }
    }
}
